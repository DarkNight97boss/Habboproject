import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell, ShopTabs } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * /shop/acquisti — sub-tab "I miei acquisti".
 *
 * Cronologia transazioni dell'utente. Per ora empty state — quando
 * implementeremo Stripe/codici/PayPal pagamenti reali sostituiamo
 * con backend GET /api/v2/me/purchases.
 */
export function ShopPurchasesPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user} tabs={<ShopTabs active="i-miei-acquisti" />}>
            <main className="wrapper wrapper--content">
                <header className="shop__header">
                    <h1 className="shop__header__title">Cronologia acquisti</h1>
                </header>
                <section>
                    <habbo-compile className="main main--fixed">
                        <habbo-empty-results>
                            <span>Non hai ancora effettuato acquisti su Habboproject.</span>
                        </habbo-empty-results>
                    </habbo-compile>
                    <habbo-web-pages className="aside aside--box aside--fixed">
                        <aside className="static-content">
                            <h3>Come iniziare</h3>
                            <p>
                                Su Habboproject non si compra nulla con soldi reali.
                                <a href="/shop" style={{ color: '#fbd33f' }}> Vedi come guadagnare crediti giocando</a>.
                            </p>
                        </aside>
                    </habbo-web-pages>
                </section>
            </main>
        </AuthedShell>
    );
}
