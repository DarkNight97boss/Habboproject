import { type ReactNode, useEffect, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { type AuthUser, avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';

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
    return (
        <div className="asteria-root">
            <Spotlight />
            <ParticleField count={18} />
            <AsteriaHeader user={user ?? null} active={activeNav} />
            {!hideMarquee && <LiveMarquee />}
            {children}
            <AsteriaFooter />
        </div>
    );
}

// ============================================================
//   SPOTLIGHT — mouse-tracking
// ============================================================
function Spotlight(): ReactNode
{
    const ref = useRef<HTMLDivElement>(null);
    useEffect(() =>
    {
        const el = ref.current;
        if(!el) return;
        let rafId: number | null = null;
        let targetX = window.innerWidth / 2, targetY = 300;
        let currX = targetX, currY = targetY;
        function onMove(ev: MouseEvent): void { targetX = ev.clientX; targetY = ev.clientY; if(rafId === null) tick(); }
        function tick(): void
        {
            currX += (targetX - currX) * 0.12;
            currY += (targetY - currY) * 0.12;
            if(el) { el.style.left = `${currX}px`; el.style.top = `${currY}px`; }
            if(Math.abs(targetX - currX) > 0.5 || Math.abs(targetY - currY) > 0.5) rafId = requestAnimationFrame(tick);
            else rafId = null;
        }
        window.addEventListener('mousemove', onMove, { passive: true });
        tick();
        return () => { window.removeEventListener('mousemove', onMove); if(rafId !== null) cancelAnimationFrame(rafId); };
    }, []);
    return <div ref={ref} className="asteria-spotlight" />;
}

function ParticleField({ count }: { count: number }): ReactNode
{
    const particles = Array.from({ length: count }, (_, i) =>
    {
        const seed = i * 9301 + 49297;
        const left = ((seed % 1000) / 10);
        const delay = (((seed * 7) % 200) / 10);
        const duration = 16 + (((seed * 13) % 100) / 10);
        const size = 1 + ((seed % 30) / 10);
        return { left, delay, duration, size };
    });
    return (
        <div className="asteria-particles" aria-hidden="true">
            {particles.map((p, i) => (
                <span key={i} className="asteria-particle" style={{
                    left: `${p.left}%`, animationDelay: `-${p.delay}s`, animationDuration: `${p.duration}s`,
                    width: `${p.size}px`, height: `${p.size}px`
                }} />
            ))}
        </div>
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

function AsteriaHeader({ user, active }: { user: AuthUser | null; active: AsteriaNavId }): ReactNode
{
    const items: { id: AsteriaNavId; label: string; href: string }[] = [
        { id: 'home', label: 'Home', href: '/' },
        { id: 'community', label: 'Community', href: '/community/photos' },
        { id: 'shop', label: 'Shop', href: '/shop' },
        { id: 'collezionabili', label: 'Collezionabili', href: '/habbo-nft' },
        { id: 'guida', label: 'Guida', href: '/playing-habbo' }
    ];
    return (
        <header className="asteria-header">
            <a href="/" className="asteria-logo">
                <AsteriaMark />
                <span className="asteria-logo__name">Asteria</span>
            </a>
            <nav className="asteria-nav">
                {items.map(i => <a key={i.id} href={i.href} className={i.id === active ? 'is-active' : ''}>{i.label}</a>)}
            </nav>
            <div className="asteria-user">
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

function AsteriaFooter(): ReactNode
{
    return (
        <footer className="asteria-footer">
            <div className="asteria-footer__links">
                <a href="/playing-habbo/safety">Sicurezza</a>
                <a href="/help">Help</a>
                <a href="/playing-habbo/terms">Termini</a>
                <a href="/playing-habbo/privacy">Privacy</a>
                <a href="/playing-habbo/cookies">Cookie</a>
            </div>
            <div className="asteria-footer__copy">
                <span className="asteria-footer__brand">ASTERIA.</span> &nbsp;©&nbsp;2026 &nbsp;·&nbsp; Made in Italy &nbsp;·&nbsp; Arcturus + Nitro V3
            </div>
        </footer>
    );
}
