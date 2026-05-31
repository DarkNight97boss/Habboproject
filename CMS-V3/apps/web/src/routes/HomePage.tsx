import { type FormEvent, type MouseEvent, type ReactNode, useState } from 'react';
import { type AuthUser, avatarUrl, useAuth } from '../hooks/useAuth';

/**
 * Homepage habbo.it — replica usando le classi del CSS UFFICIALE
 * (app.a8ea7435.css caricato dal CDN images.habbo.com).
 *
 * La struttura HTML rispecchia la versione Angular originale: i nomi
 * delle classi (header__top, register-banner__*, login-form__*,
 * navigation__link--home, news-header, footer__*) sono mappati esattamente.
 */
export function HomePage(): ReactNode
{
    // Layout main + sidebar: replica esatta dell'ufficiale.
    // <section> contiene: h1, .main.main--fixed (col sinistra news), e due
    // <habbo-web-pages> sidebar (col destra, float:right + clear:right).
    // Il .news__navigation ("Più News") sta DENTRO .main--fixed, dopo le news.
    const { data: user, isLoading } = useAuth();

    // Durante il fetch iniziale di /me, mostra header anonimo (evita flash).
    // Una volta noto lo stato (user || null), renderizza la variante giusta:
    //  - ANONYMOUS: habbo-header-large (hero + login form)
    //  - AUTHENTICATED: habbo-header-small (user-menu + GIOCA in nav)
    //    + habbo-tabs visibili (Novità | Messaggi)
    //    + habbo-moderation-notification dentro main
    const isAuthed = !isLoading && user;

    return (
        <>
            <div className="content">
                {isAuthed ? <HeaderSmallAuthed user={user} /> : <HeaderLarge />}
                {isAuthed ? <TabsAuthed /> : <Tabs />}
                <main className="wrapper wrapper--content">
                    {isAuthed ? <habbo-moderation-notification /> : null}
                    <section>
                        <h1>Ultime notizie</h1>
                        <div className="main main--fixed">
                            <NewsList />
                        </div>
                        <Sidebar />
                    </section>
                </main>
            </div>
            <FooterOfficial />
        </>
    );
}

// ============================================================
// HEADER SMALL (POST-LOGIN)
// ============================================================

function HeaderSmallAuthed({ user }: { user: AuthUser }): ReactNode
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
                <NavigationBarAuthed />
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
                            <div className="user-menu__name">{user.username}</div>
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

function NavigationBarAuthed(): ReactNode
{
    // Identica alla nav anonima ma con l'extra <li.navigation__item--hotel>
    // che contiene il pulsante verde GIOCA (porta dentro /client).
    return (
        <nav className="navigation">
            <ul className="navigation__menu">
                <li className="navigation__item"><a id="ga-linkid-home" href="/" className="navigation__link navigation__link--home navigation__link--active">Home</a></li>
                <li className="navigation__item"><a id="ga-linkid-community" href="/community" className="navigation__link navigation__link--community">Community</a></li>
                <li className="navigation__item"><a id="ga-linkid-shop" href="/shop" className="navigation__link navigation__link--shop">Shop</a></li>
                <li className="navigation__item"><a id="ga-linkid-playing-habbo" href="/playing-habbo" className="navigation__link navigation__link--playing-habbo">Il Mondo di Habbo</a></li>
                <li className="navigation__item"><a id="ga-linkid-habbo-nft" href="/habbo-nft" className="navigation__link navigation__link--habbo-nft">COLLEZIONABILI</a></li>
                <li className="navigation__item navigation__item--hotel">
                    <habbo-hotel-native-button>
                        <a id="ga-linkid-native" href="/client" className="hotel-button-native">
                            <span className="hotel-button-native__text hotel-button-native__text--play">Gioca</span>
                        </a>
                    </habbo-hotel-native-button>
                </li>
            </ul>
        </nav>
    );
}

function TabsAuthed(): ReactNode
{
    // Tabs ufficiali post-login: "Novità" (attiva) | "Messaggi".
    return (
        <habbo-tabs>
            <nav className="tabs">
                <div className="tabs__toggle">
                    <div className="tabs__toggle__title">Novità</div>
                </div>
                <ul className="tabs__menu ng-hide">
                    <habbo-tab>
                        <li className="tab">
                            <a href="/" className="tab__link tab__link--active">Novità</a>
                        </li>
                    </habbo-tab>
                    <habbo-tab>
                        <li className="tab">
                            <a href="/messaging" className="tab__link">Messaggi</a>
                        </li>
                    </habbo-tab>
                </ul>
            </nav>
        </habbo-tabs>
    );
}

// ============================================================
// HEADER LARGE: top bar + hero login banner + nav
// ============================================================

function HeaderLarge(): ReactNode
{
    // <habbo-*> sono custom-element selettori che la CSS ufficiale usa
    // come scope (es. `habbo-header-large .login-form__social { flex-direction:column }`).
    // Senza questi wrapper le regole non si attivano e il layout è sbagliato.
    return (
        <habbo-header-large>
            <div className="header__top sticky-header sticky-header--top">
                <div className="wrapper">
                    <div className="header__top__content">
                        <h1 className="register-banner__logo">Habbo</h1>
                        <h2 className="register-banner__title">Un posto bizzarro pieno di gente fantastica</h2>
                        <a href="/registration" className="register-banner__button">Iscriviti gratuitamente!</a>
                    </div>
                </div>
            </div>

            <div className="header__content">
                <habbo-register-banner className="habbo-register-banner">
                    <div className="register-banner__hotel" />
                    <div className="register-banner__wrapper">
                        <div className="register-banner__register">
                            <LoginForm />
                        </div>
                    </div>
                </habbo-register-banner>
                <habbo-navigation>
                    <NavigationBar />
                </habbo-navigation>
            </div>
        </habbo-header-large>
    );
}

function LoginForm(): ReactNode
{
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(ev: FormEvent<HTMLFormElement>)
    {
        ev.preventDefault();
        if(!username || !password || loading) return;
        setLoading(true);
        setError(null);
        try
        {
            const r = await fetch('/api/v2/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ username, password })
            });
            if(!r.ok)
            {
                const j = await r.json().catch(() => ({ error: 'unknown' }));
                setError(j.error === 'invalid_credentials' ? 'Nome utente o password errati.' : 'Login fallito.');
            }
            else
            {
                window.location.href = '/me';
            }
        }
        catch
        {
            setError('Connessione fallita. Riprova.');
        }
        finally { setLoading(false); }
    }

    // Struttura mirrorata 1:1 da habbo.it (ispezionata via Chrome extension):
    //   <habbo-login-form class="header__login-form ng-hide">
    //     <div class="social-login">
    //       <div class="login-form__login-text">…</div>
    //       <div class="login-form__social">
    //         <habbo-{facebook|google|apple}-connect type="large|small">
    //           <button class="{facebook|google|apple}-connect">…</button>
    //         </habbo-…>
    //         <div class="login-texts" id="more-login">
    //           <div class="login-form__login-text">
    //             <habbo-rpx-login><small><a class="janrainEngage">…</a></small></habbo-rpx-login>
    //           </div>
    //         </div>
    //       </div>
    //     </div>
    //     <div class="login-form__email-login">
    //       <div class="login-form__login-text">…</div>
    //       <form id="habboLoginForm" class="login-form__form">
    //         <div>
    //           <fieldset class="form__fieldset login-form__fieldset">
    //             <div class="form__field"><input class="form__input login-form__input"/></div>
    //           </fieldset>
    //           …password…
    //         </div>
    //         <div><button class="login-form__button habbo-login-button" type="submit">…</button></div>
    //       </form>
    //       <div class="login-texts" id="forgot-password">
    //         <habbo-claim-password class="login-form__login-text"><small><a>…</a></small></habbo-claim-password>
    //       </div>
    //       <div class="login-form__register"><small><a>…</a></small></div>
    //     </div>
    //   </habbo-login-form>
    //
    // La classe `ng-hide` (su habbo-login-form) è un hook CSS che attiva la regola
    //   .header__login-form.ng-hide { display:flex !important; ... }
    // del CSS ufficiale — non nasconde nulla perché Angular non è caricato.
    return (
        <habbo-login-form className="header__login-form ng-hide">
            <div className="social-login">
                <div className="login-form__login-text">Effettua il login con una di queste opzioni</div>
                <div className="login-form__social">
                    <habbo-facebook-connect type="large">
                        <button type="button" className="facebook-connect">Facebook</button>
                    </habbo-facebook-connect>
                    <habbo-facebook-connect type="small">
                        <button type="button" className="facebook-connect">Facebook</button>
                    </habbo-facebook-connect>
                    <habbo-google-connect type="large">
                        <button type="button" className="google-connect">Google</button>
                    </habbo-google-connect>
                    <habbo-google-connect type="small">
                        <button type="button" className="google-connect">Google</button>
                    </habbo-google-connect>
                    <habbo-apple-connect type="large">
                        <button type="button" className="apple-connect">Accedi con Apple</button>
                    </habbo-apple-connect>
                    <habbo-apple-connect type="small">
                        <button type="button" className="apple-connect">Accedi con Apple</button>
                    </habbo-apple-connect>
                    <div className="login-texts" id="more-login">
                        <div className="login-form__login-text">
                            <habbo-rpx-login>
                                <small><a className="janrainEngage">Altri modi di accedere</a></small>
                            </habbo-rpx-login>
                        </div>
                    </div>
                </div>
            </div>
            <div className="login-form__email-login">
                <div className="login-form__login-text">O usa il tuo nome utente &amp; password:</div>
                <form id="habboLoginForm" onSubmit={handleSubmit} className="login-form__form">
                    <div>
                        <fieldset className="form__fieldset login-form__fieldset">
                            <div className="form__field">
                                <input
                                    name="username"
                                    type="text"
                                    value={username}
                                    onChange={e => setUsername(e.target.value)}
                                    placeholder="Nome utente"
                                    autoComplete="username"
                                    autoCapitalize="none"
                                    autoCorrect="off"
                                    spellCheck={false}
                                    className="form__input login-form__input"
                                    disabled={loading}
                                />
                            </div>
                        </fieldset>
                        <fieldset className="form__fieldset login-form__fieldset">
                            <div className="form__field">
                                <input
                                    name="password"
                                    type="password"
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                    placeholder="Password"
                                    autoComplete="current-password"
                                    className="form__input login-form__input"
                                    disabled={loading}
                                />
                            </div>
                        </fieldset>
                    </div>
                    {error ? <p style={{ color: '#c33', fontSize: 12, margin: '6px 0' }}>{error}</p> : null}
                    {/*
                      L'ufficiale habbo.it usa wrapper <div style="width:100%"><div>
                      attorno al submit per portarlo a width:244px (= 256px colonna
                      .login-form__email-login - 12px padding-right del form).
                      Il submit .login-form__button è inline-block per default; senza
                      questo wrapper resta della larghezza del testo (~107px).
                    */}
                    <div style={{ width: '100%' }}>
                        <div>
                            <button
                                type="submit"
                                disabled={loading || !username || !password}
                                className="login-form__button habbo-login-button"
                                style={{ width: '100%' }}
                            >
                                {loading ? '…' : 'Entra!'}
                            </button>
                        </div>
                    </div>
                </form>
                <div className="login-texts" id="forgot-password">
                    <habbo-claim-password className="login-form__login-text">
                        <small><a href="/forgot-password">Password dimenticata?</a></small>
                    </habbo-claim-password>
                </div>
                <div className="login-form__register">
                    <small><a href="/registration">Non hai ancora un account? Registrati su Habbo!</a></small>
                </div>
            </div>
        </habbo-login-form>
    );
}

function NavigationBar(): ReactNode
{
    return (
        <nav className="navigation">
            <ul className="navigation__menu">
                <li className="navigation__item"><a id="ga-linkid-home" href="/" className="navigation__link navigation__link--home navigation__link--active">Home</a></li>
                <li className="navigation__item"><a id="ga-linkid-community" href="/community" className="navigation__link navigation__link--community">Community</a></li>
                <li className="navigation__item"><a id="ga-linkid-shop" href="/shop" className="navigation__link navigation__link--shop">Shop</a></li>
                <li className="navigation__item"><a id="ga-linkid-playing-habbo" href="/playing-habbo" className="navigation__link navigation__link--playing-habbo">Il Mondo di Habbo</a></li>
                <li className="navigation__item"><a id="ga-linkid-habbo-nft" href="/habbo-nft" className="navigation__link navigation__link--habbo-nft">COLLEZIONABILI</a></li>
            </ul>
        </nav>
    );
}

// ============================================================
// TABS
// ============================================================

function Tabs(): ReactNode
{
    // Ufficiale ha sempre <habbo-tabs><nav class="tabs ng-hide"></nav></habbo-tabs>
    // anche con una sola tab — la CSS la nasconde via .ng-hide. Lo manteniamo
    // per parità strutturale (alcune regole CSS scopano via habbo-tabs ancestor).
    return (
        <habbo-tabs>
            <nav className="tabs ng-hide" />
        </habbo-tabs>
    );
}

// ============================================================
// NEWS
// ============================================================

interface NewsItem
{
    href: string;
    image: string;
    title: string;
    date: string;
    category: string;
    summary: string;
}

const news: NewsItem[] = [
    {
        href: '/community/article/welcome',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'BENVENUTO SU HABBO',
        date: new Date().toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' }),
        category: 'Aggiornamenti su Habbo',
        summary: 'Il tuo hotel virtuale è pronto. Esplora le stanze, fai nuovi amici e personalizza il tuo Habbo. Clicca su GIOCA per iniziare!'
    },
    {
        href: '/community/article/roller-disco',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_rollerdiscoFL_may26.png',
        title: 'LIVE ORA: Roller Disco!',
        date: '01 mag 2026',
        category: 'Campagne & Attività',
        summary: "La Pista Retrò ti sta chiamando. Entra e dai un'occhiata!"
    },
    {
        href: '/community/article/comandi-italiani',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_jonas_may26.png',
        title: 'COMANDI ITALIANI',
        date: '29 mag 2026',
        category: 'Aggiornamenti su Habbo',
        summary: 'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.'
    },
    {
        href: '/community/article/oracolo',
        image: 'https://images.habbo.com/web_images/habbo-web-articles/lpromo_HabboPulse.png',
        title: 'BOT ORACOLO ATTIVO',
        date: '30 mag 2026',
        category: 'Nuove funzionalità',
        summary: 'Entra nella stanza Oracolo e proponi le tue idee per nuove feature.'
    }
];

function NewsList(): ReactNode
{
    // Struttura ufficiale dentro .main--fixed:
    //   <habbo-compile>
    //     <section>
    //       <article class="news-header news-header--column">…</article>
    //       …
    //     </section>
    //   </habbo-compile>
    //   <div class="news__navigation">
    //     <a class="news__more">Più News</a>
    //   </div>
    return (
        <>
            <habbo-compile>
                <section>
                    {news.map(n => (
                        <article key={n.href} className="news-header news-header--column">
                            <a href={n.href} className="news-header__link news-header__banner">
                                <figure className="news-header__viewport">
                                    <img src={n.image} alt={n.title} className="news-header__image news-header__image--featured" />
                                    <img src={n.image} alt={n.title} className="news-header__image news-header__image--thumbnail" />
                                </figure>
                            </a>
                            <a href={n.href} className="news-header__link news-header__wrapper">
                                <h2 className="news-header__title">{n.title}</h2>
                            </a>
                            <aside className="news-header__wrapper news-header__info">
                                <time className="news-header__date">{n.date}</time>
                                <ul className="news-header__categories">
                                    <li className="news-header__category">
                                        <a className="news-header__category__link">{n.category}</a>
                                    </li>
                                </ul>
                            </aside>
                            <p className="news-header__wrapper news-header__summary">{n.summary}</p>
                        </article>
                    ))}
                </section>
            </habbo-compile>
            <div className="news__navigation">
                <a href="/community/category/all" className="news__more">Più News</a>
            </div>
        </>
    );
}

// ============================================================
// SIDEBAR
// ============================================================

function Sidebar(): ReactNode
{
    // Sull'ufficiale ogni box laterale è renderizzato da <habbo-web-pages> che
    // applica `.aside.aside--box.aside--fixed` sul custom element stesso, con
    // un <aside class="static-content"> annidato.
    return (
        <>
            <habbo-web-pages className="aside aside--box aside--fixed">
                <aside className="static-content">
                    <h3>Consigli di sicurezza</h3>
                    <p>Sentiti sempre al sicuro su internet! Impara come <a href="/playing-habbo/safety">navigare in sicurezza</a>.</p>
                </aside>
            </habbo-web-pages>
            <habbo-web-pages className="aside aside--box aside--fixed">
                <aside className="static-content">
                    <h3>Guida per i genitori</h3>
                    <p>Vuoi saperne di più su come ci assicuriamo che i nostri utenti si divertano in un ambiente sicuro? Guarda la nostra <a href="/help/parents">Guida per i genitori</a>.</p>
                </aside>
            </habbo-web-pages>
        </>
    );
}

// ============================================================
// FOOTER OFFICIAL
// ============================================================

function FooterOfficial(): ReactNode
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
