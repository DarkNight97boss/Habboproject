import { SignJWT, jwtVerify } from 'jose';
import { randomBytes } from 'node:crypto';
import { env } from '../env.js';

const JWT_KEY = new TextEncoder().encode(env.JWT_SECRET);
const REFRESH_KEY = new TextEncoder().encode(env.REFRESH_TOKEN_SECRET);

export interface AccessTokenPayload
{
    sub: string;        // user id (string)
    username: string;
    rank: number;       // security level
    /** Token ID (jti) per future revoche puntuali. */
    jti: string;
}

export interface RefreshTokenPayload
{
    sub: string;
    family: string;     // rotation family — se uno è rubato, invalidiamo l'intera famiglia
    jti: string;
}

/**
 * Genera un access token (HS256). Durata breve (15 min default).
 */
export async function signAccessToken(payload: Omit<AccessTokenPayload, 'jti'>): Promise<{ token: string; jti: string; exp: number }>
{
    const jti = randomBytes(16).toString('hex');
    const now = Math.floor(Date.now() / 1000);
    const exp = now + env.JWT_ACCESS_TTL_SECONDS;
    const token = await new SignJWT({ ...payload, jti })
        .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
        .setIssuer(env.JWT_ISSUER)
        .setAudience(env.JWT_AUDIENCE)
        .setIssuedAt(now)
        .setExpirationTime(exp)
        .setNotBefore(now)
        .setJti(jti)
        .sign(JWT_KEY);
    return { token, jti, exp };
}

export async function verifyAccessToken(token: string): Promise<AccessTokenPayload | null>
{
    try
    {
        const { payload } = await jwtVerify(token, JWT_KEY, {
            issuer: env.JWT_ISSUER,
            audience: env.JWT_AUDIENCE,
            algorithms: ['HS256']
        });
        // Validazione dei tipi dei claim (P2.1): un token firmato ma con claim
        // malformati (rank null/stringa, sub mancante) viene rifiutato invece di
        // essere propagato con cast ciechi fino a requireRank.
        const rank = payload['rank'];
        const username = payload['username'];
        if(typeof payload.sub !== 'string' || typeof username !== 'string'
            || typeof rank !== 'number' || !Number.isFinite(rank) || typeof payload.jti !== 'string') return null;
        return { sub: payload.sub, username, rank, jti: payload.jti };
    }
    catch
    {
        return null;
    }
}

/**
 * Genera refresh token (HS256, secret SEPARATO da quello access).
 * Salvato in DB per rotazione + revoca.
 */
export async function signRefreshToken(sub: string, family: string): Promise<{ token: string; jti: string; exp: number }>
{
    const jti = randomBytes(16).toString('hex');
    const now = Math.floor(Date.now() / 1000);
    const exp = now + env.REFRESH_TOKEN_TTL_DAYS * 24 * 3600;
    const token = await new SignJWT({ sub, family, jti })
        .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
        .setIssuer(env.JWT_ISSUER)
        .setIssuedAt(now)
        .setExpirationTime(exp)
        .setJti(jti)
        .sign(REFRESH_KEY);
    return { token, jti, exp };
}

export async function verifyRefreshToken(token: string): Promise<RefreshTokenPayload | null>
{
    try
    {
        const { payload } = await jwtVerify(token, REFRESH_KEY, {
            issuer: env.JWT_ISSUER,
            algorithms: ['HS256']
        });
        return {
            sub: payload.sub as string,
            family: payload['family'] as string,
            jti: payload.jti as string
        };
    }
    catch
    {
        return null;
    }
}
