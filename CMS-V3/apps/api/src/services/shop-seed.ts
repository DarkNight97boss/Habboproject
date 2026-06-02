/**
 * Seed dei pacchetti shop al boot. Idempotente.
 *
 * Pacchetti default:
 *  - 5 pacchetti crediti (small → mega) con bonus diamanti scaling
 *  - 2 pacchetti diamanti only
 *  - 1 abbonamento HC mensile
 *
 * Per integrare con Stripe REAL: lo staff crea i Price corrispondenti sul
 * dashboard Stripe e popola `stripe_price_id` via UPDATE manuale (oppure
 * il tool admin). Senza price_id → checkout dynamic price (price_data inline).
 */

import { dbExecute } from '../db/pool.js';

interface ShopItem {
    slug: string;
    name: string;
    description: string;
    category: 'credits' | 'diamonds' | 'subscription' | 'bundle';
    price_cents: number;
    currency: string;
    credits_amount: number;
    diamonds_amount: number;
    image_gradient: string;
    badge_code: string | null;
    featured: number;
    sort_order: number;
}

const ITEMS: ShopItem[] = [
    {
        slug: 'credits-starter',
        name: 'Starter Pack',
        description: '1.000 crediti + 50 diamanti bonus. Perfetto per iniziare.',
        category: 'credits',
        price_cents: 299, currency: 'EUR',
        credits_amount: 1000, diamonds_amount: 50,
        image_gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
        badge_code: null, featured: 0, sort_order: 10
    },
    {
        slug: 'credits-pro',
        name: 'Pro Pack',
        description: '5.000 crediti + 300 diamanti + badge esclusivo "PRO".',
        category: 'credits',
        price_cents: 999, currency: 'EUR',
        credits_amount: 5000, diamonds_amount: 300,
        image_gradient: 'linear-gradient(135deg, #a855f7 0%, #ec4899 100%)',
        badge_code: 'PRO', featured: 1, sort_order: 20
    },
    {
        slug: 'credits-mega',
        name: 'Mega Pack',
        description: '15.000 crediti + 1.200 diamanti + badge "MEGA" + tema Asteria Pro.',
        category: 'credits',
        price_cents: 2499, currency: 'EUR',
        credits_amount: 15000, diamonds_amount: 1200,
        image_gradient: 'linear-gradient(135deg, #f59e0b 0%, #ec4899 50%, #a855f7 100%)',
        badge_code: 'MEGA', featured: 1, sort_order: 30
    },
    {
        slug: 'credits-titan',
        name: 'Titan Pack',
        description: '50.000 crediti + 5.000 diamanti + badge "TITAN" + room slot extra.',
        category: 'credits',
        price_cents: 6999, currency: 'EUR',
        credits_amount: 50000, diamonds_amount: 5000,
        image_gradient: 'linear-gradient(135deg, #1e40af 0%, #a855f7 100%)',
        badge_code: 'TITAN', featured: 0, sort_order: 40
    },
    {
        slug: 'diamonds-pouch',
        name: 'Sacca Diamanti',
        description: '500 diamanti per shop esclusivo + scambi premium.',
        category: 'diamonds',
        price_cents: 499, currency: 'EUR',
        credits_amount: 0, diamonds_amount: 500,
        image_gradient: 'linear-gradient(135deg, #22d3ee 0%, #8b5cf6 100%)',
        badge_code: null, featured: 0, sort_order: 50
    },
    {
        slug: 'diamonds-vault',
        name: 'Cassa Diamanti',
        description: '3.000 diamanti + badge "VAULT". La valuta premium ai massimi.',
        category: 'diamonds',
        price_cents: 1999, currency: 'EUR',
        credits_amount: 0, diamonds_amount: 3000,
        image_gradient: 'linear-gradient(135deg, #06b6d4 0%, #a855f7 50%, #ec4899 100%)',
        badge_code: 'VAULT', featured: 0, sort_order: 60
    },
    {
        slug: 'hc-month',
        name: 'Habbo Club · 1 mese',
        description: '30 giorni di HC: avatar premium, slot stanze +5, badge HC, daily bonus 50 crediti.',
        category: 'subscription',
        price_cents: 599, currency: 'EUR',
        credits_amount: 0, diamonds_amount: 0,
        image_gradient: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 50%, #ef4444 100%)',
        badge_code: 'HC1', featured: 1, sort_order: 5
    },
    {
        slug: 'bundle-creator',
        name: 'Creator Bundle',
        description: 'Tutto per builder: 10.000 crediti + 500 diamanti + room slot ×3 + badge CREATOR.',
        category: 'bundle',
        price_cents: 1499, currency: 'EUR',
        credits_amount: 10000, diamonds_amount: 500,
        image_gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 50%, #6366f1 100%)',
        badge_code: 'CREATOR', featured: 0, sort_order: 25
    }
];

export async function ensureShopSeeded(): Promise<void>
{
    for(const it of ITEMS)
    {
        await dbExecute(
            'INSERT INTO cms_v3_shop_items (slug, name, description, category, price_cents, currency, credits_amount, diamonds_amount, image_gradient, badge_code, featured, sort_order) ' +
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ' +
            'ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), category=VALUES(category), price_cents=VALUES(price_cents), credits_amount=VALUES(credits_amount), diamonds_amount=VALUES(diamonds_amount), image_gradient=VALUES(image_gradient), badge_code=VALUES(badge_code), featured=VALUES(featured), sort_order=VALUES(sort_order)',
            [it.slug, it.name, it.description, it.category, it.price_cents, it.currency, it.credits_amount, it.diamonds_amount, it.image_gradient, it.badge_code, it.featured, it.sort_order]
        );
    }
}
