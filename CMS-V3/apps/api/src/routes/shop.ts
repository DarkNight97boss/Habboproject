import { Hono } from 'hono';
import { z } from 'zod';
import { dbExecute, dbQuery } from '../db/pool.js';
import { env } from '../env.js';
import { requireAuth } from '../middleware/auth.js';
import { rcon } from '../services/rcon.js';
import { getStripe, getStripePublishableKey, getStripeWebhookSecret, isStripeConfigured } from '../services/stripe.js';

/**
 * Shop routes — /api/v2/shop/*
 *
 *   GET  /items                 — public, lista catalogo
 *   GET  /publishable-key       — public, ritorna PK per Stripe.js frontend
 *   POST /checkout              — auth, crea Stripe Checkout Session
 *   POST /webhook               — Stripe → ci notifica payment success
 *   GET  /orders                — auth, mie orders
 *   GET  /verify/:sessionId     — auth, verifica una session (success page)
 */

interface ShopItemRow {
    id: number;
    slug: string;
    name: string;
    description: string;
    category: string;
    price_cents: number;
    currency: string;
    credits_amount: number;
    diamonds_amount: number;
    image_gradient: string;
    badge_code: string | null;
    featured: number;
    sort_order: number;
    stripe_price_id: string | null;
    active: number;
}

const shop = new Hono();

shop.get('/items', async (c) =>
{
    const rows = await dbQuery<ShopItemRow>(
        'SELECT id, slug, name, description, category, price_cents, currency, credits_amount, diamonds_amount, image_gradient, badge_code, featured, sort_order FROM cms_v3_shop_items WHERE active = 1 ORDER BY featured DESC, sort_order ASC'
    );
    c.header('Cache-Control', 'public, max-age=120');
    return c.json({ items: rows });
});

shop.get('/publishable-key', async (c) =>
{
    return c.json({ key: getStripePublishableKey(), configured: isStripeConfigured() });
});

shop.post('/checkout', requireAuth, async (c) =>
{
    const stripe = await getStripe();
    if(!stripe)
    {
        return c.json({ error: 'stripe_not_configured', detail: 'Aggiungi STRIPE_SECRET_KEY all\'env' }, 503);
    }

    const body = await c.req.json().catch(() => ({}));
    const parsed = z.object({ slug: z.string().min(1).max(64) }).safeParse(body);
    if(!parsed.success) return c.json({ error: 'invalid_slug' }, 400);

    const items = await dbQuery<ShopItemRow>(
        'SELECT id, slug, name, description, price_cents, currency, stripe_price_id FROM cms_v3_shop_items WHERE slug = ? AND active = 1 LIMIT 1',
        [parsed.data.slug]
    );
    const item = items[0];
    if(!item) return c.json({ error: 'item_not_found' }, 404);

    const user = c.get('user') as { id: number; username: string; mail: string } | undefined;
    if(!user) return c.json({ error: 'unauthorized' }, 401);

    // Allowed origin per success/cancel — prendi dal primo ALLOWED_ORIGINS
    const origin = env.ALLOWED_ORIGINS[0] || 'https://dev.asteriacore.online';

    try
    {
        // Se item ha stripe_price_id usa quello (recurring/HC sub), altrimenti
        // price_data inline (one-shot crediti).
        const lineItem = item.stripe_price_id
            ? { price: item.stripe_price_id, quantity: 1 }
            : {
                price_data: {
                    currency: item.currency.toLowerCase(),
                    product_data: {
                        name: item.name,
                        // exactOptionalPropertyTypes: ometti description se vuota (no `| undefined`)
                        ...(item.description ? { description: item.description.substring(0, 500) } : {})
                    },
                    unit_amount: item.price_cents
                },
                quantity: 1
            };

        const session = await stripe.checkout.sessions.create({
            mode: 'payment',
            line_items: [lineItem],
            client_reference_id: String(user.id),
            ...(user.mail ? { customer_email: user.mail } : {}),
            metadata: {
                user_id: String(user.id),
                username: user.username,
                item_id: String(item.id),
                item_slug: item.slug
            },
            success_url: `${origin}/shop/success?session_id={CHECKOUT_SESSION_ID}`,
            cancel_url: `${origin}/shop/cancel`,
            locale: 'it',
            // Offerte (#7): abilita i codici promozionali NATIVI di Stripe. DORMIENTE
            // di default → senza codici configurati il checkout è identico a prima
            // (nessuno sconto). Lo staff crea/gestisce codici e percentuali nella
            // dashboard Stripe; nessuna matematica prezzi lato CMS (zero rischio di
            // addebito errato). Il webhook accredita per ITEM (crediti invariati),
            // quindi uno sconto incide solo sul prezzo pagato, mai sui crediti dati.
            allow_promotion_codes: true
        });

        // Pre-record pending order (per audit anche se l'utente abbandona)
        await dbExecute(
            'INSERT INTO cms_v3_shop_orders (user_id, item_id, status, amount_cents, currency, stripe_session_id) VALUES (?, ?, ?, ?, ?, ?)',
            [user.id, item.id, 'pending', item.price_cents, item.currency, session.id]
        );

        return c.json({ url: session.url, sessionId: session.id });
    }
    catch (e)
    {
        // eslint-disable-next-line no-console
        console.error('[shop/checkout] stripe error', e);
        return c.json({ error: 'stripe_session_failed', detail: e instanceof Error ? e.message : 'unknown' }, 500);
    }
});

// Webhook needs RAW body for signature verification — Hono di default fa JSON parse.
// Quindi usiamo c.req.raw direttamente per ottenere il body intatto.
shop.post('/webhook', async (c) =>
{
    const stripe = await getStripe();
    const whSecret = getStripeWebhookSecret();
    if(!stripe || !whSecret) return c.json({ error: 'stripe_not_configured' }, 503);

    const sig = c.req.header('stripe-signature');
    if(!sig) return c.json({ error: 'missing_signature' }, 400);

    const rawBody = await c.req.text();
    let event;
    try
    {
        event = stripe.webhooks.constructEvent(rawBody, sig, whSecret);
    }
    catch (e)
    {
        return c.json({ error: 'invalid_signature', detail: e instanceof Error ? e.message : 'unknown' }, 400);
    }

    // async_payment_succeeded: metodi a notifica differita (es. bonifico) completano la
    // sessione con payment_status='unpaid' e Stripe invia QUESTO evento quando il
    // pagamento è saldato. Passa dallo stesso path idempotente di consegna.
    if(event.type === 'checkout.session.completed' || event.type === 'checkout.session.async_payment_succeeded')
    {
        // Inline shape: il SDK type per session è complesso. Usiamo unknown + cast manuale.
        const session = event.data.object as {
            id: string;
            metadata?: { user_id?: string; item_id?: string };
            payment_intent?: string;
            amount_total?: number;
            payment_status?: string;
        };
        const userId = Number(session.metadata?.user_id || 0);
        const itemId = Number(session.metadata?.item_id || 0);

        // Eroghiamo SOLO se il pagamento è effettivamente saldato. Con metodi a
        // notifica differita `checkout.session.completed` arriva con
        // payment_status='unpaid': non consegniamo ora, aspettiamo async_payment_succeeded.
        // 'no_payment_required' = sessione a importo zero (codice promo 100%): e' un
        // ordine completato legittimamente, va consegnato come 'paid'.
        if(session.payment_status && !['paid', 'no_payment_required'].includes(session.payment_status))
        {
            return c.json({ received: true, skipped: `payment_status_${session.payment_status}` });
        }

        // Mark order paid — IDEMPOTENTE (P1.1 security audit). Stripe consegna i webhook
        // at-least-once e ritenta su timeout/5xx: prima si erogava incondizionatamente
        // dopo l'UPDATE, quindi un retry/replay RI-ACCREDITAVA crediti/diamanti/badge.
        // Ora consegniamo SOLO se questo UPDATE ha davvero fatto la transizione
        // pending→paid (affectedRows=1); su retry la riga è già 'paid' → 0 righe →
        // usciamo senza toccare l'economia. amount_cents = importo EFFETTIVO pagato
        // (session.amount_total, COALESCE) per un audit corretto anche con codici promo.
        const upd = await dbExecute(
            'UPDATE cms_v3_shop_orders SET status = ?, stripe_payment_intent = ?, amount_cents = COALESCE(?, amount_cents), paid_at = NOW() WHERE stripe_session_id = ? AND status = ?',
            ['paid', session.payment_intent || null, session.amount_total ?? null, session.id, 'pending']
        );
        if(upd.affectedRows !== 1)
        {
            // Già processato (retry/replay) oppure sessione sconosciuta: nessuna consegna.
            return c.json({ received: true, skipped: 'already_processed' });
        }

        // Delivery: credita user via RCON
        const item = (await dbQuery<{ credits_amount: number; diamonds_amount: number; name: string; badge_code: string | null }>(
            'SELECT credits_amount, diamonds_amount, name, badge_code FROM cms_v3_shop_items WHERE id = ? LIMIT 1',
            [itemId]
        ))[0];

        const logLines: string[] = [];

        if(item && userId)
        {
            if(item.credits_amount > 0)
            {
                try
                {
                    await rcon.giveCredits(userId, item.credits_amount);
                    logLines.push(`+${item.credits_amount} crediti`);
                }
                catch (e)
                {
                    logLines.push(`ERR crediti: ${e instanceof Error ? e.message : 'unknown'}`);
                }
            }
            if(item.diamonds_amount > 0)
            {
                try
                {
                    await rcon.giveDiamonds(userId, item.diamonds_amount);
                    logLines.push(`+${item.diamonds_amount} diamanti`);
                }
                catch (e)
                {
                    logLines.push(`ERR diamanti: ${e instanceof Error ? e.message : 'unknown'}`);
                }
            }
            if(item.badge_code)
            {
                try
                {
                    await rcon.giveBadge(userId, item.badge_code);
                    logLines.push(`+ badge ${item.badge_code}`);
                }
                catch (e)
                {
                    logLines.push(`ERR badge: ${e instanceof Error ? e.message : 'unknown'}`);
                }
            }
        }

        await dbExecute(
            'UPDATE cms_v3_shop_orders SET delivered_at = NOW(), delivery_log = ? WHERE stripe_session_id = ?',
            [logLines.join('; ').substring(0, 500), session.id]
        );
    }
    else if(event.type === 'checkout.session.expired' || event.type === 'checkout.session.async_payment_failed')
    {
        const session = event.data.object as { id: string };
        await dbExecute(
            'UPDATE cms_v3_shop_orders SET status = ? WHERE stripe_session_id = ? AND status = ?',
            ['failed', session.id, 'pending']
        );
    }

    return c.json({ received: true });
});

shop.get('/orders', requireAuth, async (c) =>
{
    const user = c.get('user') as { id: number } | undefined;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const orders = await dbQuery(
        'SELECT o.id, o.status, o.amount_cents, o.currency, o.created_at, o.paid_at, o.delivered_at, o.delivery_log, i.name AS item_name, i.image_gradient FROM cms_v3_shop_orders o JOIN cms_v3_shop_items i ON i.id = o.item_id WHERE o.user_id = ? ORDER BY o.created_at DESC LIMIT 50',
        [user.id]
    );
    return c.json({ orders });
});

shop.get('/verify/:sessionId', requireAuth, async (c) =>
{
    const sessionId = c.req.param('sessionId');
    const user = c.get('user') as { id: number } | undefined;
    if(!user) return c.json({ error: 'unauthorized' }, 401);
    const orders = await dbQuery(
        'SELECT o.id, o.status, o.amount_cents, o.currency, o.delivered_at, o.delivery_log, i.name AS item_name, i.credits_amount, i.diamonds_amount, i.badge_code FROM cms_v3_shop_orders o JOIN cms_v3_shop_items i ON i.id = o.item_id WHERE o.stripe_session_id = ? AND o.user_id = ? LIMIT 1',
        [sessionId, user.id]
    );
    if(!orders.length) return c.json({ error: 'order_not_found' }, 404);
    return c.json({ order: orders[0] });
});

export default shop;
