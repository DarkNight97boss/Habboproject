import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { type AuthUser, avatarUrl, useAuth } from '../hooks/useAuth';
import { AsteriaNewsBento } from './AsteriaNewsBento';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Dashboard /me Asteria — vista premium del proprio account.
 *
 *  - Hero personale con avatar grande + username + motto + rank badge
 *  - Card statistiche (crediti, diamanti, online time, friends, rooms)
 *  - Quick actions per le destinazioni più visitate
 *  - Recent activity feed
 *  - Order history shop (sintesi)
 *
 * Solo auth — redirect a /login se anon.
 */
export function AsteriaMePage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;
    if(!user) return <Navigate to="/login" replace />;
    return (
        <AsteriaShell activeNav="home">
            <MePanel user={user} />
            <MeStats user={user} />
            <MeQuickActions />
            <AsteriaNewsBento title="Ultime news" subtitle="Cosa succede in Asteria" />
            <MeOrders />
        </AsteriaShell>
    );
}

function MePanel({ user }: { user: AuthUser }): ReactNode
{
    return (
        <section className="asteria-me__hero">
            <div className="asteria-me__avatar-frame">
                <img src={avatarUrl(user.look, { size: 'l' })} alt="" />
            </div>
            <div className="asteria-me__info">
                <div className="asteria-hero__eyebrow">Il tuo account</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 7vw, 96px)', margin: 0 }}>
                    {user.username}
                </h1>
                <p className="asteria-me__motto">{user.motto || '— motto vuoto —'}</p>
                <div className="asteria-me__badges">
                    <span className="asteria-me__badge asteria-me__badge--rank">RANK {user.rank}</span>
                    {user.rank >= 5 && <span className="asteria-me__badge asteria-me__badge--staff">STAFF</span>}
                </div>
                <div className="asteria-hero__cta" style={{ marginTop: 24 }}>
                    <a href="/api/v2/auth/play" className="asteria-btn asteria-btn--xl">▶ Entra nel hotel</a>
                    <a href={`/profile/${user.username}`} className="asteria-btn asteria-btn--ghost asteria-btn--xl">Profilo pubblico</a>
                </div>
            </div>
        </section>
    );
}

function MeStats({ user }: { user: AuthUser }): ReactNode
{
    return (
        <div className="asteria-stats">
            <div className="asteria-stat">
                <div className="asteria-stat__value">{user.credits.toLocaleString('it-IT')}</div>
                <div className="asteria-stat__label">Crediti</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">--</div>
                <div className="asteria-stat__label">Diamanti</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">{formatDate(user.account_created)}</div>
                <div className="asteria-stat__label">Iscritto dal</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">{relativeFrom(user.last_online)}</div>
                <div className="asteria-stat__label">Ultima volta</div>
            </div>
        </div>
    );
}

function MeQuickActions(): ReactNode
{
    const actions = [
        { icon: '◉', label: 'Profilo pubblico', href: '/profile' },
        { icon: '⌂', label: 'Le mie stanze', href: '/community/rooms' },
        { icon: '⊕', label: 'Compra crediti', href: '/shop' },
        { icon: '⚙', label: 'Impostazioni', href: '/settings/privacy' },
        { icon: '✉', label: 'Messaggi', href: '/messaging' },
        { icon: '✦', label: 'Collezionabili', href: '/habbo-nft' }
    ];
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">Azioni rapide</h2>
                <span className="asteria-section__subtitle">Tutto a portata di mano</span>
            </div>
            <div className="asteria-quick">
                {actions.map(a => (
                    <a key={a.label} href={a.href}>
                        <span className="asteria-quick__icon">{a.icon}</span>
                        <span>{a.label}</span>
                    </a>
                ))}
            </div>
        </div>
    );
}

interface OrderRow {
    id: number;
    status: string;
    amount_cents: number;
    currency: string;
    created_at: string;
    delivered_at: string | null;
    delivery_log: string | null;
    item_name: string;
    image_gradient: string;
}

function MeOrders(): ReactNode
{
    const { data, isLoading } = useQuery<{ orders: OrderRow[] }>({
        queryKey: ['me', 'orders'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/shop/orders', { credentials: 'include' });
            if(!r.ok) throw new Error('orders_fetch_failed');
            return r.json();
        },
        staleTime: 30_000
    });
    if(isLoading) return null;
    const orders = data?.orders || [];
    if(!orders.length) return null;
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">I tuoi acquisti</h2>
                <a href="/shop" className="asteria-section__subtitle" style={{ textDecoration: 'none', color: '#a855f7' }}>VAI ALLO SHOP ↗</a>
            </div>
            <div className="asteria-orders">
                {orders.slice(0, 6).map(o => (
                    <div key={o.id} className="asteria-order">
                        <div className="asteria-order__cover" style={{ background: o.image_gradient }} />
                        <div className="asteria-order__body">
                            <div className="asteria-order__name">{o.item_name}</div>
                            <div className="asteria-order__meta">
                                {(o.amount_cents / 100).toFixed(2)} {o.currency} · {formatDate(Math.floor(new Date(o.created_at).getTime() / 1000))}
                            </div>
                            <span className={`asteria-order__status asteria-order__status--${o.status}`}>{translateStatus(o.status)}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function translateStatus(s: string): string
{
    return { pending: 'In attesa', paid: 'Pagato', failed: 'Fallito', refunded: 'Rimborsato' }[s] || s;
}

function formatDate(unixSeconds: number): string
{
    if(!unixSeconds) return '—';
    const d = new Date(unixSeconds * 1000);
    return d.toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' });
}

function relativeFrom(unixSeconds: number): string
{
    if(!unixSeconds) return '—';
    const ageMs = Date.now() - unixSeconds * 1000;
    if(ageMs < 60_000) return 'ora';
    if(ageMs < 3_600_000) return Math.floor(ageMs / 60_000) + ' min fa';
    if(ageMs < 86_400_000) return Math.floor(ageMs / 3_600_000) + ' h fa';
    if(ageMs < 30 * 86_400_000) return Math.floor(ageMs / 86_400_000) + ' g fa';
    return formatDate(unixSeconds);
}
