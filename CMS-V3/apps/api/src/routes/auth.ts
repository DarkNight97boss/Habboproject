import { Hono } from 'hono';
import { randomBytes } from 'node:crypto';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { env } from '../env.js';
import { makeRateLimit } from '../middleware/rate-limit.js';
import { isArgon2Hash, isPasswordPwned, hashPassword, verifyPassword, timingSafeDummyVerify } from '../security/password.js';
import { signAccessToken, signRefreshToken, verifyRefreshToken } from '../security/jwt.js';

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
    const xff = c.req.raw.headers.get('x-forwarded-for');
    return (xff?.split(',')[0]?.trim()) ?? c.req.raw.headers.get('x-real-ip') ?? 'unknown';
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

        // Cerca l'utente nella tabella `users` del vecchio CMS.
        const rows = await dbQuery<{ id: number; username: string; password: string; rank: number; mail: string }>(
            'SELECT id, username, password, rank, mail FROM users WHERE username = ? LIMIT 1',
            [parsed.data.username]
        );
        const u = rows[0];
        if(!u)
        {
            await logAudit(null, 'auth.login.failed.no_user', ip, ua, { username: parsed.data.username });
            // Timing-safe: facciamo comunque un verify dummy per evitare timing oracle.
            await timingSafeDummyVerify(parsed.data.password);
            return c.json({ error: 'invalid_credentials' }, 401);
        }

        const ok = await verifyPassword(u.password, parsed.data.password);
        if(!ok)
        {
            await logAudit(u.id, 'auth.login.failed.bad_password', ip, ua);
            return c.json({ error: 'invalid_credentials' }, 401);
        }

        // Migrazione trasparente: se la password era bcrypt/md5/etc. (non Argon2),
        // ri-hashiamo con Argon2id e salviamo.
        if(!isArgon2Hash(u.password))
        {
            try
            {
                const newHash = await hashPassword(parsed.data.password);
                await dbExecute('UPDATE users SET password = ? WHERE id = ?', [newHash, u.id]);
                await logAudit(u.id, 'auth.password.rehashed_argon2', ip, ua);
            }
            catch { /* non-blocking, log skipped */ }
        }

        // Genera coppia access + refresh.
        const access = await signAccessToken({ sub: String(u.id), username: u.username, rank: u.rank });
        const family = randomBytes(16).toString('hex');
        const refresh = await signRefreshToken(String(u.id), family);

        await dbExecute(
            'INSERT INTO cms_v3_refresh_tokens (user_id, family, jti, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, UNIX_TIMESTAMP(), ?, ?, ?)',
            [u.id, family, refresh.jti, refresh.exp, ip.slice(0, 45), ua.slice(0, 255)]
        );

        await logAudit(u.id, 'auth.login.success', ip, ua);

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
    // Rate limit aggressivo per evitare account farming.
    makeRateLimit('auth-register', 5, 60_000),
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
            if(pwned.pwned && pwned.count > 5)
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
        // - rank 1 (utente normale), credits/duckets/diamonds starter,
        //   look/look_settings da defaults, last_online ora.
        const now = Math.floor(Date.now() / 1000);
        const figure = 'hr-100-61.hd-180-1.ch-210-66.lg-270-82.sh-290-80'; // default look it
        const result = await dbExecute(
            `INSERT INTO users (
                username, password, mail, rank, credits, pixels, vip_points, diamonds, online,
                look, gender, motto, account_created, last_online, last_login, ip_register, ip_current,
                home_room
            ) VALUES (?, ?, '', 1, 1000, 100, 0, 5, '0', ?, 'M', 'Nuovo Habbo!', ?, ?, ?, ?, ?, 0)`,
            [parsed.data.username, passwordHash, figure, now, now, now, ip.slice(0, 45), ip.slice(0, 45)]
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
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const payload = await verifyRefreshToken(parsed.data.refreshToken);
    if(!payload) return c.json({ error: 'invalid_token' }, 401);

    // Controlla se questo jti è ancora attivo.
    const rows = await dbQuery<{ id: number; used_at: number | null; revoked_at: number | null; family: string }>(
        'SELECT id, used_at, revoked_at, family FROM cms_v3_refresh_tokens WHERE jti = ? LIMIT 1',
        [payload.jti]
    );
    const stored = rows[0];
    if(!stored) return c.json({ error: 'invalid_token' }, 401);
    if(stored.revoked_at !== null) return c.json({ error: 'revoked' }, 401);

    if(stored.used_at !== null)
    {
        // 🚨 token già usato → potenziale furto. Revochiamo l'INTERA family.
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

    // Genera nuova coppia (rotation).
    const access = await signAccessToken({ sub: String(u.id), username: u.username, rank: u.rank });
    const newRefresh = await signRefreshToken(String(u.id), stored.family);

    await dbExecute(
        'INSERT INTO cms_v3_refresh_tokens (user_id, family, jti, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, UNIX_TIMESTAMP(), ?, ?, ?)',
        [u.id, stored.family, newRefresh.jti, newRefresh.exp, clientIP(c).slice(0, 45), (c.req.header('user-agent') ?? '').slice(0, 255)]
    );
    await dbExecute(
        'UPDATE cms_v3_refresh_tokens SET used_at = UNIX_TIMESTAMP(), replaced_by_jti = ? WHERE jti = ?',
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
        const payload = await verifyRefreshToken(decodeURIComponent(m[1] ?? ''));
        if(payload)
        {
            await dbExecute(
                'UPDATE cms_v3_refresh_tokens SET revoked_at = UNIX_TIMESTAMP() WHERE family = ? AND revoked_at IS NULL',
                [payload.family]
            );
            await logAudit(Number(payload.sub), 'auth.logout', clientIP(c), c.req.header('user-agent') ?? '');
        }
    }
    c.header('Set-Cookie', `cms_v3_access=; ${cookieOpts(0)}`, { append: true });
    c.header('Set-Cookie', `cms_v3_refresh=; ${cookieOpts(0)}`, { append: true });
    return c.json({ ok: true });
});

// =================================================================
// GET /auth/sso — genera un auth_ticket fresco da consumare dal client Nitro
// =================================================================

auth.get('/sso', async c =>
{
    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);

    // Ticket lifetime: 60 secondi (consumato subito dal client al boot).
    const ticket = randomBytes(24).toString('hex');
    await dbExecute('UPDATE users SET auth_ticket = ? WHERE id = ?', [ticket, Number(user.sub)]);
    await logAudit(Number(user.sub), 'auth.sso.issued', clientIP(c), c.req.header('user-agent') ?? '');
    return c.json({ ticket });
});

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
