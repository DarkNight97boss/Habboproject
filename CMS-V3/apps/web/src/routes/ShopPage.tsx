import { type ReactNode, useState } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { type AuthUser, useAuth } from '../hooks/useAuth';

/**
 * Pagina /shop — replica fedele habbo.it/shop con identità Habboproject.
 *
 * Struttura DOM ufficiale:
 *   <main.wrapper--content>
 *     <header.shop__header>
 *       <h1.shop__header__title>Compra Crediti…
 *     <section>
 *       <div.main>
 *         <habbo-category-filter>
 *           <ul.category-filter><li><a.category-filter__link[--active]>…
 *         <habbo-inventory>
 *           <habbo-daily-deal><section><h3>Affare del Giorno
 *             <habbo-accordion-grid><ul>… cards pacchetti…
 *           <habbo-bundles><section><h3>Affari Stanza
 *             …
 *       <aside .aside--box> Il mio borsellino
 *
 * Identità Habboproject: il pagamento è SEMPRE 'codice prepagato' (no
 * carte di credito, no microtransazioni reali). I 'pacchetti' del shop
 * mostrano cosa puoi ottenere da achievement/eventi/staff.
 */
type Category = 'tutti' | 'codici' | 'achievement' | 'eventi';

const CATEGORIES: { key: Category; label: string }[] = [
    { key: 'tutti', label: 'Tutti' },
    { key: 'codici', label: 'Codici Prepagati' },
    { key: 'achievement', label: 'Achievement' },
    { key: 'eventi', label: 'Eventi Staff' }
];

interface Bundle
{
    id: string;
    category: Category;
    section: 'daily' | 'rooms' | 'cosmetic' | 'rares';
    title: string;
    description: string;
    rewards: string;
    badge?: string;
    duration?: string;
}

const BUNDLES: Bundle[] = [
    { id: 'daily', category: 'tutti', section: 'daily', title: 'Affare del Giorno', description: '+200 duckets gratis ogni giorno dal Daily Streak (Giorno 6 della striscia)', rewards: '+200 duckets', duration: 'Scade fra 21h' },
    { id: 'streak-7d', category: 'achievement', section: 'rares', title: 'Streak Perfetta', description: 'Completa 7 giorni consecutivi di login per il bonus finale', rewards: '+500 crediti', badge: 'STREAK_7' },
    { id: 'starter-100', category: 'codici', section: 'rooms', title: 'Starter Pack', description: 'Codice staff per nuovi utenti — chiedi su Discord', rewards: '100 crediti + 5 diamanti', badge: 'STARTER' },
    { id: 'social-100', category: 'achievement', section: 'cosmetic', title: 'Sociale 100', description: 'Aggiungi 100 amici alla tua lista', rewards: '+150 crediti + badge', badge: 'SOC_100' },
    { id: 'builder-50', category: 'achievement', section: 'rooms', title: 'Costruttore', description: 'Costruisci 50 stanze diverse con almeno 20 furni ciascuna', rewards: '+200 duckets + 2 diamanti', badge: 'BUILD_50' },
    { id: 'event-roller', category: 'eventi', section: 'cosmetic', title: 'Roller Disco Party', description: 'Partecipa alla Pista Retrò serale (ogni giovedì h 21:00)', rewards: 'Skin pattini LTD', duration: 'Settimanale' },
    { id: 'event-quiz', category: 'eventi', section: 'rares', title: 'Quiz del Sabato', description: 'Vinci il quiz settimanale staff-organizzato', rewards: 'Furni LTD + 1 diamante', duration: 'Sabato 22:00' }
];

export function ShopPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const [activeCat, setActiveCat] = useState<Category>('tutti');
    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    const filtered = activeCat === 'tutti' ? BUNDLES : BUNDLES.filter(b => b.category === activeCat);
    const daily = filtered.filter(b => b.section === 'daily');
    const rooms = filtered.filter(b => b.section === 'rooms');
    const cosmetic = filtered.filter(b => b.section === 'cosmetic');
    const rares = filtered.filter(b => b.section === 'rares');

    return (
        <AuthedShell user={user}>
            <main className="wrapper wrapper--content">
                <header className="shop__header">
                    <h1 className="shop__header__title">Guadagna crediti, diamanti e premi</h1>
                </header>
                <section>
                    <habbo-compile className="main main--fixed">
                        <habbo-category-filter>
                            <ul className="category-filter" style={{ display: 'flex', gap: 12, padding: 0, margin: '12px 0', listStyle: 'none', justifyContent: 'flex-end' }}>
                                {CATEGORIES.map((c, i) => (
                                    <li key={c.key} className="category-filter__item" style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                        <a
                                            href="#"
                                            onClick={ev => { ev.preventDefault(); setActiveCat(c.key); }}
                                            className={`category-filter__link${c.key === activeCat ? ' category-filter__link--active' : ''}`}
                                            style={{ color: '#fff', textDecoration: c.key === activeCat ? 'underline' : 'none', fontWeight: c.key === activeCat ? 'bold' : 'normal' }}
                                        >
                                            {c.label}
                                        </a>
                                        {i < CATEGORIES.length - 1 && <span style={{ color: '#7ecaee' }}>|</span>}
                                    </li>
                                ))}
                            </ul>
                        </habbo-category-filter>

                        <habbo-inventory>
                            {daily.length > 0 && <BundleSection title="Affare del Giorno" bundles={daily} />}
                            {rooms.length > 0 && <BundleSection title="Affari Stanza" bundles={rooms} />}
                            {cosmetic.length > 0 && <BundleSection title="Cosmetici" bundles={cosmetic} />}
                            {rares.length > 0 && <BundleSection title="Rari & LTD" bundles={rares} />}
                            {filtered.length === 0 && (
                                <habbo-empty-results><span>Nessun pacchetto in questa categoria.</span></habbo-empty-results>
                            )}
                        </habbo-inventory>
                    </habbo-compile>
                    <Sidebar user={user} />
                </section>
            </main>
        </AuthedShell>
    );
}

function BundleSection({ title, bundles }: { title: string; bundles: Bundle[] }): ReactNode
{
    return (
        <section className="habbo-shop-sections" style={{ marginBottom: 24 }}>
            <h3 className="inventory__section__title" style={{ color: '#fff', textTransform: 'uppercase', margin: '12px 0 8px' }}>{title}</h3>
            <habbo-accordion-grid className="inventory__grid">
                <ul style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 12, padding: 0, margin: 0, listStyle: 'none' }}>
                    {bundles.map(b => (
                        <li key={b.id} style={{ background: 'rgba(0,0,0,.25)', borderRadius: 5, padding: 16, color: '#fff' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 8 }}>
                                <h4 style={{ margin: '0 0 6px', textTransform: 'uppercase' }}>{b.title}</h4>
                                {b.duration && (
                                    <span style={{ background: '#ff9933', color: '#000', padding: '2px 8px', borderRadius: 3, fontSize: 11, fontWeight: 'bold', whiteSpace: 'nowrap' }}>
                                        {b.duration}
                                    </span>
                                )}
                            </div>
                            <p style={{ fontSize: 13, opacity: 0.85, margin: '4px 0' }}>{b.description}</p>
                            <div style={{ margin: '12px 0 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontWeight: 'bold', color: '#fbd33f' }}>{b.rewards}</span>
                                {b.badge && (
                                    <img
                                        src={`https://images.habbo.com/c_images/album1584/${b.badge}.gif`}
                                        alt={b.badge}
                                        style={{ imageRendering: 'pixelated' }}
                                        onError={e => { (e.currentTarget as HTMLImageElement).style.display = 'none'; }}
                                    />
                                )}
                            </div>
                        </li>
                    ))}
                </ul>
            </habbo-accordion-grid>
        </section>
    );
}

function Sidebar({ user }: { user: AuthUser }): ReactNode
{
    return (
        <habbo-web-pages className="aside aside--box aside--fixed">
            <aside className="static-content">
                <h3>Il mio borsellino</h3>
                <p style={{ fontSize: 15 }}>
                    🟡 <strong>{user.credits ?? 0}</strong> crediti<br />
                    💎 <strong>—</strong> diamanti<br />
                    🦆 <strong>—</strong> duckets
                </p>
                <h3>Hai un codice?</h3>
                <form onSubmit={ev => ev.preventDefault()}>
                    <input type="text" placeholder="Inserisci codice…" style={{ width: '100%', padding: '6px', borderRadius: 3, border: '2px solid #275d8e', background: '#ccd8df', color: '#000', boxSizing: 'border-box' }} />
                    <button type="submit" style={{ marginTop: 6, width: '100%', padding: '8px', background: '#00813e', color: 'white', border: '2px solid #8eda55', borderRadius: 5, fontWeight: 'bold', textTransform: 'uppercase', cursor: 'pointer' }}>
                        Converti
                    </button>
                </form>
                <p style={{ fontSize: 11, opacity: 0.7, marginTop: 8 }}>
                    Scopri <a href="/help" style={{ color: '#fbd33f' }}>come ottenere un codice prepagato</a>.
                </p>
                <h3>Info &amp; Supporto</h3>
                <ul style={{ paddingLeft: 16, fontSize: 13 }}>
                    <li><a href="/help" style={{ color: '#fbd33f' }}>Centro aiuto</a></li>
                    <li><a href="/playing-habbo/safety" style={{ color: '#fbd33f' }}>Sicurezza</a></li>
                    <li><a href="/playing-habbo/terms-of-service" style={{ color: '#fbd33f' }}>Termini</a></li>
                </ul>
            </aside>
        </habbo-web-pages>
    );
}
