import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';

/**
 * Skin Engine routes (/api/v2/...).
 *
 * Public reads:
 *   GET  /api/v2/site/skin      → manifest dello skin attivo (full JSON)
 *   GET  /api/v2/skins          → lista skin disponibili (manifest ridotto)
 *
 * Staff writes:
 *   PUT  /api/v2/site/skin      → { slug } cambia skin attivo (rank >= 5)
 *
 * Lo skin attivo è in cms_v3_site_settings.active_skin_slug. Lo skin con
 * quel slug viene fetchato da cms_v3_skins e ritornato col suo manifest JSON.
 *
 * FUTURE (Fase 4): POST /api/v2/skins per custom skin staff-created.
 */

const DEFAULT_SLUG = 'habbo-classico-2008';

interface SkinRow {
    id: number;
    slug: string;
    name: string;
    description: string;
    manifest_json: string;
    is_builtin: number;
    is_custom: number;
}

async function fetchSkinBySlug(slug: string): Promise<SkinRow | null>
{
    const rows = await dbQuery<SkinRow>(
        "SELECT id, slug, name, description, manifest_json, is_builtin, is_custom FROM cms_v3_skins WHERE slug = ? LIMIT 1",
        [slug]
    );
    return rows[0] || null;
}

async function fetchActiveSkin(): Promise<SkinRow>
{
    const settings = await dbQuery<{ value: string }>(
        "SELECT `value` FROM cms_v3_site_settings WHERE `key` = 'active_skin_slug' LIMIT 1"
    );
    const slug = settings[0]?.value || DEFAULT_SLUG;
    let skin = await fetchSkinBySlug(slug);
    if(!skin) skin = await fetchSkinBySlug(DEFAULT_SLUG);
    if(!skin) throw new Error('no_skins_seeded — run ensureSkinsSeeded()');
    return skin;
}

function parseManifest(row: SkinRow): unknown
{
    try { return JSON.parse(row.manifest_json); }
    catch { return null; }
}

// ============================================================
//   ROUTES
// ============================================================

export const skinSiteRoutes = new Hono();

// Mounted at /api/v2/site (insieme alle altre site settings)
skinSiteRoutes.get('/skin', async (c) =>
{
    try
    {
        const row = await fetchActiveSkin();
        c.header('Cache-Control', 'public, max-age=60, stale-while-revalidate=300');
        return c.json({ manifest: parseManifest(row), slug: row.slug, name: row.name });
    }
    catch (e)
    {
        return c.json({ error: 'no_skin_available', detail: String(e) }, 500);
    }
});

skinSiteRoutes.put('/skin', requireAuth, requireRank(5), async (c) =>
{
    const body = await c.req.json().catch(() => ({}));
    const parsed = z.object({ slug: z.string().min(1).max(64) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'invalid_slug' }, 400);

    const exists = await fetchSkinBySlug(parsed.data.slug);
    if(!exists) return c.json({ error: 'skin_not_found' }, 404);

    await dbExecute(
        "INSERT INTO cms_v3_site_settings (`key`, `value`, `updated_by`) VALUES ('active_skin_slug', ?, ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), `updated_by` = VALUES(`updated_by`)",
        [parsed.data.slug, (c.get('user') as { id?: number } | undefined)?.id ?? null]
    );

    return c.json({ ok: true, manifest: parseManifest(exists), slug: exists.slug, name: exists.name });
});

// ============================================================
//   /api/v2/skins (list)
// ============================================================

export const skinsListRoute = new Hono();

skinsListRoute.get('/', async (c) =>
{
    const rows = await dbQuery<SkinRow>(
        "SELECT id, slug, name, description, manifest_json, is_builtin, is_custom FROM cms_v3_skins ORDER BY is_builtin DESC, name ASC"
    );
    const skins = rows.map(r => parseManifest(r)).filter(Boolean);
    c.header('Cache-Control', 'public, max-age=120');
    return c.json({ skins });
});

export default skinsListRoute;
