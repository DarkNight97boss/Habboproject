import { Hono } from 'hono';
import { dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';

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

export default me;
