import type { MiddlewareHandler } from 'hono';
import { randomBytes } from 'node:crypto';

/**
 * Headers di sicurezza applicati a OGNI risposta API.
 *
 * - HSTS: forza HTTPS in produzione (1 anno, includeSubDomains, preload)
 * - X-Frame-Options: DENY — vieta embed in iframe (anti-clickjacking)
 * - X-Content-Type-Options: nosniff
 * - Referrer-Policy: strict-origin-when-cross-origin
 * - Permissions-Policy: nega API browser sensibili
 * - X-Permitted-Cross-Domain-Policies: none
 *
 * Niente CSP qui — la CSP è server-side sulla risposta HTML del web app
 * (Vite preview / nginx). L'API ritorna solo JSON, quindi CSP non si applica.
 */
export const securityHeaders: MiddlewareHandler = async (c, next) =>
{
    // Genera un request-id per tracing.
    c.set('requestId', randomBytes(8).toString('hex'));

    await next();

    c.header('Strict-Transport-Security', 'max-age=31536000; includeSubDomains; preload');
    c.header('X-Frame-Options', 'DENY');
    c.header('X-Content-Type-Options', 'nosniff');
    c.header('Referrer-Policy', 'strict-origin-when-cross-origin');
    c.header('Permissions-Policy', 'geolocation=(), microphone=(), camera=(), payment=()');
    c.header('X-Permitted-Cross-Domain-Policies', 'none');
    c.header('Cross-Origin-Opener-Policy', 'same-origin');
    c.header('Cross-Origin-Resource-Policy', 'same-origin');
    // Rimuovi l'header X-Powered-By che leak la tech (Hono non lo setta, ma per chiarezza):
    c.res.headers.delete('x-powered-by');
};
