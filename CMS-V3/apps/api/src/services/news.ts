import { dbExecute, dbQuery } from '../db/pool.js';

/**
 * Service di gestione news del CMS-V3.
 *
 * - Tabella `cms_v3_news` autogestita: se assente viene creata al primo
 *   accesso (pattern già usato per cms_v3_strikes). Idempotente.
 * - Seed iniziale: se la tabella è vuota inserisce le 4 news hardcoded
 *   che esistevano in community.ts pre-DB. Se sono state cancellate
 *   manualmente, NON si ripopolano (controlliamo COUNT > 0, non ogni slug).
 * - Tutti gli endpoint pubblici (community.get /news, /news/:slug) e
 *   quelli admin (/staff/news) usano questo modulo come unica sorgente
 *   per evitare duplicazione SQL.
 */

export interface NewsItem
{
    id: number;
    slug: string;
    title: string;
    category: string;
    categoryLabel: string;
    summary: string;
    bodyHtml: string;
    image: string;
    authorId: number | null;
    authorUsername: string | null;
    published: boolean;
    createdAt: number;
    updatedAt: number;
    /** Derivato per il frontend (data formattata IT). */
    date: string;
    /** Derivato per il frontend (link href). */
    href: string;
}

export interface SeedNewsItem
{
    slug: string;
    title: string;
    category: string;
    categoryLabel: string;
    summary: string;
    bodyHtml: string;
    image: string;
    date?: string;
}

interface NewsRow
{
    id: number;
    slug: string;
    title: string;
    category: string;
    category_label: string;
    summary: string;
    body_html: string;
    image: string;
    author_id: number | null;
    author_username: string | null;
    published: number;
    created_at: number;
    updated_at: number;
}

let tableReady = false;

/**
 * CREATE TABLE IF NOT EXISTS + seed iniziale.
 * Throttled: una volta verificato che la tabella esiste e non è vuota,
 * non riesegue il check (flag in memoria).
 */
export async function ensureNewsTable(seed: SeedNewsItem[]): Promise<void>
{
    if(tableReady) return;

    await dbExecute(
        `CREATE TABLE IF NOT EXISTS cms_v3_news (
            id INT AUTO_INCREMENT PRIMARY KEY,
            slug VARCHAR(80) NOT NULL,
            title VARCHAR(200) NOT NULL,
            category VARCHAR(40) NOT NULL DEFAULT 'aggiornamenti-su-habbo',
            category_label VARCHAR(60) NOT NULL DEFAULT 'Aggiornamenti su Habbo',
            summary VARCHAR(500) NOT NULL,
            body_html MEDIUMTEXT NOT NULL,
            image VARCHAR(255) NOT NULL DEFAULT '',
            author_id INT NULL DEFAULT NULL,
            published TINYINT(1) NOT NULL DEFAULT 1,
            created_at INT NOT NULL,
            updated_at INT NOT NULL,
            UNIQUE KEY uniq_slug (slug),
            KEY idx_published_created (published, created_at DESC),
            KEY idx_category (category)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
    );

    const cnt = await dbQuery<{ c: number }>('SELECT COUNT(*) AS c FROM cms_v3_news');
    if((cnt[0]?.c ?? 0) === 0 && seed.length > 0)
    {
        // Seed iniziale dai dati hardcoded che erano in community.ts.
        // Timestamp distribuiti per non averli tutti con identica data.
        const now = Math.floor(Date.now() / 1000);
        for(let i = 0; i < seed.length; i++)
        {
            const item = seed[i]!;
            const ts = now - (seed.length - i) * 86400;        // 1 giorno di sfalsamento
            await dbExecute(
                `INSERT IGNORE INTO cms_v3_news
                    (slug, title, category, category_label, summary, body_html, image,
                     author_id, published, created_at, updated_at)
                 VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 1, ?, ?)`,
                [
                    item.slug, item.title, item.category, item.categoryLabel,
                    item.summary, item.bodyHtml, item.image,
                    ts, ts
                ]
            );
        }
    }

    tableReady = true;
}

function formatItalianDate(ts: number): string
{
    const d = new Date(ts * 1000);
    const months = ['gen', 'feb', 'mar', 'apr', 'mag', 'giu', 'lug', 'ago', 'set', 'ott', 'nov', 'dic'];
    return `${d.getDate().toString().padStart(2, '0')} ${months[d.getMonth()]} ${d.getFullYear()}`;
}

function mapRow(r: NewsRow): NewsItem
{
    return {
        id: r.id,
        slug: r.slug,
        title: r.title,
        category: r.category,
        categoryLabel: r.category_label,
        summary: r.summary,
        bodyHtml: r.body_html,
        image: r.image,
        authorId: r.author_id,
        authorUsername: r.author_username,
        published: r.published === 1,
        createdAt: r.created_at,
        updatedAt: r.updated_at,
        date: formatItalianDate(r.created_at),
        href: `/community/article/${r.slug}`
    };
}

/**
 * Lista news pubblicate (published=1), opzionalmente filtrata per category.
 * Restituisce TUTTI i campi (incluso body_html) — il frontend strippa se
 * vuole una lista leggera.
 */
export async function fetchNewsList(category?: string): Promise<NewsItem[]>
{
    let sql = `
        SELECT n.id, n.slug, n.title, n.category, n.category_label,
               n.summary, n.body_html, n.image, n.author_id,
               u.username AS author_username,
               n.published, n.created_at, n.updated_at
        FROM cms_v3_news n
        LEFT JOIN users u ON u.id = n.author_id
        WHERE n.published = 1`;
    const params: unknown[] = [];
    if(category && category !== 'all')
    {
        sql += ' AND n.category = ?';
        params.push(category);
    }
    sql += ' ORDER BY n.created_at DESC, n.id DESC LIMIT 50';
    const rows = await dbQuery<NewsRow>(sql, params);
    return rows.map(mapRow);
}

/**
 * Singolo articolo per slug. Restituisce anche bozze non pubblicate se lo
 * staff cerca espressamente (parametro `includeUnpublished`).
 */
export async function fetchNewsBySlug(slug: string, includeUnpublished = false): Promise<NewsItem | null>
{
    let sql = `
        SELECT n.id, n.slug, n.title, n.category, n.category_label,
               n.summary, n.body_html, n.image, n.author_id,
               u.username AS author_username,
               n.published, n.created_at, n.updated_at
        FROM cms_v3_news n
        LEFT JOIN users u ON u.id = n.author_id
        WHERE n.slug = ?`;
    if(!includeUnpublished) sql += ' AND n.published = 1';
    sql += ' LIMIT 1';
    const rows = await dbQuery<NewsRow>(sql, [slug]);
    const r = rows[0];
    return r ? mapRow(r) : null;
}

/**
 * Versione admin: lista TUTTE le news (anche bozze).
 */
export async function fetchAllNewsAdmin(): Promise<NewsItem[]>
{
    const rows = await dbQuery<NewsRow>(
        `SELECT n.id, n.slug, n.title, n.category, n.category_label,
                n.summary, n.body_html, n.image, n.author_id,
                u.username AS author_username,
                n.published, n.created_at, n.updated_at
         FROM cms_v3_news n
         LEFT JOIN users u ON u.id = n.author_id
         ORDER BY n.created_at DESC, n.id DESC
         LIMIT 200`
    );
    return rows.map(mapRow);
}
