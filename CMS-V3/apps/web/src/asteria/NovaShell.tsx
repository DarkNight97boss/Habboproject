import type { Dispatch, ReactNode, SetStateAction } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { type AuthUser, avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';
import { CommandPalette } from './CommandPalette';
import type { AsteriaNavId } from './AsteriaShell';
import './nova.css';

/**
 * Studio Nova — shell del sito allineata al client (design D, ott 2026).
 *
 *   ┌ spina ┐ ┌──────────── stage ────────────┐
 *   │ brand │ │ [ isola: sezione · ⌘K · utente ] │
 *   │ avatar│ │                                  │
 *   │ nav   │ │   {children} (pagine Asteria)    │
 *   │ …     │ │                                  │
 *   │ opz.  │ │ footer compatto                  │
 *   └───────┘ └──────────────────────────────────┘
 *
 * Stessa API di AsteriaShell (activeNav/children): le pagine non cambiano.
 * Su mobile la spina diventa una barra in basso (vedi nova.css).
 */
export function NovaShell({ activeNav, children, cmdOpen, setCmdOpen }: {
    activeNav: AsteriaNavId;
    children: ReactNode;
    cmdOpen: boolean;
    setCmdOpen: Dispatch<SetStateAction<boolean>>;
}): ReactNode
{
    const { data: user } = useAuth();
    return (
        <div className="asteria-root nova-root">
            <NovaSpine user={ user ?? null } active={ activeNav } />
            <div className="nova-stage">
                <NovaIsland user={ user ?? null } active={ activeNav } onCmdOpen={ () => setCmdOpen(true) } />
                <main className="nova-stage__content">{ children }</main>
                <NovaFooter />
            </div>
            <CommandPalette open={ cmdOpen } onClose={ () => setCmdOpen(false) } user={ user ?? null } />
        </div>
    );
}

const NAV: { id: AsteriaNavId; label: string; href: string; icon: ReactNode }[] = [
    { id: 'home', label: 'Home', href: '/', icon: <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"/></svg> },
    { id: 'community', label: 'Community', href: '/community/rooms', icon: <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M15.5 8.5l-2 5-5 2 2-5z"/></svg> },
    { id: 'shop', label: 'Shop', href: '/shop', icon: <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6h12l-1 14H7zM9 6V4h6v2"/></svg> },
    { id: 'collezionabili', label: 'Drops', href: '/habbo-nft', icon: <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 8l9-4 9 4-9 4zM3 8v8l9 4 9-4V8M12 12v8"/></svg> },
    { id: 'guida', label: 'Guida', href: '/playing-habbo', icon: <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 5h7a3 3 0 0 1 3 3v12a2 2 0 0 0-2-2H4zM20 5h-7a3 3 0 0 0-3 3v12a2 2 0 0 1 2-2h8z"/></svg> }
];

function NovaMark(): ReactNode
{
    return (
        <svg viewBox="0 0 32 32" aria-hidden="true" className="nova-mark">
            <path d="M16 2 L18.6 13.4 L30 16 L18.6 18.6 L16 30 L13.4 18.6 L2 16 L13.4 13.4 Z" />
        </svg>
    );
}

function NovaSpine({ user, active }: { user: AuthUser | null; active: AsteriaNavId }): ReactNode
{
    const qc = useQueryClient();
    async function logout(): Promise<void>
    {
        await fetch('/api/v2/auth/logout', { method: 'POST', credentials: 'include' });
        broadcastAuth('logout');
        await qc.invalidateQueries({ queryKey: ['auth', 'me'] });
    }
    return (
        <aside className="nova-spine" aria-label="Navigazione principale">
            <a href="/" className="nova-spine__brand" aria-label="Asteria — home"><NovaMark /></a>
            { user ? (
                <a href="/me" className="nova-spine__avatar" title={ `${ user.username } · ${ user.motto }` }>
                    <span className="nova-spine__ring"><img src={ avatarUrl(user.look, { size: 's', headOnly: true }) } alt="" /></span>
                    <span className="nova-spine__avatar-name">{ user.username }</span>
                </a>
            ) : (
                <a href="/login" className="nova-spine__avatar nova-spine__avatar--anon" title="Accedi">
                    <span className="nova-spine__ring nova-spine__ring--anon"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg></span>
                    <span className="nova-spine__avatar-name">Accedi</span>
                </a>
            ) }
            <nav className="nova-spine__nav">
                { NAV.map(i => (
                    <a key={ i.id } href={ i.href } className={ `nova-spine__item${ i.id === active ? ' is-active' : '' }` } aria-current={ i.id === active ? 'page' : undefined }>
                        { i.icon }<span>{ i.label }</span>
                    </a>
                )) }
            </nav>
            <div className="nova-spine__bottom">
                { user && user.rank >= 5 && (
                    <a href="/admin" className="nova-spine__item" title="Pannello staff"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M13 2L4 14h7l-1 8 9-12h-7z"/></svg><span>Staff</span></a>
                ) }
                { user ? (
                    <>
                        <a href="/settings/privacy" className="nova-spine__item" title="Impostazioni"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 0 0-.1-1.2l2-1.5-2-3.5-2.4 1a7 7 0 0 0-2-1.2L14 3h-4l-.5 2.6a7 7 0 0 0-2 1.2l-2.4-1-2 3.5 2 1.5A7 7 0 0 0 5 12c0 .4 0 .8.1 1.2l-2 1.5 2 3.5 2.4-1a7 7 0 0 0 2 1.2L10 21h4l.5-2.6a7 7 0 0 0 2-1.2l2.4 1 2-3.5-2-1.5c.1-.4.1-.8.1-1.2z"/></svg><span>Opzioni</span></a>
                        <button type="button" className="nova-spine__item" onClick={ logout } title="Esci"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 17l5-5-5-5M15 12H3M12 3h7a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-7"/></svg><span>Esci</span></button>
                    </>
                ) : (
                    <a href="/registration" className="nova-spine__item" title="Registrati"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0M19 8v6M22 11h-6"/></svg><span>Iscriviti</span></a>
                ) }
            </div>
        </aside>
    );
}

function NovaIsland({ user, active, onCmdOpen }: { user: AuthUser | null; active: AsteriaNavId; onCmdOpen: () => void }): ReactNode
{
    const section = NAV.find(n => n.id === active)?.label ?? 'Asteria';
    return (
        <div className="nova-island" role="banner">
            <div className="nova-island__where">
                <span className="nova-island__dot" aria-hidden="true" />
                <strong>Asteria</strong>
                <span className="nova-island__sep" aria-hidden="true">·</span>
                <span>{ section }</span>
            </div>
            <button type="button" className="nova-island__search" onClick={ onCmdOpen } title="Cerca o digita un comando (⌘K)">
                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
                <span>Cerca stanze, utenti, pagine…</span>
                <kbd>⌘K</kbd>
            </button>
            <div className="nova-island__user">
                { user ? (
                    <>
                        <span className="nova-island__credits" title="Crediti"><i aria-hidden="true" />{ user.credits.toLocaleString('it-IT') }</span>
                        <a href="/gioca" className="nova-btn nova-btn--play">Gioca</a>
                    </>
                ) : (
                    <>
                        <a href="/login" className="nova-btn nova-btn--ghost">Accedi</a>
                        <a href="/registration" className="nova-btn">Iscriviti</a>
                    </>
                ) }
            </div>
        </div>
    );
}

function NovaFooter(): ReactNode
{
    return (
        <footer className="nova-footer">
            <div className="nova-footer__row">
                <a href="/" className="nova-footer__brand"><NovaMark /> Asteria</a>
                <nav className="nova-footer__links" aria-label="Footer">
                    <a href="/community/rooms">Community</a>
                    <a href="/shop">Shop</a>
                    <a href="/leaderboard">Classifica</a>
                    <a href="/help">Help</a>
                    <a href="/playing-habbo/terms">Termini</a>
                    <a href="/playing-habbo/privacy">Privacy</a>
                    <a href="/playing-habbo/cookies">Cookie</a>
                </nav>
            </div>
            <div className="nova-footer__row nova-footer__row--muted">
                <span>© 2026 Asteria · Made in Italy</span>
                <span>Sulake Corporation Ltd e HABBO® sono marchi indipendenti e non hanno alcuna relazione con Asteria.</span>
            </div>
        </footer>
    );
}
