import { dbExecute, dbQuery } from '../db/pool.js';
import { emitActivity } from './activity.js';

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
        void checkAbuse(userId, source.slice(0, 48));
    }
    catch { /* best-effort: la telemetria non blocca mai l'erogazione */ }
}

// --- Rilevamento abusi economia (#30) ---------------------------------------
// Su ogni grant controlla se l'utente ha ricevuto troppe erogazioni da una
// sorgente nell'ultima ora; oltre soglia → alert staff (visibility 'staff',
// compare in /admin/alerts) con cooldown per utente+sorgente. Chiude in
// particolare il vettore referral-farm. Best-effort, mai bloccante.
const ABUSE_THRESHOLDS: Record<string, number> = { referral: 5 };
const ABUSE_DEFAULT = 25;
const ABUSE_COOLDOWN_SEC = 1800;
const lastAbuseAlert = new Map<string, number>();

async function checkAbuse(userId: number, source: string): Promise<void>
{
    try
    {
        const limit = ABUSE_THRESHOLDS[source] ?? ABUSE_DEFAULT;
        const row = (await dbQuery<{ n: number; total: number }>(
            'SELECT COUNT(*) AS n, COALESCE(SUM(amount),0) AS total FROM cms_v3_economy_log WHERE user_id = ? AND source = ? AND created_at >= (NOW() - INTERVAL 1 HOUR)',
            [userId, source]
        ))[0];
        const n = Number(row?.n ?? 0);
        if(n < limit) return;

        const key = `${userId}:${source}`;
        const now = Math.floor(Date.now() / 1000);
        const last = lastAbuseAlert.get(key);
        if(last && (now - last) < ABUSE_COOLDOWN_SEC) return;
        lastAbuseAlert.set(key, now);

        const nameRow = (await dbQuery<{ username: string }>('SELECT username FROM users WHERE id = ?', [userId]))[0];
        await emitActivity({
            type: 'economy.alert',
            actorId: userId,
            actorName: nameRow?.username ?? '',
            payload: { source, count: n, total: Number(row?.total ?? 0), windowHours: 1 },
            visibility: 'staff'
        });
    }
    catch { /* best-effort */ }
}
