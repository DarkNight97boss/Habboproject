import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { cors } from 'hono/cors';
import { logger } from 'hono/logger';
import pino from 'pino';

import { env } from './env.js';
import { populateAuth } from './middleware/auth.js';
import { makeRateLimit } from './middleware/rate-limit.js';
import { securityHeaders } from './middleware/security-headers.js';
import authRoute from './routes/auth.js';
import communityRoute from './routes/community.js';
import meRoute from './routes/me.js';
import profileRoute from './routes/profile.js';
import shopRoute from './routes/shop.js';
import { skinSiteRoutes, default as skinsListRoute } from './routes/skin.js';
import staffRoute from './routes/staff.js';
import { ensureShopSeeded } from './services/shop-seed.js';
import { ensureSkinsSeeded } from './services/skin-seed.js';

const log = pino({
    level: env.LOG_LEVEL,
    ...(env.LOG_PRETTY ? { transport: { target: 'pino-pretty' } } : {})
});

const app = new Hono();

// === Middleware globali ===
app.use('*', securityHeaders);
app.use('*', cors({
    origin: (origin) =>
    {
        // Permetti SOLO origini in whitelist (niente wildcard).
        if(!origin) return null;
        return env.ALLOWED_ORIGINS.includes(origin) ? origin : null;
    },
    allowMethods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE', 'OPTIONS'],
    allowHeaders: ['content-type', 'authorization', env.CSRF_HEADER_NAME],
    credentials: true,
    maxAge: 600
}));
app.use('*', logger((msg) => log.info(msg)));
app.use('*', populateAuth);
app.use('*', makeRateLimit('generic', env.RATE_LIMIT_GENERIC_PER_MIN, 60_000));

// === Health check (per monitoring/uptime) ===
app.get('/healthz', c => c.json({ ok: true, ts: Date.now() }));
app.get('/version', c => c.json({ name: 'cms-v3-api', version: '0.1.0', env: env.NODE_ENV }));

// === Routes ===
app.route('/api/v2/auth', authRoute);
app.route('/api/v2/me', meRoute);
app.route('/api/v2/community', communityRoute);
app.route('/api/v2/profile', profileRoute);
app.route('/api/v2/shop', shopRoute);
app.route('/api/v2/site', skinSiteRoutes);
app.route('/api/v2/skins', skinsListRoute);
app.route('/api/v2/staff', staffRoute);

// === 404 fallback ===
app.notFound(c => c.json({ error: 'not_found', path: c.req.path }, 404));

// === Error handler globale ===
app.onError((err, c) =>
{
    const requestId = c.var.requestId ?? 'unknown';
    log.error({ err, requestId, path: c.req.path }, 'unhandled error');
    return c.json({ error: 'internal_error', requestId }, 500);
});

// === Start server ===
const server = serve({
    fetch: app.fetch,
    port: env.PORT,
    hostname: '127.0.0.1'
}, info =>
{
    log.info(`✅ CMS-V3 API listening on http://${info.address}:${info.port}`);
    log.info(`   Env: ${env.NODE_ENV} | DB: ${env.DB_HOST}:${env.DB_PORT}/${env.DB_NAME}`);
    log.info(`   Allowed origins: ${env.ALLOWED_ORIGINS.join(', ')}`);

    // Auto-seed dei 3 skin builtin (idempotente). Esegue UPDATE se i manifest
    // sono cambiati nel codice — così deploy aggiorna automaticamente i temi
    // builtin senza migration manuale.
    ensureSkinsSeeded()
        .then(() => log.info('   Skin Engine: builtin skins seeded'))
        .catch(e => log.error({ err: e }, 'failed to seed builtin skins'));

    ensureShopSeeded()
        .then(() => log.info('   Shop: catalog seeded'))
        .catch(e => log.error({ err: e }, 'failed to seed shop catalog'));
});

// Graceful shutdown
const shutdown = (signal: string): void =>
{
    log.info(`Received ${signal}, shutting down…`);
    server.close(() => process.exit(0));
    setTimeout(() => process.exit(1), 10_000).unref();
};
process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));
