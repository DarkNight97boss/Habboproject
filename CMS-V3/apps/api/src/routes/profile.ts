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

    // Amici: top 6 random tra le friendships.
    const friends = await dbQuery<{ id: number; username: string; look: string; online: string }>(
        `SELECT u2.id, u2.username, u2.look, u2.online
         FROM messenger_friendships mf
         INNER JOIN users u2 ON u2.id = CASE WHEN mf.user_one_id = ? THEN mf.user_two_id ELSE mf.user_one_id END
         WHERE mf.user_one_id = ? OR mf.user_two_id = ?
         LIMIT 6`,
        [u.id, u.id, u.id]
    );

    // Gruppi: i guild a cui l'utente appartiene (membership).
    const groups = await dbQuery<{
        id: number; name: string; description: string;
        badge: string; colorOne: number; colorTwo: number;
    }>(
        `SELECT g.id, g.name, g.description, g.badge,
                g.color_one AS colorOne, g.color_two AS colorTwo
         FROM guilds_members gm
         INNER JOIN guilds g ON g.id = gm.guild_id
         WHERE gm.user_id = ?
         ORDER BY gm.member_since DESC
         LIMIT 8`,
        [u.id]
    );

    // Badge dell'utente: prima i 5 in slot pubblico (1-5), altrimenti i
    // primi 6 dell'inventario badge (per profili senza slot configurati).
    const badges = await dbQuery<{ badge_code: string; slot_id: number }>(
        `SELECT badge_code, slot_id FROM users_badges
         WHERE user_id = ?
         ORDER BY (slot_id BETWEEN 1 AND 5) DESC, slot_id ASC, id DESC
         LIMIT 6`,
        [u.id]
    );

    // Achievements: top 8 per progress (più giocato/avanzato).
    const achievements = await dbQuery<{ achievement_name: string; progress: number }>(
        `SELECT achievement_name, progress FROM users_achievements
         WHERE user_id = ?
         ORDER BY progress DESC
         LIMIT 8`,
        [u.id]
    );

    // Conteggi totali (stats card).
    const counts = await dbQuery<{
        photos: number; rooms: number; friends: number; groups: number;
        badges: number; achievements: number;
    }>(
        `SELECT
            (SELECT COUNT(*) FROM camera_web WHERE user_id = ?) AS photos,
            (SELECT COUNT(*) FROM rooms WHERE owner_id = ? AND state <> 'invisible') AS rooms,
            (SELECT COUNT(*) FROM messenger_friendships WHERE user_one_id = ? OR user_two_id = ?) AS friends,
            (SELECT COUNT(*) FROM guilds_members WHERE user_id = ?) AS \`groups\`,
            (SELECT COUNT(*) FROM users_badges WHERE user_id = ?) AS badges,
            (SELECT COUNT(*) FROM users_achievements WHERE user_id = ?) AS achievements`,
        [u.id, u.id, u.id, u.id, u.id, u.id, u.id]
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
        friends: friends.map(f => ({
            id: f.id,
            username: f.username,
            look: f.look,
            online: f.online === '1'
        })),
        groups,
        badges: badges.map(b => ({ code: b.badge_code, slot: b.slot_id })),
        achievements,
        counts: counts[0] ?? { photos: 0, rooms: 0, friends: 0, groups: 0, badges: 0, achievements: 0 }
    });
});

export default profile;
