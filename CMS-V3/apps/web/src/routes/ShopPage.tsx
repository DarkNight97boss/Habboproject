import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { type AuthUser, useAuth } from '../hooks/useAuth';

/**
 * Pagina /shop — versione Habboproject.
 *
 * L'ufficiale habbo.it ha un catalogo di pacchetti crediti/diamanti
 * acquistabili con soldi reali. Habboproject è GRATIS al 100% — niente
 * microtransazioni, niente carte di credito. Quindi rimpiazziamo lo
 * shop con un riassunto "Come guadagnare valuta nel gioco" + borsellino
 * personale lato sidebar.
 */
export function ShopPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user}>
            <main className="wrapper wrapper--content">
                <header className="shop__header">
                    <h1 className="shop__header__title">Habboproject è gratis al 100%</h1>
                </header>
                <section>
                    <habbo-compile className="main main--fixed">
                        <article>
                            <header className="news-header news-header--single">
                                <h1 className="news-header__wrapper news-header__title">Come guadagnare crediti e diamanti</h1>
                                <p className="news-header__wrapper news-header__summary">
                                    Niente carte di credito, niente microtransazioni. Solo gioco.
                                </p>
                            </header>
                            <div className="news-article">
                                <h2>🔥 Daily Streak</h2>
                                <p>Entra nel client ogni giorno → riscuoti la ricompensa giornaliera:</p>
                                <ul>
                                    <li>Giorno 1: <strong>50 crediti</strong></li>
                                    <li>Giorno 2: <strong>100 crediti</strong></li>
                                    <li>Giorno 3: <strong>50 duckets</strong></li>
                                    <li>Giorno 4: <strong>150 crediti</strong></li>
                                    <li>Giorno 5: <strong>1 diamante 💎</strong></li>
                                    <li>Giorno 6: <strong>200 duckets</strong></li>
                                    <li>Giorno 7: <strong>500 crediti</strong> (reset, ricomincia)</li>
                                </ul>
                                <h2>🏆 Achievements</h2>
                                <p>Sbloccare achievement = crediti + badge. Categorie disponibili: social,
                                builder, helper, explorer, gamer. Vedi i tuoi a click avatar &gt; Achievements.</p>
                                <h2>⏰ Eventi staff</h2>
                                <p>Lo staff organizza eventi (quiz, gare, party tematici) con premi
                                in valuta + furni rari. Segui le notizie su <a href="/community/category/campagne-attivita">Campagne & Attività</a>.</p>
                                <h2>🎁 Codici prepagati</h2>
                                <p>Occasionalmente distribuiamo codici prepagati su Discord/social per i nostri
                                supporter. Inseriscili nel campo sulla destra.</p>
                            </div>
                        </article>
                    </habbo-compile>
                    <Sidebar user={user} />
                </section>
            </main>
        </AuthedShell>
    );
}

function Sidebar({ user }: { user: AuthUser }): ReactNode
{
    return (
        <habbo-web-pages className="aside aside--box aside--fixed">
            <aside className="static-content">
                <h3>Il mio borsellino</h3>
                <p>
                    🟡 <strong>{user.credits ?? 0}</strong> crediti<br />
                    💎 <strong>—</strong> diamanti<br />
                    🦆 <strong>—</strong> duckets
                </p>
                <h3>Hai un codice?</h3>
                <p>
                    <input type="text" placeholder="Inserisci codice…" style={{ width: '100%', padding: '6px', borderRadius: 3, border: '2px solid #275d8e' }} />
                    <button type="button" style={{ marginTop: 6, width: '100%', padding: '8px', background: '#00813e', color: 'white', border: 'none', borderRadius: 3, fontWeight: 'bold' }}>CONVERTI</button>
                </p>
            </aside>
        </habbo-web-pages>
    );
}
