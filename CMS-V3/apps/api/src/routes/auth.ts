import { Hono } from 'hono';
import { getCookie } from 'hono/cookie';
import { createHash, randomBytes } from 'node:crypto';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { clientIp } from '../security/client-ip.js';
import { env } from '../env.js';
import { makeRateLimit } from '../middleware/rate-limit.js';
import { isArgon2Hash, isPasswordPwned, hashPassword, verifyPassword, timingSafeDummyVerify } from '../security/password.js';
import { htmlAttr, jsStringLiteral } from '../security/html.js';
import { signAccessToken, signRefreshToken, verifyRefreshToken, verifyAccessToken } from '../security/jwt.js';
import { isAccountLocked, registerLoginFailure, clearLoginFailures, denylistAccessToken } from '../security/redis.js';
import { emitActivity } from '../services/activity.js';

const auth = new Hono();

// =================================================================
// SCHEMAS
// =================================================================

const loginSchema = z.object({
    username: z.string().min(1).max(64),
    password: z.string().min(1).max(1024)
});

const refreshSchema = z.object({
    refreshToken: z.string().min(10).max(2048)
});

// Username: 3-15 char, lettere/numeri/underscore/trattino — restrittivo
// per evitare nickname con caratteri ambigui (zero-width, RTL, omoglifi).
const usernameRegex = /^[a-zA-Z0-9_-]{3,15}$/;

// Password: min 6 char + almeno una lettera + almeno un numero/carattere speciale.
// Stessa policy testuale dell'ufficiale habbo.it nel form di registrazione.
function passwordIsAcceptable(pw: string): { ok: boolean; reason?: string }
{
    if(pw.length < 6) return { ok: false, reason: 'password_weak' };
    if(pw.length > 256) return { ok: false, reason: 'password_weak' };
    const hasLetter = /[a-zA-Z]/.test(pw);
    const hasDigitOrSym = /[\d\W_]/.test(pw);
    if(!hasLetter || !hasDigitOrSym) return { ok: false, reason: 'password_weak' };
    return { ok: true };
}

const registerSchema = z.object({
    username: z.string().regex(usernameRegex),
    password: z.string().min(6).max(256),
    birthdate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
    // Campi del form Asteria (skin live): opzionali, validati e SALVATI.
    // Prima venivano ignorati e la mail era hardcoded '' → dati persi.
    email: z.string().email().max(254).optional(),
    look: z.string().max(255).optional(),
    gender: z.enum(['M', 'F']).optional(),
    profilePublic: z.boolean().optional(),
    newsletter: z.boolean().optional()
});

// =================================================================
// HELPERS
// =================================================================

function cookieOpts(maxAgeSeconds: number): string
{
    const parts = [
        'HttpOnly',
        `Max-Age=${maxAgeSeconds}`,
        'Path=/',
        `SameSite=${env.COOKIE_SAMESITE}`
    ];
    if(env.COOKIE_SECURE) parts.push('Secure');
    if(env.COOKIE_DOMAIN) parts.push(`Domain=${env.COOKIE_DOMAIN}`);
    return parts.join('; ');
}

async function logAudit(userId: number | null, action: string, ip: string, ua: string, details?: unknown)
{
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_audit_log (user_id, action, ip, user_agent, details, created_at) VALUES (?, ?, ?, ?, ?, UNIX_TIMESTAMP())',
            [userId, action, ip.slice(0, 45), ua.slice(0, 255), details ? JSON.stringify(details) : null]
        );
    }
    catch { /* non-blocking */ }
}

function clientIP(c: { req: { raw: Request } }): string
{
    // IP canonico via CF-Connecting-IP (non spoofabile). Vedi security/client-ip.ts.
    return clientIp(c.req.raw);
}

// =================================================================
// POST /auth/login
// =================================================================

auth.post(
    '/login',
    makeRateLimit('auth-login', env.RATE_LIMIT_LOGIN_PER_MIN, 60_000),
    async c =>
    {
        const body = await c.req.json().catch(() => null);
        const parsed = loginSchema.safeParse(body);
        if(!parsed.success)
        {
            return c.json({ error: 'bad_request', issues: parsed.error.flatten() }, 400);
        }

        const ip = clientIP(c);
        const ua = c.req.header('user-agent') ?? '';

        // Lockout per-account (Redis): dopo troppi fallimenti su QUESTO username
        // blocchiamo PRIMA di toccare DB/bcrypt. Chiude l'attacco distribuito
        // (molti IP, un solo account) che il rate-limit per-IP non intercetta.
        // Fail-open: Redis assente => nessun blocco (comportamento storico).
        if(await isAccountLocked(parsed.data.username))
        {
            await logAudit(null, 'auth.login.locked', ip, ua, { username: parsed.data.username });
            return c.json({ error: 'account_locked', message: 'Troppi tentativi falliti. Riprova tra qualche minuto.' }, 429);
        }

        // Cerca l'utente nella tabella `users` del vecchio CMS.
        const rows = await dbQuery<{ id: number; username: string; password: string; rank: number; mail: string }>(
            'SELECT id, username, password, rank, mail FROM users WHERE username = ? LIMIT 1',
            [parsed.data.username]
        );
        const u = rows[0];
        if(!u)
        {
            await logAudit(null, 'auth.login.failed.no_user', ip, ua, { username: parsed.data.username });
            await registerLoginFailure(parsed.data.username);
            // Timing-safe: facciamo comunque un verify dummy per evitare timing oracle.
            await timingSafeDummyVerify(parsed.data.password);
            return c.json({ error: 'invalid_credentials' }, 401);
        }

        const ok = await verifyPassword(u.password, parsed.data.password);
        if(!ok)
        {
            await logAudit(u.id, 'auth.login.failed.bad_password', ip, ua);
            await registerLoginFailure(parsed.data.username);
            return c.json({ error: 'invalid_credentials' }, 401);
        }

        // Ban-check (P1.5 security audit): un utente bannato in-game NON deve poter
        // autenticare sul CMS, altrimenti continua a incassare crediti reali via
        // streak/missioni/season-pass/referral (rcon.giveCredits) pur essendo escluso
        // dal gioco. Mirror del check dell'EMU (ModToolManager): ban account/super
        // non ancora scaduti. Stesso check anche in /refresh, così un ban applicato
        // a sessione attiva taglia l'accesso entro il TTL dell'access token (15 min).
        const banRows = await dbQuery<{ ban_expire: number; ban_reason: string }>(
            "SELECT ban_expire, ban_reason FROM bans WHERE user_id = ? AND ban_expire > UNIX_TIMESTAMP() AND type IN ('account', 'super') ORDER BY ban_expire DESC LIMIT 1",
            [u.id]
        );
        if(banRows[0])
        {
            await logAudit(u.id, 'auth.login.failed.banned', ip, ua, { until: banRows[0].ban_expire });
            return c.json({ error: 'account_banned', until: banRows[0].ban_expire, reason: String(banRows[0].ban_reason ?? '').slice(0, 200) }, 403);
        }

        // Migrazione trasparente bcrypt→Argon2id DISABILITATA:
        // la colonna `users.password` è VARCHAR(64) (legacy schema Arcturus,
        // dimensionata per bcrypt $2y$10$...60-char). Un hash Argon2id completo
        // è ~96 char e viene TRONCATO da MySQL → l'hash diventa irrecuperabile
        // e l'utente non riesce più a fare login.
        // bcrypt $2y$10$ è già robusto (OWASP 2024 "ok-but-prefer-Argon2id"),
        // quindi lasciamo i legacy bcrypt come sono. La migrazione corretta
        // richiede prima `ALTER TABLE users MODIFY password VARCHAR(255)`,
        // e DOPO si può riabilitare il blocco qui sotto.
        // (Vedi memory note "habboproject-cms-v3-pwhash-overflow".)

        // Genera coppia access + refresh.
        // Number(): un eventuale rank NULL a DB diventa 0 (nessun privilegio), mai
        // un claim non numerico (che il verify ora rifiuta).
        const access = await signAccessToken({ sub: String(u.id), username: u.username, rank: Number(u.rank) || 0 });
        const family = randomBytes(16).toString('hex');
        const refresh = await signRefreshToken(String(u.id), family);

        await dbExecute(
            'INSERT INTO cms_v3_refresh_tokens (user_id, family, jti, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, UNIX_TIMESTAMP(), ?, ?, ?)',
            [u.id, family, refresh.jti, refresh.exp, ip.slice(0, 45), ua.slice(0, 255)]
        );

        await logAudit(u.id, 'auth.login.success', ip, ua);
        await clearLoginFailures(parsed.data.username);

        // Set cookie httpOnly per refresh + cookie access (più breve).
        c.header('Set-Cookie', `cms_v3_access=${access.token}; ${cookieOpts(env.JWT_ACCESS_TTL_SECONDS)}`, { append: true });
        c.header('Set-Cookie', `cms_v3_refresh=${refresh.token}; ${cookieOpts(env.REFRESH_TOKEN_TTL_DAYS * 24 * 3600)}`, { append: true });

        return c.json({
            user: { id: u.id, username: u.username, rank: u.rank, mail: u.mail },
            accessToken: access.token,
            expiresAt: access.exp
        });
    }
);

// =================================================================
// POST /auth/register
// =================================================================

auth.post(
    '/register',
    // Rate limit aggressivo per evitare account farming (P2.3): finestra ORARIA
    // da env (default 3/h). Prima era hardcoded 5/min = 300/h e la variabile
    // RATE_LIMIT_REGISTER_PER_HOUR era config morta.
    makeRateLimit('auth-register', env.RATE_LIMIT_REGISTER_PER_HOUR, 3_600_000),
    async c =>
    {
        const body = await c.req.json().catch(() => null);
        const parsed = registerSchema.safeParse(body);
        if(!parsed.success)
        {
            // Determina il primo errore semanticamente utile.
            const flat = parsed.error.flatten();
            const fieldErrors = flat.fieldErrors;
            let err = 'bad_request';
            if(fieldErrors.username?.length) err = 'username_invalid';
            else if(fieldErrors.password?.length) err = 'password_weak';
            else if(fieldErrors.birthdate?.length) err = 'birthdate_invalid';
            return c.json({ error: err, issues: flat }, 400);
        }

        const ip = clientIP(c);
        const ua = c.req.header('user-agent') ?? '';

        // Vincolo età ≥16 anni (stessa policy ufficiale habbo.it).
        const birth = new Date(parsed.data.birthdate + 'T00:00:00Z');
        if(isNaN(birth.getTime())) return c.json({ error: 'birthdate_invalid' }, 400);
        const ageMs = Date.now() - birth.getTime();
        const ageYears = ageMs / (365.25 * 24 * 3600 * 1000);
        if(ageYears < 16) return c.json({ error: 'underage' }, 400);

        // Password policy.
        const pw = passwordIsAcceptable(parsed.data.password);
        if(!pw.ok)
        {
            await logAudit(null, 'auth.register.failed.password_weak', ip, ua, { username: parsed.data.username });
            return c.json({ error: pw.reason }, 400);
        }

        // HIBP k-anonymity check — blocchiamo password già breached.
        try
        {
            const pwned = await isPasswordPwned(parsed.data.password);
            if(pwned.pwned && pwned.count > 0)
            {
                await logAudit(null, 'auth.register.failed.password_pwned', ip, ua, { username: parsed.data.username, count: pwned.count });
                return c.json({ error: 'password_pwned' }, 400);
            }
        }
        catch { /* HIBP fallibile, non-bloccante */ }

        // Username unique check (case-insensitive: l'EMU compara username
        // ignorando il case, quindi blocchiamo varianti).
        const existing = await dbQuery<{ id: number }>(
            'SELECT id FROM users WHERE LOWER(username) = LOWER(?) LIMIT 1',
            [parsed.data.username]
        );
        if(existing.length > 0)
        {
            await logAudit(null, 'auth.register.failed.username_taken', ip, ua, { username: parsed.data.username });
            return c.json({ error: 'username_taken' }, 409);
        }

        const passwordHash = await hashPassword(parsed.data.password);

        // Crea utente — schema Arcturus standard.
        // Schema reale Arcturus `users`: tutti i campi non specificati hanno
        // default (rank=1, credits=2500, pixels=500, points=10, online='0',
        // motto='', auth_ticket='', machine_id='', home_room=0, ecc).
        // NB: NON ci sono colonne `vip_points` o `diamonds` qui (l'Arcturus
        // base usa solo `points`; ricchezze extra → tabelle separate).
        const now = Math.floor(Date.now() / 1000);
        const defaultFigure = 'hr-100-61.hd-180-1.ch-210-66.lg-270-82.sh-290-80'; // default look IT
        // look: accettiamo solo il set di caratteri di una figure-string Habbo
        // (lettere/cifre/`.`/`-`), così un valore ostile non può iniettare nell'URL
        // dell'imager o nel client. Fallback al default.
        const look = (parsed.data.look && /^[a-zA-Z0-9._-]{1,255}$/.test(parsed.data.look))
            ? parsed.data.look
            : defaultFigure;
        const gender = parsed.data.gender === 'F' ? 'F' : 'M';
        const mail = parsed.data.email ?? ''; // opzionale: registrazione username-only resta valida
        const result = await dbExecute(
            `INSERT INTO users (
                username, password, mail, look, gender, motto,
                account_created, last_online, last_login,
                ip_register, ip_current
            ) VALUES (?, ?, ?, ?, ?, 'Nuovo Habbo!', ?, ?, ?, ?, ?)`,
            [
                parsed.data.username,
                passwordHash,
                mail,
                look,
                gender,
                now, now, now,
                ip.slice(0, 45), ip.slice(0, 45)
            ]
        );

        const insertedId = (result as { insertId?: number }).insertId;
        if(!insertedId)
        {
            await logAudit(null, 'auth.register.failed.db_no_insert_id', ip, ua, { username: parsed.data.username });
            return c.json({ error: 'server_error' }, 500);
        }

        // Auto-login: emetti subito access + refresh.
        const access = await signAccessToken({ sub: String(insertedId), username: parsed.data.username, rank: 1 });
        const family = randomBytes(16).toString('hex');
        const refresh = await signRefreshToken(String(insertedId), family);

        await dbExecute(
            'INSERT INTO cms_v3_refresh_tokens (user_id, family, jti, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, UNIX_TIMESTAMP(), ?, ?, ?)',
            [insertedId, family, refresh.jti, refresh.exp, ip.slice(0, 45), ua.slice(0, 255)]
        );

        await logAudit(insertedId, 'auth.register.success', ip, ua, {
            profilePublic: !!parsed.data.profilePublic,
            newsletter: !!parsed.data.newsletter
        });

        // Activity feed: benvenuto pubblico (fire-and-forget, best-effort —
        // non aggiunge latenza alla registrazione, non la fa fallire).
        void emitActivity({ type: 'user.registered', actorId: insertedId, actorName: parsed.data.username });

        c.header('Set-Cookie', `cms_v3_access=${access.token}; ${cookieOpts(env.JWT_ACCESS_TTL_SECONDS)}`, { append: true });
        c.header('Set-Cookie', `cms_v3_refresh=${refresh.token}; ${cookieOpts(env.REFRESH_TOKEN_TTL_DAYS * 24 * 3600)}`, { append: true });

        return c.json({
            user: { id: insertedId, username: parsed.data.username, rank: 1 },
            accessToken: access.token,
            expiresAt: access.exp
        }, 201);
    }
);

// =================================================================
// POST /auth/refresh — rotazione token con detection del riuso
// =================================================================

auth.post('/refresh', async c =>
{
    const body = await c.req.json().catch(() => null);
    const parsed = refreshSchema.safeParse(body);
    // Token dal body (legacy/mobile) OPPURE dal cookie httpOnly cms_v3_refresh.
    // Il browser non può leggere un cookie httpOnly per rimetterlo nel body,
    // quindi senza questo fallback /refresh era inutilizzabile dal frontend e
    // la sessione moriva alla scadenza dell'access token (15 min).
    const refreshToken = parsed.success ? parsed.data.refreshToken : (getCookie(c, 'cms_v3_refresh') ?? '');
    if(!refreshToken || refreshToken.length < 10) return c.json({ error: 'bad_request' }, 400);

    const payload = await verifyRefreshToken(refreshToken);
    if(!payload) return c.json({ error: 'invalid_token' }, 401);

    // Controlla se questo jti è ancora attivo.
    const rows = await dbQuery<{ id: number; used_at: number | null; revoked_at: number | null; family: string }>(
        'SELECT id, used_at, revoked_at, family FROM cms_v3_refresh_tokens WHERE jti = ? LIMIT 1',
        [payload.jti]
    );
    const stored = rows[0];
    if(!stored) return c.json({ error: 'invalid_token' }, 401);
    if(stored.revoked_at !== null) return c.json({ error: 'revoked' }, 401);

    // Claim ATOMICO (security review 2026-09-20): il vecchio check used_at===null +
    // UPDATE separato era TOCTOU -> due /refresh concorrenti con lo stesso token
    // passavano entrambi e la reuse-detection non scattava. Ora vince UN SOLO
    // refresh; affectedRows!==1 => token già usato (reuse) o race persa -> revoca family.
    const claim = await dbExecute(
        'UPDATE cms_v3_refresh_tokens SET used_at = UNIX_TIMESTAMP() WHERE jti = ? AND used_at IS NULL',
        [payload.jti]
    );
    if((claim as { affectedRows: number }).affectedRows !== 1)
    {
        await dbExecute(
            'UPDATE cms_v3_refresh_tokens SET revoked_at = UNIX_TIMESTAMP() WHERE family = ? AND revoked_at IS NULL',
            [stored.family]
        );
        await logAudit(Number(payload.sub), 'auth.refresh.reuse_detected', clientIP(c), c.req.header('user-agent') ?? '', { family: stored.family });
        return c.json({ error: 'reuse_detected' }, 401);
    }

    // Carica info utente (rank può cambiare nel mentre).
    const userRows = await dbQuery<{ id: number; username: string; rank: number }>(
        'SELECT id, username, rank FROM users WHERE id = ? LIMIT 1',
        [Number(payload.sub)]
    );
    const u = userRows[0];
    if(!u) return c.json({ error: 'invalid_token' }, 401);

    // Ban-check anche al refresh (P1.5): se nel frattempo l'utente è stato bannato,
    // non rinnoviamo la sessione e revochiamo l'intera refresh-family, così il ban
    // ha effetto entro il TTL dell'access token invece che fino alla scadenza del
    // refresh (14 giorni).
    const banRows = await dbQuery<{ ban_expire: number }>(
        "SELECT ban_expire FROM bans WHERE user_id = ? AND ban_expire > UNIX_TIMESTAMP() AND type IN ('account', 'super') LIMIT 1",
        [u.id]
    );
    if(banRows[0])
    {
        await dbExecute(
            'UPDATE cms_v3_refresh_tokens SET revoked_at = UNIX_TIMESTAMP() WHERE family = ? AND revoked_at IS NULL',
            [stored.family]
        );
        await logAudit(u.id, 'auth.refresh.denied.banned', clientIP(c), c.req.header('user-agent') ?? '', { until: banRows[0].ban_expire, family: stored.family });
        return c.json({ error: 'account_banned', until: banRows[0].ban_expire }, 403);
    }

    // Genera nuova coppia (rotation).
    const access = await signAccessToken({ sub: String(u.id), username: u.username, rank: Number(u.rank) || 0 });
    const newRefresh = await signRefreshToken(String(u.id), stored.family);

    await dbExecute(
        'INSERT INTO cms_v3_refresh_tokens (user_id, family, jti, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, UNIX_TIMESTAMP(), ?, ?, ?)',
        [u.id, stored.family, newRefresh.jti, newRefresh.exp, clientIP(c).slice(0, 45), (c.req.header('user-agent') ?? '').slice(0, 255)]
    );
    await dbExecute(
        'UPDATE cms_v3_refresh_tokens SET replaced_by_jti = ? WHERE jti = ?',
        [newRefresh.jti, payload.jti]
    );

    c.header('Set-Cookie', `cms_v3_access=${access.token}; ${cookieOpts(env.JWT_ACCESS_TTL_SECONDS)}`, { append: true });
    c.header('Set-Cookie', `cms_v3_refresh=${newRefresh.token}; ${cookieOpts(env.REFRESH_TOKEN_TTL_DAYS * 24 * 3600)}`, { append: true });

    return c.json({ accessToken: access.token, expiresAt: access.exp });
});

// =================================================================
// POST /auth/logout
// =================================================================

auth.post('/logout', async c =>
{
    const cookies = c.req.header('cookie') ?? '';
    const m = cookies.match(/(?:^|;\s*)cms_v3_refresh=([^;]+)/);
    if(m)
    {
        let refreshRaw = '';
        try { refreshRaw = decodeURIComponent(m[1] ?? ''); } catch { refreshRaw = ''; }
        const payload = await verifyRefreshToken(refreshRaw);
        if(payload)
        {
            await dbExecute(
                'UPDATE cms_v3_refresh_tokens SET revoked_at = UNIX_TIMESTAMP() WHERE family = ? AND revoked_at IS NULL',
                [payload.family]
            );
            await logAudit(Number(payload.sub), 'auth.logout', clientIP(c), c.req.header('user-agent') ?? '');
        }
    }
    // Deny-list dell'access token ancora vivo: senza questa, un access token
    // rubato resta valido fino a ~15 min DOPO il logout. Con Redis lo revochiamo
    // subito (TTL = durata residua massima). Fail-open se Redis è assente.
    const am = cookies.match(/(?:^|;\s*)cms_v3_access=([^;]+)/);
    if(am)
    {
        let accessRaw = '';
        try { accessRaw = decodeURIComponent(am[1] ?? ''); } catch { accessRaw = ''; }
        const ap = accessRaw ? await verifyAccessToken(accessRaw) : null;
        if(ap?.jti) await denylistAccessToken(ap.jti, env.JWT_ACCESS_TTL_SECONDS);
    }
    c.header('Set-Cookie', `cms_v3_access=; ${cookieOpts(0)}`, { append: true });
    c.header('Set-Cookie', `cms_v3_refresh=; ${cookieOpts(0)}`, { append: true });
    return c.json({ ok: true });
});

// =================================================================
// GET /auth/sso — RIMOSSO (Wave 18 SSO hardening)
// GET /auth/sso-token — RIMOSSO (Wave 18 SSO hardening)
// =================================================================
//
// Entrambi gli endpoint ritornavano il ticket in JSON: un XSS sul CMS
// poteva scriptare `fetch('/api/v2/auth/sso-token')` e exfiltrare il
// ticket. Il solo path supportato ora è `/auth/play` che inietta il
// ticket SERVER-SIDE direttamente nell'HTML del Nitro (mai esposto
// come JSON consumabile da JavaScript).
//
// Ritorniamo 410 Gone esplicito per non confondere debug:
//   - 404 → "endpoint mai esistito" (fuorvia)
//   - 410 → "rimosso intenzionalmente, vedi changelog"

auth.get('/sso', c => c.json({ error: 'endpoint_removed', useInstead: '/api/v2/auth/play' }, 410));
auth.get('/sso-token', c => c.json({ error: 'endpoint_removed', useInstead: '/api/v2/auth/play' }, 410));

// =================================================================
// GET /auth/health — Nitro pinga questo all'avvio per check connettività
// =================================================================

auth.get('/health', c => c.json({ status: 'ok', service: 'cms-v3-api' }));

// =================================================================
// GET /auth/play — launcher Nitro con SSO ticket pre-iniettato
// =================================================================
// Nitro V3 NON legge `?sso=` da URL e NON chiama autonomamente
// /api/auth/sso-token al boot: aspetta che `window.NitroConfig['sso.ticket']`
// sia popolato (di solito dal CMS server-side prima dello shell HTML).
//
// Questo endpoint:
//   1. richiede auth (cookie cms_v3_access)
//   2. emette un auth_ticket fresco in users.auth_ticket
//   3. fetcha l'index.html del Nitro standalone su :8091
//   4. iniettata `<base href="/client/">` per risolvere asset relativi
//      tramite il Vite proxy → :8091
//   5. iniettata un interceptor `Object.defineProperty(window,'NitroConfig'…)`
//      che, alla PRIMA assegnazione da parte di bootstrap.js, scrive
//      automaticamente `NitroConfig['sso.ticket']` col valore generato
//      (questo evita la race con il caricamento async del React app)
//   6. ritorna l'HTML modificato come text/html
//
// GIOCA button quindi punta a /api/v2/auth/play.

auth.get('/play', async c =>
{
    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);

    const ip = clientIP(c);
    const ua = c.req.header('user-agent') ?? '';

    // 1. Genera ticket + binding IP/UA (Wave 18 hardening).
    //
    // CMS-V3 popola 4 colonne:
    //   - auth_ticket            : 192 bit random hex
    //   - auth_ticket_issued_at  : UNIX timestamp per TTL check (EMU 60s)
    //   - auth_ticket_bound_ip   : IP del client HTTP — l'EMU lo confronta
    //                              con il peer del WebSocket all'handshake.
    //                              Mismatch → ticket rifiutato.
    //   - auth_ticket_ua_hash    : SHA-256 dell'User-Agent — l'EMU lo confronta
    //                              col header che il client invierà (canale wired
    //                              MFA o future client-handshake header).
    //
    // Risultato: anche se un attaccante exfiltrasse il ticket via leak DB
    // (read-only) o XSS, non potrebbe usarlo da un altro browser perché:
    //   • TTL 60s scade prima
    //   • peer IP non matcha (deve venire dalla stessa rete del browser
    //     dell'utente al momento del /play)
    //   • UA hash differente
    const ticket = randomBytes(24).toString('hex');
    const uaHash = createHash('sha256').update(ua).digest('hex');
    await dbExecute(
        `UPDATE users SET
            auth_ticket = ?,
            auth_ticket_issued_at = UNIX_TIMESTAMP(),
            auth_ticket_bound_ip = ?,
            auth_ticket_ua_hash = ?
         WHERE id = ?`,
        [ticket, ip.slice(0, 45), uaHash, Number(user.sub)]
    );
    await logAudit(Number(user.sub), 'auth.play.launched', ip, ua);

    // 2. Carica l'HTML del bundle Nitro dal CDN (cdn.asteriacore.online).
    //
    //    OTTIMIZZAZIONE: cache in-memory module-scoped (TTL 5 min).
    //    L'index.html del Nitro V3 e' statico (~1.2KB) — non ha senso fetchare
    //    da CDN ad ogni /play. Cachiamo in memoria per evitare:
    //      - Latency extra (~50ms per fetch)
    //      - Possibile rate-limit CF se /play e' chiamato di frequente
    //      - Bot Fight Mode di Cloudflare che blocca server-side fetch
    //        (vede Node UA non-browser e ritorna challenge HTML 403)
    //
    //    L'invalidazione TTL 5 min e' sufficiente: l'index Nitro cambia solo
    //    quando rilasciamo nuova build (raro, eventi staff/PR merge).
    //
    //    UA spoof: usiamo Mozilla/5.0 generic per bypassare Bot Fight CF
    //    (CDN R2 dietro proxy CF). Tecnicamente e' UA spoofing, ma per accedere
    //    al PROPRIO bucket R2 dal PROPRIO VPS e' legittimo.
    const CDN_BASE = env.CDN_BASE_URL || 'https://cdn.asteriacore.online';
    const nitroHtml = NITRO_INDEX_TEMPLATE;

    // 3. Inject <base href={CDN_BASE}/> + NitroConfig interceptor.
    //    Il <base> e' cruciale: il client Nitro fa request relative
    //    (configuration/bootstrap.js, assets/*.js, gamedata/*.json) che senza
    //    base risolverebbero a asteriacore.online (= CMS-V3 frontend, 404).
    //    Con <base href="https://cdn.asteriacore.online/"> tutte risolvono a CDN.
    //
    //    L'interceptor NitroConfig assegna il ticket SSO al primo set di
    //    window.NitroConfig fatto da bootstrap.js (timing-safe wrap).
    //    Usiamo JSON.stringify per escape pulito (no XSS via ticket).
    const ticketJson = jsStringLiteral(ticket);
    const baseHref = htmlAttr(`${CDN_BASE}/`);

    // 3b. (DEV-only) Override socket.url verso l'EMU dev.
    //
    //   PROBLEMA: il client carica la config CONDIVISA dal CDN
    //   (renderer-config.json) dove "socket.url" = wss://hotel.asteriacore.online/
    //   (EMU di PROD). Su dev il client si connetterebbe quindi all'EMU prod
    //   con un ticket dev → l'EMU rifiuta → schermata "session expired".
    //
    //   SOLUZIONE: se env.NITRO_SOCKET_URL è settata (SOLO dev), settiamo
    //   socket.url DIRETTAMENTE nel base window.NitroConfig. Il
    //   ConfigurationManager (Nitro_Render_V3) parsa il base con overrides=TRUE
    //   PRIMA, poi i file di config.urls (renderer-config.json, …) con
    //   overrides=FALSE — quindi un file NON sovrascrive una chiave già nel
    //   base. Mettendo socket.url nel base, il socket.url=PROD di
    //   renderer-config.json viene skippato → vince il nostro valore dev.
    //   (NB: appendere a config.urls NON funziona — il merge dei file è
    //   first-wins e renderer-config.json è il primo della lista.)
    //
    //   In PROD env.NITRO_SOCKET_URL è assente ⇒ overrideScript = '' ⇒ l'HTML
    //   iniettato è identico a prima ⇒ comportamento di produzione invariato.
    const overrideScript = env.NITRO_SOCKET_URL
        ? `x['socket.url']=${jsStringLiteral(env.NITRO_SOCKET_URL)};`
        : '';

    const inject = `<base href="${baseHref}"><script>(function(){var t=${ticketJson},v;Object.defineProperty(window,'NitroConfig',{get:function(){return v},set:function(x){v=x;if(x&&typeof x==='object'){x['sso.ticket']=t;${overrideScript}}},configurable:true})})();</script>`;

    const modified = nitroHtml.replace(/<head>/i, '<head>' + inject);

    // 4. Headers di hardening sulla response /play:
    //    - Cache-Control no-store: NO cache disco browser, NO cache proxy
    //    - Pragma no-cache: legacy HTTP/1.0 caches
    //    - X-Frame-Options SAMEORIGIN: blocca clickjacking via iframe esterno
    //    - Referrer-Policy no-referrer: override globale, niente leak Referer
    //    - X-Robots-Tag noindex: nessun motore di ricerca cache-a /play
    //    - X-Content-Type-Options nosniff: blocca MIME confusion
    //    - Permissions-Policy: disabilita feature browser non necessarie
    c.header('Content-Type', 'text/html; charset=utf-8');
    c.header('Cache-Control', 'no-store, no-cache, must-revalidate, private');
    c.header('Pragma', 'no-cache');
    c.header('X-Frame-Options', 'SAMEORIGIN');
    c.header('Referrer-Policy', 'no-referrer');
    c.header('X-Robots-Tag', 'noindex, nofollow');
    c.header('X-Content-Type-Options', 'nosniff');
    // microphone=(self): la pagina /gioca ospita la dettatura vocale in chat
    // (Web Speech API). (self) = solo questa origine; prompt utente comunque richiesto.
    c.header('Permissions-Policy', 'geolocation=(), microphone=(self), camera=(), payment=(), usb=(), interest-cohort=()');
    return c.body(modified);
});

// =================================================================
// Template HTML del Nitro V3 (hardcoded per evitare CF Bot Fight su fetch CDN).
// =================================================================
// Tentativo precedente: fetch da https://cdn.asteriacore.online/index.html
//   → 403 Bot Fight challenge anche con UA Chrome reale (CF blocca server-side
//   fetch da IP datacenter Hetzner, è una protezione legittima).
//
// Soluzione: serviamo direttamente un template HTML minimale identico a
// quello buildato dal Nitro V3 (vedi CMS/react/index.html). Il bundle vero
// e' su CDN — questo HTML lo carica via <script src="configuration/bootstrap.js">
// relativo, che con <base href=CDN> risolve sul CDN.
//
// Se in futuro la Nitro V3 cambia il template (es. aggiunge meta tag, ecc),
// basta aggiornare questa costante. L'HTML e' ~330 byte, no overhead.
const NITRO_INDEX_TEMPLATE = '<!DOCTYPE html><html lang="it"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Asteria Hotel</title></head><body><div id="root"></div><script src="configuration/bootstrap.js"></script></body></html>';

// =================================================================
// HIBP check utility per il register form
// =================================================================

auth.post('/check-password', makeRateLimit('check-pwd', 30, 60_000), async c =>
{
    const body = await c.req.json().catch(() => null);
    if(!body || typeof body !== 'object' || typeof (body as { password?: unknown }).password !== 'string')
    {
        return c.json({ error: 'bad_request' }, 400);
    }
    const r = await isPasswordPwned((body as { password: string }).password);
    return c.json(r);
});

export default auth;
