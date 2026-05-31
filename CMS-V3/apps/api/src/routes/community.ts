import { Hono } from 'hono';
import { dbQuery } from '../db/pool.js';
import { ensureNewsTable, fetchNewsBySlug, fetchNewsList } from '../services/news.js';

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

interface NewsItem
{
    slug: string;
    href: string;
    image: string;
    title: string;
    date: string;
    category: string;
    categoryLabel: string;
    summary: string;
    bodyHtml: string;
}

const NEWS: NewsItem[] = [
    {
        slug: 'welcome',
        href: '/community/article/welcome',
        image: '/assets/habbo/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'BENVENUTO SU HABBO',
        date: '31 mag 2026',
        category: 'aggiornamenti-su-habbo',
        categoryLabel: 'Aggiornamenti su Habbo',
        summary: 'Il tuo hotel virtuale è pronto. Esplora le stanze, fai nuovi amici e personalizza il tuo Habbo. Clicca su GIOCA per iniziare!',
        bodyHtml: `
            <h2>Benvenuto, nuovo Habbo!</h2>
            <p>Habboproject è una community italiana dedicata al gioco originale, restaurata e curata
               per offrire la stessa esperienza pixel-art che amavamo nei primi 2000.</p>
            <p>Cosa puoi fare:</p>
            <ul>
                <li>Crea il tuo avatar e personalizza il look</li>
                <li>Visita le stanze pubbliche e fai nuove amicizie</li>
                <li>Costruisci la tua stanza usando il furni del catalogo</li>
                <li>Partecipa agli eventi staff-organizzati</li>
            </ul>
            <p>Clicca su <strong>GIOCA</strong> in alto a destra per entrare nell'hotel.</p>
        `
    },
    {
        slug: 'roller-disco',
        href: '/community/article/roller-disco',
        image: '/assets/habbo/web_images/habbo-web-articles/lpromo_rollerdiscoFL_may26.png',
        title: 'LIVE ORA: Roller Disco!',
        date: '01 mag 2026',
        category: 'campagne-attivita',
        categoryLabel: 'Campagne & Attività',
        summary: "La Pista Retrò ti sta chiamando. Entra e dai un'occhiata!",
        bodyHtml: `
            <h2>Pattini, luci, musica anni '70!</h2>
            <p>La Pista Retrò è aperta nella stanza pubblica "Roller Disco". Indossa i pattini gratis
               che trovi nel guardaroba e lanciati sulla pista a tempo di musica.</p>
            <p>Eventi schedulati ogni sera alle 21:00 con DJ Habbo dal vivo.</p>
        `
    },
    {
        slug: 'comandi-italiani',
        href: '/community/article/comandi-italiani',
        image: '/assets/habbo/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'COMANDI ITALIANI',
        date: '29 mag 2026',
        category: 'aggiornamenti-su-habbo',
        categoryLabel: 'Aggiornamenti su Habbo',
        summary: 'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.',
        bodyHtml: `
            <h2>140 comandi tradotti</h2>
            <p>Ora puoi usare i comandi in italiano in chat senza dover ricordare quelli in inglese:</p>
            <ul>
                <li><code>:bando &lt;nome&gt;</code> — banna un utente dalla tua stanza</li>
                <li><code>:tira &lt;nome&gt;</code> — chiama un utente vicino a te</li>
                <li><code>:spingi &lt;nome&gt;</code> — sposta un utente di una tile</li>
                <li><code>:silenzia &lt;nome&gt; &lt;minuti&gt;</code> — mute temporaneo</li>
                <li>e altri 140 comandi…</li>
            </ul>
            <p>Lista completa via <code>:comandi</code> in chat.</p>
        `
    },
    {
        slug: 'oracolo',
        href: '/community/article/oracolo',
        image: '/assets/habbo/web_images/habbo-web-articles/lpromo_HabboPulse.png',
        title: 'BOT ORACOLO ATTIVO',
        date: '30 mag 2026',
        category: 'nuove-funzionalita',
        categoryLabel: 'Nuove funzionalità',
        summary: 'Entra nella stanza Oracolo e proponi le tue idee per nuove feature.',
        bodyHtml: `
            <h2>Cosa è Oracolo?</h2>
            <p>Oracolo è un bot che vive nella stanza omonima e ascolta le proposte di feature
               che la community vorrebbe vedere implementate in Habboproject.</p>
            <p>Come funziona:</p>
            <ul>
                <li>Entra nella stanza "Oracolo" (cerca su Navigator)</li>
                <li>Scrivi la tua proposta normalmente in chat</li>
                <li>Oracolo la salva e altri utenti possono votarla</li>
                <li>Le top-10 proposte vengono valutate dallo staff per l'implementazione</li>
            </ul>
            <p>Comandi staff: <code>:oracolo lista</code>, <code>:oracolo accetta &lt;id&gt;</code>,
               <code>:oracolo rifiuta &lt;id&gt;</code>.</p>
        `
    }
];

community.get('/news', async c =>
{
    const cat = c.req.query('category');
    // Auto-create tabella + seed con NEWS hardcoded al primo accesso.
    // Idempotente: se cms_v3_news contiene già righe, il seed non fa nulla.
    await ensureNewsTable(NEWS);
    const items = await fetchNewsList(cat);
    return c.json({ news: items });
});

community.get('/news/:slug', async c =>
{
    const slug = c.req.param('slug');
    await ensureNewsTable(NEWS);
    const article = await fetchNewsBySlug(slug);
    if(!article) return c.json({ error: 'not_found' }, 404);
    return c.json({ article });
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

// =================================================================
// GET /community/photos/:id — singola foto + like + creator full
// =================================================================
// Pubblico. Ritorna 404 se la foto non esiste.

community.get('/photos/:id', async c =>
{
    const id = Number(c.req.param('id'));
    if(!Number.isFinite(id) || id <= 0) return c.json({ error: 'bad_id' }, 400);

    const rows = await dbQuery<{
        id: number; user_id: number; username: string; look: string;
        room_id: number; room_name: string; timestamp: number; url: string;
    }>(
        `SELECT cw.id, cw.user_id, u.username, u.look,
                cw.room_id, COALESCE(r.name, '') AS room_name,
                cw.timestamp, cw.url
         FROM camera_web cw
         INNER JOIN users u ON u.id = cw.user_id
         LEFT JOIN rooms r ON r.id = cw.room_id
         WHERE cw.id = ? LIMIT 1`,
        [id]
    );
    const p = rows[0];
    if(!p) return c.json({ error: 'not_found' }, 404);

    // "Altri scatti dell'autore": le ultime 6 foto dello stesso utente,
    // escluso lo scatto corrente.
    const others = await dbQuery<{ id: number; timestamp: number; url: string }>(
        `SELECT id, timestamp, url FROM camera_web
         WHERE user_id = ? AND id <> ?
         ORDER BY timestamp DESC LIMIT 6`,
        [p.user_id, p.id]
    );

    return c.json({
        photo: {
            id: p.id,
            userId: p.user_id,
            username: p.username,
            userLook: p.look,
            roomId: p.room_id,
            roomName: p.room_name,
            timestamp: p.timestamp,
            url: p.url,
            likes: 0
        },
        otherPhotos: others.map(o => ({ id: o.id, timestamp: o.timestamp, url: o.url }))
    });
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
