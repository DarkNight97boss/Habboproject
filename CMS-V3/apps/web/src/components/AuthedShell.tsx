import { type MouseEvent, type ReactNode, useState } from 'react';
import { type AuthUser, avatarUrl, broadcastAuth, useNotifications } from '../hooks/useAuth';

/**
 * Chrome layout per le pagine authenticated del CMS-V3 — mirrora 1:1
 * la struttura `<div.content><habbo-header-small/>...</div><habbo-footer/>`
 * del sito habbo.it post-login.
 *
 * Estratto da HomePage/MessagingPage per evitare duplicazione mano a mano
 * che aggiungiamo route (community/photos, community/rooms, ecc).
 *
 * Props:
 *  - user: AuthUser (obbligatorio)
 *  - tabs: ReactNode opzionale renderizzato sotto l'header (es. tab
 *          Novità|Messaggi o Foto|Stanze|Fansite|Notizie)
 *  - children: contenuto della pagina (di solito <main class="wrapper--content">)
 */
export function AuthedShell({ user, tabs, children, extraHeaderClass, extraHeaderContent }: {
    user: AuthUser;
    tabs?: ReactNode;
    children: ReactNode;
    /** Aggiunto come className extra a <habbo-header-small> — es. 'profile__header'
     *  per applicare il bg isometric pattern alla pagina profilo. */
    extraHeaderClass?: string;
    /** Renderizzato dentro l'header dopo nav — es. <habbo-profile-header> per profilo. */
    extraHeaderContent?: ReactNode;
}): ReactNode
{
    return (
        <>
            <div className="content">
                <HeaderSmall user={user} extraClass={extraHeaderClass} extraContent={extraHeaderContent} />
                {tabs}
                {children}
            </div>
            <Footer />
        </>
    );
}

function HeaderSmall({ user, extraClass, extraContent }: { user: AuthUser; extraClass?: string; extraContent?: ReactNode }): ReactNode
{
    return (
        <habbo-header-small className={extraClass}>
            <header className="header__wrapper wrapper">
                <a href="/" className="header__habbo__logo">
                    <h1 className="header__habbo__name">Habbo</h1>
                </a>
                <UserMenu user={user} />
            </header>
            <habbo-navigation>
                <NavigationBar />
                <habbo-landing-menu />
            </habbo-navigation>
            {extraContent}
            <div className="wrapper" />
        </habbo-header-small>
    );
}

function UserMenu({ user }: { user: AuthUser }): ReactNode
{
    const [open, setOpen] = useState(false);
    const { data: notif } = useNotifications(true);
    const notifCount = notif?.total ?? 0;

    async function handleLogout(ev: MouseEvent): Promise<void>
    {
        ev.preventDefault();
        try { await fetch('/api/v2/auth/logout', { method: 'POST', credentials: 'include' }); }
        finally
        {
            // Broadcast a tutte le altre tab CMS aperte: si invalideranno
            // automaticamente via useAuth → re-fetch /me → 401 → anonima.
            broadcastAuth('logout');
            window.location.href = '/';
        }
    }

    return (
        <habbo-user-menu className="header__aside header__aside--user-menu">
            <div className="user-menu">
                <div className="user-menu__header">
                    <a
                        href="#"
                        onClick={ev => { ev.preventDefault(); setOpen(o => !o); }}
                        className="user-menu__toggle"
                    >
                        <div className="user-menu__name__wrapper">
                            <div className={`user-menu__name${open ? ' user-menu__name--open' : ''}`}>{user.username}</div>
                        </div>
                        <habbo-imager className="user-menu__avatar" style={{ position: 'relative' }}>
                            <img
                                src={avatarUrl(user.look, { headOnly: true, size: 'm' })}
                                alt={user.username}
                                className="imager"
                            />
                            {/* Badge notifiche live: red-dot con contatore
                                quando ci sono messaggi non letti o richieste
                                amicizia pending. Polled ogni 15s. */}
                            {notifCount > 0 && (
                                <span
                                    aria-label={`${notifCount} notifiche`}
                                    title={`${notif?.unreadMessages ?? 0} messaggi · ${notif?.friendRequests ?? 0} richieste amicizia`}
                                    style={{
                                        position: 'absolute',
                                        top: 0,
                                        right: -4,
                                        background: '#ff3333',
                                        color: '#fff',
                                        fontSize: 11,
                                        fontWeight: 'bold',
                                        minWidth: 18,
                                        height: 18,
                                        borderRadius: 9,
                                        textAlign: 'center',
                                        lineHeight: '18px',
                                        boxShadow: '0 1px 2px rgba(0,0,0,.5)',
                                        zIndex: 10,
                                        pointerEvents: 'none'
                                    }}
                                >
                                    {notifCount > 99 ? '99+' : notifCount}
                                </span>
                            )}
                        </habbo-imager>
                    </a>
                </div>
                <ul className={`user-menu__list${open ? '' : ' ng-hide'}`}>
                    <li className="user-menu__item">
                        <a href={`/profile/${encodeURIComponent(user.username)}`} className="user-menu__link user-menu__link--profile">Il mio profilo</a>
                    </li>
                    <li className="user-menu__item">
                        <a href="/settings" className="user-menu__link user-menu__link--settings">Impostazioni</a>
                    </li>
                    {/* Voce staff-only: rank >= 5 nell'EMU Arcturus = staff/mod/admin */}
                    {user.rank >= 5 && (
                        <li className="user-menu__item">
                            <a href="/admin" className="user-menu__link user-menu__link--settings" style={{ color: '#fbd33f' }}>
                                Pannello Amministrazione
                            </a>
                        </li>
                    )}
                    <li className="user-menu__item">
                        <a href="/help" className="user-menu__link user-menu__link--help">Aiuto</a>
                    </li>
                    <li className="user-menu__item">
                        <a href="#" onClick={handleLogout} className="user-menu__link user-menu__link--logout">Esci</a>
                    </li>
                </ul>
            </div>
        </habbo-user-menu>
    );
}

function NavigationBar(): ReactNode
{
    return (
        <nav className="navigation">
            <ul className="navigation__menu">
                <li className="navigation__item"><a id="ga-linkid-home" href="/" className="navigation__link navigation__link--home">Home</a></li>
                <li className="navigation__item"><a id="ga-linkid-community" href="/community" className="navigation__link navigation__link--community">Community</a></li>
                <li className="navigation__item"><a id="ga-linkid-shop" href="/shop" className="navigation__link navigation__link--shop">Shop</a></li>
                <li className="navigation__item"><a id="ga-linkid-playing-habbo" href="/playing-habbo" className="navigation__link navigation__link--playing-habbo">Il Mondo di Habbo</a></li>
                <li className="navigation__item"><a id="ga-linkid-habbo-nft" href="/habbo-nft" className="navigation__link navigation__link--habbo-nft">COLLEZIONABILI</a></li>
                <li className="navigation__item navigation__item--hotel">
                    <habbo-hotel-native-button>
                        {/* target="_blank" → Nitro si apre in nuova tab, così
                            l'utente può tornare al CMS senza perdere lo stato
                            di gioco (il client Nitro mantiene la WS aperta).
                            Anche, evita di "killare" la SPA CMS quando l'utente
                            esce dal gioco. */}
                        <a id="ga-linkid-native" href="/gioca" target="_blank" rel="noopener" className="hotel-button-native">
                            <span className="hotel-button-native__text hotel-button-native__text--play">Gioca</span>
                        </a>
                    </habbo-hotel-native-button>
                </li>
            </ul>
        </nav>
    );
}

function Footer(): ReactNode
{
    const links = [
        { href: '/help', label: 'Supporto al Giocatore & Centro Aiuto' },
        { href: '/playing-habbo/safety', label: 'Sicurezza' },
        { href: '/help/parents', label: 'Per i genitori' },
        { href: '/playing-habbo/terms-of-service', label: 'Termini e Condizioni' },
        { href: '/playing-habbo/privacy-notice', label: 'Politica sulla Privacy' },
        { href: 'mailto:advertising@habboproject', label: 'advertising@habboproject' },
        { href: '/help/cookies', label: "Politica relativa all'uso di cookies" },
        { href: '/playing-habbo/dsa', label: 'Regolamento Sui Servizi Digitali' }
    ];

    return (
        <habbo-footer>
            <footer className="wrapper">
                <div className="footer__media">
                    <p className="footer__media__label">Segui Habbo</p>
                    <ul>
                        <li className="footer__media__item"><a href="#" className="footer__media__link"><i className="icon icon--facebook" /></a></li>
                        <li className="footer__media__item"><a href="#" className="footer__media__link"><i className="icon icon--twitter" /></a></li>
                        <li className="footer__media__item"><a href="#" className="footer__media__link"><i className="icon icon--youtube" /></a></li>
                        <li className="footer__media__item"><a href="#" className="footer__media__link"><i className="icon icon--instagram" /></a></li>
                        <li className="footer__media__item"><a href="#" className="footer__media__link"><i className="icon icon--rss" /></a></li>
                    </ul>
                </div>
                <div className="footer__content">
                    <ul className="footer__nav">
                        {links.map(l => (
                            <li key={l.href} className="footer__nav__item">
                                <a href={l.href} className="footer__nav__link" target="_blank" rel="noopener noreferrer">{l.label}</a>
                            </li>
                        ))}
                    </ul>
                    <p className="footer__copyright">
                        © 2004 — {new Date().getFullYear()} Habboproject — clone non ufficiale a scopo educativo.
                        HABBO® è un marchio registrato di proprietà di Sulake Oy.
                    </p>
                    <a href="http://www.sulake.com" target="_blank" rel="noopener noreferrer" className="footer__sulake">Sulake</a>
                </div>
            </footer>
        </habbo-footer>
    );
}

// ============================================================
// TABS: sotto-navigazioni che alcune pagine renderizzano subito
// dopo l'header. Riusabili.
// ============================================================

/** Tabs home post-login: Novità | Messaggi. */
export function HomeTabs({ active }: { active: 'novita' | 'messaggi' }): ReactNode
{
    const novActive = active === 'novita';
    const msgActive = active === 'messaggi';
    return (
        <habbo-tabs>
            <nav className="tabs">
                <div className="tabs__toggle">
                    <div className="tabs__toggle__title">{msgActive ? 'Messaggi' : 'Novità'}</div>
                </div>
                <ul className="tabs__menu ng-hide">
                    <habbo-tab>
                        <li className="tab">
                            <a href="/" className={`tab__link${novActive ? ' tab__link--active' : ''}`}>Novità</a>
                        </li>
                    </habbo-tab>
                    <habbo-tab>
                        <li className="tab">
                            <a href="/messaging" className={`tab__link${msgActive ? ' tab__link--active' : ''}`}>Messaggi</a>
                        </li>
                    </habbo-tab>
                </ul>
            </nav>
        </habbo-tabs>
    );
}

type ShopTab = 'acquista' | 'prepagate' | 'i-miei-acquisti';

/**
 * Tabs sezione Shop: Acquista | Prepagate | I miei acquisti
 * (replica habbo-it/shop subnav).
 */
export function ShopTabs({ active }: { active: ShopTab }): ReactNode
{
    const items: { key: ShopTab; label: string; href: string }[] = [
        { key: 'acquista', label: 'Acquista', href: '/shop' },
        { key: 'prepagate', label: 'Prepagate', href: '/shop/prepagate' },
        { key: 'i-miei-acquisti', label: 'I miei acquisti', href: '/shop/acquisti' }
    ];
    const activeLabel = items.find(i => i.key === active)?.label ?? 'Acquista';
    return (
        <habbo-tabs>
            <nav className="tabs">
                <div className="tabs__toggle">
                    <div className="tabs__toggle__title">{activeLabel}</div>
                </div>
                <ul className="tabs__menu ng-hide">
                    {items.map(it => (
                        <habbo-tab key={it.key}>
                            <li className="tab">
                                <a href={it.href} className={`tab__link${it.key === active ? ' tab__link--active' : ''}`}>{it.label}</a>
                            </li>
                        </habbo-tab>
                    ))}
                </ul>
            </nav>
        </habbo-tabs>
    );
}

type CommunityTab = 'foto' | 'stanze' | 'forum' | 'notizie';

/**
 * Tabs sezione Community: Foto | Stanze | Forum | Notizie.
 *
 * NB: l'ufficiale habbo.it ha "Fansite" come terzo tab, ma la pagina è
 * 404 (link morto da anni). Habboproject sostituisce con "Forum" che
 * punta a /community/forum — sezione discussion board interna.
 */
export function CommunityTabs({ active }: { active: CommunityTab }): ReactNode
{
    const items: { key: CommunityTab; label: string; href: string }[] = [
        { key: 'foto', label: 'Foto', href: '/community/photos' },
        { key: 'stanze', label: 'Stanze', href: '/community/rooms' },
        { key: 'forum', label: 'Forum', href: '/community/forum' },
        { key: 'notizie', label: 'Notizie', href: '/community/category/all' }
    ];
    const activeLabel = items.find(i => i.key === active)?.label ?? 'Foto';
    return (
        <habbo-tabs>
            <nav className="tabs">
                <div className="tabs__toggle">
                    <div className="tabs__toggle__title">{activeLabel}</div>
                </div>
                <ul className="tabs__menu ng-hide">
                    {items.map(it => (
                        <habbo-tab key={it.key}>
                            <li className="tab">
                                <a href={it.href} className={`tab__link${it.key === active ? ' tab__link--active' : ''}`}>{it.label}</a>
                            </li>
                        </habbo-tab>
                    ))}
                </ul>
            </nav>
        </habbo-tabs>
    );
}
