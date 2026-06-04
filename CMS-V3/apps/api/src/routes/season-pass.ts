import { Hono } from 'hono';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';
import { isFeatureEnabled } from '../services/flags.js';
import { rcon } from '../services/rcon.js';

/**
 * Season Pass / Battle Pass (/api/v2/season-pass) — feature #2, v1.
 *
 *   GET  /            → XP della stagione + tier raggiunto + lista tier
 *                       (soglia, ricompensa, riscattato, riscattabile).
 *   POST /claim/:tier → riscatta la ricompensa di un tier raggiunto (atomico:
 *                       PK su (user, season, tier) → un riscatto/tier).
 *
 * L'XP si calcola sommando (pesato) gli activity event dell'utente dall'inizio
 * stagione (via actor_id) — stesso approccio delle missioni. Dietro il flag
 * `season_pass` (OFF → {enabled:false}).
 */

const SEASON = { id: 'S1', startTs: 1748736000 }; // 2026-06-01 00:00 UTC — inizio Stagione 1
const TIERS = 10;
const XP_PER_TIER = 100;

// Pesi XP per tipo di evento (solo eventi con actor_id affidabile).
const XP_WEIGHTS: Record<string, number> = {
    'streak.claimed': 10,
    'photo.posted': 8,
    'room.created': 15,
    'achievement.unlocked': 20,
    'guild.created': 25,
    'referral.redeemed': 50
};

const tierThreshold = (tier: number): number => tier * XP_PER_TIER;
const tierReward = (tier: number): number => 20 + tier * 5; // tier1=25 … tier10=70 crediti

async function seasonXp(userId: number): Promise<number>
{
    // SUM(CASE type WHEN ... THEN peso ...) sugli eventi dall'inizio stagione.
    const cases = Object.entries(XP_WEIGHTS)
        .map(([type, w]) => `WHEN ${escapeSqlString(type)} THEN ${Number(w)}`)
        .join(' ');
    const row = (await dbQuery<{ xp: number }>(
        `SELECT COALESCE(SUM(CASE type ${cases} ELSE 0 END), 0) AS xp
           FROM cms_v3_activity_events
          WHERE actor_id = ? AND created_at >= FROM_UNIXTIME(?)`,
        [userId, SEASON.startTs]
    ))[0];
    return Number(row?.xp ?? 0);
}

// I tipi sono costanti note (XP_WEIGHTS), ma quotiamo comunque per sicurezza.
function escapeSqlString(s: string): string
{
    return `'${s.replace(/'/g, "''")}'`;
}

const seasonPass = new Hono();
seasonPass.use('*', requireAuth);

seasonPass.get('/', async c =>
{
    if(!(await isFeatureEnabled('season_pass'))) return c.json({ enabled: false });

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const xp = await seasonXp(userId);
    const claimedRows = await dbQuery<{ tier: number }>(
        'SELECT tier FROM cms_v3_season_pass_claims WHERE user_id = ? AND season = ?',
        [userId, SEASON.id]
    );
    const claimed = new Set(claimedRows.map(r => Number(r.tier)));

    const currentTier = Math.min(TIERS, Math.floor(xp / XP_PER_TIER));
    const tiers = [];
    for(let t = 1; t <= TIERS; t++)
    {
        const threshold = tierThreshold(t);
        tiers.push({
            tier: t,
            threshold,
            reward: tierReward(t),
            claimed: claimed.has(t),
            claimable: xp >= threshold && !claimed.has(t)
        });
    }

    return c.json({
        enabled: true,
        season: SEASON.id,
        xp,
        tier: currentTier,
        maxTier: TIERS,
        nextThreshold: currentTier < TIERS ? tierThreshold(currentTier + 1) : null,
        tiers
    });
});

seasonPass.post('/claim/:tier', async c =>
{
    if(!(await isFeatureEnabled('season_pass'))) return c.json({ error: 'feature_disabled' }, 403);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const tier = Number(c.req.param('tier'));
    if(!Number.isInteger(tier) || tier < 1 || tier > TIERS) return c.json({ error: 'invalid_tier' }, 400);

    const xp = await seasonXp(userId);
    if(xp < tierThreshold(tier)) return c.json({ error: 'tier_not_reached', xp, needed: tierThreshold(tier) }, 409);

    const reward = tierReward(tier);
    // Riscatto atomico: PK (user_id, season, tier) impedisce il doppio riscatto.
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_season_pass_claims (user_id, season, tier, reward, claimed_at) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP())',
            [userId, SEASON.id, tier, reward]
        );
    }
    catch
    {
        return c.json({ error: 'already_claimed' }, 409);
    }

    const res = await rcon.giveCredits(userId, reward).catch(() => ({ ok: false }));
    return c.json({ ok: true, tier, reward, delivered: res.ok });
});

export default seasonPass;
