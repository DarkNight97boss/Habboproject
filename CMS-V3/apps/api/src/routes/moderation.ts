import { Hono } from 'hono';
import type { Context } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';

/**
 * Moderazione (/api/v2/moderation) — strumenti staff.
 *
 * Shadow-mute (#19): la "mute ombra" e' una riga in `cms_v3_shadow_mutes`. L'EMU
 * (core/ShadowMute) legge questo set con un poller e, SOLO se il flag CMS
 * `shadow_mute` e' ON, nasconde i messaggi TALK/SHOUT dell'utente a tutti tranne
 * lui e lo staff. Qui ci sono solo gli endpoint di gestione (rank >= 5):
 *
 *   GET    /shadow-mutes           → elenco delle mute attive (con username)
 *   PUT    /shadow-mutes/:userId   → applica/aggiorna (body {minutes?, reason?})
 *   DELETE /shadow-mutes/:userId   → rimuove
 *
 * Gestire la lista e' sempre permesso allo staff; l'effetto in gioco dipende dal
 * flag `shadow_mute` (gestito da /api/v2/flags/admin/shadow_mute).
 */

const reqIp = (c: Context): string =>
    c.req.header('cf-connecting-ip')
    ?? c.req.header('x-real-ip')
    ?? c.req.header('x-forwarded-for')?.split(',')[0]?.trim()
    ?? '';

async function audit(userId: number, action: string, ip: string, ua: string, details: Record<string, unknown>): Promise<void>
{
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_audit_log (user_id, action, ip, user_agent, details, created_at) VALUES (?, ?, ?, ?, ?, UNIX_TIMESTAMP())',
            [userId, action, ip.slice(0, 45), ua.slice(0, 255), JSON.stringify(details)]
        );
    }
    catch { /* audit best-effort: non bloccare l'azione di moderazione */ }
}

const moderation = new Hono();

// Tutte le rotte sono staff-only (rank >= 5), come la gestione dei feature flag.
moderation.use('*', requireAuth);
moderation.use('*', requireRank(5));

interface ShadowMuteRow
{
    user_id: number;
    username: string | null;
    until_ts: number;
    reason: string;
    created_by: number;
    created_at: number;
}

moderation.get('/shadow-mutes', async c =>
{
    const rows = await dbQuery<ShadowMuteRow>(
        `SELECT s.user_id, u.username, s.until_ts, s.reason, s.created_by, s.created_at
           FROM cms_v3_shadow_mutes s
           LEFT JOIN users u ON u.id = s.user_id
          WHERE s.until_ts = 0 OR s.until_ts > UNIX_TIMESTAMP()
          ORDER BY s.created_at DESC`
    );
    return c.json({ mutes: rows });
});

const putSchema = z.object({
    minutes: z.number().int().min(0).max(43200).optional(), // 0 = permanente, max 30 giorni
    reason: z.string().max(255).optional()
});

moderation.put('/shadow-mutes/:userId', async c =>
{
    const userId = Number(c.req.param('userId'));
    if(!Number.isInteger(userId) || userId <= 0) return c.json({ error: 'invalid_user' }, 400);

    const parsed = putSchema.safeParse(await c.req.json().catch(() => ({})));
    if(!parsed.success) return c.json({ error: 'invalid_body', issues: parsed.error.issues }, 400);

    const actor = c.var.user;
    if(!actor) return c.json({ error: 'unauthorized' }, 401);
    const actorId = Number(actor.sub);

    // L'utente deve esistere e non avere rango pari/superiore al moderatore.
    const target = (await dbQuery<{ id: number; username: string; rank: number }>(
        'SELECT id, username, `rank` FROM users WHERE id = ? LIMIT 1', [userId]
    ))[0];
    if(!target) return c.json({ error: 'user_not_found' }, 404);
    if(target.id === actorId) return c.json({ error: 'cannot_target_self' }, 400);
    if(Number(target.rank) >= Number(actor.rank)) return c.json({ error: 'target_rank_too_high' }, 403);

    const minutes = parsed.data.minutes ?? 0;
    const reason = parsed.data.reason ?? '';

    if(minutes > 0)
    {
        await dbExecute(
            `INSERT INTO cms_v3_shadow_mutes (user_id, until_ts, reason, created_by, created_at)
               VALUES (?, UNIX_TIMESTAMP() + ?, ?, ?, UNIX_TIMESTAMP())
             ON DUPLICATE KEY UPDATE until_ts=VALUES(until_ts), reason=VALUES(reason), created_by=VALUES(created_by), created_at=VALUES(created_at)`,
            [userId, minutes * 60, reason, actorId]
        );
    }
    else
    {
        await dbExecute(
            `INSERT INTO cms_v3_shadow_mutes (user_id, until_ts, reason, created_by, created_at)
               VALUES (?, 0, ?, ?, UNIX_TIMESTAMP())
             ON DUPLICATE KEY UPDATE until_ts=VALUES(until_ts), reason=VALUES(reason), created_by=VALUES(created_by), created_at=VALUES(created_at)`,
            [userId, reason, actorId]
        );
    }

    await audit(actorId, 'moderation.shadowmute.set', reqIp(c), c.req.header('user-agent') ?? '', { userId, username: target.username, minutes, reason });
    return c.json({ ok: true, userId, username: target.username, minutes, permanent: minutes === 0 });
});

moderation.delete('/shadow-mutes/:userId', async c =>
{
    const userId = Number(c.req.param('userId'));
    if(!Number.isInteger(userId) || userId <= 0) return c.json({ error: 'invalid_user' }, 400);

    const actor = c.var.user;
    if(!actor) return c.json({ error: 'unauthorized' }, 401);
    const actorId = Number(actor.sub);

    const res = await dbExecute('DELETE FROM cms_v3_shadow_mutes WHERE user_id = ?', [userId]);
    await audit(actorId, 'moderation.shadowmute.clear', reqIp(c), c.req.header('user-agent') ?? '', { userId });
    return c.json({ ok: true, deleted: res.affectedRows });
});

// Variante per-username (usata dal pannello staff): risolve username -> id e
// applica la stessa upsert della PUT. Comodo perche' lo staff ragiona per nome.
const postSchema = z.object({
    username: z.string().min(1).max(64),
    minutes: z.number().int().min(0).max(43200).optional(),
    reason: z.string().max(255).optional()
});

moderation.post('/shadow-mutes', async c =>
{
    const parsed = postSchema.safeParse(await c.req.json().catch(() => ({})));
    if(!parsed.success) return c.json({ error: 'invalid_body', issues: parsed.error.issues }, 400);

    const actor = c.var.user;
    if(!actor) return c.json({ error: 'unauthorized' }, 401);
    const actorId = Number(actor.sub);

    const target = (await dbQuery<{ id: number; username: string; rank: number }>(
        'SELECT id, username, `rank` FROM users WHERE username = ? LIMIT 1', [parsed.data.username]
    ))[0];
    if(!target) return c.json({ error: 'user_not_found' }, 404);
    if(target.id === actorId) return c.json({ error: 'cannot_target_self' }, 400);
    if(Number(target.rank) >= Number(actor.rank)) return c.json({ error: 'target_rank_too_high' }, 403);

    const minutes = parsed.data.minutes ?? 0;
    const reason = parsed.data.reason ?? '';

    if(minutes > 0)
    {
        await dbExecute(
            `INSERT INTO cms_v3_shadow_mutes (user_id, until_ts, reason, created_by, created_at)
               VALUES (?, UNIX_TIMESTAMP() + ?, ?, ?, UNIX_TIMESTAMP())
             ON DUPLICATE KEY UPDATE until_ts=VALUES(until_ts), reason=VALUES(reason), created_by=VALUES(created_by), created_at=VALUES(created_at)`,
            [target.id, minutes * 60, reason, actorId]
        );
    }
    else
    {
        await dbExecute(
            `INSERT INTO cms_v3_shadow_mutes (user_id, until_ts, reason, created_by, created_at)
               VALUES (?, 0, ?, ?, UNIX_TIMESTAMP())
             ON DUPLICATE KEY UPDATE until_ts=VALUES(until_ts), reason=VALUES(reason), created_by=VALUES(created_by), created_at=VALUES(created_at)`,
            [target.id, reason, actorId]
        );
    }

    await audit(actorId, 'moderation.shadowmute.set', reqIp(c), c.req.header('user-agent') ?? '', { userId: target.id, username: target.username, minutes, reason });
    return c.json({ ok: true, userId: target.id, username: target.username, minutes, permanent: minutes === 0 });
});

export default moderation;
