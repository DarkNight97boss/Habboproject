import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate, useSearchParams } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

interface VerifyResponse {
    order: {
        id: number;
        status: 'pending' | 'paid' | 'failed' | 'refunded';
        amount_cents: number;
        currency: string;
        delivered_at: string | null;
        delivery_log: string | null;
        item_name: string;
        credits_amount: number;
        diamonds_amount: number;
        badge_code: string | null;
    };
}

/** /shop/success?session_id=... — landing dopo checkout riuscito. */
export function AsteriaShopSuccessPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const [params] = useSearchParams();
    const sessionId = params.get('session_id') || '';

    const { data, isLoading } = useQuery<VerifyResponse>({
        queryKey: ['shop', 'verify', sessionId],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/shop/verify/${encodeURIComponent(sessionId)}`, { credentials: 'include' });
            if(!r.ok) throw new Error('verify_failed');
            return r.json();
        },
        enabled: !!sessionId && !!user,
        // Polling fino a quando paid (webhook arriva in qualche secondo)
        refetchInterval: (q) =>
        {
            const status = q.state.data?.order?.status;
            return status === 'paid' || status === 'failed' ? false : 2000;
        },
        retry: 5
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/login" replace />;
    if(!sessionId) return <Navigate to="/shop" replace />;

    const order = data?.order;
    const isPaid = order?.status === 'paid';
    const isPending = !order || order.status === 'pending';

    return (
        <AsteriaShell activeNav="shop" hideMarquee>
            <section className="asteria-hero" style={{ textAlign: 'center', paddingTop: 80 }}>
                {isPending && <PendingState />}
                {isPaid && order && <SuccessState order={order} />}
                {order?.status === 'failed' && <FailedState />}
            </section>
        </AsteriaShell>
    );
}

function PendingState(): ReactNode
{
    return (
        <>
            <div className="asteria-hero__eyebrow">Pagamento in elaborazione</div>
            <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>Quasi fatto.</h1>
            <p className="asteria-hero__tagline">
                Stiamo verificando il pagamento con Stripe. La consegna automatica avviene entro pochi secondi.
            </p>
            <div className="asteria-loader">
                <div className="asteria-loader__bar" />
            </div>
        </>
    );
}

function SuccessState({ order }: { order: VerifyResponse['order'] }): ReactNode
{
    return (
        <>
            <div className="asteria-hero__eyebrow" style={{ background: 'rgba(16,185,129,0.20)', borderColor: 'rgba(16,185,129,0.40)', color: '#6ee7b7' }}>
                ✓ Pagamento ricevuto
            </div>
            <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>
                Grazie!
            </h1>
            <p className="asteria-hero__tagline">
                <strong>{order.item_name}</strong> è stato consegnato sul tuo account.
                <br />Ricevi:
            </p>
            <div className="asteria-shop__success-list">
                {order.credits_amount > 0 && <div><strong>+ {order.credits_amount.toLocaleString('it-IT')}</strong> crediti</div>}
                {order.diamonds_amount > 0 && <div><strong>+ {order.diamonds_amount.toLocaleString('it-IT')}</strong> diamanti</div>}
                {order.badge_code && <div><strong>+ Badge</strong> {order.badge_code}</div>}
            </div>
            <div className="asteria-hero__cta" style={{ marginTop: 32 }}>
                <a href="/gioca" className="asteria-btn asteria-btn--xl">▶ Entra nel hotel</a>
                <a href="/me" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Vedi il mio account</a>
            </div>
        </>
    );
}

function FailedState(): ReactNode
{
    return (
        <>
            <div className="asteria-hero__eyebrow" style={{ background: 'rgba(244,63,94,0.20)', borderColor: 'rgba(244,63,94,0.40)', color: '#fda4af' }}>
                ✗ Pagamento fallito
            </div>
            <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>Ops.</h1>
            <p className="asteria-hero__tagline">
                Il pagamento non è andato a buon fine. Non ti è stato addebitato nulla.
            </p>
            <div className="asteria-hero__cta">
                <a href="/shop" className="asteria-btn asteria-btn--xl">Torna allo shop</a>
                <a href="/help" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Contatta supporto</a>
            </div>
        </>
    );
}

/** /shop/cancel — Stripe redirige qui se l'utente abbandona checkout. */
export function AsteriaShopCancelPage(): ReactNode
{
    return (
        <AsteriaShell activeNav="shop" hideMarquee>
            <section className="asteria-hero" style={{ textAlign: 'center', paddingTop: 80 }}>
                <div className="asteria-hero__eyebrow">Acquisto annullato</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>
                    Quando<br />vuoi.
                </h1>
                <p className="asteria-hero__tagline">
                    Niente di addebitato. Il carrello è ancora qui se cambi idea.
                </p>
                <div className="asteria-hero__cta">
                    <a href="/shop" className="asteria-btn asteria-btn--xl">Torna allo shop</a>
                    <a href="/" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Home</a>
                </div>
            </section>
        </AsteriaShell>
    );
}
