import type { MiddlewareHandler } from 'hono';
import { verifyAccessToken, type AccessTokenPayload } from '../security/jwt.js';
import { dbQuery } from '../db/pool.js';

declare module 'hono'
{
    interface ContextVariableMap
    {
        user?: AccessTokenPayload;
        requestId: string;
    }
}

/**
 * Estrae l'access token da:
 *   1. header Authorization: Bearer <token>
 *   2. cookie cms_v3_access (per richieste same-origin dal frontend)
 *
 * Se valido popola c.var.user; se mancante o invalido, ritorna 401 SOLO
 * quando il route handler lo richiede (vedi requireAuth sotto).
 */
export const populateAuth: MiddlewareHandler = async (c, next) =>
{
    let token: string | undefined;

    const auth = c.req.header('authorization');
    if(auth?.startsWith('Bearer '))
    {
        token = auth.slice(7);
    }

    if(!token)
    {
        // Fallback su cookie httpOnly (vedi route /auth/login).
        const cookies = c.req.header('cookie') ?? '';
        const m = cookies.match(/(?:^|;\s*)cms_v3_access=([^;]+)/);
        if(m) { try { token = decodeURIComponent(m[1] ?? ''); } catch { token = ''; } }
    }

    if(token)
    {
        const payload = await verifyAccessToken(token);
        if(payload) c.set('user', payload);
    }

    await next();
};

export const requireAuth: MiddlewareHandler = async (c, next) =>
{
    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    await next();
};

export function requireRank(minRank: number): MiddlewareHandler
{
    return async (c, next) =>
    {
        const user = c.var.user;
        if(!user) return c.json({ error: 'unauthorized' }, 401);
        // Fail-CLOSED (security audit P2.1): con `rank` mancante/null/non numerico
        // `undefined < 5` e `null < 5` valgono false → il vecchio check lasciava
        // passare. Un claim malformato non deve MAI concedere privilegi.
        if(typeof user.rank !== 'number' || !Number.isFinite(user.rank) || user.rank < minRank) return c.json({ error: 'forbidden' }, 403);

        // Difesa-in-profondità (pentest 2026-09-16): il rank vive nel JWT (access TTL
        // ~15 min). Uno staff demansionato o bannato NON deve conservare i poteri fino
        // alla scadenza del token: rileggiamo rank e stato ban dal DB a ogni azione
        // privilegiata e propaghiamo il rank FRESCO a valle (il ceiling di promozione
        // in staff.ts legge c.var.user.rank). Fail-closed: id non valido, utente
        // assente o qualsiasi errore DB => 403.
        const uid = Number(user.sub);
        if(!Number.isInteger(uid) || uid <= 0) return c.json({ error: 'forbidden' }, 403);
        try
        {
            const rows = await dbQuery<{ rank: number }>('SELECT rank FROM users WHERE id = ? LIMIT 1', [uid]);
            const dbRank = rows.length ? (Number(rows[0]!.rank) || 0) : -1;
            if(dbRank < minRank) return c.json({ error: 'forbidden' }, 403);

            const banRows = await dbQuery<{ ban_expire: number }>(
                "SELECT ban_expire FROM bans WHERE user_id = ? AND ban_expire > UNIX_TIMESTAMP() AND type IN ('account', 'super') LIMIT 1",
                [uid],
            );
            if(banRows.length) return c.json({ error: 'forbidden' }, 403);

            c.set('user', { ...user, rank: dbRank });
        }
        catch
        {
            return c.json({ error: 'forbidden' }, 403);
        }
        await next();
    };
}
