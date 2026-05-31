import { type FormEvent, type ReactNode, useState } from 'react';

/**
 * Pagina registrazione — replica DOM ufficiale habbo.it/registration.
 *
 * Differenze rispetto all'ufficiale:
 *  - chiediamo il NOME UTENTE invece di email (l'EMU Habboproject
 *    autentica per username, non per email)
 *  - submit POST /api/v2/auth/register
 *
 * Struttura mirrorata 1:1 da habbo.it (ispezionata via Chrome extension):
 *   <div class="content">
 *     <habbo-header-small>
 *       <header class="header__wrapper wrapper">
 *         <a class="header__habbo__logo"><h1>Habbo</h1></a>
 *         <div class="header__aside"><button class="header__login__button">…Login</button></div>
 *       </header>
 *       <habbo-navigation><nav .navigation/></habbo-navigation>
 *     </habbo-header-small>
 *     <main class="wrapper wrapper--content">
 *       <habbo-registration-form>
 *         <h1>Registrati!</h1>
 *         <form class="form form--left registration-form">
 *           <div class="registration-form__social__wrapper">
 *             <div class="registration-form__social">
 *               <h3 class="registration-form__connect">O scegli un altro modo per registrarti</h3>
 *               <habbo-{facebook|google|apple}-connect type="large"><button/></habbo-…>
 *             </div>
 *           </div>
 *           <fieldset class="form__fieldset">  ← USERNAME (era Email)
 *             <label class="form__label">Nome utente</label>
 *             <p>…help text…</p>
 *             <div class="form__field"><input class="form__input"/></div>
 *           </fieldset>
 *           <habbo-password-new>2 fieldsets password</habbo-password-new>
 *           <habbo-birthdate>3 select day/month/year</habbo-birthdate>
 *           <habbo-profile-visibility>1 checkbox profile public</habbo-profile-visibility>
 *           <habbo-policies>1 checkbox ToS</habbo-policies>
 *           <p class="registration-form__safety">…</p>
 *           <div class="form__footer"><button class="form__submit registration-form__button habbo-registration-button">…</button></div>
 *           <p class="registration-form__purchases">Include acquisti in-app opzionali.</p>
 *         </form>
 *       </habbo-registration-form>
 *     </main>
 *   </div>
 *   <habbo-footer/>
 */
export function RegistrationPage(): ReactNode
{
    return (
        <>
            <div className="content">
                <HeaderSmall />
                <main className="wrapper wrapper--content">
                    <habbo-registration-form>
                        <h1>Registrati!</h1>
                        <RegistrationForm />
                    </habbo-registration-form>
                </main>
            </div>
            <Footer />
        </>
    );
}

function HeaderSmall(): ReactNode
{
    return (
        <habbo-header-small>
            <header className="header__wrapper wrapper">
                <a href="/" className="header__habbo__logo">
                    <h1 className="header__habbo__name">Habbo</h1>
                </a>
                <div className="header__aside">
                    <button type="button" onClick={() => { window.location.href = '/'; }} className="header__login__button">
                        <span className="header__login__icon">Login</span>
                    </button>
                </div>
            </header>
            <habbo-navigation>
                <NavigationBar />
            </habbo-navigation>
        </habbo-header-small>
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
            </ul>
        </nav>
    );
}

const MONTHS_IT = [
    'Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno',
    'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'
];
const CURRENT_YEAR = new Date().getFullYear();
// Stesso range ufficiale: 120 anni indietro
const YEARS = Array.from({ length: 120 }, (_, i) => CURRENT_YEAR - 7 - i);
const DAYS = Array.from({ length: 31 }, (_, i) => i + 1);

interface FormState
{
    username: string;
    password: string;
    passwordRepeat: string;
    birthDay: string;
    birthMonth: string;
    birthYear: string;
    profilePublic: boolean;
    termsAccepted: boolean;
    newsletter: boolean;
}

function RegistrationForm(): ReactNode
{
    const [state, setState] = useState<FormState>({
        username: '',
        password: '',
        passwordRepeat: '',
        birthDay: '',
        birthMonth: '',
        birthYear: '',
        profilePublic: false,
        termsAccepted: false,
        newsletter: false
    });
    const [showPw, setShowPw] = useState(false);
    const [showPw2, setShowPw2] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    function update<K extends keyof FormState>(k: K, v: FormState[K]): void
    {
        setState(s => ({ ...s, [k]: v }));
    }

    const canSubmit = state.username.length >= 3
        && state.password.length >= 6
        && state.password === state.passwordRepeat
        && state.birthDay && state.birthMonth && state.birthYear
        && state.termsAccepted
        && !loading;

    async function handleSubmit(ev: FormEvent<HTMLFormElement>): Promise<void>
    {
        ev.preventDefault();
        if(!canSubmit) return;
        setLoading(true);
        setError(null);
        try
        {
            const r = await fetch('/api/v2/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    username: state.username,
                    password: state.password,
                    birthdate: `${state.birthYear}-${state.birthMonth.padStart(2, '0')}-${state.birthDay.padStart(2, '0')}`,
                    profilePublic: state.profilePublic,
                    newsletter: state.newsletter
                })
            });
            if(!r.ok)
            {
                const j = await r.json().catch(() => ({ error: 'unknown' }));
                const map: Record<string, string> = {
                    username_taken: 'Questo nome utente è già in uso.',
                    username_invalid: 'Nome utente non valido (3-15 caratteri, lettere/numeri/underscore).',
                    password_weak: 'La password deve avere almeno 6 caratteri, una lettera e un numero o carattere speciale.',
                    password_pwned: 'Questa password è comparsa in un data breach pubblico. Scegline un\'altra.',
                    underage: 'Devi avere almeno 16 anni per registrarti.',
                    terms_required: 'Devi accettare i Termini e Condizioni.'
                };
                setError(map[j.error] || 'Registrazione fallita. Riprova.');
            }
            else
            {
                // Registrazione + auto-login OK → vai alla home autenticata.
                // Hard navigation (non React Router push) per garantire che
                // l'intero providers tree rilegga il cookie cms_v3_access
                // appena settato e useAuth() ritorni il nuovo utente.
                window.location.href = '/';
            }
        }
        catch
        {
            setError('Connessione fallita. Riprova.');
        }
        finally { setLoading(false); }
    }

    return (
        <form
            name="registrationForm"
            onSubmit={handleSubmit}
            className="form form--left registration-form"
        >
            {/* Social wrapper — posizionato absolute right da CSS ufficiale */}
            <div className="registration-form__social__wrapper">
                <div className="registration-form__social">
                    <h3 className="registration-form__connect">O scegli un altro modo per registrarti</h3>
                    <habbo-facebook-connect type="large">
                        <button type="button" className="facebook-connect">Facebook</button>
                    </habbo-facebook-connect>
                    <habbo-google-connect type="large">
                        <button type="button" className="google-connect">Google</button>
                    </habbo-google-connect>
                    <habbo-apple-connect type="large">
                        <button type="button" className="apple-connect">Accedi con Apple</button>
                    </habbo-apple-connect>
                </div>
            </div>

            {/* USERNAME — al posto del campo Email ufficiale */}
            <fieldset className="form__fieldset">
                <label htmlFor="username" className="form__label">Nome utente</label>
                <p>
                    Scegli il nome con cui ti identificheranno gli altri Habbo. 3-15 caratteri,
                    solo lettere, numeri, underscore e trattini. Non potrai cambiarlo dopo.
                </p>
                <div className="form__field">
                    <input
                        id="username"
                        name="username"
                        type="text"
                        autoComplete="username"
                        autoCapitalize="none"
                        autoCorrect="off"
                        spellCheck={false}
                        maxLength={15}
                        value={state.username}
                        onChange={e => update('username', e.target.value)}
                        className="form__input"
                        disabled={loading}
                    />
                </div>
            </fieldset>

            <habbo-password-new>
                <fieldset className="form__fieldset form__fieldset--box form__fieldset--box-top">
                    <label htmlFor="password" className="form__label">Password</label>
                    <p>Usa almeno 6 caratteri. Includi almeno una lettera e almeno un numero o carattere speciale.</p>
                    <div className="form__field">
                        <input
                            id="password"
                            name="passwordNew"
                            type={showPw ? 'text' : 'password'}
                            autoComplete="new-password"
                            value={state.password}
                            onChange={e => update('password', e.target.value)}
                            className="form__input"
                            disabled={loading}
                        />
                        <i
                            className="password-toggle-mask__icon"
                            role="button"
                            aria-label={showPw ? 'Nascondi password' : 'Mostra password'}
                            onClick={() => setShowPw(v => !v)}
                        />
                    </div>
                </fieldset>
                <fieldset className="form__fieldset form__fieldset--box form__fieldset--box-bottom">
                    <label htmlFor="password-repeat" className="form__label">Ripeti password</label>
                    <div className="form__field">
                        <input
                            id="password-repeat"
                            name="passwordNewRepeated"
                            type={showPw2 ? 'text' : 'password'}
                            autoComplete="new-password"
                            value={state.passwordRepeat}
                            onChange={e => update('passwordRepeat', e.target.value)}
                            className="form__input"
                            disabled={loading}
                        />
                        <i
                            className="password-toggle-mask__icon"
                            role="button"
                            aria-label={showPw2 ? 'Nascondi password' : 'Mostra password'}
                            onClick={() => setShowPw2(v => !v)}
                        />
                    </div>
                </fieldset>
            </habbo-password-new>

            <habbo-birthdate>
                <fieldset className="form__fieldset">
                    <label className="form__label">Data di nascita</label>
                    <p>
                        Per favore inserisci la tua data di nascita reale. Questa informazione ci servirà
                        per poterti restituire l'account nel caso in cui perdessi i dati di accesso. La tua
                        data di nascita non sarà mai divulgata. Ricorda che devi avere dai 16 anni in su
                        per giocare su Habbo!
                    </p>
                    <div className="form__field">
                        <select
                            name="birthDay"
                            value={state.birthDay}
                            onChange={e => update('birthDay', e.target.value)}
                            className="form__select birthdate__day"
                            disabled={loading}
                        >
                            <option value="">Giorno</option>
                            {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                        <select
                            name="birthMonth"
                            value={state.birthMonth}
                            onChange={e => update('birthMonth', e.target.value)}
                            className="form__select birthdate__month"
                            disabled={loading}
                        >
                            <option value="">Mese</option>
                            {MONTHS_IT.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
                        </select>
                        <select
                            name="birthYear"
                            value={state.birthYear}
                            onChange={e => update('birthYear', e.target.value)}
                            className="form__select birthdate__year"
                            disabled={loading}
                        >
                            <option value="">Anno</option>
                            {YEARS.map(y => <option key={y} value={y}>{y}</option>)}
                        </select>
                    </div>
                </fieldset>
            </habbo-birthdate>

            <habbo-profile-visibility>
                <fieldset className="form__fieldset form__fieldset--box">
                    <label className="form__label">Visibilità profilo</label>
                    <p>
                        Il tuo profilo è privato per impostazione predefinita. Spunta la casella per renderlo
                        pubblico. È possibile modificarlo anche in un secondo momento nelle impostazioni del profilo.
                    </p>
                    <div className="form__field">
                        <label htmlFor="profile-public" className="form__label form__label--checkbox">
                            <input
                                id="profile-public"
                                type="checkbox"
                                className="form__checkbox"
                                checked={state.profilePublic}
                                onChange={e => update('profilePublic', e.target.checked)}
                                disabled={loading}
                            />
                            <span>Rendi pubblico il mio profilo</span>
                        </label>
                    </div>
                </fieldset>
            </habbo-profile-visibility>

            <habbo-policies>
                <fieldset className="form__fieldset">
                    <div className="form__field">
                        <label htmlFor="terms-of-service" className="form__label form__label--checkbox">
                            <input
                                id="terms-of-service"
                                name="termsOfServiceAccepted"
                                type="checkbox"
                                className="form__checkbox"
                                checked={state.termsAccepted}
                                onChange={e => update('termsAccepted', e.target.checked)}
                                required
                                disabled={loading}
                            />
                            <span>
                                Accetto i <a href="/playing-habbo/terms-of-service" target="_blank" rel="noopener noreferrer">Termini e Condizioni</a>,
                                la <a href="/playing-habbo/privacy-notice" target="_blank" rel="noopener noreferrer">Politica sulla privacy</a> e
                                la <a href="/help/cookies" target="_blank" rel="noopener noreferrer">Politica sui cookies</a>.
                            </span>
                        </label>
                    </div>
                    <div className="form__field">
                        <label htmlFor="newsletter" className="form__label form__label--checkbox">
                            <input
                                id="newsletter"
                                type="checkbox"
                                className="form__checkbox"
                                checked={state.newsletter}
                                onChange={e => update('newsletter', e.target.checked)}
                                disabled={loading}
                            />
                            <span>Mantienimi aggiornato sugli ultimi eventi, le novità e i gossip di Habbo!</span>
                        </label>
                    </div>
                </fieldset>
            </habbo-policies>

            <p className="registration-form__safety">
                Benvenuto su Habbo! Divertiti <a href="/playing-habbo/safety" target="_blank" rel="noopener noreferrer">in tutta sicurezza</a>!
            </p>

            {error ? <p style={{ color: '#ffaaaa', padding: '6px 12px', fontWeight: 'bold' }}>{error}</p> : null}

            <div className="form__footer">
                <div style={{ width: '100%' }}>
                    <div>
                        <button
                            type="submit"
                            disabled={!canSubmit}
                            className="form__submit registration-form__button habbo-registration-button"
                            style={{ width: '100%' }}
                        >
                            {loading ? '…' : 'Fatto! Ora crea il tuo avatar!'}
                        </button>
                    </div>
                </div>
            </div>

            <p className="registration-form__purchases">Include acquisti in-app opzionali.</p>
        </form>
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
