import { type MouseEvent, type ReactNode, useState } from 'react';
import { type AuthUser, avatarUrl } from '../hooks/useAuth';

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
export function AuthedShell({ user, tabs, children }: { user: AuthUser; tabs?: ReactNode; children: ReactNode }): ReactNode
{
    return (
        <>
            <div className="content">
                <HeaderSmall user={user} />
                {tabs}
                {children}
            </div>
            <Footer />
        </>
    );
}

function HeaderSmall({ user }: { user: AuthUser }): ReactNode
{
    return (
        <habbo-header-small>
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
            <div className="wrapper" />
        </habbo-header-small>
    );
}

function UserMenu({ user }: { user: AuthUser }): ReactNode
{
    const [open, setOpen] = useState(false);

    async function handleLogout(ev: MouseEvent): Promise<void>
    {
        ev.preventDefault();
        try { await fetch('/api/v2/auth/logout', { method: 'POST', credentials: 'include' }); }
        finally { window.location.href = '/'; }
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
                        <habbo-imager className="user-menu__avatar">
                            <img
                                src={avatarUrl(user.look, { headOnly: true, size: 'm' })}
                                alt={user.username}
                                className="imager"
                            />
                        </habbo-imager>
                    </a>
                </div>
                <ul className={`user-menu__list${open ? '' : ' ng-hide'}`}>
                    <li className="user-menu__item">
                        <a href="/profile" className="user-menu__link user-menu__link--profile">Il mio profilo</a>
                    </li>
                    <li className="user-menu__item">
                        <a href="/settings" className="user-menu__link user-menu__link--settings">Impostazioni</a>
                    </li>
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
                        <a id="ga-linkid-native" href="/gioca" className="hotel-button-native">
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

type CommunityTab = 'foto' | 'stanze' | 'fansite' | 'notizie';

/** Tabs sezione Community: Foto | Stanze | Fansite | Notizie. */
export function CommunityTabs({ active }: { active: CommunityTab }): ReactNode
{
    const items: { key: CommunityTab; label: string; href: string }[] = [
        { key: 'foto', label: 'Foto', href: '/community/photos' },
        { key: 'stanze', label: 'Stanze', href: '/community/rooms' },
        { key: 'fansite', label: 'Fansite', href: '/community/fansite' },
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
