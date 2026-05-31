import { Hono } from 'hono';
import { dbQuery } from '../db/pool.js';

const community = new Hono();

// =================================================================
// GET /community/photos — feed pubblico delle foto camera
// =================================================================
// Pubblico (no auth required): chiunque vede le foto scattate via
// client Nitro nelle stanze del nostro hotel.
//
// Source: tabella `camera_web` (schema Arcturus standard) — viene
// popolata dall'EMU quando un utente conferma la foto col CameraComposer.
// Columns: id, user_id, room_id, timestamp, url (path/URL della PNG).
//
// JOIN con `users` per pescare username + look (per renderizzare
// l'avatar a fianco di ogni foto card).

community.get('/photos', async c =>
{
    const limit = Math.min(Number(c.req.query('limit') ?? 48), 200);
    const offset = Math.max(Number(c.req.query('offset') ?? 0), 0);

    const rows = await dbQuery<{
        id: number;
        user_id: number;
        username: string;
        look: string;
        room_id: number;
        timestamp: number;
        url: string;
        likes: number;
    }>(
        `SELECT cw.id, cw.user_id, u.username, u.look, cw.room_id,
                cw.timestamp, cw.url,
                0 AS likes
         FROM camera_web cw
         INNER JOIN users u ON u.id = cw.user_id
         ORDER BY cw.timestamp DESC
         LIMIT ? OFFSET ?`,
        [limit, offset]
    );

    return c.json({
        photos: rows.map(r => ({
            id: r.id,
            userId: r.user_id,
            username: r.username,
            userLook: r.look,
            roomId: r.room_id,
            timestamp: r.timestamp,
            url: r.url,
            likes: r.likes
        })),
        limit,
        offset
    });
});

export default community;
