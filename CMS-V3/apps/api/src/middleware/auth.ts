import type { MiddlewareHandler } from 'hono';
import { verifyAccessToken, type AccessTokenPayload } from '../security/jwt.js';

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
        if(m) token = decodeURIComponent(m[1] ?? '');
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
        await next();
    };
}
