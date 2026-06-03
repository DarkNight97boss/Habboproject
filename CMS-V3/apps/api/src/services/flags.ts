import { dbQuery } from '../db/pool.js';

/**
 * Lettura server-side dei feature flag (sorgente: cms_v3_feature_flags).
 * Cache 15s in-memory: i flag cambiano di rado e i check sono frequenti.
 *
 * NB: indipendente dalla cache del route /api/v2/flags (che espone solo i
 * flag pubblici). Qui includiamo TUTTI i flag (anche audience='staff') perché
 * serve alla logica server. Lag massimo dopo un toggle: 15s.
 */

interface FlagRow
{
    key: string;
    enabled: number;
}

let cache: { at: number; map: Map<string, boolean> } | null = null;
const TTL_MS = 15_000;

async function loadFlags(): Promise<Map<string, boolean>>
{
    const now = Date.now();
    if(cache && (now - cache.at) < TTL_MS) return cache.map;

    const rows = await dbQuery<FlagRow>('SELECT `key`, enabled FROM cms_v3_feature_flags');
    const map = new Map<string, boolean>();
    for(const r of rows) map.set(r.key, r.enabled === 1);

    cache = { at: now, map };
    return map;
}

/** True se il flag esiste ed è attivo. Default false (fail-closed). */
export async function isFeatureEnabled(key: string): Promise<boolean>
{
    try
    {
        const map = await loadFlags();
        return map.get(key) === true;
    }
    catch
    {
        // DB non raggiungibile → fail-closed (feature spenta).
        return false;
    }
}
