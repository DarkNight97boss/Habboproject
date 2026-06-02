import { type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, Navigate } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { useAuth } from '../hooks/useAuth';
import './asteria.css';

/** /shop/acquisti — cronologia ordini. GET /api/v2/shop/orders (auth) */
interface Order {
    id: number; status: 'pending' | 'paid' | 'failed' | 'refunded';
    amount_cents: number; currency: string; created_at: string;
    paid_at: string | null; delivered_at: string | null; delivery_log: string | null;
    item_name: string; image_gradient: string;
}

const STATUS: Record<Order['status'], string> = { pending: 'In attesa', paid: 'Pagato', failed: 'Fallito', refunded: 'Rimborsato' };

function fmt(ts: string): string
{
    const d = new Date(ts);
    return isNaN(d.getTime()) ? ts : d.toLocaleDateString('it-IT', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function AsteriaPurchasesPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const { data, isLoading } = useQuery<Order[]>({
        queryKey: ['shop', 'orders'],
        queryFn: async ({ signal }) =>
        {
            const r = await fetch('/api/v2/shop/orders', { credentials: 'include', signal });
            if(!r.ok) throw new Error('orders_' + r.status);
            return (await r.json() as { orders: Order[] }).orders;
        },
        enabled: !!user
    });

    if(authLoading) return <AsteriaShell activeNav="shop"><div className="asteria-news__state">Caricamento…</div></AsteriaShell>;
    if(!user) return <Navigate to="/login" replace />;

    const orders = data ?? [];
    return (
        <AsteriaShell activeNav="shop">
            <section className="asteria-news">
                <header className="asteria-news__head">
                    <div className="asteria-hero__eyebrow">Shop · Acquisti</div>
                    <h1 className="asteria-news__h1">I tuoi acquisti</h1>
                    <p className="asteria-news__sub">Cronologia ordini e consegne.</p>
                </header>
                {isLoading && <div className="asteria-news__state">Caricamento ordini…</div>}
                {!isLoading && orders.length === 0 && (
                    <div className="asteria-news__state">
                        Non hai ancora effettuato acquisti.<br />
                        <Link to="/shop" className="asteria-btn" style={{ marginTop: 16, display: 'inline-flex' }}>Vai allo shop</Link>
                    </div>
                )}
                <div className="asteria-orders">
                    {orders.map(o => (
                        <div key={o.id} className="asteria-order">
                            <div className="asteria-order__cover" style={{ background: o.image_gradient || 'linear-gradient(135deg,#a855f7,#06b6d4)' }} />
                            <div className="asteria-order__body">
                                <div className="asteria-order__name">{o.item_name}</div>
                                <div className="asteria-order__meta">€{(o.amount_cents / 100).toFixed(2)} · {fmt(o.created_at)}</div>
                                <span className={`asteria-order__status asteria-order__status--${o.status}`}>{STATUS[o.status]}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </section>
        </AsteriaShell>
    );
}
