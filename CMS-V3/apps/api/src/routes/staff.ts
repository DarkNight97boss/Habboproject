import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';
import { rcon } from '../services/rcon.js';

const staff = new Hono();

// Tutti gli endpoint qui sotto richiedono rank >= 5 (staff Arcturus).
// requireAuth viene applicato a tutto, requireRank è la barriera elevata.
staff.use('*', requireAuth);
staff.use('*', requireRank(5));

// =================================================================
// Audit helper: ogni comando RCON è loggato in cms_v3_audit_log
// =================================================================
async function logRconAudit(
    actorId: number,
    action: string,
    ip: string,
    ua: string,
    details: Record<string, unknown>
): Promise<void>
{
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_audit_log (user_id, action, ip, user_agent, details, created_at) VALUES (?, ?, ?, ?, ?, UNIX_TIMESTAMP())',
            [actorId, `staff.rcon.${action}`, ip.slice(0, 45), ua.slice(0, 255), JSON.stringify(details)]
        );
    }
    catch { /* non-blocking */ }
}

function clientIp(c: { req: { raw: Request } }): string
{
    const xff = c.req.raw.headers.get('x-forwarded-for');
    return (xff?.split(',')[0]?.trim()) ?? c.req.raw.headers.get('x-real-ip') ?? 'unknown';
}

/** Risolve username → user_id. Ritorna null se non esiste. */
async function lookupUserId(username: string): Promise<number | null>
{
    const rows = await dbQuery<{ id: number }>(
        'SELECT id FROM users WHERE username = ? LIMIT 1',
        [username]
    );
    return rows[0]?.id ?? null;
}

// =================================================================
// POST /staff/alert — popup in-game al singolo utente
// =================================================================
staff.post('/alert', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        message: z.string().min(1).max(2000)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);

    const r = await rcon.alertUser(userId, parsed.data.message);
    await logRconAudit(Number(actor.sub), 'alert', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, message: parsed.data.message, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/hotel-alert — alert broadcast a tutto l'hotel
// =================================================================
staff.post('/hotel-alert', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ message: z.string().min(1).max(2000) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const r = await rcon.hotelAlert(parsed.data.message);
    await logRconAudit(Number(actor.sub), 'hotel_alert', clientIp(c), c.req.header('user-agent') ?? '',
        { message: parsed.data.message, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/credits — +/- crediti real-time
// =================================================================
staff.post('/credits', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        amount: z.number().int().min(-1_000_000).max(1_000_000),
        alertMessage: z.string().max(2000).optional()
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);

    const r = await rcon.giveCredits(userId, parsed.data.amount);
    if(parsed.data.alertMessage)
    {
        await rcon.alertUser(userId, parsed.data.alertMessage);
    }
    await logRconAudit(Number(actor.sub), 'give_credits', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, amount: parsed.data.amount, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/duckets (pixels) e /staff/diamonds (points)
// =================================================================
staff.post('/duckets', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        amount: z.number().int().min(-1_000_000).max(1_000_000)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.giveDuckets(userId, parsed.data.amount);
    await logRconAudit(Number(actor.sub), 'give_duckets', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, amount: parsed.data.amount, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/diamonds', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        amount: z.number().int().min(-1_000_000).max(1_000_000)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.giveDiamonds(userId, parsed.data.amount);
    await logRconAudit(Number(actor.sub), 'give_diamonds', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, amount: parsed.data.amount, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/badge — assegna badge
// =================================================================
staff.post('/badge', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        badge: z.string().min(1).max(64)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.giveBadge(userId, parsed.data.badge);
    await logRconAudit(Number(actor.sub), 'give_badge', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, badge: parsed.data.badge, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/disconnect — kick utente
// =================================================================
staff.post('/disconnect', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.disconnect(userId);
    await logRconAudit(Number(actor.sub), 'disconnect', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/mute — mute temporaneo
// =================================================================
staff.post('/mute', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        minutes: z.number().int().min(1).max(60 * 24)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.muteUser(userId, parsed.data.minutes);
    await logRconAudit(Number(actor.sub), 'mute', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, minutes: parsed.data.minutes, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// POST /staff/refresh-catalog e /staff/refresh-wordfilter
// =================================================================
staff.post('/refresh-catalog', async c =>
{
    const actor = c.var.user!;
    const r = await rcon.updateCatalog();
    await logRconAudit(Number(actor.sub), 'refresh_catalog', clientIp(c), c.req.header('user-agent') ?? '', { ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/refresh-wordfilter', async c =>
{
    const actor = c.var.user!;
    const r = await rcon.updateWordfilter();
    await logRconAudit(Number(actor.sub), 'refresh_wordfilter', clientIp(c), c.req.header('user-agent') ?? '', { ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// GET /staff/audit-log — viewer audit log (ultime 100 entries)
// =================================================================
staff.get('/audit-log', async c =>
{
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);

    const rows = await dbQuery<{
        id: number; user_id: number | null; action: string;
        ip: string; user_agent: string; details: string | null;
        created_at: number; username: string | null;
    }>(
        `SELECT al.id, al.user_id, al.action, al.ip, al.user_agent,
                al.details, al.created_at, u.username
         FROM cms_v3_audit_log al
         LEFT JOIN users u ON u.id = al.user_id
         ORDER BY al.id DESC
         LIMIT ?`,
        [limit]
    );

    return c.json({
        entries: rows.map(r => ({
            id: r.id,
            userId: r.user_id,
            username: r.username,
            action: r.action,
            ip: r.ip,
            userAgent: r.user_agent,
            details: r.details ? safeParseJson(r.details) : null,
            createdAt: r.created_at
        }))
    });
});

function safeParseJson(s: string): unknown
{
    try { return JSON.parse(s); }
    catch { return s; }
}

// =================================================================
// GET /staff/stats — quick stats dashboard
// =================================================================
staff.get('/stats', async c =>
{
    const rows = await dbQuery<{
        total_users: number; online_users: number; total_rooms: number;
        total_photos: number; staff_count: number;
    }>(
        `SELECT
            (SELECT COUNT(*) FROM users) AS total_users,
            (SELECT COUNT(*) FROM users WHERE online = '1') AS online_users,
            (SELECT COUNT(*) FROM rooms WHERE state <> 'invisible') AS total_rooms,
            (SELECT COUNT(*) FROM camera_web) AS total_photos,
            (SELECT COUNT(*) FROM users WHERE rank >= 5) AS staff_count`
    );

    return c.json({
        totalUsers: rows[0]?.total_users ?? 0,
        onlineUsers: rows[0]?.online_users ?? 0,
        totalRooms: rows[0]?.total_rooms ?? 0,
        totalPhotos: rows[0]?.total_photos ?? 0,
        staffCount: rows[0]?.staff_count ?? 0
    });
});

export default staff;
