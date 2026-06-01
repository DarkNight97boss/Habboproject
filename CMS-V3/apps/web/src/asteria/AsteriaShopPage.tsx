import { useMutation, useQuery } from '@tanstack/react-query';
import { type ReactNode, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Pagina /shop Asteria proprietaria con Stripe Checkout.
 *
 * Layout:
 *  - Hero header "Pacchetti Asteria"
 *  - Tabs filtro categoria (Crediti / Diamanti / HC / Bundle / Tutti)
 *  - Grid prodotti con card premium gradient cover + prezzo prominente
 *  - Click "Compra" → POST /api/v2/shop/checkout → redirect a Stripe Checkout
 *  - Anon → redirect a /login con returnTo=/shop
 */

interface ShopItem {
    id: number;
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

const CATEGORIES: { id: ShopItem['category'] | 'all'; label: string; icon: string }[] = [
    { id: 'all', label: 'Tutti', icon: '✦' },
    { id: 'credits', label: 'Crediti', icon: '◉' },
    { id: 'diamonds', label: 'Diamanti', icon: '◆' },
    { id: 'subscription', label: 'Habbo Club', icon: '★' },
    { id: 'bundle', label: 'Bundle', icon: '⊕' }
];

export function AsteriaShopPage(): ReactNode
{
    const { data: user } = useAuth();
    const [filter, setFilter] = useState<ShopItem['category'] | 'all'>('all');
    const { data, isLoading } = useQuery<{ items: ShopItem[] }>({
        queryKey: ['shop', 'items'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/shop/items', { credentials: 'include' });
            if(!r.ok) throw new Error('shop_fetch_failed');
            return r.json();
        },
        staleTime: 5 * 60_000
    });

    const checkout = useMutation<{ url: string }, Error, string>({
        mutationFn: async (slug: string) =>
        {
            const r = await fetch('/api/v2/shop/checkout', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ slug })
            });
            const j = await r.json();
            if(!r.ok) throw new Error(j.error || 'checkout_failed');
            return j;
        },
        onSuccess: ({ url }) => { if(url) window.location.href = url; }
    });

    const items = (data?.items || []).filter(i => filter === 'all' || i.category === filter);

    function buy(slug: string): void
    {
        if(!user) { window.location.href = `/login?next=/shop`; return; }
        checkout.mutate(slug);
    }

    return (
        <AsteriaShell activeNav="shop">
            <section className="asteria-hero" style={{ paddingBottom: 40 }}>
                <div className="asteria-hero__eyebrow">Shop · Pagamento sicuro Stripe</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(48px, 10vw, 140px)' }}>
                    Potenzia<br />
                    <span className="asteria-hero__title__outline">la tua esperienza.</span>
                </h1>
                <p className="asteria-hero__tagline">
                    Crediti, diamanti, Habbo Club e bundle esclusivi. Tutti i pacchetti includono badge a vita.
                    <br />Pagamento istantaneo, consegna automatica.
                </p>
            </section>

            <div className="asteria-section">
                <div className="asteria-shop__filters">
                    {CATEGORIES.map(c => (
                        <button key={c.id} type="button"
                            className={`asteria-shop__filter ${filter === c.id ? 'is-active' : ''}`}
                            onClick={() => setFilter(c.id)}>
                            <span>{c.icon}</span> {c.label}
                        </button>
                    ))}
                </div>

                {checkout.isError && (
                    <div className="asteria-error" style={{ marginBottom: 20 }}>
                        {checkout.error.message === 'stripe_not_configured'
                            ? 'Pagamenti non ancora attivi. Lo staff sta configurando Stripe.'
                            : `Errore: ${checkout.error.message}`}
                    </div>
                )}

                {isLoading ? (
                    <p style={{ color: '#94a3b8', textAlign: 'center', padding: 40 }}>Caricamento catalogo…</p>
                ) : (
                    <div className="asteria-shop__grid">
                        {items.map(item => (
                            <ShopCard key={item.id} item={item}
                                onBuy={() => buy(item.slug)}
                                loading={checkout.isPending} />
                        ))}
                    </div>
                )}
            </div>
        </AsteriaShell>
    );
}

function ShopCard({ item, onBuy, loading }: { item: ShopItem; onBuy: () => void; loading: boolean }): ReactNode
{
    const price = (item.price_cents / 100).toFixed(2);
    return (
        <div className={`asteria-shop__card ${item.featured ? 'is-featured' : ''}`}>
            {item.featured > 0 && <div className="asteria-shop__featured-tag">★ Bestseller</div>}
            <div className="asteria-shop__cover" style={{ background: item.image_gradient }}>
                <div className="asteria-shop__cover-amount">
                    {item.credits_amount > 0 && <div><strong>{item.credits_amount.toLocaleString('it-IT')}</strong><span>crediti</span></div>}
                    {item.diamonds_amount > 0 && <div><strong>{item.diamonds_amount.toLocaleString('it-IT')}</strong><span>diamanti</span></div>}
                    {item.badge_code && <div className="asteria-shop__badge-chip">+ Badge {item.badge_code}</div>}
                </div>
            </div>
            <div className="asteria-shop__body">
                <div className="asteria-shop__cat">{categoryLabel(item.category)}</div>
                <h3 className="asteria-shop__name">{item.name}</h3>
                <p className="asteria-shop__desc">{item.description}</p>
                <div className="asteria-shop__footer">
                    <div className="asteria-shop__price">
                        <span className="asteria-shop__price-amount">€{price}</span>
                        <span className="asteria-shop__price-currency">{item.currency}</span>
                    </div>
                    <button type="button" onClick={onBuy} disabled={loading}
                        className="asteria-btn">
                        {loading ? 'Apertura…' : 'Compra ↗'}
                    </button>
                </div>
            </div>
        </div>
    );
}

function categoryLabel(c: ShopItem['category']): string
{
    return { credits: 'CREDITI', diamonds: 'DIAMANTI', subscription: 'ABBONAMENTO', bundle: 'BUNDLE' }[c];
}
