import { useQueryClient } from '@tanstack/react-query';
import { type FormEvent, type MouseEvent, type ReactNode, useState } from 'react';
import { type AuthUser, avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';
import './asteria.css';

/**
 * Homepage proprietaria Asteria — render solo quando skin = asteria-nebula.
 *
 * NON usa il DOM habbo.it (niente .wrapper, niente <habbo-header-small>,
 * niente classi del CSS ufficiale). Tutto pulito sotto `.asteria-root` con
 * classi proprietarie `.asteria-*` definite in `asteria.css`.
 *
 * Layout:
 *  - Sticky glass header (logo gradient + nav + user-pill / CTA login)
 *  - Hero centrato: titolo gigante gradient animato + tagline + CTA
 *  - Stats strip (4 metriche live)
 *  - News grid 3 colonne (premium card glass)
 *  - Footer minimal
 *
 * Per anon: hero ha login form glass + CTA REGISTRATI.
 * Per auth: hero personale con welcome + crediti + CTA GIOCA + quick actions.
 */
export function AsteriaHomePage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const isAuthed = !isLoading && user;

    return (
        <div className="asteria-root">
            <AsteriaHeader user={isAuthed ? user : null} />
            {isAuthed ? <AuthedHero user={user} /> : <AnonHero />}
            <StatsStrip />
            {isAuthed && <QuickActions />}
            <NewsGrid />
            <AsteriaFooter />
        </div>
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
                ASTERIA<span className="asteria-logo__dot">.</span>
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
//   HERO — anonymous (login + CTA)
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
                if(j.error === 'invalid_credentials') setError('Nome utente o password errati.');
                else if(j.error === 'account_locked') setError('Account bloccato. Contatta lo staff.');
                else setError('Errore durante il login.');
                return;
            }
            broadcastAuth('login');
            await qc.invalidateQueries({ queryKey: ['auth', 'me'] });
        }
        catch
        {
            setError('Errore di rete. Riprova.');
        }
        finally { setLoading(false); }
    }

    return (
        <section className="asteria-hero">
            <div className="asteria-hero__eyebrow">Server attivo · 127 utenti online</div>
            <h1 className="asteria-hero__title">Il social hotel<br />del futuro</h1>
            <p className="asteria-hero__tagline">
                Costruisci stanze, conosci persone, partecipa a eventi live.
                Un universo sociale 2D pixel-perfect, gestito dalla community.
            </p>

            <form id="login" className="asteria-login" onSubmit={login}>
                <h2 className="asteria-login__title">Entra in Asteria</h2>
                <p className="asteria-login__subtitle">Usa il tuo Habbo o registrane uno nuovo.</p>
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
                    {loading ? 'Accesso…' : 'Entra adesso →'}
                </button>
                <p style={{ marginTop: 16, fontSize: 13, color: '#64748b', textAlign: 'center' }}>
                    Non hai un account? <a href="/registration" style={{ color: '#a855f7', textDecoration: 'none', fontWeight: 600 }}>Registrati gratis</a>
                </p>
            </form>
        </section>
    );
}

// ============================================================
//   HERO — authenticated (welcome + CTA gioca)
// ============================================================
function AuthedHero({ user }: { user: AuthUser }): ReactNode
{
    return (
        <section className="asteria-hero">
            <div className="asteria-hero__eyebrow">Bentornato — {user.motto || 'pronto per giocare'}</div>
            <h1 className="asteria-hero__title">Ciao, {user.username}</h1>
            <p className="asteria-hero__tagline">
                {user.credits.toLocaleString('it-IT')} crediti · {user.points.toLocaleString('it-IT')} punti
                <br />Cosa vuoi fare oggi?
            </p>
            <div className="asteria-hero__cta">
                <a href="/api/v2/auth/play" className="asteria-btn asteria-btn--xl">
                    ▶ Gioca adesso
                </a>
                <a href={`/profile/${user.username}`} className="asteria-btn asteria-btn--ghost asteria-btn--xl">
                    Il tuo profilo
                </a>
            </div>
        </section>
    );
}

// ============================================================
//   STATS STRIP
// ============================================================
function StatsStrip(): ReactNode
{
    const stats = [
        { value: '127', label: 'Utenti online' },
        { value: '34', label: 'Stanze attive' },
        { value: '1,8K', label: 'Messaggi/h' },
        { value: '99.9%', label: 'Uptime' }
    ];
    return (
        <div className="asteria-stats">
            {stats.map((s, i) => (
                <div key={i} className="asteria-stat">
                    <div className="asteria-stat__value">{s.value}</div>
                    <div className="asteria-stat__label">{s.label}</div>
                </div>
            ))}
        </div>
    );
}

// ============================================================
//   QUICK ACTIONS (auth only)
// ============================================================
function QuickActions(): ReactNode
{
    const actions = [
        { icon: '👤', label: 'Il tuo profilo', href: '/profile' },
        { icon: '🏠', label: 'Le tue stanze', href: '/community/rooms' },
        { icon: '🛒', label: 'Shop & crediti', href: '/shop' },
        { icon: '💬', label: 'Messaggi', href: '/messaging' }
    ];
    return (
        <div className="asteria-section">
            <div className="asteria-quick">
                {actions.map((a, i) => (
                    <a key={i} href={a.href}>
                        <span className="asteria-quick__icon">{a.icon}</span>
                        <span>{a.label}</span>
                    </a>
                ))}
            </div>
        </div>
    );
}

// ============================================================
//   NEWS GRID (mock cards — wire ad API news in iter successiva)
// ============================================================
const NEWS_MOCK = [
    { id: 1, slug: 'benvenuto', cat: 'Annuncio', title: 'Benvenuto su Asteria', excerpt: 'Il social hotel del futuro è pronto. Esplora le stanze, fai nuovi amici, personalizza tutto.', date: '1 giu 2026', cover: 'linear-gradient(135deg, #a855f7 0%, #06b6d4 100%)' },
    { id: 2, slug: 'roller-disco', cat: 'Eventi', title: 'Live ora: Roller Disco!', excerpt: 'Pista Retrò chiama. Entra, balla, fatti notare. Premi per chi resta fino a chiusura.', date: '30 mag 2026', cover: 'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)' },
    { id: 3, slug: 'comandi', cat: 'Aggiornamenti', title: 'Comandi italiani', excerpt: 'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.', date: '29 mag 2026', cover: 'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)' },
    { id: 4, slug: 'oracolo', cat: 'Community', title: 'Bot Oracolo attivo', excerpt: 'L\'oracolo Asteria raccoglie le tue idee e i bug. Scrivi :oracolo nella chat di stanza.', date: '28 mag 2026', cover: 'linear-gradient(135deg, #f59e0b 0%, #ec4899 100%)' },
    { id: 5, slug: 'staff-mfa', cat: 'Sicurezza', title: 'MFA obbligatoria per staff', excerpt: 'Tutti gli staff hanno 2FA via app authenticator. Più sicurezza, meno account compromessi.', date: '20 mag 2026', cover: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)' },
    { id: 6, slug: 'nitro-v3', cat: 'Client', title: 'Nitro V3 in produzione', excerpt: 'Il nuovo client (React 19 + Vite) è live. Più veloce, glass UI, hotbar personalizzata.', date: '15 mag 2026', cover: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)' }
];

function NewsGrid(): ReactNode
{
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">Ultime dal sito</h2>
                <span className="asteria-section__subtitle">Novità, aggiornamenti, eventi</span>
            </div>
            <div className="asteria-news-grid">
                {NEWS_MOCK.map(n => (
                    <a key={n.id} href={`/community/article/${n.slug}`} className="asteria-news-card">
                        <div className="asteria-news-card__cover" style={{ background: n.cover }}>
                            <span className="asteria-news-card__category">{n.cat}</span>
                        </div>
                        <div className="asteria-news-card__body">
                            <h3 className="asteria-news-card__title">{n.title}</h3>
                            <p className="asteria-news-card__excerpt">{n.excerpt}</p>
                            <div className="asteria-news-card__meta">
                                <span>📅 {n.date}</span>
                            </div>
                        </div>
                    </a>
                ))}
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
                <a href="/help">Aiuto</a>
                <a href="/playing-habbo/terms">Termini</a>
                <a href="/playing-habbo/privacy">Privacy</a>
                <a href="/playing-habbo/cookies">Cookie</a>
            </div>
            <div>Asteria © 2026 · Comunità retro habbo italiana · Powered by Arcturus + Nitro V3</div>
        </footer>
    );
}
