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
//   VALIDAZIONE MANIFEST (Skin Builder — Fase 19)
// ============================================================
// Schema Zod che rispecchia SkinManifest (apps/web/src/lib/skin.ts).
// I valori CSS sono bounded + vietano `<`/`>` (difesa: anche se finiscono
// in un <link>/HTML context non possono iniettare markup). Gli URL (font,
// asset) sono validati come URL http(s). Le skin sono create solo da staff
// (rank >= 5) ma validiamo comunque server-side: il manifest finisce in
// :root come CSS custom properties applicate a TUTTI i visitatori.

const cssSafe = (max: number) => z.string().min(1).max(max).regex(/^[^<>]*$/, 'caratteri non ammessi');
const httpUrl = z.string().url().max(500).regex(/^https?:\/\//i, 'solo http(s)');

const manifestSchema = z.object({
    version: z.number().int().min(1).max(99),
    meta: z.object({
        slug: z.string().max(64).optional(),
        name: z.string().min(1).max(100),
        description: z.string().max(500).default(''),
        swatches: z.tuple([ cssSafe(80), cssSafe(80), cssSafe(80), cssSafe(80) ])
    }),
    tokens: z.object({
        colors: z.record(z.string(), cssSafe(80)).refine(
            o => { const k = Object.keys(o); return k.length >= 1 && k.length <= 60 && k.every(x => /^[a-z0-9-]{1,40}$/.test(x)); },
            'chiavi colore non valide (atteso: kebab-case, 1-60 voci)'
        ),
        typography: z.object({
            fontFamily: cssSafe(200),
            fontFamilyHeading: cssSafe(200).optional(),
            fontUrl: httpUrl.optional(),
            sizeScale: z.number().min(0.5).max(2),
            weightHeading: z.number().int().min(100).max(900),
            letterSpacingHeading: cssSafe(20).optional()
        }),
        spacing: z.object({ unit: z.number().min(1).max(40), densityScale: z.number().min(0.5).max(3) }),
        radius: z.object({ base: z.number().min(0).max(80), button: z.number().min(0).max(80), input: z.number().min(0).max(80) }),
        shadow: z.object({ style: z.enum([ 'flat', 'soft', 'neon-glow', 'pixel' ]), card: cssSafe(400), button: cssSafe(400) }),
        animation: z.object({ speed: z.enum([ 'instant', 'fast', 'normal', 'slow' ]), easing: cssSafe(80) })
    }),
    assets: z.object({
        logoUrl: httpUrl.optional(),
        heroBackgroundUrl: httpUrl.optional(),
        patternUrl: httpUrl.optional(),
        faviconUrl: httpUrl.optional()
    }).optional(),
    layout: z.object({ variant: z.enum([ 'classic', 'modern', 'compact', 'magazine' ]).optional() }).optional(),
    background: z.object({ type: z.enum([ 'solid', 'gradient', 'pattern', 'animated' ]), value: cssSafe(400), animation: cssSafe(40).optional() }).optional()
});

/** Genera uno slug URL-safe dal nome. */
function slugify(name: string): string
{
    return name.toLowerCase().normalize('NFKD').replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 56) || 'skin';
}

/** Garantisce uno slug unico in cms_v3_skins (append -2, -3, …). */
async function uniqueSlug(base: string): Promise<string>
{
    let slug = base;
    for(let n = 2; n <= 200; n++)
    {
        if(!(await fetchSkinBySlug(slug))) return slug;
        slug = `${base}-${n}`;
    }
    return `${base}-${slug.length}${base.length}`; // fallback estremamente improbabile
}

type MinCtx = { req: { header: (name: string) => string | undefined } };
function clientIp(c: MinCtx): string
{
    const xff = c.req.header('x-forwarded-for');
    return (xff?.split(',')[0]?.trim()) || c.req.header('x-real-ip') || 'unknown';
}

/** Scrive una riga in cms_v3_audit_log (best-effort, non blocca la response). */
async function audit(c: MinCtx, userId: number | null, action: string, details: unknown): Promise<void>
{
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_audit_log (user_id, action, ip, user_agent, details, created_at) VALUES (?, ?, ?, ?, ?, UNIX_TIMESTAMP())',
            [ userId, action, clientIp(c).slice(0, 45), (c.req.header('user-agent') ?? '').slice(0, 255), JSON.stringify(details).slice(0, 1000) ]
        );
    }
    catch { /* audit best-effort */ }
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
    // entries: metadati DB (id + flag) per il Skin Builder staff. Nessun
    // segreto (created_by escluso). I consumer pubblici usano solo `.skins`.
    const entries = rows.map(r => ({
        id: r.id,
        slug: r.slug,
        name: r.name,
        description: r.description,
        isBuiltin: r.is_builtin === 1,
        isCustom: r.is_custom === 1
    }));
    c.header('Cache-Control', 'public, max-age=120');
    return c.json({ skins, entries });
});

// ------------------------------------------------------------
//  POST /api/v2/skins — crea una custom skin (staff, rank >= 5)
// ------------------------------------------------------------
// Body: { manifest: SkinManifest } (o il manifest direttamente). Lo slug
// viene generato server-side dal nome (unico) e SOVRASCRIVE meta.slug, così
// il manifest salvato combacia col record (applySkin usa meta.slug per data-skin).
skinsListRoute.post('/', requireAuth, requireRank(5), async (c) =>
{
    const body = await c.req.json().catch(() => null) as { manifest?: unknown } | null;
    const parsed = manifestSchema.safeParse(body?.manifest ?? body);
    if(!parsed.success) return c.json({ error: 'invalid_manifest', issues: parsed.error.issues.slice(0, 8) }, 400);

    const m = parsed.data;
    const slug = await uniqueSlug(slugify(m.meta.name));
    m.meta.slug = slug;
    const uid = (c.get('user') as { id?: number } | undefined)?.id ?? null;

    const res = await dbExecute(
        'INSERT INTO cms_v3_skins (slug, name, description, manifest_json, is_builtin, is_custom, created_by, created_at, updated_at) ' +
        'VALUES (?, ?, ?, ?, 0, 1, ?, NOW(), NOW())',
        [ slug, m.meta.name.slice(0, 100), (m.meta.description || '').slice(0, 500), JSON.stringify(m), uid ]
    );
    await audit(c, uid, 'skin.create', { slug, name: m.meta.name });

    return c.json({ ok: true, id: (res as { insertId?: number }).insertId ?? null, slug, manifest: m }, 201);
});

// ------------------------------------------------------------
//  PATCH /api/v2/skins/:id — modifica una custom skin (staff)
// ------------------------------------------------------------
// Solo skin con is_builtin = 0. Lo slug è IMMUTABILE (preserva il riferimento
// active_skin_slug se questa skin è attiva).
skinsListRoute.patch('/:id', requireAuth, requireRank(5), async (c) =>
{
    const id = Number(c.req.param('id'));
    if(!Number.isInteger(id) || id <= 0) return c.json({ error: 'invalid_id' }, 400);

    const rows = await dbQuery<SkinRow>(
        'SELECT id, slug, name, description, manifest_json, is_builtin, is_custom FROM cms_v3_skins WHERE id = ? LIMIT 1',
        [id]
    );
    const existing = rows[0];
    if(!existing) return c.json({ error: 'skin_not_found' }, 404);
    if(existing.is_builtin) return c.json({ error: 'cannot_edit_builtin' }, 403);

    const body = await c.req.json().catch(() => null) as { manifest?: unknown } | null;
    const parsed = manifestSchema.safeParse(body?.manifest ?? body);
    if(!parsed.success) return c.json({ error: 'invalid_manifest', issues: parsed.error.issues.slice(0, 8) }, 400);

    const m = parsed.data;
    m.meta.slug = existing.slug; // slug immutabile in edit
    const uid = (c.get('user') as { id?: number } | undefined)?.id ?? null;

    await dbExecute(
        'UPDATE cms_v3_skins SET name = ?, description = ?, manifest_json = ?, updated_at = NOW() WHERE id = ? AND is_builtin = 0',
        [ m.meta.name.slice(0, 100), (m.meta.description || '').slice(0, 500), JSON.stringify(m), id ]
    );
    await audit(c, uid, 'skin.update', { id, slug: existing.slug });

    return c.json({ ok: true, id, slug: existing.slug, manifest: m });
});

// ------------------------------------------------------------
//  DELETE /api/v2/skins/:id — elimina una custom skin (staff)
// ------------------------------------------------------------
// Vietato su builtin e sulla skin ATTIVA (l'utente deve prima cambiarla).
skinsListRoute.delete('/:id', requireAuth, requireRank(5), async (c) =>
{
    const id = Number(c.req.param('id'));
    if(!Number.isInteger(id) || id <= 0) return c.json({ error: 'invalid_id' }, 400);

    const rows = await dbQuery<{ id: number; slug: string; is_builtin: number }>(
        'SELECT id, slug, is_builtin FROM cms_v3_skins WHERE id = ? LIMIT 1',
        [id]
    );
    const existing = rows[0];
    if(!existing) return c.json({ error: 'skin_not_found' }, 404);
    if(existing.is_builtin) return c.json({ error: 'cannot_delete_builtin' }, 403);

    const settings = await dbQuery<{ value: string }>(
        "SELECT `value` FROM cms_v3_site_settings WHERE `key` = 'active_skin_slug' LIMIT 1"
    );
    if((settings[0]?.value || DEFAULT_SLUG) === existing.slug)
    {
        return c.json({ error: 'skin_is_active', detail: 'Cambia lo skin attivo prima di eliminare questo.' }, 409);
    }

    await dbExecute('DELETE FROM cms_v3_skins WHERE id = ? AND is_builtin = 0', [id]);
    const uid = (c.get('user') as { id?: number } | undefined)?.id ?? null;
    await audit(c, uid, 'skin.delete', { id, slug: existing.slug });

    return c.json({ ok: true });
});

export default skinsListRoute;
