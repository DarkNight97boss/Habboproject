import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';
import { hashPassword } from '../security/password.js';
import { fetchAllNewsAdmin, fetchNewsBySlug } from '../services/news.js';
import { rcon } from '../services/rcon.js';

/**
 * Staff routes (/api/v2/staff/*) — porting completo del pannello housekeeping
 * legacy PHP. Tutti gli endpoint richiedono rank >= 5.
 *
 * Sezioni:
 *   1. Azioni RCON dirette (alert/credits/badge/...) — già usate dal CMS-V3
 *   2. User management (lookup, edit, delete, reset password, strike)
 *   3. Bans (list, add, remove)
 *   4. Logs (chat room/PM, command, namechange)
 *   5. IP/machine tool (clone search)
 *   6. Word filter (list, add, remove)
 *   7. Vouchers (list, create)
 *   8. Stats + audit log viewer
 *
 * Tutti gli SQL usano prepared statements (lezione dal legacy PHP che era
 * fuso a SQL-injection in ogni endpoint).
 */

const staff = new Hono();
staff.use('*', requireAuth);
staff.use('*', requireRank(5));

// =================================================================
// Helpers
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
            [actorId, `staff.${action}`, ip.slice(0, 45), ua.slice(0, 255), JSON.stringify(details)]
        );
    }
    catch { /* non-blocking */ }
}

function clientIp(c: { req: { raw: Request } }): string
{
    const xff = c.req.raw.headers.get('x-forwarded-for');
    return (xff?.split(',')[0]?.trim()) ?? c.req.raw.headers.get('x-real-ip') ?? 'unknown';
}

async function lookupUserId(username: string): Promise<number | null>
{
    const rows = await dbQuery<{ id: number }>('SELECT id FROM users WHERE username = ? LIMIT 1', [username]);
    return rows[0]?.id ?? null;
}

function safeParseJson(s: string): unknown
{
    try { return JSON.parse(s); }
    catch { return s; }
}

// Sanitize tipi audit: i numeri infiniti / NaN / oggetti ciclici non vanno in JSON.
function auditDetails(d: Record<string, unknown>): Record<string, unknown>
{
    return JSON.parse(JSON.stringify(d, (_k, v) => (typeof v === 'string' && v.length > 1000 ? v.slice(0, 1000) + '…' : v)));
}

// =================================================================
// 1) AZIONI RCON DIRETTE
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
    await logRconAudit(Number(actor.sub), 'rcon.alert', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, message: parsed.data.message, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/hotel-alert', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ message: z.string().min(1).max(2000) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const r = await rcon.hotelAlert(parsed.data.message);
    await logRconAudit(Number(actor.sub), 'rcon.hotel_alert', clientIp(c), c.req.header('user-agent') ?? '',
        { message: parsed.data.message, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

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
    await logRconAudit(Number(actor.sub), 'rcon.give_credits', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, amount: parsed.data.amount, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

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
    await logRconAudit(Number(actor.sub), 'rcon.give_duckets', clientIp(c), c.req.header('user-agent') ?? '',
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
    await logRconAudit(Number(actor.sub), 'rcon.give_diamonds', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, amount: parsed.data.amount, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

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
    await logRconAudit(Number(actor.sub), 'rcon.give_badge', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, badge: parsed.data.badge, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/disconnect', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ username: z.string().min(1).max(25) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);
    const userId = await lookupUserId(parsed.data.username);
    if(!userId) return c.json({ error: 'user_not_found' }, 404);
    const r = await rcon.disconnect(userId);
    await logRconAudit(Number(actor.sub), 'rcon.disconnect', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

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
    await logRconAudit(Number(actor.sub), 'rcon.mute', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, minutes: parsed.data.minutes, ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/refresh-catalog', async c =>
{
    const actor = c.var.user!;
    const r = await rcon.updateCatalog();
    await logRconAudit(Number(actor.sub), 'rcon.refresh_catalog', clientIp(c), c.req.header('user-agent') ?? '', { ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

staff.post('/refresh-wordfilter', async c =>
{
    const actor = c.var.user!;
    const r = await rcon.updateWordfilter();
    await logRconAudit(Number(actor.sub), 'rcon.refresh_wordfilter', clientIp(c), c.req.header('user-agent') ?? '', { ok: r.ok });
    return c.json({ ok: r.ok, error: r.errorMessage });
});

// =================================================================
// 2) USER MANAGEMENT (porting di hk/edituser.php)
// =================================================================

// GET /staff/user/:username — lookup completo per pannello edit user.
staff.get('/user/:username', async c =>
{
    const username = c.req.param('username');
    const rows = await dbQuery<{
        id: number; username: string; mail: string; motto: string;
        look: string; gender: string; rank: number; credits: number;
        vip_points: number; activity_points: number;
        account_created: number; last_login: number; last_online: number;
        online: string; ip_current: string | null; ip_register: string | null;
        machine_id: string | null; mail_verified: string;
    }>(
        `SELECT id, username, mail, motto, look, gender, rank, credits,
                vip_points, activity_points,
                account_created, last_login, last_online, online,
                ip_current, ip_register, machine_id, mail_verified
         FROM users WHERE username = ? LIMIT 1`,
        [username]
    );
    const u = rows[0];
    if(!u) return c.json({ error: 'not_found' }, 404);

    // Diamanti = vip_points (Arcturus convention).
    return c.json({
        user: {
            id: u.id, username: u.username, mail: u.mail,
            motto: u.motto, look: u.look, gender: u.gender,
            rank: u.rank, credits: u.credits, diamonds: u.vip_points,
            duckets: u.activity_points,
            accountCreated: u.account_created,
            lastLogin: u.last_login, lastOnline: u.last_online,
            online: u.online === '1',
            ipCurrent: u.ip_current, ipRegister: u.ip_register,
            machineId: u.machine_id, mailVerified: u.mail_verified === '1'
        }
    });
});

// PATCH /staff/user/:userId — update fields (motto, mail, rank, look).
// Niente possibilità di toccare la propria rank o impostarla > propria rank.
staff.patch('/user/:userId', async c =>
{
    const actor = c.var.user!;
    const userId = Number(c.req.param('userId'));
    if(!Number.isFinite(userId) || userId <= 0) return c.json({ error: 'bad_request' }, 400);

    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        motto: z.string().max(127).optional(),
        mail: z.string().email().max(500).optional(),
        rank: z.number().int().min(1).max(20).optional(),
        look: z.string().max(256).optional()
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request', details: parsed.error.errors }, 400);

    // Constraint sicurezza: non si modifica sé stessi tramite questo endpoint
    // (eviterebbe lock-out: uno staff abbassa la propria rank e perde accesso).
    if(userId === Number(actor.sub)) return c.json({ error: 'cannot_edit_self' }, 403);

    // Constraint: non si può promuovere un utente a rank >= della propria.
    const myRank = actor.rank ?? 0;
    if(parsed.data.rank !== undefined && parsed.data.rank >= myRank)
    {
        return c.json({ error: 'rank_too_high' }, 403);
    }

    // Costruisci UPDATE dinamico solo sulle colonne presenti.
    const sets: string[] = [];
    const vals: unknown[] = [];
    if(parsed.data.motto !== undefined) { sets.push('motto = ?'); vals.push(parsed.data.motto); }
    if(parsed.data.mail !== undefined) { sets.push('mail = ?'); vals.push(parsed.data.mail); }
    if(parsed.data.rank !== undefined) { sets.push('rank = ?'); vals.push(parsed.data.rank); }
    if(parsed.data.look !== undefined) { sets.push('look = ?'); vals.push(parsed.data.look); }
    if(!sets.length) return c.json({ error: 'no_changes' }, 400);

    vals.push(userId);
    await dbExecute(`UPDATE users SET ${sets.join(', ')} WHERE id = ?`, vals);

    // RCON reload motto se cambiato (push real-time se l'utente è online).
    if(parsed.data.motto !== undefined)
    {
        await rcon.setMotto(userId, parsed.data.motto);
    }

    await logRconAudit(Number(actor.sub), 'user.update', clientIp(c), c.req.header('user-agent') ?? '',
        auditDetails({ target: userId, changes: parsed.data }));
    return c.json({ ok: true });
});

// POST /staff/user/:userId/reset-password — reset password (porting changepass.php)
staff.post('/user/:userId/reset-password', async c =>
{
    const actor = c.var.user!;
    const userId = Number(c.req.param('userId'));
    if(!Number.isFinite(userId) || userId <= 0) return c.json({ error: 'bad_request' }, 400);
    if(userId === Number(actor.sub)) return c.json({ error: 'cannot_reset_self' }, 403);

    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ newPassword: z.string().min(8).max(255) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const hash = await hashPassword(parsed.data.newPassword);
    // VARCHAR(64) constraint (vedi memory): bcrypt fits in 60 chars → ok.
    if(hash.length > 64) return c.json({ error: 'hash_too_long_for_column' }, 500);

    await dbExecute('UPDATE users SET password = ? WHERE id = ?', [hash, userId]);
    await logRconAudit(Number(actor.sub), 'user.password_reset', clientIp(c), c.req.header('user-agent') ?? '',
        { target: userId });
    return c.json({ ok: true });
});

// POST /staff/user/:userId/strike — incrementa strikes (porting striketool.php)
staff.post('/user/:userId/strike', async c =>
{
    const actor = c.var.user!;
    const userId = Number(c.req.param('userId'));
    if(!Number.isFinite(userId) || userId <= 0) return c.json({ error: 'bad_request' }, 400);

    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ reason: z.string().max(255) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    // Le colonne `strikes` e `strikereason` sono custom del CMS legacy.
    // Le creiamo se non esistono via cms_v3_strikes per stare sul nostro spazio.
    await dbExecute(
        `INSERT INTO cms_v3_strikes (user_id, staff_id, reason, created_at)
         VALUES (?, ?, ?, UNIX_TIMESTAMP())
         ON DUPLICATE KEY UPDATE reason = VALUES(reason), created_at = VALUES(created_at)`,
        [userId, Number(actor.sub), parsed.data.reason]
    ).catch(async () =>
    {
        // Se la tabella non esiste ancora, fallback con CREATE.
        await dbExecute(
            `CREATE TABLE IF NOT EXISTS cms_v3_strikes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                staff_id INT NOT NULL,
                reason VARCHAR(255) NOT NULL,
                created_at INT NOT NULL,
                KEY user_id (user_id)
             )`
        );
        await dbExecute(
            `INSERT INTO cms_v3_strikes (user_id, staff_id, reason, created_at)
             VALUES (?, ?, ?, UNIX_TIMESTAMP())`,
            [userId, Number(actor.sub), parsed.data.reason]
        );
    });

    await logRconAudit(Number(actor.sub), 'user.strike', clientIp(c), c.req.header('user-agent') ?? '',
        { target: userId, reason: parsed.data.reason });
    return c.json({ ok: true });
});

// =================================================================
// 3) BANS (porting di bans.php + adminbans.php)
// =================================================================

// GET /staff/bans — lista ultime 100 con join username target + staff.
staff.get('/bans', async c =>
{
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);
    const search = c.req.query('q')?.trim() ?? '';

    let sql = `
        SELECT b.id, b.user_id, b.ip, b.machine_id, b.user_staff_id,
               b.timestamp, b.ban_expire, b.ban_reason, b.type,
               u.username AS target_username,
               s.username AS staff_username
        FROM bans b
        LEFT JOIN users u ON u.id = b.user_id
        LEFT JOIN users s ON s.id = b.user_staff_id
    `;
    const params: unknown[] = [];
    if(search)
    {
        sql += ' WHERE u.username LIKE ? OR b.ip LIKE ? OR b.machine_id LIKE ? OR b.ban_reason LIKE ?';
        const like = `%${search}%`;
        params.push(like, like, like, like);
    }
    sql += ' ORDER BY b.id DESC LIMIT ?';
    params.push(limit);

    const rows = await dbQuery<{
        id: number; user_id: number; ip: string; machine_id: string;
        user_staff_id: number; timestamp: number; ban_expire: number;
        ban_reason: string; type: string;
        target_username: string | null; staff_username: string | null;
    }>(sql, params);

    return c.json({
        bans: rows.map(b => ({
            id: b.id,
            userId: b.user_id,
            targetUsername: b.target_username,
            staffUsername: b.staff_username,
            ip: b.ip,
            machineId: b.machine_id,
            type: b.type,
            reason: b.ban_reason,
            timestamp: b.timestamp,
            expireAt: b.ban_expire
        }))
    });
});

// POST /staff/bans — aggiungi ban (account/ip/machine/super)
staff.post('/bans', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        username: z.string().min(1).max(25),
        reason: z.string().min(1).max(200),
        type: z.enum(['account', 'ip', 'machine', 'super']).default('account'),
        hours: z.number().int().min(1).max(24 * 365 * 10).default(24)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const rows = await dbQuery<{ id: number; ip_current: string; machine_id: string; rank: number }>(
        'SELECT id, ip_current, machine_id, rank FROM users WHERE username = ? LIMIT 1',
        [parsed.data.username]
    );
    const target = rows[0];
    if(!target) return c.json({ error: 'user_not_found' }, 404);

    // Constraint: non si banna nessuno con rank >= della propria.
    if(target.rank >= (actor.rank ?? 0))
    {
        return c.json({ error: 'target_rank_protected' }, 403);
    }

    const now = Math.floor(Date.now() / 1000);
    const expireAt = now + parsed.data.hours * 3600;

    await dbExecute(
        `INSERT INTO bans (user_id, ip, machine_id, user_staff_id, timestamp, ban_expire, ban_reason, type)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
        [
            target.id,
            parsed.data.type === 'account' ? '' : (target.ip_current ?? ''),
            parsed.data.type === 'account' ? '' : (target.machine_id ?? ''),
            Number(actor.sub),
            now,
            expireAt,
            parsed.data.reason,
            parsed.data.type
        ]
    );

    // RCON: disconnect immediato + reload ban list.
    await rcon.disconnect(target.id);

    await logRconAudit(Number(actor.sub), 'ban.add', clientIp(c), c.req.header('user-agent') ?? '',
        { target: parsed.data.username, type: parsed.data.type, hours: parsed.data.hours, reason: parsed.data.reason });
    return c.json({ ok: true });
});

// DELETE /staff/bans/:id — rimuovi ban
staff.delete('/bans/:id', async c =>
{
    const actor = c.var.user!;
    const id = Number(c.req.param('id'));
    if(!Number.isFinite(id) || id <= 0) return c.json({ error: 'bad_request' }, 400);
    await dbExecute('DELETE FROM bans WHERE id = ? LIMIT 1', [id]);
    await logRconAudit(Number(actor.sub), 'ban.remove', clientIp(c), c.req.header('user-agent') ?? '',
        { banId: id });
    return c.json({ ok: true });
});

// =================================================================
// 4) LOGS (chatlogs_room, chatlogs_private, commandlogs, namechange_log)
// =================================================================
staff.get('/logs/chat-room', async c =>
{
    const username = c.req.query('user')?.trim();
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);

    let sql = `
        SELECT cr.room_id, cr.user_from_id, cr.user_to_id, cr.message, cr.timestamp,
               uf.username AS from_username,
               r.name AS room_name
        FROM chatlogs_room cr
        LEFT JOIN users uf ON uf.id = cr.user_from_id
        LEFT JOIN rooms r ON r.id = cr.room_id
    `;
    const params: unknown[] = [];
    if(username)
    {
        sql += ' WHERE uf.username = ?';
        params.push(username);
    }
    sql += ' ORDER BY cr.timestamp DESC LIMIT ?';
    params.push(limit);

    const rows = await dbQuery<{
        room_id: number; user_from_id: number; user_to_id: number;
        message: string; timestamp: number;
        from_username: string | null; room_name: string | null;
    }>(sql, params);

    return c.json({
        entries: rows.map(r => ({
            roomId: r.room_id, roomName: r.room_name,
            fromUserId: r.user_from_id, fromUsername: r.from_username,
            toUserId: r.user_to_id,
            message: r.message, timestamp: r.timestamp
        }))
    });
});

// Replay incidenti (#22): chat di UNA stanza in una finestra temporale attorno
// a un momento (per rivedere il contesto di un alert/segnalazione). Read-only.
staff.get('/logs/room-replay', async c =>
{
    const roomParam = (c.req.query('room') ?? '').trim();
    if(!roomParam) return c.json({ error: 'room_required' }, 400);
    const windowMin = Math.min(120, Math.max(1, Number(c.req.query('window') ?? 15)));
    const atRaw = Number(c.req.query('at'));
    const at = Number.isFinite(atRaw) && atRaw > 0 ? Math.trunc(atRaw) : Math.floor(Date.now() / 1000);

    let roomId: number | null = null;
    let roomName: string | null = null;
    if(/^\d+$/.test(roomParam))
    {
        roomId = Number(roomParam);
        roomName = (await dbQuery<{ name: string }>('SELECT name FROM rooms WHERE id = ? LIMIT 1', [roomId]))[0]?.name ?? null;
    }
    else
    {
        const r = (await dbQuery<{ id: number; name: string }>('SELECT id, name FROM rooms WHERE name = ? LIMIT 1', [roomParam]))[0];
        if(r) { roomId = r.id; roomName = r.name; }
    }
    if(roomId === null) return c.json({ error: 'room_not_found' }, 404);

    const from = at - windowMin * 60;
    const to = at + windowMin * 60;
    const rows = await dbQuery<{ user_from_id: number; from_username: string | null; message: string; timestamp: number }>(
        `SELECT cr.user_from_id, uf.username AS from_username, cr.message, cr.timestamp
           FROM chatlogs_room cr LEFT JOIN users uf ON uf.id = cr.user_from_id
          WHERE cr.room_id = ? AND cr.timestamp BETWEEN ? AND ?
          ORDER BY cr.timestamp ASC LIMIT 500`,
        [roomId, from, to]
    );
    return c.json({
        roomId, roomName, at, windowMin,
        entries: rows.map(r => ({ userId: r.user_from_id, username: r.from_username, message: r.message, ts: r.timestamp }))
    });
});

staff.get('/logs/chat-pm', async c =>
{
    const username = c.req.query('user')?.trim();
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);

    let sql = `
        SELECT cp.id, cp.user_from_id, cp.user_to_id, cp.message, cp.timestamp,
               uf.username AS from_username,
               ut.username AS to_username
        FROM chatlogs_private cp
        LEFT JOIN users uf ON uf.id = cp.user_from_id
        LEFT JOIN users ut ON ut.id = cp.user_to_id
    `;
    const params: unknown[] = [];
    if(username)
    {
        sql += ' WHERE uf.username = ? OR ut.username = ?';
        params.push(username, username);
    }
    sql += ' ORDER BY cp.id DESC LIMIT ?';
    params.push(limit);

    const rows = await dbQuery<{
        id: number; user_from_id: number; user_to_id: number;
        message: string; timestamp: number;
        from_username: string | null; to_username: string | null;
    }>(sql, params);

    return c.json({
        entries: rows.map(r => ({
            id: r.id,
            fromUserId: r.user_from_id, fromUsername: r.from_username,
            toUserId: r.user_to_id, toUsername: r.to_username,
            message: r.message, timestamp: r.timestamp
        }))
    });
});

staff.get('/logs/commands', async c =>
{
    const username = c.req.query('user')?.trim();
    const command = c.req.query('cmd')?.trim();
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);

    let sql = `
        SELECT cl.user_id, cl.timestamp, cl.command, cl.params, cl.succes,
               u.username
        FROM commandlogs cl
        LEFT JOIN users u ON u.id = cl.user_id
        WHERE 1=1
    `;
    const params: unknown[] = [];
    if(username) { sql += ' AND u.username = ?'; params.push(username); }
    if(command) { sql += ' AND cl.command LIKE ?'; params.push(`%${command}%`); }
    sql += ' ORDER BY cl.timestamp DESC LIMIT ?';
    params.push(limit);

    const rows = await dbQuery<{
        user_id: number; timestamp: number; command: string;
        params: string; succes: string; username: string | null;
    }>(sql, params);

    return c.json({
        entries: rows.map(r => ({
            userId: r.user_id, username: r.username,
            timestamp: r.timestamp,
            command: r.command, params: r.params,
            success: r.succes === 'yes'
        }))
    });
});

staff.get('/logs/namechange', async c =>
{
    const username = c.req.query('user')?.trim();
    const limit = Math.min(Number(c.req.query('limit') ?? 100), 500);

    let sql = `
        SELECT nc.user_id, nc.old_name, nc.new_name, nc.timestamp,
               u.username AS current_username
        FROM namechange_log nc
        LEFT JOIN users u ON u.id = nc.user_id
        WHERE 1=1
    `;
    const params: unknown[] = [];
    if(username) { sql += ' AND (nc.old_name = ? OR nc.new_name = ?)'; params.push(username, username); }
    sql += ' ORDER BY nc.timestamp DESC LIMIT ?';
    params.push(limit);

    const rows = await dbQuery<{
        user_id: number; old_name: string; new_name: string;
        timestamp: number; current_username: string | null;
    }>(sql, params);

    return c.json({
        entries: rows.map(r => ({
            userId: r.user_id, currentUsername: r.current_username,
            oldName: r.old_name, newName: r.new_name,
            timestamp: r.timestamp
        }))
    });
});

// =================================================================
// 5) IP/MACHINE TOOL (porting iptool.php)
// =================================================================
staff.get('/ip-lookup', async c =>
{
    const ip = c.req.query('ip')?.trim();
    const machine = c.req.query('machine')?.trim();
    const username = c.req.query('user')?.trim();

    if(!ip && !machine && !username) return c.json({ error: 'missing_query' }, 400);

    let sql = `
        SELECT id, username, mail, rank, online,
               ip_current, ip_register, machine_id,
               last_login, last_online, account_created
        FROM users
        WHERE
    `;
    const params: unknown[] = [];
    const conds: string[] = [];
    if(ip) { conds.push('(ip_current = ? OR ip_register = ?)'); params.push(ip, ip); }
    if(machine) { conds.push('machine_id = ?'); params.push(machine); }
    if(username) { conds.push('username = ?'); params.push(username); }

    sql += conds.join(' OR ') + ' ORDER BY last_login DESC LIMIT 50';

    const rows = await dbQuery<{
        id: number; username: string; mail: string; rank: number;
        online: string; ip_current: string | null; ip_register: string | null;
        machine_id: string | null; last_login: number; last_online: number;
        account_created: number;
    }>(sql, params);

    return c.json({
        matches: rows.map(r => ({
            id: r.id, username: r.username, mail: r.mail, rank: r.rank,
            online: r.online === '1',
            ipCurrent: r.ip_current, ipRegister: r.ip_register,
            machineId: r.machine_id,
            lastLogin: r.last_login, lastOnline: r.last_online,
            accountCreated: r.account_created
        }))
    });
});

// =================================================================
// 6) WORD FILTER (porting filter.php)
// =================================================================
staff.get('/wordfilter', async c =>
{
    const rows = await dbQuery<{
        key: string; replacement: string; hide: string; report: string; mute: number;
    }>('SELECT `key`, replacement, hide, report, mute FROM wordfilter ORDER BY `key` ASC LIMIT 500');
    return c.json({
        entries: rows.map(r => ({
            word: r.key,
            replacement: r.replacement,
            hide: r.hide === '1',
            report: r.report === '1',
            mute: r.mute
        }))
    });
});

staff.post('/wordfilter', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        word: z.string().min(1).max(255),
        replacement: z.string().max(16).default('bobba'),
        hide: z.boolean().default(false),
        report: z.boolean().default(false),
        mute: z.number().int().min(0).max(24 * 60).default(0)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    await dbExecute(
        `INSERT INTO wordfilter (\`key\`, replacement, hide, report, mute)
         VALUES (?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE replacement = VALUES(replacement),
                                 hide = VALUES(hide),
                                 report = VALUES(report),
                                 mute = VALUES(mute)`,
        [
            parsed.data.word, parsed.data.replacement,
            parsed.data.hide ? '1' : '0',
            parsed.data.report ? '1' : '0',
            parsed.data.mute
        ]
    );

    // RCON reload immediato.
    await rcon.updateWordfilter();

    await logRconAudit(Number(actor.sub), 'wordfilter.upsert', clientIp(c), c.req.header('user-agent') ?? '',
        { word: parsed.data.word });
    return c.json({ ok: true });
});

staff.delete('/wordfilter/:word', async c =>
{
    const actor = c.var.user!;
    const word = decodeURIComponent(c.req.param('word'));
    await dbExecute('DELETE FROM wordfilter WHERE `key` = ? LIMIT 1', [word]);
    await rcon.updateWordfilter();
    await logRconAudit(Number(actor.sub), 'wordfilter.delete', clientIp(c), c.req.header('user-agent') ?? '',
        { word });
    return c.json({ ok: true });
});

// =================================================================
// 7) VOUCHERS (porting vouchers.php)
// =================================================================
staff.get('/vouchers', async c =>
{
    const rows = await dbQuery<{
        id: number; code: string; credits: number; points: number;
        points_type: number; catalog_item_id: number; amount: number; limit: number;
    }>('SELECT id, code, credits, points, points_type, catalog_item_id, amount, `limit` FROM vouchers ORDER BY id DESC LIMIT 200');
    return c.json({
        vouchers: rows.map(v => ({
            id: v.id, code: v.code,
            credits: v.credits, points: v.points,
            pointsType: v.points_type,
            catalogItemId: v.catalog_item_id,
            amount: v.amount, limit: v.limit
        }))
    });
});

staff.post('/vouchers', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        code: z.string().min(3).max(10).regex(/^[A-Za-z0-9_-]+$/),
        credits: z.number().int().min(0).max(10_000_000).default(0),
        points: z.number().int().min(0).max(10_000_000).default(0),
        pointsType: z.number().int().min(0).max(9).default(0),
        catalogItemId: z.number().int().min(0).default(0),
        amount: z.number().int().min(1).max(1000).default(1),
        limit: z.number().int().min(-1).max(100_000).default(-1)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    await dbExecute(
        `INSERT INTO vouchers (code, credits, points, points_type, catalog_item_id, amount, \`limit\`)
         VALUES (?, ?, ?, ?, ?, ?, ?)`,
        [
            parsed.data.code, parsed.data.credits, parsed.data.points,
            parsed.data.pointsType, parsed.data.catalogItemId,
            parsed.data.amount, parsed.data.limit
        ]
    );
    await logRconAudit(Number(actor.sub), 'voucher.create', clientIp(c), c.req.header('user-agent') ?? '',
        auditDetails({ code: parsed.data.code, credits: parsed.data.credits, points: parsed.data.points }));
    return c.json({ ok: true });
});

staff.delete('/vouchers/:id', async c =>
{
    const actor = c.var.user!;
    const id = Number(c.req.param('id'));
    if(!Number.isFinite(id) || id <= 0) return c.json({ error: 'bad_request' }, 400);
    await dbExecute('DELETE FROM vouchers WHERE id = ? LIMIT 1', [id]);
    await logRconAudit(Number(actor.sub), 'voucher.delete', clientIp(c), c.req.header('user-agent') ?? '',
        { voucherId: id });
    return c.json({ ok: true });
});

// =================================================================
// 8) STATS + AUDIT LOG VIEWER
// =================================================================
staff.get('/stats', async c =>
{
    const rows = await dbQuery<{
        total_users: number; online_users: number; total_rooms: number;
        total_photos: number; staff_count: number;
        active_bans: number; total_chatlogs: number;
    }>(
        `SELECT
            (SELECT COUNT(*) FROM users) AS total_users,
            (SELECT COUNT(*) FROM users WHERE online = '1') AS online_users,
            (SELECT COUNT(*) FROM rooms WHERE state <> 'invisible') AS total_rooms,
            (SELECT COUNT(*) FROM camera_web) AS total_photos,
            (SELECT COUNT(*) FROM users WHERE rank >= 5) AS staff_count,
            (SELECT COUNT(*) FROM bans WHERE ban_expire > UNIX_TIMESTAMP()) AS active_bans,
            (SELECT COUNT(*) FROM chatlogs_room) AS total_chatlogs`
    );

    return c.json({
        totalUsers: rows[0]?.total_users ?? 0,
        onlineUsers: rows[0]?.online_users ?? 0,
        totalRooms: rows[0]?.total_rooms ?? 0,
        totalPhotos: rows[0]?.total_photos ?? 0,
        staffCount: rows[0]?.staff_count ?? 0,
        activeBans: rows[0]?.active_bans ?? 0,
        totalChatlogs: rows[0]?.total_chatlogs ?? 0
    });
});

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

// =================================================================
// 9) NEWS MANAGEMENT (porting news.php + editnews.php)
// =================================================================
// La tabella cms_v3_news viene autogestita dal service `news.ts`
// (CREATE TABLE IF NOT EXISTS + seed) — qui assumiamo che il primo
// accesso pubblico l'abbia già inizializzata.

const slugPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
function slugify(s: string): string
{
    return s.toLowerCase()
        .normalize('NFD').replace(/[̀-ͯ]/g, '')  // strip accenti
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .slice(0, 80);
}

staff.get('/news', async c =>
{
    const items = await fetchAllNewsAdmin();
    return c.json({ news: items });
});

staff.get('/news/:slug', async c =>
{
    const slug = c.req.param('slug');
    const item = await fetchNewsBySlug(slug, true);
    if(!item) return c.json({ error: 'not_found' }, 404);
    return c.json({ article: item });
});

staff.post('/news', async c =>
{
    const actor = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        slug: z.string().max(80).optional(),                 // se omesso → auto-slugify(title)
        title: z.string().min(3).max(200),
        category: z.string().max(40).default('aggiornamenti-su-habbo'),
        categoryLabel: z.string().max(60).default('Aggiornamenti su Habbo'),
        summary: z.string().min(10).max(500),
        bodyHtml: z.string().min(10),
        image: z.string().max(255).default(''),
        published: z.boolean().default(true)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request', details: parsed.error.errors }, 400);

    const slug = parsed.data.slug && slugPattern.test(parsed.data.slug)
        ? parsed.data.slug
        : slugify(parsed.data.title);
    if(!slug) return c.json({ error: 'invalid_slug' }, 400);

    const now = Math.floor(Date.now() / 1000);
    try
    {
        const r = await dbExecute(
            `INSERT INTO cms_v3_news
                (slug, title, category, category_label, summary, body_html, image,
                 author_id, published, created_at, updated_at)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                slug, parsed.data.title, parsed.data.category, parsed.data.categoryLabel,
                parsed.data.summary, parsed.data.bodyHtml, parsed.data.image,
                Number(actor.sub), parsed.data.published ? 1 : 0, now, now
            ]
        );
        await logRconAudit(Number(actor.sub), 'news.create', clientIp(c), c.req.header('user-agent') ?? '',
            { newsId: r.insertId, slug, title: parsed.data.title });
        return c.json({ ok: true, id: r.insertId, slug });
    }
    catch(e)
    {
        const msg = (e as Error).message;
        if(msg.includes('Duplicate')) return c.json({ error: 'slug_already_exists' }, 409);
        throw e;
    }
});

staff.patch('/news/:id', async c =>
{
    const actor = c.var.user!;
    const id = Number(c.req.param('id'));
    if(!Number.isFinite(id) || id <= 0) return c.json({ error: 'bad_request' }, 400);

    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        slug: z.string().max(80).optional(),
        title: z.string().min(3).max(200).optional(),
        category: z.string().max(40).optional(),
        categoryLabel: z.string().max(60).optional(),
        summary: z.string().min(10).max(500).optional(),
        bodyHtml: z.string().min(10).optional(),
        image: z.string().max(255).optional(),
        published: z.boolean().optional()
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request', details: parsed.error.errors }, 400);

    const sets: string[] = [];
    const vals: unknown[] = [];
    if(parsed.data.slug !== undefined)
    {
        if(!slugPattern.test(parsed.data.slug)) return c.json({ error: 'invalid_slug' }, 400);
        sets.push('slug = ?'); vals.push(parsed.data.slug);
    }
    if(parsed.data.title !== undefined) { sets.push('title = ?'); vals.push(parsed.data.title); }
    if(parsed.data.category !== undefined) { sets.push('category = ?'); vals.push(parsed.data.category); }
    if(parsed.data.categoryLabel !== undefined) { sets.push('category_label = ?'); vals.push(parsed.data.categoryLabel); }
    if(parsed.data.summary !== undefined) { sets.push('summary = ?'); vals.push(parsed.data.summary); }
    if(parsed.data.bodyHtml !== undefined) { sets.push('body_html = ?'); vals.push(parsed.data.bodyHtml); }
    if(parsed.data.image !== undefined) { sets.push('image = ?'); vals.push(parsed.data.image); }
    if(parsed.data.published !== undefined) { sets.push('published = ?'); vals.push(parsed.data.published ? 1 : 0); }
    if(!sets.length) return c.json({ error: 'no_changes' }, 400);

    sets.push('updated_at = ?');
    vals.push(Math.floor(Date.now() / 1000));
    vals.push(id);

    try
    {
        await dbExecute(`UPDATE cms_v3_news SET ${sets.join(', ')} WHERE id = ?`, vals);
    }
    catch(e)
    {
        const msg = (e as Error).message;
        if(msg.includes('Duplicate')) return c.json({ error: 'slug_already_exists' }, 409);
        throw e;
    }

    await logRconAudit(Number(actor.sub), 'news.update', clientIp(c), c.req.header('user-agent') ?? '',
        auditDetails({ newsId: id, changes: parsed.data }));
    return c.json({ ok: true });
});

staff.delete('/news/:id', async c =>
{
    const actor = c.var.user!;
    const id = Number(c.req.param('id'));
    if(!Number.isFinite(id) || id <= 0) return c.json({ error: 'bad_request' }, 400);
    await dbExecute('DELETE FROM cms_v3_news WHERE id = ? LIMIT 1', [id]);
    await logRconAudit(Number(actor.sub), 'news.delete', clientIp(c), c.req.header('user-agent') ?? '',
        { newsId: id });
    return c.json({ ok: true });
});

// =================================================================
// 10) STAFF LIST (porting staffpage.php)
// =================================================================
staff.get('/staff-list', async c =>
{
    const rows = await dbQuery<{
        id: number; username: string; motto: string; look: string;
        rank: number; online: string; last_online: number;
    }>(
        `SELECT id, username, motto, look, rank, online, last_online
         FROM users
         WHERE rank >= 5
         ORDER BY rank DESC, username ASC
         LIMIT 200`
    );
    return c.json({
        staff: rows.map(s => ({
            id: s.id, username: s.username, motto: s.motto, look: s.look,
            rank: s.rank, online: s.online === '1', lastOnline: s.last_online
        }))
    });
});

// =================================================================
// 9. SHOP ANALYTICS + ORDINI (Fase 20)
// =================================================================
staff.get('/shop/analytics', async (c) =>
{
    const totalsRows = await dbQuery<{ orders: number; paid: number; revenue: number }>(
        "SELECT COUNT(*) AS orders, COALESCE(SUM(status = 'paid'), 0) AS paid, " +
        "COALESCE(SUM(CASE WHEN status = 'paid' THEN amount_cents ELSE 0 END), 0) AS revenue " +
        'FROM cms_v3_shop_orders'
    );
    const totalsRow = totalsRows[0];
    const byStatus = await dbQuery<{ status: string; count: number }>(
        'SELECT status, COUNT(*) AS count FROM cms_v3_shop_orders GROUP BY status'
    );
    const series = await dbQuery<{ day: string; revenue: number; orders: number }>(
        "SELECT DATE_FORMAT(paid_at, '%Y-%m-%d') AS day, COALESCE(SUM(amount_cents), 0) AS revenue, COUNT(*) AS orders " +
        "FROM cms_v3_shop_orders WHERE status = 'paid' AND paid_at IS NOT NULL AND paid_at >= (NOW() - INTERVAL 30 DAY) " +
        'GROUP BY day ORDER BY day ASC'
    );
    const topItems = await dbQuery<{ name: string; count: number; revenue: number }>(
        "SELECT COALESCE(i.name, CONCAT('#', o.item_id)) AS name, COUNT(*) AS count, COALESCE(SUM(o.amount_cents), 0) AS revenue " +
        'FROM cms_v3_shop_orders o LEFT JOIN cms_v3_shop_items i ON i.id = o.item_id ' +
        "WHERE o.status = 'paid' GROUP BY o.item_id, i.name ORDER BY revenue DESC LIMIT 8"
    );
    const orders = Number(totalsRow?.orders ?? 0);
    const paid = Number(totalsRow?.paid ?? 0);
    return c.json({
        totals: {
            orders,
            paid,
            revenueCents: Number(totalsRow?.revenue ?? 0),
            conversionPct: orders > 0 ? Math.round((paid / orders) * 1000) / 10 : 0
        },
        byStatus: byStatus.map(r => ({ status: r.status, count: Number(r.count) })),
        series: series.map(r => ({ day: r.day, revenueCents: Number(r.revenue), orders: Number(r.orders) })),
        topItems: topItems.map(r => ({ name: r.name, count: Number(r.count), revenueCents: Number(r.revenue) }))
    });
});

staff.get('/shop/orders', async (c) =>
{
    const status = c.req.query('status');
    const valid = [ 'pending', 'paid', 'failed', 'refunded' ];
    const useFilter = !!status && valid.includes(status);
    const rows = await dbQuery<{ id: number; user_id: number; username: string | null; item_name: string | null; status: string; amount_cents: number; currency: string; created_at: string; paid_at: string | null; delivery_log: string | null }>(
        'SELECT o.id, o.user_id, u.username, i.name AS item_name, o.status, o.amount_cents, o.currency, o.created_at, o.paid_at, o.delivery_log ' +
        'FROM cms_v3_shop_orders o LEFT JOIN cms_v3_shop_items i ON i.id = o.item_id LEFT JOIN users u ON u.id = o.user_id ' +
        (useFilter ? 'WHERE o.status = ? ' : '') +
        'ORDER BY o.created_at DESC LIMIT 100',
        useFilter ? [ status as string ] : []
    );
    return c.json({
        orders: rows.map(r => ({
            id: r.id, userId: r.user_id, username: r.username, item: r.item_name,
            status: r.status, amountCents: Number(r.amount_cents), currency: r.currency,
            createdAt: r.created_at, paidAt: r.paid_at, deliveryLog: r.delivery_log
        }))
    });
});

// =================================================================
// 10. CODA MODERAZIONE — CFH / support_tickets (read-only, Fase 20)
// =================================================================
// support_tickets.state Arcturus: 0 = aperto, 1 = preso in carico. Mostriamo
// questi due per il triage. Le AZIONI (mute/ban/kick) restano negli strumenti
// esistenti (Azioni rapide / Ban) o in-game, per non desincronizzare lo stato
// dei ticket gestito live dall'EMU.
staff.get('/moderation/queue', async (c) =>
{
    const tickets = await dbQuery<{
        id: number; state: number; type: number; timestamp: number; score: number; issue: string;
        sender_id: number; sender_name: string | null; reported_id: number; reported_name: string | null;
        mod_id: number; mod_name: string | null; room_id: number; room_name: string | null;
    }>(
        'SELECT t.id, t.state, t.type, t.timestamp, t.score, t.issue, ' +
        't.sender_id, su.username AS sender_name, t.reported_id, ru.username AS reported_name, ' +
        't.mod_id, mu.username AS mod_name, t.room_id, r.name AS room_name ' +
        'FROM support_tickets t ' +
        'LEFT JOIN users su ON su.id = t.sender_id ' +
        'LEFT JOIN users ru ON ru.id = t.reported_id ' +
        'LEFT JOIN users mu ON mu.id = t.mod_id ' +
        'LEFT JOIN rooms r ON r.id = t.room_id ' +
        'WHERE t.state IN (0, 1) ORDER BY t.timestamp DESC LIMIT 100'
    );
    const counts = await dbQuery<{ state: number; count: number }>(
        'SELECT state, COUNT(*) AS count FROM support_tickets GROUP BY state'
    );
    return c.json({
        tickets: tickets.map(t => ({
            id: t.id, state: t.state, type: t.type, timestamp: t.timestamp, score: t.score,
            issue: t.issue, sender: t.sender_name, senderId: t.sender_id,
            reported: t.reported_name, reportedId: t.reported_id, mod: t.mod_name, room: t.room_name
        })),
        counts: counts.map(r => ({ state: r.state, count: Number(r.count) }))
    });
});

// =================================================================
// 11. FEED ALERT STAFF — eventi visibility='staff' (raid/macro/...)
// =================================================================
// Eventi prodotti dall'EMU/CMS con visibility='staff' (es. raid.detected,
// macro.suspected) che NON compaiono nel feed pubblico. Sola lettura: la UI
// del pannello li mostra in "Alert staff". I relativi flag (raid_detection,
// anti_macro, ...) si accendono dalla sezione "Feature flag".
staff.get('/activity', async (c) =>
{
    const limitRaw = Number(c.req.query('limit') ?? 60);
    const limit = Math.min(200, Math.max(1, Number.isFinite(limitRaw) ? Math.trunc(limitRaw) : 60));
    const events = await dbQuery<{
        id: number; actor_id: number | null; actor_name: string; type: string;
        payload: unknown; created_at: number;
    }>(
        'SELECT id, actor_id, actor_name, type, payload, UNIX_TIMESTAMP(created_at) AS created_at ' +
        "FROM cms_v3_activity_events WHERE visibility = 'staff' " +
        `ORDER BY id DESC LIMIT ${limit}`
    );
    return c.json({
        events: events.map(e => ({
            id: e.id, actorId: e.actor_id, actor: e.actor_name, type: e.type,
            payload: e.payload, ts: e.created_at
        }))
    });
});

// =================================================================
// 12. TELEMETRIA ECONOMIA — grant CMS (faucet) da cms_v3_economy_log
// =================================================================
// Mostra quanto immettono i rubinetti CMS (streak/referral/missioni/season pass).
// I flussi interni all'EMU (scambi, shop in-game) non passano da qui.
staff.get('/economy', async (c) =>
{
    const totals = (await dbQuery<{ total30: number; grants30: number }>(
        'SELECT COALESCE(SUM(amount),0) AS total30, COUNT(*) AS grants30 FROM cms_v3_economy_log WHERE created_at >= (NOW() - INTERVAL 30 DAY)'
    ))[0];
    const bySource = await dbQuery<{ source: string; n: number; total: number }>(
        'SELECT source, COUNT(*) AS n, COALESCE(SUM(amount),0) AS total FROM cms_v3_economy_log WHERE created_at >= (NOW() - INTERVAL 30 DAY) GROUP BY source ORDER BY total DESC'
    );
    const byDay = await dbQuery<{ day: string; total: number }>(
        'SELECT DATE(created_at) AS day, COALESCE(SUM(amount),0) AS total FROM cms_v3_economy_log WHERE created_at >= (NOW() - INTERVAL 14 DAY) GROUP BY DATE(created_at) ORDER BY day'
    );
    const recent = await dbQuery<{ ts: number; user_id: number; username: string | null; amount: number; source: string }>(
        'SELECT UNIX_TIMESTAMP(e.created_at) AS ts, e.user_id, u.username, e.amount, e.source FROM cms_v3_economy_log e LEFT JOIN users u ON u.id = e.user_id ORDER BY e.id DESC LIMIT 40'
    );
    return c.json({
        totals: { total30: Number(totals?.total30 ?? 0), grants30: Number(totals?.grants30 ?? 0) },
        bySource: bySource.map(r => ({ source: r.source, count: Number(r.n), total: Number(r.total) })),
        byDay: byDay.map(r => ({ day: r.day, total: Number(r.total) })),
        recent: recent.map(r => ({ ts: r.ts, userId: r.user_id, username: r.username, amount: r.amount, source: r.source }))
    });
});

export default staff;
