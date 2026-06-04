import { Hono } from 'hono';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';
import { emitActivity } from '../services/activity.js';
import { isFeatureEnabled } from '../services/flags.js';
import { rcon } from '../services/rcon.js';
import { logGrant } from '../services/economy.js';

/**
 * Daily Streak (/api/v2/streak) — Fase 3, feature #1.
 *
 *   GET  /        → stato (streak corrente, claimabile oggi, prossimo premio)
 *   POST /claim   → claim giornaliero: avanza/azzera lo streak (atomico,
 *                   race-safe), premia in crediti via RCON, emette
 *                   l'activity event 'streak.claimed'.
 *
 * Dietro il feature flag `daily_streak`: se OFF, GET → {enabled:false},
 * POST → 403. Tutta la logica di data è DB-side (CURDATE) per coerenza tz.
 */

interface StreakRow
{
    current_streak: number;
    longest_streak: number;
    total_claims: number;
    claimable_today: number;
    continues: number;
}

// Curva premio in crediti: 10→50 (cap), +bonus milestone (7°→+100, 30°→+500).
function streakReward(streak: number): number
{
    const base = Math.min(50, 10 + (streak - 1) * 5);
    let bonus = 0;
    if(streak > 0 && streak % 30 === 0) bonus = 500;
    else if(streak > 0 && streak % 7 === 0) bonus = 100;
    return base + bonus;
}

const streak = new Hono();
streak.use('*', requireAuth);

const readState = (userId: number) => dbQuery<StreakRow>(
    `SELECT current_streak, longest_streak, total_claims,
            (last_claim_date IS NULL OR last_claim_date < CURDATE()) AS claimable_today,
            (last_claim_date = (CURDATE() - INTERVAL 1 DAY))         AS continues
     FROM cms_v3_user_streaks WHERE user_id = ?`,
    [userId]
);

streak.get('/', async c =>
{
    if(!(await isFeatureEnabled('daily_streak'))) return c.json({ enabled: false });

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const row = (await readState(userId))[0];
    const current = row?.current_streak ?? 0;
    const claimable = row ? row.claimable_today === 1 : true;
    const continues = row ? row.continues === 1 : false;
    const nextStreak = claimable ? (continues ? current + 1 : 1) : current;

    return c.json({
        enabled: true,
        currentStreak: current,
        longestStreak: row?.longest_streak ?? 0,
        totalClaims: row?.total_claims ?? 0,
        claimableToday: claimable,
        nextReward: claimable ? streakReward(nextStreak) : null
    });
});

streak.post('/claim', async c =>
{
    if(!(await isFeatureEnabled('daily_streak'))) return c.json({ error: 'feature_disabled' }, 403);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    // Assicura la riga utente.
    await dbExecute('INSERT IGNORE INTO cms_v3_user_streaks (user_id) VALUES (?)', [userId]);

    // Claim ATOMICO: la WHERE last_claim_date<CURDATE() garantisce un solo
    // claim al giorno anche con richieste concorrenti. La continuazione
    // (streak+1) vs reset (1) è decisa DB-side sul "ieri".
    const upd = await dbExecute(
        `UPDATE cms_v3_user_streaks
         SET current_streak = IF(last_claim_date = (CURDATE() - INTERVAL 1 DAY), current_streak + 1, 1),
             longest_streak = GREATEST(longest_streak, IF(last_claim_date = (CURDATE() - INTERVAL 1 DAY), current_streak + 1, 1)),
             last_claim_date = CURDATE(),
             total_claims    = total_claims + 1
         WHERE user_id = ? AND (last_claim_date IS NULL OR last_claim_date < CURDATE())`,
        [userId]
    );

    if(upd.affectedRows === 0)
    {
        const cur = (await dbQuery<{ current_streak: number }>(
            'SELECT current_streak FROM cms_v3_user_streaks WHERE user_id = ?', [userId]
        ))[0];
        return c.json({ claimed: false, alreadyClaimedToday: true, currentStreak: cur?.current_streak ?? 0 }, 409);
    }

    const after = (await dbQuery<{ current_streak: number; longest_streak: number; total_claims: number }>(
        'SELECT current_streak, longest_streak, total_claims FROM cms_v3_user_streaks WHERE user_id = ?', [userId]
    ))[0];
    const newStreak = after?.current_streak ?? 1;
    const reward = streakReward(newStreak);

    // Premio crediti via RCON (Arcturus persiste anche per utenti offline,
    // come la consegna dello shop). Se fallisce NON annulliamo il claim
    // (rollback racy): lo streak resta, segnaliamo rewardDelivered=false.
    const res = await rcon.giveCredits(userId, reward);
    void logGrant(userId, reward, 'streak');

    const nameRow = (await dbQuery<{ username: string }>(
        'SELECT username FROM users WHERE id = ?', [userId]
    ))[0];
    await emitActivity({
        type: 'streak.claimed',
        actorId: userId,
        actorName: nameRow?.username ?? '',
        payload: { streak: newStreak, reward }
    });

    return c.json({
        claimed: true,
        currentStreak: newStreak,
        longestStreak: after?.longest_streak ?? newStreak,
        totalClaims: after?.total_claims ?? 1,
        reward,
        rewardDelivered: res.ok
    });
});

export default streak;
