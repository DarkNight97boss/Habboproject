import { type ReactNode, useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { type AuthUser, avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';
import { CommandPalette } from './CommandPalette';

/**
 * Shell riusabile per tutte le pagine Asteria proprietarie.
 *
 *   <AsteriaShell activeNav="shop">
 *     <SectionA />
 *     <SectionB />
 *   </AsteriaShell>
 *
 * Renderizza header sticky glass + spotlight cursore + particle field +
 * marquee + footer. La pagina figlia fa il suo contenuto dentro `<main>`.
 */

export type AsteriaNavId = 'home' | 'community' | 'shop' | 'collezionabili' | 'guida';

export function AsteriaShell({ activeNav, children, hideMarquee = false }: { activeNav: AsteriaNavId; children: ReactNode; hideMarquee?: boolean }): ReactNode
{
    const { data: user } = useAuth();
    const [cmdOpen, setCmdOpen] = useState(false);

    // Cmd+K shortcut globale (lightweight, no event-loop overhead).
    useEffect(() =>
    {
        function onKey(ev: KeyboardEvent): void
        {
            if((ev.key === 'k' || ev.key === 'K') && (ev.metaKey || ev.ctrlKey))
            {
                ev.preventDefault();
                setCmdOpen(open => !open);
            }
            else if(ev.key === 'Escape' && cmdOpen)
            {
                setCmdOpen(false);
            }
        }
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [cmdOpen]);

    return (
        <div className="asteria-root">
            {/* Decorativi static (zero costo runtime) */}
            <DecorOrbs />
            <AsteriaHeader user={user ?? null} active={activeNav} onCmdOpen={() => setCmdOpen(true)} />
            {!hideMarquee && <LiveMarquee />}
            {children}
            <AsteriaFooter />
            <CommandPalette open={cmdOpen} onClose={() => setCmdOpen(false)} user={user ?? null} />
        </div>
    );
}

// ============================================================
//   DECOR ORBS — gradient orbs statici, zero animation, GPU-cheap
// ============================================================
function DecorOrbs(): ReactNode
{
    return (
        <>
            <div className="asteria-orb asteria-orb--top-left" aria-hidden="true" />
            <div className="asteria-orb asteria-orb--bottom-right" aria-hidden="true" />
            <div className="asteria-orb asteria-orb--center" aria-hidden="true" />
        </>
    );
}

function AsteriaMark(): ReactNode
{
    return (
        <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="asteria-mark-grad" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#a855f7" />
                    <stop offset="50%" stopColor="#06b6d4" />
                    <stop offset="100%" stopColor="#ec4899" />
                </linearGradient>
            </defs>
            <path d="M16 0 L18.5 13.5 L32 16 L18.5 18.5 L16 32 L13.5 18.5 L0 16 L13.5 13.5 Z"
                fill="url(#asteria-mark-grad)" stroke="rgba(255,255,255,0.30)" strokeWidth="0.5" />
        </svg>
    );
}

function AsteriaHeader({ user, active, onCmdOpen }: { user: AuthUser | null; active: AsteriaNavId; onCmdOpen: () => void }): ReactNode
{
    const items: { id: AsteriaNavId; label: string; href: string }[] = [
        { id: 'home', label: 'Home', href: '/' },
        { id: 'community', label: 'Community', href: '/community/photos' },
        { id: 'shop', label: 'Shop', href: '/shop' },
        { id: 'collezionabili', label: 'Drops', href: '/habbo-nft' },
        { id: 'guida', label: 'Guida', href: '/playing-habbo' }
    ];

    // Scroll-shrink: header diventa più compatto dopo 60px scroll
    const [scrolled, setScrolled] = useState(false);
    useEffect(() =>
    {
        function onScroll(): void { setScrolled(window.scrollY > 60); }
        window.addEventListener('scroll', onScroll, { passive: true });
        return () => window.removeEventListener('scroll', onScroll);
    }, []);

    return (
        <header className={`asteria-header ${scrolled ? 'is-scrolled' : ''}`}>
            <a href="/" className="asteria-logo">
                <AsteriaMark />
                <span className="asteria-logo__name">Asteria</span>
            </a>
            <nav className="asteria-nav">
                {items.map(i => <a key={i.id} href={i.href} className={i.id === active ? 'is-active' : ''}>{i.label}</a>)}
            </nav>
            <div className="asteria-user">
                <button type="button" onClick={onCmdOpen} className="asteria-cmd-trigger" title="Cmd+K">
                    <span>Cerca</span>
                    <kbd>⌘K</kbd>
                </button>
                {user ? <UserPill user={user} /> : (
                    <>
                        <a href="/login" className="asteria-btn asteria-btn--ghost">Accedi</a>
                        <a href="/registration" className="asteria-btn">Iscriviti</a>
                    </>
                )}
            </div>
        </header>
    );
}

function UserPill({ user }: { user: AuthUser }): ReactNode
{
    const qc = useQueryClient();
    async function logout(): Promise<void>
    {
        await fetch('/api/v2/auth/logout', { method: 'POST', credentials: 'include' });
        broadcastAuth('logout');
        await qc.invalidateQueries({ queryKey: ['auth', 'me'] });
    }
    return (
        <>
            <a href="/me" className="asteria-pill" title={user.motto}>
                <img src={avatarUrl(user.look, { size: 's', headOnly: true })} alt="" />
                <span>{user.username}</span>
            </a>
            {user.rank >= 5 && <a href="/admin" className="asteria-btn asteria-btn--ghost" title="Pannello staff">⚡</a>}
            <button type="button" className="asteria-btn asteria-btn--ghost" onClick={logout} title="Esci">⏻</button>
        </>
    );
}

function AsteriaFooter(): ReactNode
{
    return (
        <footer className="asteria-footer">
            <div className="asteria-footer__top">
                <div className="asteria-footer__brand-block">
                    <a href="/" className="asteria-logo">
                        <AsteriaMark />
                        <span className="asteria-logo__name">Asteria</span>
                    </a>
                    <p className="asteria-footer__brand-tagline">
                        Il social hotel del futuro. Pixel-perfect, gestito dalla community italiana.
                    </p>
                    <div className="asteria-footer__cta-row">
                        <a href="/registration" className="asteria-btn">Registrati gratis</a>
                    </div>
                </div>
                <div className="asteria-footer__col">
                    <div className="asteria-footer__col-title">Esplora</div>
                    <a href="/">Home</a>
                    <a href="/community/photos">Community</a>
                    <a href="/shop">Shop</a>
                    <a href="/leaderboard">Leaderboard</a>
                    <a href="/habbo-nft">Drops</a>
                </div>
                <div className="asteria-footer__col">
                    <div className="asteria-footer__col-title">Account</div>
                    <a href="/login">Accedi</a>
                    <a href="/registration">Registrati</a>
                    <a href="/me">Il tuo profilo</a>
                    <a href="/settings/privacy">Impostazioni</a>
                    <a href="/help">Help</a>
                </div>
                <div className="asteria-footer__col">
                    <div className="asteria-footer__col-title">Legale</div>
                    <a href="/playing-habbo/safety">Sicurezza</a>
                    <a href="/playing-habbo/terms">Termini</a>
                    <a href="/playing-habbo/privacy">Privacy</a>
                    <a href="/playing-habbo/cookies">Cookie</a>
                </div>
            </div>
            <div className="asteria-footer__bottom">
                <span className="asteria-footer__brand">ASTERIA.</span>
                <span>© 2026 · Made in Italy 🇮🇹</span>
                <span>Arcturus + Nitro V3</span>
            </div>
        </footer>
    );
}

function LiveMarquee(): ReactNode
{
    const items = [
        'Roller Disco live ora', '12 stanze full', '127 utenti online',
        'Shop premium attivo', 'MFA staff abilitata', 'Nitro V3 in produzione',
        'Asteria Core build #142', 'Eventi questo weekend', 'Daily streak resetta a mezzanotte'
    ];
    const looped = [...items, ...items];
    return (
        <div className="asteria-marquee">
            <div className="asteria-marquee__track">
                {looped.map((s, i) => <span key={i} className="asteria-marquee__item">{s}</span>)}
            </div>
        </div>
    );
}

