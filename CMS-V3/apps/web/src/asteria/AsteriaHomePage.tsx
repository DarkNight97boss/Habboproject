import { useQueryClient } from '@tanstack/react-query';
import { type FormEvent, type ReactNode, useEffect, useRef, useState } from 'react';
import { type AuthUser, broadcastAuth, useAuth } from '../hooks/useAuth';
import { AsteriaNewsBento } from './AsteriaNewsBento';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Asteria Nebula v2 — Homepage proprietaria 2026.
 *
 * Pattern moderni: mouse-tracking spotlight, particle field, marquee live,
 * bento grid asimmetrico, count-up animati, gradient massive title, noise overlay,
 * 3D tilt on hover, variable typography.
 *
 * Solo render se skin=asteria-nebula (vedi HomePage.tsx switch).
 */
export function AsteriaHomePage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const isAuthed = !isLoading && user;

    // Usa AsteriaShell standardizzato (orbi statici, no spotlight, header sticky scroll-shrink, Cmd+K, footer 3-col)
    return (
        <AsteriaShell activeNav="home">
            {isAuthed ? <AuthedHero user={user} /> : <AnonHero />}
            <StatsStrip />
            {isAuthed && <QuickActions />}
            <AsteriaNewsBento />
            <SpotlightCTA isAuthed={!!isAuthed} />
        </AsteriaShell>
    );
}

// ============================================================
//   COUNT-UP HOOK — solo su IntersectionObserver visible
// ============================================================
function useCountUp(target: number, duration = 1800): { value: number; ref: (el: HTMLElement | null) => void }
{
    const [val, setVal] = useState(0);
    const [seen, setSeen] = useState(false);
    const observerRef = useRef<IntersectionObserver | null>(null);

    const ref = (el: HTMLElement | null): void =>
    {
        if(observerRef.current) observerRef.current.disconnect();
        if(!el || seen) return;
        observerRef.current = new IntersectionObserver(entries =>
        {
            if(entries[0]?.isIntersecting) { setSeen(true); observerRef.current?.disconnect(); }
        }, { threshold: 0.1 });
        observerRef.current.observe(el);
    };

    useEffect(() =>
    {
        if(!seen) return;
        let raf = 0;
        const startTime = performance.now();
        function step(now: number): void
        {
            const t = Math.min((now - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - t, 3);
            setVal(Math.floor(target * eased));
            if(t < 1) raf = requestAnimationFrame(step);
        }
        raf = requestAnimationFrame(step);
        return () => cancelAnimationFrame(raf);
    }, [seen, target, duration]);

    return { value: val, ref };
}

function CountUpValue({ value, suffix = '' }: { value: number; suffix?: string }): ReactNode
{
    const { value: v, ref } = useCountUp(value, 1800);
    return <span ref={ref}>{v.toLocaleString('it-IT')}{suffix}</span>;
}

// ============================================================
//   HERO — anonymous (gigantic title + login)
// ============================================================
function AnonHero(): ReactNode
{
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const qc = useQueryClient();

    async function login(ev: FormEvent<HTMLFormElement>): Promise<void>
    {
        ev.preventDefault();
        setError(null);
        setLoading(true);
        try
        {
            const r = await fetch('/api/v2/auth/login', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            if(!r.ok)
            {
                const j = await r.json().catch(() => ({ error: 'unknown' }));
                if(j.error === 'invalid_credentials') setError('Credenziali errate.');
                else if(j.error === 'account_locked') setError('Account bloccato.');
                else setError('Errore login.');
                return;
            }
            broadcastAuth('login');
            await qc.invalidateQueries({ queryKey: ['auth', 'me'] });
        }
        catch { setError('Errore di rete.'); }
        finally { setLoading(false); }
    }

    return (
        <section className="asteria-hero">
            <div className="asteria-hero__eyebrow">Server v3.5.5 · Live</div>
            <h1 className="asteria-hero__title">
                Il social hotel<br />
                <span className="asteria-hero__title__outline">del futuro.</span>
            </h1>
            <p className="asteria-hero__tagline">
                Stanze, eventi, identità digitale, economia interna. Asteria è il primo social
                hotel italiano costruito dalla community, per la community. Pixel-perfect, cross-device.
            </p>

            <form id="login" className="asteria-login" onSubmit={login}>
                <h2 className="asteria-login__title">Entra in Asteria</h2>
                <p className="asteria-login__subtitle">Usa il tuo account</p>
                {error && <div className="asteria-error">{error}</div>}
                <input
                    type="text"
                    className="asteria-input"
                    placeholder="Nome utente"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    autoComplete="username"
                    required
                />
                <input
                    type="password"
                    className="asteria-input"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    autoComplete="current-password"
                    required
                />
                <button type="submit" disabled={loading} className="asteria-btn asteria-btn--xl" style={{ width: '100%', marginTop: 12 }}>
                    {loading ? 'Accesso…' : 'Entra adesso ↗'}
                </button>
                <p style={{ marginTop: 18, fontSize: 12, color: '#475569', textAlign: 'center', fontFamily: 'JetBrains Mono, monospace', letterSpacing: '0.05em' }}>
                    Nuovo? <a href="/registration" style={{ color: '#c4b5fd', textDecoration: 'none', fontWeight: 600 }}>CREA ACCOUNT GRATIS →</a>
                </p>
            </form>
        </section>
    );
}

// ============================================================
//   HERO — authenticated
// ============================================================
function AuthedHero({ user }: { user: AuthUser }): ReactNode
{
    return (
        <section className="asteria-hero">
            <div className="asteria-hero__eyebrow">Bentornato</div>
            <h1 className="asteria-hero__title">{user.username}</h1>
            <p className="asteria-hero__tagline">
                {user.credits.toLocaleString('it-IT')} crediti disponibili · rank {user.rank}
                <br />
                Pronto a tornare in gioco?
            </p>
            <div className="asteria-hero__cta">
                <a href="/api/v2/auth/play" className="asteria-btn asteria-btn--xl">
                    ▶ Entra nel hotel
                </a>
                <a href={`/profile/${user.username}`} className="asteria-btn asteria-btn--ghost asteria-btn--xl">
                    Il tuo profilo
                </a>
            </div>
        </section>
    );
}

// ============================================================
//   STATS — count-up animati
// ============================================================
interface HomeStats { onlineUsers: number; activeRooms: number; msgPerHour: number; totalUsers: number; }

function StatsStrip(): ReactNode
{
    // Dati REALI da /api/v2/community/stats (utenti online, stanze attive,
    // messaggi nell'ultima ora, iscritti). Fallback a 0 finché non arrivano
    // o se l'API è giù — i CountUp animano fino al valore reale al fetch.
    const [stats, setStats] = useState<HomeStats | null>(null);

    useEffect(() =>
    {
        const ctrl = new AbortController();
        fetch('/api/v2/community/stats', { signal: ctrl.signal })
            .then(r => (r.ok ? r.json() : null))
            .then((d: HomeStats | null) => { if(d) setStats(d); })
            .catch(() => { /* rete/API giù: resta sul fallback */ });
        return () => ctrl.abort();
    }, []);

    return (
        <div className="asteria-stats">
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={stats?.onlineUsers ?? 0} /></div>
                <div className="asteria-stat__label">Utenti online</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={stats?.activeRooms ?? 0} /></div>
                <div className="asteria-stat__label">Stanze attive</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={stats?.msgPerHour ?? 0} /></div>
                <div className="asteria-stat__label">Msg/ora</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={stats?.totalUsers ?? 0} /></div>
                <div className="asteria-stat__label">Iscritti</div>
            </div>
        </div>
    );
}

// ============================================================
//   QUICK ACTIONS
// ============================================================
function QuickActions(): ReactNode
{
    const actions = [
        { icon: '◉', label: 'Profilo', href: '/profile' },
        { icon: '⌂', label: 'Stanze', href: '/community/rooms' },
        { icon: '⊕', label: 'Shop', href: '/shop' },
        { icon: '✉', label: 'Messaggi', href: '/messaging' }
    ];
    return (
        <div className="asteria-quick">
            {actions.map((a, i) => (
                <a key={i} href={a.href}>
                    <span className="asteria-quick__icon">{a.icon}</span>
                    <span>{a.label}</span>
                </a>
            ))}
        </div>
    );
}

// ============================================================
//   NEWS BENTO — asimmetrico premium
// ============================================================
const NEWS_BENTO = [
    {
        slug: 'benvenuto',
        cat: 'Annuncio',
        title: 'Il futuro arriva su Asteria.',
        excerpt: 'Un mondo sociale pixel-perfect, gestito dalla community italiana. Stanze, eventi, identità digitale.',
        date: '01.06.2026',
        cover: 'linear-gradient(135deg, #a855f7 0%, #06b6d4 50%, #ec4899 100%)',
        size: 'feat' as const
    },
    {
        slug: 'roller-disco',
        cat: 'Live',
        title: 'Roller Disco è iniziato',
        excerpt: 'Pista retro in mainsquare. Premi per chi resta fino a chiusura.',
        date: '30.05.2026',
        cover: 'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
        size: 'default' as const
    },
    {
        slug: 'comandi',
        cat: 'Update',
        title: 'Comandi italiani',
        excerpt: '140 comandi tradotti: :bando, :tira, :spingi, :silenzia.',
        date: '29.05.2026',
        cover: 'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)',
        size: 'default' as const
    },
    {
        slug: 'oracolo',
        cat: 'Bot',
        title: 'Oracolo è attivo nelle stanze',
        excerpt: 'Raccoglie le tue idee e bug report. Scrivi :oracolo nella chat.',
        date: '28.05.2026',
        cover: 'linear-gradient(135deg, #f59e0b 0%, #ec4899 100%)',
        size: 'wide' as const
    },
    {
        slug: 'staff-mfa',
        cat: 'Sicurezza',
        title: 'MFA staff obbligatoria',
        excerpt: '2FA via authenticator app per tutti gli admin.',
        date: '20.05.2026',
        cover: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
        size: 'default' as const
    },
    {
        slug: 'nitro-v3',
        cat: 'Client',
        title: 'Nitro V3 prod',
        excerpt: 'React 19 + Vite, glass UI, hotbar custom, +50% perf.',
        date: '15.05.2026',
        cover: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)',
        size: 'default' as const
    }
];

function NewsBento(): ReactNode
{
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">In evidenza</h2>
                <span className="asteria-section__subtitle">Novità, eventi, sicurezza</span>
            </div>
            <div className="asteria-bento">
                {NEWS_BENTO.map(n => {
                    const cardCls = `asteria-bento__card asteria-bento__card--${n.size}`;
                    return (
                        <a key={n.slug} href={`/community/article/${n.slug}`} className={cardCls}>
                            <div className="asteria-bento__cover" style={{ background: n.cover }}>
                                <span className="asteria-bento__category">{n.cat}</span>
                            </div>
                            <div className="asteria-bento__body">
                                <h3 className="asteria-bento__title">{n.title}</h3>
                                <p className="asteria-bento__excerpt">{n.excerpt}</p>
                                <div className="asteria-bento__meta">{n.date}</div>
                            </div>
                        </a>
                    );
                })}
            </div>
        </div>
    );
}

// ============================================================
//   SPOTLIGHT CTA — invito grande full-width
// ============================================================
function SpotlightCTA({ isAuthed }: { isAuthed: boolean }): ReactNode
{
    return (
        <div className="asteria-spotlight-cta">
            <div className="asteria-spotlight-cta__eyebrow">↓ Pronto?</div>
            <h2 className="asteria-spotlight-cta__title">
                Inizia<br />l'avventura.
            </h2>
            <p className="asteria-spotlight-cta__sub">
                Gratis per sempre. Niente pubblicità, niente pay-to-win.
                La community costruisce, la community decide.
            </p>
            <div className="asteria-hero__cta">
                {isAuthed ? (
                    <a href="/api/v2/auth/play" className="asteria-btn asteria-btn--xl">▶ Entra nel hotel</a>
                ) : (
                    <>
                        <a href="/registration" className="asteria-btn asteria-btn--xl">Crea il tuo account →</a>
                        <a href="#login" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Ho già un account</a>
                    </>
                )}
            </div>
        </div>
    );
}

// ============================================================
//   FOOTER
// ============================================================
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
