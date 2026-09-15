import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { env } from '../env.js';
import { isFeatureEnabled } from '../services/flags.js';

/**
 * Activity / Event-bus (/api/v2/activity) — Fase 1 della piattaforma.
 *
 *   POST /ingest → landing dell'event-bus EMU→CMS. Autenticato con secret
 *                  interno condiviso (header X-Internal-Secret == env
 *                  INTERNAL_API_SECRET). In Fase 2 l'EMU pubblica qui gli
 *                  eventi (badge guadagnato, stanza creata, acquisto, ...).
 *                  Se il secret non è configurato l'endpoint è disabilitato
 *                  (503) → nessun rischio di ingest anonimo.
 *
 *   GET  /feed   → activity feed pubblico (eventi visibility=public, recenti).
 *                  Consumato dalla pagina community del web.
 */

interface EventRow
{
    id: number;
    actor_name: string;
    type: string;
    payload: unknown;
    created_at: string;
}

const safeParse = (v: unknown): unknown =>
{
    if(typeof v !== 'string') return v;
    try { return JSON.parse(v); }
    catch { return v; }
};

const activity = new Hono();

const ingestSchema = z.object({
    type: z.string().min(1).max(48),
    actorId: z.number().int().positive().optional(),
    actorName: z.string().max(64).optional(),
    payload: z.record(z.string(), z.unknown()).optional(),
    visibility: z.enum(['public', 'friends', 'staff']).default('public')
});

activity.post('/ingest', async c =>
{
    const secret = env.INTERNAL_API_SECRET;
    if(!secret) return c.json({ error: 'ingest_disabled' }, 503);
    if(c.req.header('x-internal-secret') !== secret) return c.json({ error: 'unauthorized' }, 401);

    const parsed = ingestSchema.safeParse(await c.req.json().catch(() => ({})));
    if(!parsed.success) return c.json({ error: 'invalid_body', issues: parsed.error.issues }, 400);
    const e = parsed.data;

    await dbExecute(
        'INSERT INTO cms_v3_activity_events (actor_id, actor_name, type, payload, visibility) VALUES (?, ?, ?, ?, ?)',
        [e.actorId ?? null, e.actorName ?? '', e.type, e.payload ? JSON.stringify(e.payload) : null, e.visibility]
    );

    // Scala sanzioni (#18) — dark-launch OSSERVAZIONALE. Quando l'evento è una
    // segnalazione di moderazione (raid/macro/minor/toxicity) di un utente, conta
    // le sue segnalazioni nelle ultime 24h e, al raggiungimento di una soglia,
    // SUGGERISCE allo staff un'azione (evento staff `sanction.suggested`). Non
    // applica MAI sanzioni: solo segnalazione. Gated da `sanctions_ladder` (OFF di
    // default → no-op). Non deve mai far fallire l'ingest (try/catch).
    if(e.actorId && /^(raid|macro|minor|toxicity)\./.test(e.type) && await isFeatureEnabled('sanctions_ladder'))
    {
        try
        {
            const win = await dbQuery<{ n: number }>(
                `SELECT COUNT(*) AS n FROM cms_v3_activity_events
                   WHERE actor_id = ?
                     AND (type LIKE 'raid.%' OR type LIKE 'macro.%' OR type LIKE 'minor.%' OR type LIKE 'toxicity.%')
                     AND created_at >= (NOW() - INTERVAL 24 HOUR)`,
                [e.actorId]
            );
            const count = Number(win[0]?.n ?? 0);
            // Soglia → azione SUGGERITA. Scatta esattamente sull'evento che porta
            // al valore-soglia, così emette una sola volta per gradino.
            const ladder: Array<{ at: number; action: string }> = [
                { at: 3, action: 'warn' }, { at: 6, action: 'mute' },
                { at: 10, action: 'kick' }, { at: 15, action: 'ban' }
            ];
            const tier = ladder.find(t => t.at === count);
            if(tier)
            {
                await dbExecute(
                    'INSERT INTO cms_v3_activity_events (actor_id, actor_name, type, payload, visibility) VALUES (?, ?, ?, ?, ?)',
                    [e.actorId, e.actorName ?? '', 'sanction.suggested',
                        JSON.stringify({ userId: e.actorId, user: e.actorName ?? '', count, windowHours: 24, suggested: tier.action, trigger: e.type }),
                        'staff']
                );
            }
        }
        catch { /* la scala sanzioni non deve mai far fallire l'ingest */ }
    }

    return c.json({ ok: true }, 202);
});

activity.get('/feed', async c =>
{
    // limit clampato a [1,50] e coerced a int → safe da inlinare (evita il
    // quirk di mysql2 sui placeholder in LIMIT).
    if(!(await isFeatureEnabled('activity_feed'))) return c.json({ enabled: false, events: [] });

    const limit = Math.min(50, Math.max(1, Number(c.req.query('limit') ?? 20))) | 0;
    const rows = await dbQuery<EventRow>(
        `SELECT id, actor_name, type, payload, created_at FROM cms_v3_activity_events WHERE visibility = 'public' ORDER BY id DESC LIMIT ${limit}`
    );
    return c.json({ enabled: true, events: rows.map(r => ({ ...r, payload: safeParse(r.payload) })) });
});

export default activity;
