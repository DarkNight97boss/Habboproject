import { Algorithm, hash, verify } from '@node-rs/argon2';
import bcrypt from 'bcryptjs';
import { env } from '../env.js';

/**
 * Argon2id — winner OWASP 2024 raccomandazione password hashing.
 * Implementazione Rust (@node-rs/argon2) — prebuilds per Linux/Mac/Win x64/arm64.
 *
 * Params:
 *  - memoryCost: 64 MB (64*1024 KB)
 *  - timeCost: 3 iterazioni
 *  - parallelism: 4 thread
 */
const ARGON_OPTIONS = {
    algorithm: Algorithm.Argon2id,
    memoryCost: env.ARGON2_MEMORY_KB,
    timeCost: env.ARGON2_TIME_COST,
    parallelism: env.ARGON2_PARALLELISM
} as const;

/** Hash di reference per timing-safe verify (in caso di user inesistente). */
const TIMING_SAFE_DUMMY_HASH = '$argon2id$v=19$m=65536,t=3,p=4$YWFhYWFhYWFhYWFhYWFhYQ$YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE';

export async function hashPassword(plain: string): Promise<string>
{
    if(plain.length === 0) throw new Error('password vuota');
    if(plain.length > 1024) throw new Error('password troppo lunga');
    return hash(plain, ARGON_OPTIONS);
}

/**
 * Verifica password contro hash di formato:
 *  - Argon2id (`$argon2id$...`) — preferito, nuovi utenti CMS-V3
 *  - bcrypt (`$2a$`, `$2b$`, `$2y$`) — formato Arcturus EMU legacy
 *  - SHA-512 raw (lunghezza 128 hex) — fallback per setup molto vecchi
 *
 * Il chiamante deve poi controllare isArgon2Hash() e ri-hashare con
 * hashPassword() per la migrazione trasparente al login successivo.
 */
export async function verifyPassword(hashed: string, plain: string): Promise<boolean>
{
    try
    {
        if(hashed.startsWith('$argon2'))
        {
            return await verify(hashed, plain);
        }
        if(hashed.startsWith('$2a$') || hashed.startsWith('$2b$') || hashed.startsWith('$2y$'))
        {
            return await bcrypt.compare(plain, hashed);
        }
        // Fallback SHA-512 raw esadecimale (alcuni setup PHP molto vecchi).
        if(/^[a-f0-9]{128}$/i.test(hashed))
        {
            const enc = new TextEncoder();
            const buf = await crypto.subtle.digest('SHA-512', enc.encode(plain));
            const hex = Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');
            return hex.toLowerCase() === hashed.toLowerCase();
        }
        return false;
    }
    catch
    {
        return false;
    }
}

/** Verify timing-safe contro dummy hash quando non c'è user (anti timing oracle). */
export async function timingSafeDummyVerify(plain: string): Promise<void>
{
    try { await verify(TIMING_SAFE_DUMMY_HASH, plain); } catch { /* ignored */ }
}

/** Heuristica: il vecchio hash è argon2 se inizia con $argon2id$. */
export function isArgon2Hash(value: string): boolean
{
    return value.startsWith('$argon2id$') || value.startsWith('$argon2i$') || value.startsWith('$argon2d$');
}

/**
 * Check password contro HaveIBeenPwned (k-anonymity, niente leak).
 * Invia SOLO i primi 5 hex char di SHA-1, riceve indietro tutti i suffix
 * che matchano e cerca il nostro localmente.
 */
export async function isPasswordPwned(plain: string): Promise<{ pwned: boolean; count: number }>
{
    if(!env.HIBP_CHECK_ENABLED) return { pwned: false, count: 0 };
    try
    {
        const enc = new TextEncoder();
        const buf = await crypto.subtle.digest('SHA-1', enc.encode(plain));
        const hex = Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('').toUpperCase();
        const prefix = hex.slice(0, 5);
        const suffix = hex.slice(5);

        const r = await fetch(`${env.HIBP_API_URL}/${prefix}`, {
            headers: { 'Add-Padding': 'true' }
        });
        if(!r.ok) return { pwned: false, count: 0 };
        const body = await r.text();

        for(const line of body.split('\n'))
        {
            const [hashSuffix, countStr] = line.trim().split(':');
            if(hashSuffix === suffix)
            {
                return { pwned: true, count: parseInt(countStr ?? '0', 10) || 0 };
            }
        }
        return { pwned: false, count: 0 };
    }
    catch
    {
        return { pwned: false, count: 0 };
    }
}
