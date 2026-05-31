import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';
import { hashPassword, verifyPassword } from '../security/password.js';

const me = new Hono();

me.use('*', requireAuth);

me.get('/', async c =>
{
    const user = c.var.user!;
    const rows = await dbQuery<{
        id: number; username: string; mail: string; motto: string;
        look: string; gender: string; credits: number; rank: number;
        last_online: number; account_created: number;
    }>(
        'SELECT id, username, mail, motto, look, gender, credits, rank, last_online, account_created FROM users WHERE id = ? LIMIT 1',
        [Number(user.sub)]
    );
    const u = rows[0];
    if(!u) return c.json({ error: 'not_found' }, 404);
    return c.json(u);
});

// =================================================================
// PATCH /me/email — cambio email
// =================================================================
me.patch('/email', async c =>
{
    const user = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ email: z.string().email().max(500) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'invalid_email' }, 400);
    await dbExecute('UPDATE users SET mail = ? WHERE id = ?', [parsed.data.email, Number(user.sub)]);
    return c.json({ ok: true });
});

// =================================================================
// PATCH /me/password — cambio password (con verifica della vecchia)
// =================================================================
me.patch('/password', async c =>
{
    const user = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        currentPassword: z.string().min(1).max(256),
        newPassword: z.string().min(6).max(72)
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const rows = await dbQuery<{ password: string }>(
        'SELECT password FROM users WHERE id = ? LIMIT 1',
        [Number(user.sub)]
    );
    if(rows.length === 0) return c.json({ error: 'not_found' }, 404);
    const ok = await verifyPassword(rows[0]!.password, parsed.data.currentPassword);
    if(!ok) return c.json({ error: 'invalid_current_password' }, 401);

    const newHash = await hashPassword(parsed.data.newPassword);
    await dbExecute('UPDATE users SET password = ? WHERE id = ?', [newHash, Number(user.sub)]);
    return c.json({ ok: true });
});

// =================================================================
// PATCH /me/motto — cambio motto
// =================================================================
me.patch('/motto', async c =>
{
    const user = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({ motto: z.string().max(127) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'invalid_motto' }, 400);
    await dbExecute('UPDATE users SET motto = ? WHERE id = ?', [parsed.data.motto, Number(user.sub)]);
    return c.json({ ok: true });
});

// =================================================================
// PATCH /me/privacy — visibilità profilo (cms_currency_private)
// =================================================================
me.patch('/privacy', async c =>
{
    const user = c.var.user!;
    const body = await c.req.json().catch(() => null);
    const parsed = z.object({
        profileHidden: z.boolean().optional(),
        currencyPrivate: z.boolean().optional()
    }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'bad_request' }, 400);

    const updates: string[] = [];
    const params: unknown[] = [];
    if(parsed.data.profileHidden !== undefined)
    {
        updates.push('hidden = ?');
        params.push(parsed.data.profileHidden ? 1 : 0);
    }
    if(parsed.data.currencyPrivate !== undefined)
    {
        updates.push('cms_currency_private = ?');
        params.push(parsed.data.currencyPrivate ? 1 : 0);
    }
    if(updates.length === 0) return c.json({ ok: true });
    params.push(Number(user.sub));
    await dbExecute(`UPDATE users SET ${updates.join(', ')} WHERE id = ?`, params);
    return c.json({ ok: true });
});

export default me;
