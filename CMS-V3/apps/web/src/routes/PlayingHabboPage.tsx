import { type ReactNode } from 'react';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /playing-habbo — "Il Mondo di Habbo".
 *
 * Intro al gameplay per nuovi e curiosi. Tematicamente è la pagina
 * "scopri cosa puoi fare". Replica del concetto habbo.it ma con
 * contenuto Habboproject-specific.
 */
export function PlayingHabboPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;

    const body = (
        <main className="wrapper wrapper--content">
            <section>
                <habbo-compile className="main main--fixed">
                    <article>
                        <header className="news-header news-header--single">
                            <h1 className="news-header__wrapper news-header__title">Il Mondo di Habboproject</h1>
                            <p className="news-header__wrapper news-header__summary">
                                Un hotel virtuale italiano, pixel-art, gratis, dal 2026.
                            </p>
                        </header>
                        <div className="news-article">
                            <h2>🏨 Cosa è</h2>
                            <p>Habboproject è un mondo virtuale 2.5D isometrico in pixel-art, ispirato a Habbo Hotel
                            originale. Crei un avatar, esplori stanze fatte dalla community, chatti, fai amicizia,
                            costruisci la tua casa.</p>

                            <h2>👤 Avatar &amp; Look</h2>
                            <p>Personalizza completamente: capelli, viso, vestiti, accessori. Più di 1500
                            combinazioni di abbigliamento. Lo cambi quando vuoi dal guardaroba.</p>

                            <h2>🏠 Stanze</h2>
                            <p>Crea fino a 20 stanze. Scegli il layout, posiziona furni dal catalogo, configura
                            mosse wired, password, accessi. Le stanze pubbliche più popolari vanno in vetrina su
                            <a href="/community/rooms"> Galleria Stanze</a>.</p>

                            <h2>💬 Chat &amp; Comandi</h2>
                            <p>Chat normale + 140+ comandi in italiano (vedi <a href="/community/article/comandi-italiani">l'articolo dedicato</a>).
                            Bolle chat personalizzabili (regalo del Daily Streak).</p>

                            <h2>🤝 Social</h2>
                            <p>Amici, messaggi privati, gruppi/guild con forum interno, eventi staff settimanali,
                            Battle Pass, achievements, sistema reputazione (respect).</p>

                            <h2>📸 Camera</h2>
                            <p>Scatta foto delle tue stanze e dei momenti epici. Tutte le foto vanno in
                            <a href="/community/photos"> Foto da Habbo</a> con likes e commenti.</p>

                            <h2>🎯 Sistemi unici Habboproject</h2>
                            <ul>
                                <li><strong>Daily Streak</strong>: ricompensa giornaliera che cresce</li>
                                <li><strong>Bot Oracolo</strong>: proponi feature da una stanza</li>
                                <li><strong>Anti-multiaccount</strong>: fair-play garantito via fingerprint</li>
                                <li><strong>Trade Safety Lock</strong>: niente scam negli scambi</li>
                                <li><strong>Notifiche browser</strong>: ti avviso anche fuori dal client</li>
                            </ul>
                        </div>
                    </article>
                </habbo-compile>
                <habbo-web-pages className="aside aside--box aside--fixed">
                    <aside className="static-content">
                        <h3>Inizia a giocare</h3>
                        <p>{user
                            ? <a href="/gioca"><strong>Entra nell'Hotel →</strong></a>
                            : <a href="/registration"><strong>Iscriviti gratis →</strong></a>}
                        </p>
                    </aside>
                </habbo-web-pages>
            </section>
        </main>
    );

    if(user) return <AuthedShell user={user}>{body}</AuthedShell>;
    return (
        <>
            <div className="content">
                <header className="header__wrapper wrapper">
                    <a href="/" className="header__habbo__logo">
                        <h1 className="header__habbo__name">Habbo</h1>
                    </a>
                </header>
                {body}
            </div>
        </>
    );
}
