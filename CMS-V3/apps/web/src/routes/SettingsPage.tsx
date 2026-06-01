import { type FormEvent, type ReactNode, useState } from 'react';
import { Navigate, useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { ThemePicker } from '../components/ThemePicker';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /settings/:section?
 *
 * Layout DOM mirror habbo.it/settings/privacy:
 *   <main.wrapper--content>
 *     <header.settings__header>
 *       <h1>Impostazioni</h1>
 *       <nav>PRIVACY|PROTEZIONE ACCOUNT|2FA|PASSWORD|EMAIL|PERSONAGGI|…
 *     <section>
 *       <div.main--fixed>
 *         <form>… sub-section settings …
 *       <habbo-web-pages.aside.aside--box> CONSIGLI DI SICUREZZA + SERVE AIUTO
 *
 * Sezioni: privacy (default) | password | email | motto. Le altre
 * (2FA, personaggi, wallet) le aggiungeremo quando avranno backend.
 */
type Section = 'privacy' | 'account-protection' | '2fa' | 'password' | 'email' | 'characters' | 'motto' | 'appearance';

const SECTIONS: { key: Section; label: string }[] = [
    { key: 'privacy', label: 'Privacy' },
    { key: 'account-protection', label: 'Protezione Account' },
    { key: '2fa', label: '2FA' },
    { key: 'password', label: 'Password' },
    { key: 'email', label: 'Email' },
    { key: 'characters', label: 'Personaggi' },
    { key: 'motto', label: 'Motto' },
    { key: 'appearance', label: 'Aspetto' }
];

export function SettingsPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const params = useParams<{ section?: string }>();
    const section = (params.section as Section | undefined) ?? 'privacy';
    const validSection = SECTIONS.some(s => s.key === section) ? section : 'privacy';

    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user}>
            <main className="wrapper wrapper--content">
                <header style={{ background: 'rgba(0,0,0,.3)', padding: '12px 16px', margin: '0 -12px 16px', borderRadius: 3, color: '#fff' }}>
                    <h1 style={{ margin: '0 0 8px', textTransform: 'uppercase' }}>Impostazioni</h1>
                    <nav>
                        {SECTIONS.map(s => (
                            <a
                                key={s.key}
                                href={`/settings/${s.key}`}
                                style={{
                                    display: 'inline-block',
                                    padding: '6px 12px',
                                    marginRight: 8,
                                    color: '#fff',
                                    textTransform: 'uppercase',
                                    fontWeight: s.key === validSection ? 'bold' : 'normal',
                                    borderBottom: s.key === validSection ? '2px solid #25b8ee' : '2px solid transparent'
                                }}
                            >
                                {s.label}
                            </a>
                        ))}
                    </nav>
                </header>
                <section>
                    <habbo-compile className="main main--fixed">
                        {validSection === 'privacy' && <PrivacySection />}
                        {validSection === 'account-protection' && <AccountProtectionSection />}
                        {validSection === '2fa' && <TwoFactorSection />}
                        {validSection === 'password' && <PasswordSection />}
                        {validSection === 'email' && <EmailSection currentEmail={user.mail} />}
                        {validSection === 'characters' && <CharactersSection username={user.username} />}
                        {validSection === 'motto' && <MottoSection currentMotto={user.motto} />}
                        {validSection === 'appearance' && <ThemePicker />}
                    </habbo-compile>
                    <habbo-web-pages className="aside aside--box aside--fixed">
                        <aside className="static-content">
                            <h3>Consigli di sicurezza</h3>
                            <p>Sentiti sempre al sicuro su internet! Impara come <a href="/playing-habbo/safety">navigare in sicurezza</a>.</p>
                        </aside>
                    </habbo-web-pages>
                </section>
            </main>
        </AuthedShell>
    );
}

function PrivacySection(): ReactNode
{
    const [profileHidden, setProfileHidden] = useState(false);
    const [currencyPrivate, setCurrencyPrivate] = useState(false);
    const [msg, setMsg] = useState<string | null>(null);

    async function save(ev: FormEvent<HTMLFormElement>)
    {
        ev.preventDefault();
        const r = await fetch('/api/v2/me/privacy', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ profileHidden, currencyPrivate })
        });
        setMsg(r.ok ? '✓ Salvato.' : '✗ Errore nel salvataggio.');
    }

    return (
        <form onSubmit={save} style={{ padding: 16, color: '#fff' }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Impostazioni Privacy</h2>
            <h4 style={{ margin: '16px 0 4px', textTransform: 'uppercase' }}>Visibilità profilo</h4>
            <p style={{ margin: '0 0 8px', opacity: 0.85 }}>Chi può vedere la pagina del tuo profilo:</p>
            <label style={{ display: 'block', margin: '4px 0' }}>
                <input type="radio" checked={!profileHidden} onChange={() => setProfileHidden(false)} /> Tutti
            </label>
            <label style={{ display: 'block', margin: '4px 0' }}>
                <input type="radio" checked={profileHidden} onChange={() => setProfileHidden(true)} /> Nessuno
            </label>
            <h4 style={{ margin: '16px 0 4px', textTransform: 'uppercase' }}>Crediti e diamanti</h4>
            <label style={{ display: 'block', margin: '4px 0' }}>
                <input type="checkbox" checked={currencyPrivate} onChange={e => setCurrencyPrivate(e.target.checked)} /> Nascondi le mie valute negli altri profili
            </label>
            <button type="submit" style={{ marginTop: 16, padding: '8px 16px', background: '#0f7dbc', color: 'white', border: '2px solid #2a9cde', borderRadius: 5, cursor: 'pointer', fontWeight: 'bold' }}>SALVA</button>
            {msg && <span style={{ marginLeft: 12 }}>{msg}</span>}
        </form>
    );
}

function PasswordSection(): ReactNode
{
    const [current, setCurrent] = useState('');
    const [next, setNext] = useState('');
    const [confirm, setConfirm] = useState('');
    const [msg, setMsg] = useState<string | null>(null);

    async function save(ev: FormEvent<HTMLFormElement>)
    {
        ev.preventDefault();
        setMsg(null);
        if(next !== confirm) { setMsg('✗ Le nuove password non coincidono.'); return; }
        if(next.length < 6) { setMsg('✗ La nuova password è troppo corta (min 6).'); return; }
        const r = await fetch('/api/v2/me/password', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentPassword: current, newPassword: next })
        });
        if(r.ok) { setMsg('✓ Password aggiornata.'); setCurrent(''); setNext(''); setConfirm(''); }
        else { const j = await r.json().catch(() => ({})); setMsg('✗ ' + (j.error === 'invalid_current_password' ? 'Password attuale errata.' : 'Errore.')); }
    }

    return (
        <form onSubmit={save} style={{ padding: 16, color: '#fff', maxWidth: 420 }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Cambia password</h2>
            <label style={{ display: 'block', margin: '8px 0' }}>Password attuale<br /><input type="password" value={current} onChange={e => setCurrent(e.target.value)} className="form__input" style={{ width: '100%' }} required /></label>
            <label style={{ display: 'block', margin: '8px 0' }}>Nuova password<br /><input type="password" value={next} onChange={e => setNext(e.target.value)} className="form__input" style={{ width: '100%' }} minLength={6} required /></label>
            <label style={{ display: 'block', margin: '8px 0' }}>Conferma nuova password<br /><input type="password" value={confirm} onChange={e => setConfirm(e.target.value)} className="form__input" style={{ width: '100%' }} required /></label>
            <button type="submit" style={{ marginTop: 8, padding: '8px 16px', background: '#0f7dbc', color: 'white', border: '2px solid #2a9cde', borderRadius: 5, cursor: 'pointer', fontWeight: 'bold' }}>AGGIORNA</button>
            {msg && <p style={{ marginTop: 8 }}>{msg}</p>}
        </form>
    );
}

function EmailSection({ currentEmail }: { currentEmail: string }): ReactNode
{
    const [email, setEmail] = useState(currentEmail || '');
    const [msg, setMsg] = useState<string | null>(null);

    async function save(ev: FormEvent<HTMLFormElement>)
    {
        ev.preventDefault();
        const r = await fetch('/api/v2/me/email', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        setMsg(r.ok ? '✓ Email aggiornata.' : '✗ Email non valida.');
    }

    return (
        <form onSubmit={save} style={{ padding: 16, color: '#fff', maxWidth: 420 }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Email</h2>
            <p style={{ margin: '0 0 12px', opacity: 0.85 }}>L'email è usata per il recupero password. Non riceverai mai newsletter o marketing.</p>
            <label style={{ display: 'block', margin: '8px 0' }}>Email<br /><input type="email" value={email} onChange={e => setEmail(e.target.value)} className="form__input" style={{ width: '100%' }} required /></label>
            <button type="submit" style={{ marginTop: 8, padding: '8px 16px', background: '#0f7dbc', color: 'white', border: '2px solid #2a9cde', borderRadius: 5, cursor: 'pointer', fontWeight: 'bold' }}>AGGIORNA</button>
            {msg && <p style={{ marginTop: 8 }}>{msg}</p>}
        </form>
    );
}

function MottoSection({ currentMotto }: { currentMotto: string }): ReactNode
{
    const [motto, setMotto] = useState(currentMotto || '');
    const [msg, setMsg] = useState<string | null>(null);

    async function save(ev: FormEvent<HTMLFormElement>)
    {
        ev.preventDefault();
        const r = await fetch('/api/v2/me/motto', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ motto })
        });
        setMsg(r.ok ? '✓ Motto aggiornato.' : '✗ Motto non valido (max 127 caratteri).');
    }

    return (
        <form onSubmit={save} style={{ padding: 16, color: '#fff', maxWidth: 420 }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Motto</h2>
            <p style={{ margin: '0 0 12px', opacity: 0.85 }}>Il messaggio sotto il tuo avatar in chat. Max 127 caratteri.</p>
            <label style={{ display: 'block', margin: '8px 0' }}>Motto<br /><input type="text" value={motto} onChange={e => setMotto(e.target.value)} maxLength={127} className="form__input" style={{ width: '100%' }} /></label>
            <button type="submit" style={{ marginTop: 8, padding: '8px 16px', background: '#0f7dbc', color: 'white', border: '2px solid #2a9cde', borderRadius: 5, cursor: 'pointer', fontWeight: 'bold' }}>AGGIORNA</button>
            {msg && <p style={{ marginTop: 8 }}>{msg}</p>}
        </form>
    );
}

function AccountProtectionSection(): ReactNode
{
    return (
        <div style={{ padding: 16, color: '#fff' }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Protezione Account</h2>
            <p style={{ opacity: 0.85 }}>
                Blocca o sblocca l'accesso al tuo account. Quando bloccato, nessuno potrà fare login
                anche con la password corretta — utile se sospetti che qualcuno abbia visto le tue
                credenziali. Lo sblocchi via email o contattando lo staff.
            </p>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: 16, borderRadius: 5, marginTop: 16 }}>
                <h3 style={{ margin: '0 0 8px', textTransform: 'uppercase' }}>Stato attuale</h3>
                <p style={{ margin: 0 }}>🟢 Account <strong>attivo</strong></p>
                <button
                    type="button"
                    disabled
                    style={{ marginTop: 12, padding: '8px 16px', background: '#666', color: 'white', border: 'none', borderRadius: 5, cursor: 'not-allowed', opacity: 0.6 }}
                    title="Disponibile a breve"
                >
                    BLOCCA ACCOUNT (in arrivo)
                </button>
            </div>
        </div>
    );
}

function TwoFactorSection(): ReactNode
{
    return (
        <div style={{ padding: 16, color: '#fff' }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Autenticazione a 2 Fattori (2FA)</h2>
            <p style={{ opacity: 0.85 }}>
                Aggiungi un livello extra di protezione richiedendo un codice TOTP dall'app authenticator
                (Google Authenticator, Authy, 1Password, ecc.) al login.
            </p>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: 16, borderRadius: 5, marginTop: 16 }}>
                <h3 style={{ margin: '0 0 8px', textTransform: 'uppercase' }}>Stato 2FA</h3>
                <p style={{ margin: 0 }}>🔒 2FA <strong>non attivo</strong></p>
                <p style={{ fontSize: 12, opacity: 0.7, margin: '8px 0' }}>
                    Lo staff ha già 2FA obbligatoria. Per gli utenti normali il 2FA opt-in è in arrivo.
                </p>
                <button
                    type="button"
                    disabled
                    style={{ marginTop: 8, padding: '8px 16px', background: '#666', color: 'white', border: 'none', borderRadius: 5, cursor: 'not-allowed', opacity: 0.6 }}
                >
                    ATTIVA 2FA (in arrivo)
                </button>
            </div>
        </div>
    );
}

function CharactersSection({ username }: { username: string }): ReactNode
{
    return (
        <div style={{ padding: 16, color: '#fff' }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Personaggi</h2>
            <p style={{ opacity: 0.85 }}>
                Habboproject permette <strong>un solo personaggio per persona</strong> (anti-multiaccount
                tramite fingerprint browser + IP). Questo per garantire fair-play e mantenere la community
                sana — niente smurf, niente alt account abusati.
            </p>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: 16, borderRadius: 5, marginTop: 16 }}>
                <h3 style={{ margin: '0 0 8px', textTransform: 'uppercase' }}>Il tuo personaggio</h3>
                <p style={{ margin: 0, fontSize: 18 }}>👤 <strong>{username}</strong></p>
                <p style={{ fontSize: 12, opacity: 0.7, margin: '8px 0' }}>
                    Per cambiare nome utente, contatta lo staff (<code>:cfh</code> in chat).
                    Il cambio è disponibile una sola volta a vita.
                </p>
            </div>
        </div>
    );
}
