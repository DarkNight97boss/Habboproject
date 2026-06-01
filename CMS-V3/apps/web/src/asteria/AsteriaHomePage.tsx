import { useQueryClient } from '@tanstack/react-query';
import { type FormEvent, type ReactNode, useEffect, useRef, useState } from 'react';
import { type AuthUser, avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';
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

    return (
        <div className="asteria-root">
            <Spotlight />
            <ParticleField count={18} />
            <AsteriaHeader user={isAuthed ? user : null} />
            <LiveMarquee />
            {isAuthed ? <AuthedHero user={user} /> : <AnonHero />}
            <StatsStrip />
            {isAuthed && <QuickActions />}
            <NewsBento />
            <SpotlightCTA isAuthed={!!isAuthed} />
            <AsteriaFooter />
        </div>
    );
}

// ============================================================
//   SPOTLIGHT — blob glow viola che segue il cursore
// ============================================================
function Spotlight(): ReactNode
{
    const ref = useRef<HTMLDivElement>(null);
    useEffect(() =>
    {
        const el = ref.current;
        if(!el) return;
        let rafId: number | null = null;
        let targetX = window.innerWidth / 2;
        let targetY = 300;
        let currX = targetX, currY = targetY;

        function onMove(ev: MouseEvent): void
        {
            targetX = ev.clientX;
            targetY = ev.clientY;
            if(rafId === null) tick();
        }
        function tick(): void
        {
            // Easing: smooth follow
            currX += (targetX - currX) * 0.12;
            currY += (targetY - currY) * 0.12;
            if(el)
            {
                el.style.left = `${currX}px`;
                el.style.top = `${currY}px`;
            }
            if(Math.abs(targetX - currX) > 0.5 || Math.abs(targetY - currY) > 0.5)
            {
                rafId = requestAnimationFrame(tick);
            }
            else
            {
                rafId = null;
            }
        }
        window.addEventListener('mousemove', onMove, { passive: true });
        tick();
        return () =>
        {
            window.removeEventListener('mousemove', onMove);
            if(rafId !== null) cancelAnimationFrame(rafId);
        };
    }, []);
    return <div ref={ref} className="asteria-spotlight" />;
}

// ============================================================
//   PARTICLE FIELD — 30+ particelle floating con random delay
// ============================================================
function ParticleField({ count }: { count: number }): ReactNode
{
    // Genera position+delay+duration deterministically (no Date.now / Math.random
    // alla mount per evitare hydration mismatch e per repeatability)
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
                <span
                    key={i}
                    className="asteria-particle"
                    style={{
                        left: `${p.left}%`,
                        animationDelay: `-${p.delay}s`,
                        animationDuration: `${p.duration}s`,
                        width: `${p.size}px`,
                        height: `${p.size}px`
                    }}
                />
            ))}
        </div>
    );
}

// ============================================================
//   ASTERIA MARK — logo SVG 8-point star geometric
// ============================================================
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
            <path
                d="M16 0 L18.5 13.5 L32 16 L18.5 18.5 L16 32 L13.5 18.5 L0 16 L13.5 13.5 Z"
                fill="url(#asteria-mark-grad)"
                stroke="rgba(255,255,255,0.30)"
                strokeWidth="0.5"
            />
        </svg>
    );
}

// ============================================================
//   HEADER
// ============================================================
function AsteriaHeader({ user }: { user: AuthUser | null }): ReactNode
{
    return (
        <header className="asteria-header">
            <a href="/" className="asteria-logo">
                <AsteriaMark />
                <span className="asteria-logo__name">Asteria</span>
            </a>
            <nav className="asteria-nav">
                <a href="/" className="is-active">Home</a>
                <a href="/community/photos">Community</a>
                <a href="/shop">Shop</a>
                <a href="/habbo-nft">Collezionabili</a>
                <a href="/playing-habbo">Guida</a>
            </nav>
            <div className="asteria-user">
                {user ? <UserPill user={user} /> : (
                    <>
                        <a href="#login" className="asteria-btn asteria-btn--ghost">Accedi</a>
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
            <div className="asteria-pill" title={user.motto}>
                <img src={avatarUrl(user.look, { size: 's', headOnly: true })} alt="" />
                <span>{user.username}</span>
            </div>
            {user.rank >= 5 && (
                <a href="/admin" className="asteria-btn asteria-btn--ghost" title="Pannello staff">⚡</a>
            )}
            <button type="button" className="asteria-btn asteria-btn--ghost" onClick={logout} title="Esci">⏻</button>
        </>
    );
}

// ============================================================
//   LIVE MARQUEE — ticker scrolling con eventi live
// ============================================================
function LiveMarquee(): ReactNode
{
    const items = [
        'Roller Disco live ora',
        '12 stanze full',
        '127 utenti online',
        'Nuovo: comandi italiani',
        'MFA staff abilitata',
        'Nitro V3 in produzione',
        'Asteria Core build #142',
        'Eventi questo weekend',
        'Daily streak resetta a mezzanotte'
    ];
    // Duplica per loop infinito
    const looped = [...items, ...items];
    return (
        <div className="asteria-marquee">
            <div className="asteria-marquee__track">
                {looped.map((s, i) => (
                    <span key={i} className="asteria-marquee__item">{s}</span>
                ))}
            </div>
        </div>
    );
}

// ============================================================
//   COUNT-UP HOOK
// ============================================================
function useCountUp(target: number, duration = 2000): number
{
    const [val, setVal] = useState(0);
    useEffect(() =>
    {
        let raf = 0;
        const startTime = performance.now();
        function step(now: number): void
        {
            const t = Math.min((now - startTime) / duration, 1);
            // Easing out cubic
            const eased = 1 - Math.pow(1 - t, 3);
            setVal(Math.floor(target * eased));
            if(t < 1) raf = requestAnimationFrame(step);
        }
        raf = requestAnimationFrame(step);
        return () => cancelAnimationFrame(raf);
    }, [target, duration]);
    return val;
}

function CountUpValue({ value, suffix = '' }: { value: number; suffix?: string }): ReactNode
{
    const v = useCountUp(value, 1800);
    return <>{v.toLocaleString('it-IT')}{suffix}</>;
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
function StatsStrip(): ReactNode
{
    return (
        <div className="asteria-stats">
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={127} /></div>
                <div className="asteria-stat__label">Utenti online</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value"><CountUpValue value={34} /></div>
                <div className="asteria-stat__label">Stanze attive</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">1.8K</div>
                <div className="asteria-stat__label">Msg/ora</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">99.9<span style={{fontSize: 24}}>%</span></div>
                <div className="asteria-stat__label">Uptime 30g</div>
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
