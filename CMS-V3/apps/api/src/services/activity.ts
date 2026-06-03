import { dbExecute } from '../db/pool.js';

/**
 * Event-bus (lato producer interno al CMS). Inserisce un evento in
 * cms_v3_activity_events — la stessa landing usata dall'ingest EMU→CMS, ma
 * chiamata direttamente dal codice CMS (co-locato col DB, niente HTTP).
 *
 * Best-effort: un fallimento non deve mai rompere il flusso chiamante
 * (es. un claim streak riuscito non va annullato perché l'evento non è
 * stato scritto).
 */

export interface ActivityEvent
{
    type: string;
    actorId?: number | null;
    actorName?: string;
    payload?: Record<string, unknown>;
    visibility?: 'public' | 'friends' | 'staff';
}

export async function emitActivity(e: ActivityEvent): Promise<void>
{
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_activity_events (actor_id, actor_name, type, payload, visibility) VALUES (?, ?, ?, ?, ?)',
            [
                e.actorId ?? null,
                e.actorName ?? '',
                e.type,
                e.payload ? JSON.stringify(e.payload) : null,
                e.visibility ?? 'public'
            ]
        );
    }
    catch { /* best-effort */ }
}
