import type { MiddlewareHandler } from 'hono';
import { env } from '../env.js';

/**
 * Protezione CSRF per l'auth basata su cookie (security audit P1.7).
 *
 * Il CMS accetta l'access token anche dal cookie httpOnly `cms_v3_access`: un
 * sito terzo puo' quindi far partire dal browser della vittima una richiesta
 * non-safe (POST/PATCH/DELETE) con i cookie allegati. CORS NON protegge da
 * questo: blocca solo la LETTURA della risposta, la richiesta viene comunque
 * eseguita lato server. `SameSite=lax` mitiga ma e' un singolo knob di config.
 *
 * Difesa: verifica dell'origine con gli header standard (raccomandazione OWASP
 * "Verifying Origin with Standard Headers"). Quando la richiesta e' non-safe E
 * porta un cookie di sessione, l'`Origin` (o in fallback l'origin del `Referer`)
 * deve essere nella whitelist ALLOWED_ORIGINS — la stessa lista di CORS, quindi
 * nessuna regressione per il frontend che gia' funziona. Se manca sia Origin
 * che Referer si rifiuta (fail-closed): i browser moderni li inviano SEMPRE
 * sulle richieste non-safe, quindi l'unico caso reale sono form cross-site
 * "vecchio stile" o richieste manipolate.
 *
 * Esenti: endpoint che non usano cookie di sessione (autenticati in altro modo):
 * webhook Stripe (firma HMAC), ingest interno EMU→CMS (secret condiviso).
 * Le richieste con SOLO `Authorization: Bearer` (mobile/CLI) non hanno cookie e
 * non sono CSRF-abili: passano senza controlli.
 */
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);
const EXEMPT_PATHS = new Set(['/api/v2/shop/webhook', '/api/v2/activity/ingest']);
const SESSION_COOKIE_RE = /(?:^|;\s*)cms_v3_(?:access|refresh)=/;

export const csrfGuard: MiddlewareHandler = async (c, next) =>
{
    if(SAFE_METHODS.has(c.req.method.toUpperCase())) return next();

    const path = new URL(c.req.url).pathname;
    if(EXEMPT_PATHS.has(path)) return next();

    // Nessun cookie di sessione → il browser non allega credenziali → niente CSRF.
    if(!SESSION_COOKIE_RE.test(c.req.header('cookie') ?? '')) return next();

    const allowed = (origin: string): boolean => env.ALLOWED_ORIGINS.includes(origin);

    const origin = c.req.header('origin');
    if(origin)
    {
        if(!allowed(origin)) return c.json({ error: 'csrf_origin_mismatch' }, 403);
        return next();
    }

    const referer = c.req.header('referer');
    if(referer)
    {
        let refOrigin = '';
        try { refOrigin = new URL(referer).origin; }
        catch { return c.json({ error: 'csrf_bad_referer' }, 403); }
        if(!allowed(refOrigin)) return c.json({ error: 'csrf_referer_mismatch' }, 403);
        return next();
    }

    return c.json({ error: 'csrf_missing_origin' }, 403);
};
