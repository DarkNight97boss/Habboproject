import { Hono } from 'hono';
import { dbQuery } from '../db/pool.js';

const profile = new Hono();

// =================================================================
// GET /profile/:username — info pubbliche profilo + stanze + foto
// =================================================================
// Pubblico (no auth). Ritorna 404 se l'utente non esiste.

profile.get('/:username', async c =>
{
    const username = c.req.param('username');

    const userRows = await dbQuery<{
        id: number;
        username: string;
        motto: string;
        look: string;
        rank: number;
        account_created: number;
        last_online: number;
        online: string;
    }>(
        `SELECT id, username, motto, look, rank,
                account_created, last_online, online
         FROM users WHERE username = ? LIMIT 1`,
        [username]
    );
    const u = userRows[0];
    if(!u) return c.json({ error: 'not_found' }, 404);

    // Stanze posseduto dall'utente, escluse invisibili.
    const rooms = await dbQuery<{
        id: number; name: string; description: string;
        users: number; users_max: number; model: string;
    }>(
        `SELECT id, name, description, users, users_max, model
         FROM rooms
         WHERE owner_id = ? AND state <> 'invisible'
         ORDER BY users DESC, id DESC
         LIMIT 12`,
        [u.id]
    );

    // Conteggi totali (stats card).
    const counts = await dbQuery<{ photos: number; rooms: number; friends: number }>(
        `SELECT
            (SELECT COUNT(*) FROM camera_web WHERE user_id = ?) AS photos,
            (SELECT COUNT(*) FROM rooms WHERE owner_id = ? AND state <> 'invisible') AS rooms,
            (SELECT COUNT(*) FROM messenger_friendships WHERE user_one_id = ? OR user_two_id = ?) AS friends`,
        [u.id, u.id, u.id, u.id]
    );

    return c.json({
        user: {
            id: u.id,
            username: u.username,
            motto: u.motto,
            look: u.look,
            rank: u.rank,
            accountCreated: u.account_created,
            lastOnline: u.last_online,
            online: u.online === '1'
        },
        rooms: rooms.map(r => ({
            id: r.id,
            name: r.name,
            description: r.description,
            users: r.users,
            maxUsers: r.users_max,
            model: r.model
        })),
        counts: counts[0] ?? { photos: 0, rooms: 0, friends: 0 }
    });
});

export default profile;
