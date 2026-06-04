import { dbExecute } from '../db/pool.js';

/**
 * Telemetria economia (#8) — registra i grant di valuta emessi dal CMS (faucet)
 * in `cms_v3_economy_log`, per la dashboard staff.
 *
 * Best-effort: non deve mai bloccare o far fallire il grant vero e proprio.
 * Da chiamare (fire-and-forget) accanto a rcon.giveCredits/giveDuckets/...
 */
export async function logGrant(
    userId: number,
    amount: number,
    source: string,
    currency = 'credits'
): Promise<void>
{
    try
    {
        if(!Number.isFinite(amount) || amount === 0) return;
        await dbExecute(
            'INSERT INTO cms_v3_economy_log (user_id, currency, amount, source) VALUES (?, ?, ?, ?)',
            [userId, currency.slice(0, 16), Math.trunc(amount), source.slice(0, 48)]
        );
    }
    catch { /* best-effort: la telemetria non blocca mai l'erogazione */ }
}
