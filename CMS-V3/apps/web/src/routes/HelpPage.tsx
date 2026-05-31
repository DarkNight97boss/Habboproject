import { type ReactNode } from 'react';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /help — knowledge base / FAQ / contact.
 *
 * Layout 2-col: indice categorie a sinistra + box contatto a destra.
 */
export function HelpPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;

    const body = (
        <main className="wrapper wrapper--content">
            <section>
                <habbo-compile className="main main--fixed">
                    <article>
                        <header className="news-header news-header--single">
                            <h1 className="news-header__wrapper news-header__title">Centro Aiuto</h1>
                            <p className="news-header__wrapper news-header__summary">
                                Risposte alle domande più frequenti. Se non trovi quello che cerchi, contatta lo staff.
                            </p>
                        </header>
                        <div className="news-article">
                            <h2>🚀 Inizio rapido</h2>
                            <ul>
                                <li><a href="/registration"><strong>Come registrarsi</strong></a> — crea un account in 30 secondi</li>
                                <li><strong>Come entrare nell'hotel</strong> — clicca <a href="/gioca">GIOCA</a> in alto a destra</li>
                                <li><strong>Come creare la prima stanza</strong> — Navigator &gt; Crea stanza nel client</li>
                                <li><strong>Come scattare una foto</strong> — Toolbar &gt; Camera nel client</li>
                            </ul>

                            <h2>🔐 Account</h2>
                            <ul>
                                <li><a href="/settings/password"><strong>Cambio password</strong></a></li>
                                <li><a href="/settings/email"><strong>Cambio email</strong></a></li>
                                <li><a href="/settings/motto"><strong>Cambio motto</strong></a></li>
                                <li><a href="/settings/privacy"><strong>Visibilità profilo &amp; privacy</strong></a></li>
                            </ul>

                            <h2>💰 Crediti, diamanti, duckets</h2>
                            <ul>
                                <li><a href="/shop"><strong>Come guadagnare valuta</strong></a> — Daily Streak, achievements, eventi</li>
                                <li><strong>Codici prepagati</strong> — distribuiti dallo staff su Discord</li>
                                <li><strong>Trade Safety Lock</strong> — proteggi i tuoi scambi da scam</li>
                            </ul>

                            <h2>🛡️ Moderazione e sicurezza</h2>
                            <ul>
                                <li><a href="/playing-habbo/safety"><strong>Regole d'oro per la sicurezza</strong></a></li>
                                <li><strong>Segnalare un abuso</strong> — usa <code>:cfh</code> in chat o il bottone "Supporto al Giocatore" in basso a sinistra del client</li>
                                <li><strong>Appellare un ban</strong> — <code>:cfh-appello</code> in chat entro 30 giorni</li>
                                <li><strong>Anti-link filter</strong> — i link sono bloccati per non-staff (anti-phishing)</li>
                            </ul>

                            <h2>👨‍👩‍👧 Per i genitori</h2>
                            <ul>
                                <li><a href="/help/parents"><strong>Guida per i genitori</strong></a></li>
                                <li><strong>Cancellare l'account di un minore</strong> — vedi link sopra</li>
                            </ul>

                            <h2>🐛 Bug, suggerimenti, idee</h2>
                            <ul>
                                <li><strong>Segnalare un bug</strong> — entra nella stanza "Oracolo" e descrivi il problema in chat</li>
                                <li><strong>Proporre una nuova feature</strong> — stessa stanza, comando <code>:oracolo proposta &lt;testo&gt;</code></li>
                                <li><strong>Vedere le proposte attive</strong> — <code>:oracolo lista</code> in chat</li>
                            </ul>

                            <h2>📚 Documenti legali</h2>
                            <ul>
                                <li><a href="/playing-habbo/terms-of-service"><strong>Termini e Condizioni</strong></a></li>
                                <li><a href="/playing-habbo/privacy-notice"><strong>Politica sulla Privacy</strong></a></li>
                                <li><a href="/playing-habbo/dsa"><strong>Regolamento Servizi Digitali</strong></a></li>
                                <li><a href="/help/cookies"><strong>Politica sui cookie</strong></a></li>
                            </ul>
                        </div>
                    </article>
                </habbo-compile>
                <habbo-web-pages className="aside aside--box aside--fixed">
                    <aside className="static-content">
                        <h3>Contatta lo staff</h3>
                        <p>Non trovi la risposta?</p>
                        <p>
                            📧 <a href="mailto:advertising@habboproject">email staff</a><br />
                            💬 <code>:cfh</code> dal client<br />
                            🐛 Stanza "Oracolo" per bug
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
