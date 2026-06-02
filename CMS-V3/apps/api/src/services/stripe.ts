/**
 * Stripe client singleton.
 *
 * Init lazy: la prima invocazione importa il SDK + crea il client con
 * STRIPE_SECRET_KEY. Se la key non c'è (dev env senza Stripe configurato),
 * ritorna null → le route /checkout e /webhook risponderanno 503 con
 * messaggio chiaro per l'admin.
 *
 * Test mode: usa una key `sk_test_...` da dashboard.stripe.com/test/apikeys.
 * Production: `sk_live_...` (NEVER committare in repo).
 */

import type Stripe from 'stripe';

let client: Stripe | null = null;
let initAttempted = false;

export async function getStripe(): Promise<Stripe | null>
{
    if(initAttempted) return client;
    initAttempted = true;

    const key = process.env.STRIPE_SECRET_KEY;
    if(!key || key.length < 10)
    {
        return null;
    }

    try
    {
        const { default: StripeImport } = await import('stripe');
        client = new StripeImport(key, {
            apiVersion: '2025-09-30.clover' as Stripe.LatestApiVersion,
            telemetry: false
        });
        return client;
    }
    catch (e)
    {
        // eslint-disable-next-line no-console
        console.error('[stripe] init failed', e);
        return null;
    }
}

export function getStripeWebhookSecret(): string | null
{
    const s = process.env.STRIPE_WEBHOOK_SECRET;
    return s && s.length > 10 ? s : null;
}

export function getStripePublishableKey(): string | null
{
    const k = process.env.STRIPE_PUBLISHABLE_KEY;
    return k && k.length > 10 ? k : null;
}

export function isStripeConfigured(): boolean
{
    return Boolean(process.env.STRIPE_SECRET_KEY && process.env.STRIPE_SECRET_KEY.length > 10);
}
