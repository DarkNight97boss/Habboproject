import { Hono } from 'hono';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';
import { isFeatureEnabled } from '../services/flags.js';
import { rcon } from '../services/rcon.js';

/**
 * Missioni (/api/v2/missions) — feature #3.
 *
 *   GET  /            → missioni del periodo con progresso/target/riscattabile.
 *   POST /claim/:id   → riscatta la ricompensa se la missione è completa
 *                       (atomico: PK su (user,mission,periodo) → un riscatto/periodo).
 *
 * Il progresso si calcola contando gli activity event dell'utente
 * (cms_v3_activity_events, attribuiti via actor_id) dall'inizio del periodo —
 * nessuna nuova instrumentazione. Dietro il flag `missions` (OFF → {enabled:false}).
 */

interface Mission
{
    id: string;
    title: string;
    eventType: string;
    target: number;
    period: 'daily' | 'weekly';
    reward: number;
}

// v1: missioni basate su eventi con actor_id affidabile (streak/photo/room/achievement).
const MISSIONS: Mission[] = [
    { id: 'daily_streak',       title: 'Riscatta il tuo streak giornaliero', eventType: 'streak.claimed',       target: 1, period: 'daily',  reward: 15 },
    { id: 'daily_photo',        title: 'Scatta una foto',                    eventType: 'photo.posted',         target: 1, period: 'daily',  reward: 15 },
    { id: 'weekly_room',        title: 'Crea una stanza',                    eventType: 'room.created',         target: 1, period: 'weekly', reward: 40 },
    { id: 'weekly_achievement', title: 'Sblocca un obiettivo',               eventType: 'achievement.unlocked', target: 1, period: 'weekly', reward: 40 }
];

// Espressioni SQL costanti (NON input utente) per inizio-periodo e chiave-periodo.
// daily = oggi (CURDATE); weekly = lunedì della settimana corrente.
function periodSql(period: 'daily' | 'weekly'): { start: string; key: string }
{
    if(period === 'daily')
    {
        return { start: 'CURDATE()', key: "DATE_FORMAT(CURDATE(), '%Y-%m-%d')" };
    }
    return {
        start: 'CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY',
        key: "DATE_FORMAT(CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY, '%Y-%m-%d')"
    };
}

async function progressOf(userId: number, m: Mission): Promise<number>
{
    const { start } = periodSql(m.period);
    const row = (await dbQuery<{ n: number }>(
        `SELECT COUNT(*) AS n FROM cms_v3_activity_events WHERE actor_id = ? AND type = ? AND created_at >= ${start}`,
        [userId, m.eventType]
    ))[0];
    return Number(row?.n ?? 0);
}

const missions = new Hono();
missions.use('*', requireAuth);

missions.get('/', async c =>
{
    if(!(await isFeatureEnabled('missions'))) return c.json({ enabled: false });

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const out = [];
    for(const m of MISSIONS)
    {
        const { key } = periodSql(m.period);
        const progress = await progressOf(userId, m);
        const claimed = !!(await dbQuery(
            `SELECT 1 FROM cms_v3_mission_claims WHERE user_id = ? AND mission_id = ? AND period_key = ${key}`,
            [userId, m.id]
        ))[0];
        out.push({
            id: m.id,
            title: m.title,
            period: m.period,
            target: m.target,
            reward: m.reward,
            progress: Math.min(progress, m.target),
            claimed,
            claimable: progress >= m.target && !claimed
        });
    }

    return c.json({ enabled: true, missions: out });
});

missions.post('/claim/:id', async c =>
{
    if(!(await isFeatureEnabled('missions'))) return c.json({ error: 'feature_disabled' }, 403);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const m = MISSIONS.find(x => x.id === c.req.param('id'));
    if(!m) return c.json({ error: 'unknown_mission' }, 404);

    const progress = await progressOf(userId, m);
    if(progress < m.target) return c.json({ error: 'not_completed', progress, target: m.target }, 409);

    // Riscatto atomico: la PK (user_id, mission_id, period_key) impedisce il doppio
    // riscatto nello stesso periodo, anche con richieste concorrenti.
    const { key } = periodSql(m.period);
    try
    {
        await dbExecute(
            `INSERT INTO cms_v3_mission_claims (user_id, mission_id, period_key, reward, claimed_at) VALUES (?, ?, ${key}, ?, UNIX_TIMESTAMP())`,
            [userId, m.id, m.reward]
        );
    }
    catch
    {
        return c.json({ error: 'already_claimed' }, 409);
    }

    const res = await rcon.giveCredits(userId, m.reward).catch(() => ({ ok: false }));
    return c.json({ ok: true, reward: m.reward, delivered: res.ok });
});

export default missions;
