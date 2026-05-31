import { type ReactNode } from 'react';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina 404 — replica DOM ufficiale habbo.it (es. habbo.it/inesistente).
 *
 * Struttura:
 *   <main class="wrapper wrapper--content not-found">
 *     <section class="not-found__content">
 *       <h3>Oh bobba! Pagina non trovata.</h3>
 *       <div><a href="/">Homepage di Habbo</a></div>
 *
 * L'illustrazione Frank con la lente d'ingrandimento è applicata via
 * la CSS ufficiale come `::before` del `.not-found__content`.
 *
 * Quando l'utente è autenticato usiamo lo shell standard (header-small +
 * nav + GIOCA). Da anonimo non mostriamo header-large/login: solo un
 * header essenziale per non distrarre dal messaggio 404.
 */
export function NotFoundPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();

    if(isLoading) return null;

    const body = (
        <main className="wrapper wrapper--content not-found">
            <section className="not-found__content">
                <h3>Oh bobba! Pagina non trovata.</h3>
                <div>
                    Frank non ha trovato la pagina che stai cercando.
                    Verifica che l'URL sia corretta o riprova dalla{' '}
                    <a href="/"><strong>Homepage di Habbo</strong></a>.
                </div>
            </section>
        </main>
    );

    if(user) return <AuthedShell user={user}>{body}</AuthedShell>;

    // Anonimo: mostra solo content + minimal footer (no hero login form).
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
