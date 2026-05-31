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

// =================================================================
// GET /community/rooms — galleria stanze (pubbliche + popolari)
// =================================================================
// Pubblico. Lista stanze ordinate per N utenti online DESC, poi score.
// Esclude le invisible (state='invisible'). Include il nome owner per
// rendere card "room + owner avatar".

community.get('/rooms', async c =>
{
    const limit = Math.min(Number(c.req.query('limit') ?? 60), 200);
    const offset = Math.max(Number(c.req.query('offset') ?? 0), 0);

    const rows = await dbQuery<{
        id: number;
        owner_id: number;
        owner_name: string;
        owner_look: string;
        name: string;
        description: string;
        model: string;
        users: number;
        users_max: number;
        state: string;
        category: number;
        score: number;
        is_staff_picked: string;
    }>(
        `SELECT r.id, r.owner_id, r.owner_name, u.look AS owner_look,
                r.name, r.description, r.model, r.users, r.users_max,
                r.state, r.category, r.score, r.is_staff_picked
         FROM rooms r
         LEFT JOIN users u ON u.id = r.owner_id
         WHERE r.state <> 'invisible'
         ORDER BY r.users DESC, r.score DESC, r.id DESC
         LIMIT ? OFFSET ?`,
        [limit, offset]
    );

    return c.json({
        rooms: rows.map(r => ({
            id: r.id,
            ownerId: r.owner_id,
            ownerName: r.owner_name,
            ownerLook: r.owner_look ?? '',
            name: r.name,
            description: r.description,
            model: r.model,
            users: r.users,
            maxUsers: r.users_max,
            state: r.state,
            category: r.category,
            score: r.score,
            staffPicked: r.is_staff_picked === '1'
        })),
        limit,
        offset
    });
});

// =================================================================
// GET /community/news — feed news/articoli del CMS
// =================================================================
// Per ora dati hardcoded (stessi 4 articoli mostrati in HomePage).
// Quando avremo una tabella `cms_articles` o simile la migrazione
// è solo cambiare la sorgente.

const NEWS = [
    {
        href: '/community/article/welcome',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'BENVENUTO SU HABBO',
        date: '31 mag 2026',
        category: 'aggiornamenti-su-habbo',
        categoryLabel: 'Aggiornamenti su Habbo',
        summary: 'Il tuo hotel virtuale è pronto. Esplora le stanze, fai nuovi amici e personalizza il tuo Habbo. Clicca su GIOCA per iniziare!'
    },
    {
        href: '/community/article/roller-disco',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_rollerdiscoFL_may26.png',
        title: 'LIVE ORA: Roller Disco!',
        date: '01 mag 2026',
        category: 'campagne-attivita',
        categoryLabel: 'Campagne & Attività',
        summary: "La Pista Retrò ti sta chiamando. Entra e dai un'occhiata!"
    },
    {
        href: '/community/article/comandi-italiani',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'COMANDI ITALIANI',
        date: '29 mag 2026',
        category: 'aggiornamenti-su-habbo',
        categoryLabel: 'Aggiornamenti su Habbo',
        summary: 'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.'
    },
    {
        href: '/community/article/oracolo',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_HabboPulse.png',
        title: 'BOT ORACOLO ATTIVO',
        date: '30 mag 2026',
        category: 'nuove-funzionalita',
        categoryLabel: 'Nuove funzionalità',
        summary: 'Entra nella stanza Oracolo e proponi le tue idee per nuove feature.'
    }
];

community.get('/news', c =>
{
    const cat = c.req.query('category');
    const items = (cat && cat !== 'all') ? NEWS.filter(n => n.category === cat) : NEWS;
    return c.json({ news: items });
});

// =================================================================
// GET /community/forum/threads — discussion board interno Habboproject
// =================================================================
// Stub MVP: ritorna sempre []. Quando aggiungeremo la tabella
// `cms_v3_forum_threads` o riuseremo guilds_forums_threads, basta
// cambiare la SELECT.

community.get('/forum/threads', c =>
{
    return c.json({ threads: [], total: 0 });
});

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
