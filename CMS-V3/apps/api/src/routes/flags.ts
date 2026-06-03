import { Hono } from 'hono';
import type { Context } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';

/**
 * Feature Flags (/api/v2/flags) — Fase 1 della piattaforma.
 *
 *   GET    /            → set PUBBLICO dei flag attivi (non-staff), per il
 *                         client Nitro e il web. Cache 15s.
 *   GET    /admin       → elenco completo (rank>=5).
 *   PUT    /admin/:key  → upsert di un flag (rank>=5) + audit.
 *   DELETE /admin/:key  → elimina un flag (rank>=5) + audit.
 *
 * Sorgente di verità = tabella cms_v3_feature_flags. In Fase 2 il set verrà
 * anche distribuito all'EMU via Redis (cache + invalidazione su scrittura).
 */

interface FlagRow
{
    key: string;
    enabled: number;
    description: string;
    rollout_pct: number;
    audience: string;
    updated_by: number | null;
    updated_at: string;
}

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
    catch { /* audit best-effort: non bloccare la scrittura del flag */ }
}

const flags = new Hono();

// Cache breve del set pubblico (i flag cambiano di rado; invalidata su scrittura).
let publicCache: { at: number; data: Record<string, boolean> } | null = null;
const PUBLIC_TTL_MS = 15_000;

flags.get('/', async c =>
{
    const now = Date.now();
    if(publicCache && (now - publicCache.at) < PUBLIC_TTL_MS)
    {
        return c.json({ flags: publicCache.data });
    }

    const rows = await dbQuery<FlagRow>(
        "SELECT `key` FROM cms_v3_feature_flags WHERE enabled = 1 AND audience <> 'staff'"
    );

    const data: Record<string, boolean> = {};
    for(const r of rows) data[r.key] = true;

    publicCache = { at: now, data };
    return c.json({ flags: data });
});

// --- Gestione staff (rank >= 5) ---
const admin = new Hono();
admin.use('*', requireAuth);
admin.use('*', requireRank(5));

admin.get('/', async c =>
{
    const rows = await dbQuery<FlagRow>(
        'SELECT `key`, enabled, description, rollout_pct, audience, updated_by, updated_at FROM cms_v3_feature_flags ORDER BY `key`'
    );
    return c.json({ flags: rows.map(r => ({ ...r, enabled: r.enabled === 1 })) });
});

const upsertSchema = z.object({
    enabled: z.boolean().optional(),
    description: z.string().max(255).optional(),
    rollout_pct: z.number().int().min(0).max(100).optional(),
    audience: z.enum(['all', 'staff', 'hc']).optional()
});

admin.put('/:key', async c =>
{
    const key = c.req.param('key');
    if(!/^[a-z0-9_]{1,64}$/.test(key)) return c.json({ error: 'invalid_key' }, 400);

    const parsed = upsertSchema.safeParse(await c.req.json().catch(() => ({})));
    if(!parsed.success) return c.json({ error: 'invalid_body', issues: parsed.error.issues }, 400);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const existing = (await dbQuery<FlagRow>('SELECT * FROM cms_v3_feature_flags WHERE `key` = ?', [key]))[0];
    const merged = {
        enabled: parsed.data.enabled ?? (existing ? existing.enabled === 1 : false),
        description: parsed.data.description ?? existing?.description ?? '',
        rollout_pct: parsed.data.rollout_pct ?? existing?.rollout_pct ?? 100,
        audience: parsed.data.audience ?? existing?.audience ?? 'all'
    };

    await dbExecute(
        'INSERT INTO cms_v3_feature_flags (`key`, enabled, description, rollout_pct, audience, updated_by) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE enabled=VALUES(enabled), description=VALUES(description), rollout_pct=VALUES(rollout_pct), audience=VALUES(audience), updated_by=VALUES(updated_by)',
        [key, merged.enabled ? 1 : 0, merged.description, merged.rollout_pct, merged.audience, userId]
    );

    publicCache = null;
    await audit(userId, 'flags.update', reqIp(c), c.req.header('user-agent') ?? '', { key, ...merged });
    return c.json({ ok: true, key, ...merged });
});

admin.delete('/:key', async c =>
{
    const key = c.req.param('key');
    if(!/^[a-z0-9_]{1,64}$/.test(key)) return c.json({ error: 'invalid_key' }, 400);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const res = await dbExecute('DELETE FROM cms_v3_feature_flags WHERE `key` = ?', [key]);
    publicCache = null;
    await audit(userId, 'flags.delete', reqIp(c), c.req.header('user-agent') ?? '', { key });
    return c.json({ ok: true, deleted: res.affectedRows });
});

flags.route('/admin', admin);

export default flags;
