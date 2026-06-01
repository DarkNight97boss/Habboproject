import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth, requireRank } from '../middleware/auth.js';

/**
 * Site-wide settings routes (/api/v2/site/*).
 *
 * Public reads (GET): chiunque legge per applicare il tema attivo (no auth).
 * Staff writes (PUT): solo rank >= 5 (sincronizzato con StaffPanelPage check).
 *
 * Currently: solo `theme`. Estensibile a banner di emergenza, MOTD del giorno,
 * toggle features, ecc.
 */

const VALID_THEMES = ['habbo-classico', 'habbo-dark', 'habbo-sunset', 'minimal-light'] as const;
const DEFAULT_THEME = 'habbo-classico';

const site = new Hono();

/**
 * GET /api/v2/site/theme — tema attivo del sito (pubblico, cacheable).
 * Risposta: { theme: "habbo-classico" }
 *
 * Cache: 60s edge + stale-while-revalidate 5min. Il cambio è raro e gli
 * utenti possono aspettare al massimo 1 minuto per vedere il nuovo tema.
 */
site.get('/theme', async (c) =>
{
    try
    {
        const rows = await dbQuery<{ value: string }>(
            "SELECT `value` FROM cms_v3_site_settings WHERE `key` = 'theme' LIMIT 1"
        );
        const theme = rows[0]?.value || DEFAULT_THEME;
        // Validate enum at read time too — protegge contro DB poisoning manuale.
        const safe = (VALID_THEMES as readonly string[]).includes(theme) ? theme : DEFAULT_THEME;
        c.header('Cache-Control', 'public, max-age=60, stale-while-revalidate=300');
        return c.json({ theme: safe });
    }
    catch
    {
        // Tabella mancante (migration non applicata) → ritorna default, non rompere il sito.
        return c.json({ theme: DEFAULT_THEME });
    }
});

/**
 * PUT /api/v2/site/theme — cambia il tema attivo (staff only).
 * Body: { theme: "habbo-dark" }
 * Risposta: { ok: true, theme: "habbo-dark" }
 */
site.put('/theme', requireAuth, requireRank(5), async (c) =>
{
    const body = await c.req.json().catch(() => ({}));
    const parsed = z.object({ theme: z.enum(VALID_THEMES) }).safeParse(body);
    if(!parsed.success)
    {
        return c.json({ error: 'invalid_theme', allowed: VALID_THEMES }, 400);
    }

    // c.get('user') è popolato da requireAuth. Tipizzazione minima.
    const user = c.get('user') as { id: number; username: string } | undefined;
    const userId = user?.id ?? null;

    await dbExecute(
        "INSERT INTO cms_v3_site_settings (`key`, `value`, `updated_by`) VALUES ('theme', ?, ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), `updated_by` = VALUES(`updated_by`)",
        [parsed.data.theme, userId]
    );

    return c.json({ ok: true, theme: parsed.data.theme });
});

export default site;
