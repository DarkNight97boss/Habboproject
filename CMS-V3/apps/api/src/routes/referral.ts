import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { requireAuth } from '../middleware/auth.js';
import { emitActivity } from '../services/activity.js';
import { isFeatureEnabled } from '../services/flags.js';
import { rcon } from '../services/rcon.js';
import { logGrant } from '../services/economy.js';

/**
 * Referral (/api/v2/referral) — feature #15.
 *
 *   GET  /         → il mio codice (creato al volo se assente) + chi ho invitato
 *                    + se ho già riscattato un codice.
 *   POST /redeem   → riscatta un codice altrui (una sola volta, solo account
 *                    recenti, niente auto-referral). Premia in crediti via RCON
 *                    chi invita e chi riscatta, ed emette 'referral.redeemed'.
 *
 * Dietro il feature flag `referral`: se OFF, GET → {enabled:false}, POST → 403.
 * NON tocca il flusso di registrazione (critico): il riscatto è un'azione
 * autenticata separata, così resta additivo e a basso rischio.
 */

const REWARD_REFERRER = 50; // crediti a chi invita
const REWARD_REFERRED = 25; // crediti a chi riscatta
const MAX_ACCOUNT_AGE_DAYS = 14; // anti-abuso: solo account recenti possono riscattare
const CODE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // niente caratteri ambigui (0/O/1/I)

function genCode(): string
{
    let s = '';
    for(let i = 0; i < 6; i++) s += CODE_ALPHABET[Math.floor(Math.random() * CODE_ALPHABET.length)];
    return s;
}

async function ensureCode(userId: number): Promise<string | null>
{
    const existing = (await dbQuery<{ code: string }>('SELECT code FROM cms_v3_referral_codes WHERE user_id = ?', [userId]))[0];
    if(existing) return existing.code;
    // Genera con retry su collisione UNIQUE(code) o doppia INSERT concorrente.
    for(let i = 0; i < 6; i++)
    {
        try
        {
            const code = genCode();
            await dbExecute('INSERT INTO cms_v3_referral_codes (user_id, code, created_at) VALUES (?, ?, UNIX_TIMESTAMP())', [userId, code]);
            return code;
        }
        catch
        {
            const r = (await dbQuery<{ code: string }>('SELECT code FROM cms_v3_referral_codes WHERE user_id = ?', [userId]))[0];
            if(r) return r.code;
        }
    }
    return null;
}

const referral = new Hono();
referral.use('*', requireAuth);

referral.get('/', async c =>
{
    if(!(await isFeatureEnabled('referral'))) return c.json({ enabled: false });

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const code = await ensureCode(userId);
    const invited = await dbQuery<{ username: string | null; created_at: number; reward_credits: number }>(
        `SELECT u.username, r.created_at, r.reward_credits
           FROM cms_v3_referrals r LEFT JOIN users u ON u.id = r.referred_id
          WHERE r.referrer_id = ? ORDER BY r.created_at DESC LIMIT 50`,
        [userId]
    );
    const redeemed = (await dbQuery<{ referrer_id: number }>('SELECT referrer_id FROM cms_v3_referrals WHERE referred_id = ?', [userId]))[0];

    return c.json({
        enabled: true,
        code,
        rewardReferrer: REWARD_REFERRER,
        rewardReferred: REWARD_REFERRED,
        invited: invited.map(x => ({ username: x.username, at: x.created_at, reward: x.reward_credits })),
        invitedCount: invited.length,
        alreadyRedeemed: !!redeemed
    });
});

const redeemSchema = z.object({ code: z.string().min(4).max(16) });

referral.post('/redeem', async c =>
{
    if(!(await isFeatureEnabled('referral'))) return c.json({ error: 'feature_disabled' }, 403);

    const user = c.var.user;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const userId = Number(user.sub);

    const parsed = redeemSchema.safeParse(await c.req.json().catch(() => ({})));
    if(!parsed.success) return c.json({ error: 'invalid_body' }, 400);
    const code = parsed.data.code.trim().toUpperCase();

    // Già riscattato? (un solo referral per account)
    if((await dbQuery('SELECT referred_id FROM cms_v3_referrals WHERE referred_id = ?', [userId]))[0])
        return c.json({ error: 'already_redeemed' }, 409);

    // Anti-abuso: solo account recenti possono riscattare.
    const me = (await dbQuery<{ account_created: number }>('SELECT account_created FROM users WHERE id = ? LIMIT 1', [userId]))[0];
    if(!me) return c.json({ error: 'user_not_found' }, 404);
    const ageDays = (Math.floor(Date.now() / 1000) - Number(me.account_created)) / 86400;
    if(ageDays > MAX_ACCOUNT_AGE_DAYS) return c.json({ error: 'account_too_old' }, 403);

    // Risolvi il codice → proprietario.
    const owner = (await dbQuery<{ user_id: number }>('SELECT user_id FROM cms_v3_referral_codes WHERE code = ? LIMIT 1', [code]))[0];
    if(!owner) return c.json({ error: 'code_not_found' }, 404);
    if(owner.user_id === userId) return c.json({ error: 'cannot_self_refer' }, 400);

    // Anti-farming (pentest 2026-09-20): l'anti-abuso economico era solo
    // osservazionale (allerta, non blocca). Un attaccante registrava N account
    // usa-e-getta e riscattava il proprio codice → crediti reali a ripetizione.
    // Blocchiamo i pattern da faucet a costo quasi zero, senza penalizzare i
    // referral legittimi (amici su macchine/reti diverse):
    //   a) stessa MACCHINA invitante/invitato (machine_id uguale) → quasi certo
    //      auto-farming → blocco;
    //   b) troppi inviti dalla stessa RETE dell'invitato nelle 24h (ip_register)
    //      → cap (le famiglie condividono l'IP: si consente qualche invito, non
    //      un faucet).
    const nets = await dbQuery<{ id: number; ip_register: string | null; machine_id: string | null }>(
        'SELECT id, ip_register, machine_id FROM users WHERE id IN (?, ?)',
        [userId, owner.user_id],
    );
    const meNet = nets.find(x => x.id === userId);
    const ownerNet = nets.find(x => x.id === owner.user_id);
    const sameMachine = !!meNet?.machine_id && meNet.machine_id === ownerNet?.machine_id;
    if(sameMachine)
    {
        void emitActivity({
            type: 'economy.alert',
            actorId: owner.user_id,
            actorName: '',
            payload: { source: 'referral', reason: 'same_machine', referredId: userId },
            visibility: 'staff',
        });
        return c.json({ error: 'referral_same_network' }, 403);
    }
    if(meNet?.ip_register)
    {
        const fromNet = (await dbQuery<{ n: number }>(
            `SELECT COUNT(*) AS n FROM cms_v3_referrals r JOIN users u ON u.id = r.referred_id
             WHERE r.referrer_id = ? AND u.ip_register = ? AND r.created_at >= UNIX_TIMESTAMP() - 86400`,
            [owner.user_id, meNet.ip_register],
        ))[0];
        if(Number(fromNet?.n ?? 0) >= 3)
        {
            void emitActivity({
                type: 'economy.alert',
                actorId: owner.user_id,
                actorName: '',
                payload: { source: 'referral', reason: 'same_ip_cap', referredId: userId },
                visibility: 'staff',
            });
            return c.json({ error: 'referral_rate_limited' }, 429);
        }
    }

    // Registra il legame (PK referred_id → race-safe, una volta sola).
    try
    {
        await dbExecute(
            'INSERT INTO cms_v3_referrals (referred_id, referrer_id, code, reward_credits, created_at) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP())',
            [userId, owner.user_id, code, REWARD_REFERRED]
        );
    }
    catch
    {
        return c.json({ error: 'already_redeemed' }, 409);
    }

    // Premi via RCON (Arcturus persiste anche per utenti offline). Best-effort:
    // se un premio fallisce NON annulliamo il legame.
    const r1 = await rcon.giveCredits(owner.user_id, REWARD_REFERRER).catch(() => ({ ok: false }));
    const r2 = await rcon.giveCredits(userId, REWARD_REFERRED).catch(() => ({ ok: false }));
    void logGrant(owner.user_id, REWARD_REFERRER, 'referral');
    void logGrant(userId, REWARD_REFERRED, 'referral');

    const nameRow = (await dbQuery<{ username: string }>('SELECT username FROM users WHERE id = ?', [userId]))[0];
    await emitActivity({
        type: 'referral.redeemed',
        actorId: userId,
        actorName: nameRow?.username ?? '',
        payload: { referrerId: owner.user_id },
        visibility: 'public'
    });

    return c.json({
        ok: true,
        rewardReferrer: REWARD_REFERRER,
        rewardReferred: REWARD_REFERRED,
        delivered: { referrer: r1.ok, referred: r2.ok }
    });
});

export default referral;
