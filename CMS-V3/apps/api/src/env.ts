import { config } from 'dotenv';
import { z } from 'zod';

config();

/**
 * Un secret "reale": lunghezza minima E nessun placeholder/valore d'esempio E un
 * minimo di varietà di caratteri. I placeholder di .env.example (REPLACE_ME_...)
 * superano il solo min(32): un deploy con .env copiato e non completato partirebbe
 * con una chiave di firma PUBBLICA (è nel repo) → JWT forgeabili da chiunque,
 * rank incluso. Meglio fallire al boot (P1.3 del security audit).
 */
const PLACEHOLDER_RE = /replace[_-]?me|change[_-]?me|placeholder|your[_-]?secret|insert[_-]?here|dummy/i;
const realSecret = (name: string) => z.string()
    .min(32, `${name} deve essere ≥32 caratteri (64 bytes hex consigliato: openssl rand -hex 64)`)
    .refine(v => !PLACEHOLDER_RE.test(v), `${name} contiene un placeholder: genera un secret reale (openssl rand -hex 64)`)
    .refine(v => new Set(v).size >= 8, `${name} ha troppa poca varietà di caratteri: non sembra un secret casuale`);

/**
 * Validazione env tramite Zod. Se manca un secret o un valore è invalido,
 * il server NON parte — meglio fail-fast che boot in stato insicuro.
 */
const envSchema = z.object({
    PORT: z.coerce.number().int().positive().default(8091),
    NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
    ALLOWED_ORIGINS: z.string().min(1).transform(s => s.split(',').map(o => o.trim()).filter(Boolean)),

    DB_HOST: z.string().min(1),
    DB_PORT: z.coerce.number().int().positive().default(3306),
    DB_USER: z.string().min(1),
    DB_PASSWORD: z.string(),
    DB_NAME: z.string().min(1),
    DB_POOL_MIN: z.coerce.number().int().min(0).default(2),
    DB_POOL_MAX: z.coerce.number().int().positive().default(20),
    DB_CONNECT_TIMEOUT_MS: z.coerce.number().int().positive().default(10_000),

    JWT_SECRET: realSecret('JWT_SECRET'),
    JWT_ACCESS_TTL_SECONDS: z.coerce.number().int().positive().default(900),
    JWT_ISSUER: z.string().default('cms-v3'),
    JWT_AUDIENCE: z.string().default('cms-v3-web'),

    REFRESH_TOKEN_SECRET: realSecret('REFRESH_TOKEN_SECRET'),
    REFRESH_TOKEN_TTL_DAYS: z.coerce.number().int().positive().default(14),

    ARGON2_MEMORY_KB: z.coerce.number().int().min(8192).default(65536),
    ARGON2_TIME_COST: z.coerce.number().int().min(2).default(3),
    ARGON2_PARALLELISM: z.coerce.number().int().min(1).default(4),

    RATE_LIMIT_LOGIN_PER_MIN: z.coerce.number().int().positive().default(5),
    RATE_LIMIT_REGISTER_PER_HOUR: z.coerce.number().int().positive().default(3),
    RATE_LIMIT_GENERIC_PER_MIN: z.coerce.number().int().positive().default(100),

    CSRF_COOKIE_NAME: z.string().default('cms_v3_csrf'),
    CSRF_HEADER_NAME: z.string().default('x-csrf-token'),

    COOKIE_SECURE: z.coerce.boolean().default(false),
    COOKIE_SAMESITE: z.enum(['strict', 'lax', 'none']).default('lax'),
    COOKIE_DOMAIN: z.string().default(''),

    LOG_LEVEL: z.enum(['trace', 'debug', 'info', 'warn', 'error', 'fatal']).default('info'),
    LOG_PRETTY: z.coerce.boolean().default(false),

    HIBP_CHECK_ENABLED: z.coerce.boolean().default(true),
    HIBP_API_URL: z.string().url().default('https://api.pwnedpasswords.com/range'),

    // Bridge RCON/MUS verso EMU Arcturus (loopback only).
    RCON_HOST: z.string().default('127.0.0.1'),
    RCON_PORT: z.coerce.number().int().positive().default(3001),
    RCON_TOKEN: z.string().default(''),

    // CDN per il client Nitro V3 (asset statici hostati su Cloudflare R2).
    // Il /play endpoint fetcha l'index.html da qui e inietta SSO ticket +
    // <base href> per far risolvere tutti gli asset relativi dal CDN.
    // Default: cdn.asteriacore.online (production). Per dev: override con
    // dev-cdn.asteriacore.online o test bucket separato.
    CDN_BASE_URL: z.string().url().default('https://cdn.asteriacore.online'),

    // DEV-only: WebSocket dell'EMU dev per il client Nitro. Quando settata
    // (solo ambiente dev), /play appende a `config.urls` un override che forza
    // `socket.url` su questo valore. Serve perché il client dev carica la
    // config CONDIVISA dal CDN (renderer-config.json) dove `socket.url` punta
    // all'EMU di PRODUZIONE (wss://hotel.asteriacore.online); senza override il
    // client dev si connetterebbe all'EMU prod con un ticket dev → "session
    // expired". In PROD questa var NON va settata: assente ⇒ /play non inietta
    // nulla ⇒ comportamento invariato. Es. dev: wss://dev-hotel.asteriacore.online/websockets
    NITRO_SOCKET_URL: z.string().optional(),

    // Secret condiviso per l'ingest interno dell'event-bus (EMU→CMS via POST
    // /api/v2/activity/ingest). Vuoto ⇒ endpoint disabilitato (503): nessun
    // rischio di ingest anonimo finché non viene configurato.
    INTERNAL_API_SECRET: z.string().default('')
        .refine(v => v === '' || (v.length >= 32 && !PLACEHOLDER_RE.test(v)),
            'INTERNAL_API_SECRET: lascialo vuoto (ingest disabilitato) oppure usa un secret reale >= 32 char (openssl rand -hex 32)')
});

const parsed = envSchema.safeParse(process.env);

if(!parsed.success)
{
    // eslint-disable-next-line no-console
    console.error('❌ Env validation failed:');
    // eslint-disable-next-line no-console
    console.error(parsed.error.format());
    process.exit(1);
}

if(parsed.data.JWT_SECRET === parsed.data.REFRESH_TOKEN_SECRET)
{
    // eslint-disable-next-line no-console
    console.error('❌ JWT_SECRET e REFRESH_TOKEN_SECRET devono essere DIVERSI.');
    process.exit(1);
}

if(parsed.data.NODE_ENV === 'production' && !parsed.data.COOKIE_SECURE)
{
    // eslint-disable-next-line no-console
    console.error('❌ In NODE_ENV=production, COOKIE_SECURE deve essere true.');
    process.exit(1);
}

// P1.7 (security audit): con SameSite=none il cookie di sessione viaggia su OGNI
// richiesta cross-site e ogni endpoint state-changing diventa CSRF-abile (azioni
// staff incluse). In produzione non e' ammesso: 'lax' o 'strict' soltanto.
if(parsed.data.NODE_ENV === 'production' && parsed.data.COOKIE_SAMESITE === 'none')
{
    // eslint-disable-next-line no-console
    console.error('❌ In NODE_ENV=production, COOKIE_SAMESITE non puo\' essere "none" (usa lax o strict).');
    process.exit(1);
}

export const env = parsed.data;
export type Env = typeof env;
