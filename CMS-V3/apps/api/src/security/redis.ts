/**
 * Client Redis minimale, dependency-free e FAIL-SAFE.
 *
 * Perché fatto a mano: il repo è pubblico (zero supply-chain aggiunta) e non
 * possiamo aggiornare il lockfile in questo ambiente. Copre solo i comandi che
 * ci servono (INCR, EXPIRE, GET, SETEX, EXISTS, DEL) via protocollo RESP su un
 * singolo socket con coda FIFO delle risposte.
 *
 * REGOLA D'ORO: Redis è un MIGLIORAMENTO, mai una dipendenza dura. Se REDIS_URL
 * non è impostata, o Redis è irraggiungibile, TUTTE le operazioni ritornano un
 * sentinel "non disponibile" e i chiamanti fanno fallback al comportamento
 * in-memory. Redis giù NON deve mai bloccare un utente legittimo, rifiutare un
 * token valido, o rompere il login. Ogni percorso è fail-open verso l'utente.
 */
import net from 'node:net';
import { env } from '../env.js';

export const REDIS_UNAVAILABLE = Symbol('redis_unavailable');
type RedisReply = string | number | null;

interface Pending { resolve: (v: RedisReply) => void; reject: (e: Error) => void; timer: NodeJS.Timeout }

const CMD_TIMEOUT_MS = 80;      // Redis in loopback è sub-ms; oltre questo consideriamo "giù".
const RECONNECT_MIN_MS = 500;
const RECONNECT_MAX_MS = 15_000;

class MiniRedis
{
    private sock: net.Socket | null = null;
    private connected = false;
    private buf = Buffer.alloc(0);
    private queue: Pending[] = [];
    private reconnectMs = RECONNECT_MIN_MS;
    private readonly enabled: boolean;
    private host = '127.0.0.1';
    private port = 6379;
    private password = '';
    private db = 0;

    constructor()
    {
        this.enabled = Boolean(env.REDIS_URL);
        if(!this.enabled) return;
        try
        {
            const u = new URL(env.REDIS_URL);
            this.host = u.hostname || '127.0.0.1';
            this.port = u.port ? Number(u.port) : 6379;
            this.password = decodeURIComponent(u.password || '');
            const path = u.pathname.replace(/^\//, '');
            if(path) this.db = Number(path) || 0;
        }
        catch { /* URL malformata → resta sui default */ }
        this.connect();
    }

    private connect(): void
    {
        if(!this.enabled) return;
        const sock = net.connect({ host: this.host, port: this.port });
        sock.setNoDelay(true);
        this.sock = sock;
        sock.on('connect', () =>
        {
            this.connected = true;
            this.reconnectMs = RECONNECT_MIN_MS;
            // AUTH/SELECT best-effort (non attendiamo: le risposte transitano nella coda).
            if(this.password) void this.raw(['AUTH', this.password]).catch(() => {});
            if(this.db) void this.raw(['SELECT', String(this.db)]).catch(() => {});
        });
        sock.on('data', (d: Buffer | string) => this.onData(typeof d === 'string' ? Buffer.from(d) : d));
        sock.on('error', () => { /* gestito da 'close' */ });
        sock.on('close', () => this.onClose());
    }

    private onClose(): void
    {
        this.connected = false;
        this.sock = null;
        // Fallisci tutte le pending in coda (i chiamanti fanno fallback).
        for(const p of this.queue) { clearTimeout(p.timer); p.reject(new Error('redis_closed')); }
        this.queue = [];
        this.buf = Buffer.alloc(0);
        if(this.enabled) setTimeout(() => this.connect(), this.reconnectMs);
        this.reconnectMs = Math.min(this.reconnectMs * 2, RECONNECT_MAX_MS);
    }

    private onData(chunk: Buffer): void
    {
        this.buf = Buffer.concat([this.buf, chunk]);
        // Parsa quante più risposte complete possibile. Su parse desync -> reset socket.
        for(;;)
        {
            const parsed = this.parseOne(this.buf, 0);
            if(!parsed) break;
            this.buf = this.buf.subarray(parsed.next);
            const p = this.queue.shift();
            if(!p) continue;
            clearTimeout(p.timer);
            if(parsed.value instanceof Error) p.reject(parsed.value);
            else p.resolve(parsed.value);
        }
    }

    /** Ritorna {value, next} o null se la risposta non è ancora completa. value può essere Error. */
    private parseOne(b: Buffer, i: number): { value: RedisReply | Error; next: number } | null
    {
        if(i >= b.length) return null;
        const type = b[i];
        const nl = b.indexOf(0x0a, i);          // \n
        if(nl === -1) return null;
        const line = b.subarray(i + 1, nl - 1).toString('utf8'); // esclude \r
        if(type === 0x2b) return { value: line, next: nl + 1 };            // +simple
        if(type === 0x2d) return { value: new Error(line), next: nl + 1 }; // -error
        if(type === 0x3a) return { value: Number(line), next: nl + 1 };    // :integer
        if(type === 0x24)                                                  // $bulk
        {
            const len = Number(line);
            if(len === -1) return { value: null, next: nl + 1 };
            const start = nl + 1;
            const end = start + len;
            if(b.length < end + 2) return null; // dati + \r\n non ancora arrivati
            return { value: b.subarray(start, end).toString('utf8'), next: end + 2 };
        }
        // Tipi non attesi (array *): non li usiamo → segnala desync.
        return { value: new Error('redis_unexpected_type'), next: b.length };
    }

    private raw(args: string[]): Promise<RedisReply>
    {
        if(!this.enabled || !this.connected || !this.sock) return Promise.reject(new Error('redis_unavailable'));
        // Encoding RESP array di bulk string.
        let cmd = `*${args.length}\r\n`;
        for(const a of args) cmd += `$${Buffer.byteLength(a)}\r\n${a}\r\n`;
        return new Promise<RedisReply>((resolve, reject) =>
        {
            const timer = setTimeout(() =>
            {
                // Timeout: consideriamo Redis inaffidabile → resettiamo il socket per resync.
                reject(new Error('redis_timeout'));
                try { this.sock?.destroy(); } catch { /* noop */ }
            }, CMD_TIMEOUT_MS);
            this.queue.push({ resolve, reject, timer });
            try { this.sock!.write(cmd); } catch(e) { clearTimeout(timer); reject(e as Error); }
        });
    }

    get available(): boolean { return this.enabled && this.connected; }

    /** Fixed-window counter atomico-abbastanza: INCR + (se primo) EXPIRE. Ritorna il conteggio o UNAVAILABLE. */
    async incrWindow(key: string, windowSec: number): Promise<number | typeof REDIS_UNAVAILABLE>
    {
        try
        {
            const n = Number(await this.raw(['INCR', key]));
            if(n === 1) void this.raw(['EXPIRE', key, String(windowSec)]).catch(() => {});
            return n;
        }
        catch { return REDIS_UNAVAILABLE; }
    }

    async setex(key: string, sec: number, val: string): Promise<void>
    {
        try { await this.raw(['SETEX', key, String(Math.max(1, Math.floor(sec))), val]); } catch { /* fail-open */ }
    }

    async exists(key: string): Promise<boolean | typeof REDIS_UNAVAILABLE>
    {
        try { return Number(await this.raw(['EXISTS', key])) === 1; }
        catch { return REDIS_UNAVAILABLE; }
    }

    async del(key: string): Promise<void>
    {
        try { await this.raw(['DEL', key]); } catch { /* fail-open */ }
    }
}

export const redis = new MiniRedis();

// ====================================================================
// Helper di alto livello (tutti FAIL-OPEN: Redis giù => nessun blocco).
// ====================================================================

// Deny-list degli access token (logout / revoca puntuale). Chiude la finestra
// in cui un access token restava valido ~15 min dopo il logout.
export async function denylistAccessToken(jti: string, ttlSec: number): Promise<void>
{
    if(!jti || ttlSec <= 0) return;
    await redis.setex(`dl:${jti}`, ttlSec, '1');
}
export async function isAccessTokenDenied(jti: string): Promise<boolean>
{
    return (await redis.exists(`dl:${jti}`)) === true; // UNAVAILABLE/false => non negato
}

// Lockout per-account: blocca i tentativi di login su UN account dopo troppi
// fallimenti (chiude l'attacco distribuito che il rate-limit per-IP non vede).
// Griefing mitigato: servono LOCK_THRESHOLD fallimenti (il per-IP limita quanti
// ne può fare una singola sorgente) e il lock dura poco.
const LOCK_THRESHOLD = 15;
const LOCK_WINDOW_SEC = 900;
const LOCK_SEC = 900;

export async function isAccountLocked(username: string): Promise<boolean>
{
    return (await redis.exists(`lock:${username.toLowerCase()}`)) === true;
}
export async function registerLoginFailure(username: string): Promise<void>
{
    const u = username.toLowerCase();
    const n = await redis.incrWindow(`fail:${u}`, LOCK_WINDOW_SEC);
    if(typeof n === 'number' && n >= LOCK_THRESHOLD) await redis.setex(`lock:${u}`, LOCK_SEC, '1');
}
export async function clearLoginFailures(username: string): Promise<void>
{
    const u = username.toLowerCase();
    await redis.del(`fail:${u}`);
    await redis.del(`lock:${u}`);
}
