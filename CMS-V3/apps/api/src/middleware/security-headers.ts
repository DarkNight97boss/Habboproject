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
    // Request-id per tracing/correlazione (#29). Riusa un X-Request-Id in ingresso
    // se valido (propagazione cross-servizio: web/EMU/load-balancer → API),
    // altrimenti ne genera uno nuovo. Validazione a charset sicuro + lunghezza
    // massima per evitare header-injection quando lo facciamo "eco" in risposta.
    const incoming = c.req.header('x-request-id');
    const requestId = incoming && /^[A-Za-z0-9._-]{1,64}$/.test(incoming)
        ? incoming
        : randomBytes(8).toString('hex');
    c.set('requestId', requestId);

    await next();

    // Eco in risposta: il chiamante può citare l'X-Request-Id nei bug report e
    // correlarlo ai log server (grep per requestId). Fondamento del tracing.
    c.header('X-Request-Id', requestId);

    c.header('Strict-Transport-Security', 'max-age=31536000; includeSubDomains; preload');
    c.header('X-Frame-Options', 'DENY');
    c.header('X-Content-Type-Options', 'nosniff');
    c.header('Referrer-Policy', 'strict-origin-when-cross-origin');
    // microphone=(self): la dettatura vocale in chat (Web Speech API sul client
    // Nitro, stesso origine) ha bisogno del microfono. (self) consente SOLO la
    // propria origine e l'utente riceve comunque il prompt del browser.
    c.header('Permissions-Policy', 'geolocation=(), microphone=(self), camera=(), payment=()');
    c.header('X-Permitted-Cross-Domain-Policies', 'none');
    c.header('Cross-Origin-Opener-Policy', 'same-origin');
    c.header('Cross-Origin-Resource-Policy', 'same-origin');
    // Rimuovi l'header X-Powered-By che leak la tech (Hono non lo setta, ma per chiarezza):
    c.res.headers.delete('x-powered-by');
};
