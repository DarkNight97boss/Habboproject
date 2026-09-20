import { verify as argon2Verify } from '@node-rs/argon2';
import bcrypt from 'bcryptjs';
import { env } from '../env.js';

/**
 * Password hashing — bcrypt cost 12.
 *
 * Argon2id DISABILITATO per ora: la colonna `users.password` è VARCHAR(64)
 * (legacy schema Arcturus dimensionato per bcrypt $2y$10$...60-char). Un
 * hash Argon2id completo è ~96 char e viene troncato silenziosamente da
 * MySQL → credenziali corrotte. Vedi memory `cms-v3-pwhash-overflow`.
 *
 * Bcrypt $2b$12$ rimane sicuro (OWASP 2024 minimum cost = 10, noi usiamo
 * 12 per margine extra) e fitta perfettamente nei 60 char della colonna,
 * mantenendo compatibilità con l'EMU Arcturus che pure scrive bcrypt.
 *
 * Per riabilitare Argon2id in futuro: ALTER TABLE users MODIFY password
 * VARCHAR(255) (coordinato con EMU). Poi swap BCRYPT_COST → ARGON_OPTIONS
 * e import { hash } as argon2Hash. La verifica multi-formato qui sotto
 * legge già entrambi i formati, quindi la transizione è zero-downtime.
 */
const BCRYPT_COST = 12;

/**
 * Hash di reference 60-char per timing-safe verify (quando l'utente non
 * esiste, dobbiamo comunque "perdere tempo" per non rivelarlo al chiamante
 * via timing oracle). Pre-generato con bcrypt.hashSync(rand_str, 12).
 */
// Dummy hash per timing-safe verify: NON è un credential reale.
// cost 10 = stesso costo degli hash legacy Arcturus ($2y$10$) predominanti -> il
// path 'utente inesistente' impiega lo stesso tempo di uno esistente (no oracle timing).
const TIMING_SAFE_DUMMY_HASH = '$2b$10$Clvoylom.TkQVdHbY0OrKuVtd8SxrEb20ByhKRRhO5rO0Om2zS8Wy'; // nosemgrep

export async function hashPassword(plain: string): Promise<string>
{
    if(plain.length === 0) throw new Error('password vuota');
    if(plain.length > 72) throw new Error('password troppo lunga (bcrypt max 72 byte)');
    // bcryptjs è puro JS — niente native binding, niente prebuilds. Costo 12
    // ≈ 250-400ms su laptop moderno, accettabile per login one-shot.
    return bcrypt.hash(plain, BCRYPT_COST);
}

/**
 * Verifica password contro hash di formato:
 *  - bcrypt (`$2a$`, `$2b$`, `$2y$`) — formato corrente CMS-V3 + Arcturus
 *  - Argon2id (`$argon2id$...`) — eventuali hash legacy CMS-V3 (transition)
 *  - SHA-512 raw (lunghezza 128 hex) — fallback per setup molto vecchi
 *
 * NB: dopo il login OK NON ri-hashiamo automaticamente (vedi auth.ts).
 * La migrazione di formato è un esercizio coordinato con l'EMU, non un
 * effetto collaterale del login.
 */
export async function verifyPassword(hashed: string, plain: string): Promise<boolean>
{
    try
    {
        if(hashed.startsWith('$2a$') || hashed.startsWith('$2b$') || hashed.startsWith('$2y$'))
        {
            return await bcrypt.compare(plain, hashed);
        }
        if(hashed.startsWith('$argon2'))
        {
            return await argon2Verify(hashed, plain);
        }
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
    try { await bcrypt.compare(plain, TIMING_SAFE_DUMMY_HASH); } catch { /* ignored */ }
}

/** Heuristica: il vecchio hash è argon2 se inizia con $argon2*. */
export function isArgon2Hash(value: string): boolean
{
    return value.startsWith('$argon2id$') || value.startsWith('$argon2i$') || value.startsWith('$argon2d$');
}

/** Heuristica: l'hash è bcrypt se inizia con $2a$/$2b$/$2y$ ed è 60 char. */
export function isBcryptHash(value: string): boolean
{
    return value.length === 60 && (value.startsWith('$2a$') || value.startsWith('$2b$') || value.startsWith('$2y$'));
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
