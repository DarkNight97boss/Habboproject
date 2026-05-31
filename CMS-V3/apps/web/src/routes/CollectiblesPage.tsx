import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /habbo-nft — "Collezionabili".
 *
 * L'ufficiale habbo.it ha /habbo-nft con marketplace NFT su blockchain.
 * Habboproject DICE NO agli NFT: il punto è giocare, non speculare.
 * Mostriamo invece la collezione di rari/LTD/badges interni.
 */
export function CollectiblesPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user}>
            <main className="wrapper wrapper--content">
                <section>
                    <habbo-compile className="main main--fixed">
                        <article>
                            <header className="news-header news-header--single">
                                <h1 className="news-header__wrapper news-header__title">Collezionabili Habboproject</h1>
                                <p className="news-header__wrapper news-header__summary">
                                    Rari, LTD, badge — niente NFT, niente blockchain, niente speculazione.
                                </p>
                            </header>
                            <div className="news-article">
                                <h2>La nostra posizione su NFT</h2>
                                <p>L'ufficiale habbo.it ha lanciato nel 2021 una marketplace NFT che ha generato
                                molte critiche dalla community. <strong>Habboproject NON usa blockchain</strong>:
                                tutti i nostri rari sono on-chain... pixel-on-chain del database MariaDB, e
                                stanno lì gratuitamente.</p>

                                <h2>🎴 Rari & LTD</h2>
                                <p>Lo staff distribuisce mensilmente furni Limited Edition (LTD) tramite eventi.
                                Numero stampe pubblico, ogni copia ha un seriale univoco. Sono scambiabili
                                tra utenti via il trade in-game (con Trade Safety Lock attivo).</p>

                                <h2>🏅 Badge</h2>
                                <p>Più di 200 badge da sbloccare:</p>
                                <ul>
                                    <li>Achievement (categorie social/builder/helper/explorer/gamer)</li>
                                    <li>Eventi staff (partecipazione, vittoria)</li>
                                    <li>Anniversari iscrizione (1 anno, 5 anni, 10 anni)</li>
                                    <li>Stagione Battle Pass (oggetti cosmetici esclusivi)</li>
                                </ul>

                                <h2>🌸 Furni del catalogo</h2>
                                <p>Oltre 5000 furni nel <a href="/shop">catalogo</a> — comprali con i crediti
                                guadagnati giocando. Il furni è binding sull'account ma puoi regalarlo
                                ad un amico, o scambiarlo nel trade.</p>

                                <h2>📊 Trasparenza</h2>
                                <p>Ogni distribuzione di LTD/rari è loggata nel database con timestamp,
                                staff member responsabile, e numero stampe. Lista pubblica in arrivo nel
                                pannello <a href="/community/forum">Forum &gt; Annunci LTD</a>.</p>
                            </div>
                        </article>
                    </habbo-compile>
                    <habbo-web-pages className="aside aside--box aside--fixed">
                        <aside className="static-content">
                            <h3>Le mie collezioni</h3>
                            <p>Il tuo inventario è dentro il <a href="/gioca"><strong>Client</strong></a> &gt;
                            Inventario. Lì vedi furni, badge, sticky notes, dispenser.</p>
                        </aside>
                    </habbo-web-pages>
                </section>
            </main>
        </AuthedShell>
    );
}
