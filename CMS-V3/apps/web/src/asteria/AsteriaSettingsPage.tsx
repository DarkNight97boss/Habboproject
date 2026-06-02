import { type ReactNode, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useParams } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { useAuth, type AuthUser } from '../hooks/useAuth';
import './asteria.css';

/**
 * /settings/:section — impostazioni account in stile Asteria (auth).
 * Sezioni: privacy, password, email, motto (+ 2fa/personaggio stub).
 * PATCH /api/v2/me/{privacy,password,email,motto}.
 */

const SECTIONS = [
    { slug: 'privacy', label: 'Privacy' },
    { slug: 'password', label: 'Password' },
    { slug: 'email', label: 'Email' },
    { slug: 'motto', label: 'Motto' },
    { slug: '2fa', label: '2FA', soon: true },
    { slug: 'personaggio', label: 'Personaggio' }
] as const;

function usePatch(path: string)
{
    const qc = useQueryClient();
    return useMutation<unknown, Error, Record<string, unknown>>({
        mutationFn: async (body) =>
        {
            const r = await fetch(`/api/v2/me/${path}`, {
                method: 'PATCH', credentials: 'include',
                headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
            });
            if(!r.ok)
            {
                const e = await r.json().catch(() => ({})) as { error?: string };
                throw new Error(e.error || 'err_' + r.status);
            }
            return r.json();
        },
        onSuccess: () => { qc.invalidateQueries({ queryKey: ['auth', 'me'] }); }
    });
}

export function AsteriaSettingsPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const params = useParams<{ section: string }>();
    const section = params.section ?? 'privacy';

    if(isLoading) return <AsteriaShell activeNav="home"><div className="asteria-news__state">Caricamento…</div></AsteriaShell>;
    if(!user) return <Navigate to="/login" replace />;

    return (
        <AsteriaShell activeNav="home">
            <section className="asteria-settings">
                <header className="asteria-settings__head">
                    <div className="asteria-hero__eyebrow">Account</div>
                    <h1 className="asteria-settings__h1">Impostazioni</h1>
                </header>
                <div className="asteria-settings__layout">
                    <nav className="asteria-settings__nav">
                        {SECTIONS.map(s => (
                            <Link key={s.slug} to={`/settings/${s.slug}`}
                                  className={`asteria-settings__navitem${section === s.slug ? ' is-active' : ''}`}>
                                {s.label}{'soon' in s && s.soon ? <span className="asteria-settings__soon">soon</span> : null}
                            </Link>
                        ))}
                    </nav>
                    <div className="asteria-settings__panel">
                        {section === 'privacy' && <PrivacyForm />}
                        {section === 'password' && <PasswordForm />}
                        {section === 'email' && <EmailForm user={user} />}
                        {section === 'motto' && <MottoForm user={user} />}
                        {section === '2fa' && <Stub title="Autenticazione a due fattori" text="Il 2FA via app authenticator per gli account utente arriverà presto. Lo staff lo usa già." />}
                        {section === 'personaggio' && <CharacterInfo user={user} />}
                    </div>
                </div>
            </section>
        </AsteriaShell>
    );
}

function Feedback({ m }: { m: ReturnType<typeof usePatch> }): ReactNode
{
    if(m.isPending) return <span className="asteria-settings__fb">Salvataggio…</span>;
    if(m.isSuccess) return <span className="asteria-settings__fb is-ok">Salvato ✓</span>;
    if(m.isError) return <span className="asteria-settings__fb is-err">{m.error.message}</span>;
    return null;
}

function PrivacyForm(): ReactNode
{
    const m = usePatch('privacy');
    const [profileHidden, setHidden] = useState(false);
    const [currencyPrivate, setCurrency] = useState(false);
    return (
        <form className="asteria-login" style={{ margin: 0, maxWidth: 'none' }} onSubmit={e => { e.preventDefault(); m.mutate({ profileHidden, currencyPrivate }); }}>
            <h2 className="asteria-settings__title">Privacy</h2>
            <label className="asteria-toggle">
                <input type="checkbox" className="asteria-checkbox" checked={profileHidden} onChange={e => setHidden(e.target.checked)} />
                <span>Nascondi il mio profilo pubblico</span>
            </label>
            <label className="asteria-toggle">
                <input type="checkbox" className="asteria-checkbox" checked={currencyPrivate} onChange={e => setCurrency(e.target.checked)} />
                <span>Nascondi le mie valute (crediti/diamanti)</span>
            </label>
            <div className="asteria-settings__actions">
                <button type="submit" className="asteria-btn" disabled={m.isPending}>Salva</button>
                <Feedback m={m} />
            </div>
        </form>
    );
}

function PasswordForm(): ReactNode
{
    const m = usePatch('password');
    const [cur, setCur] = useState(''); const [nw, setNw] = useState(''); const [cf, setCf] = useState('');
    const mismatch = nw.length > 0 && cf.length > 0 && nw !== cf;
    return (
        <form className="asteria-login" style={{ margin: 0, maxWidth: 'none' }} onSubmit={e => { e.preventDefault(); if(!mismatch && nw.length >= 6) m.mutate({ currentPassword: cur, newPassword: nw }); }}>
            <h2 className="asteria-settings__title">Cambia password</h2>
            <input className="asteria-input" type="password" placeholder="Password attuale" value={cur} onChange={e => setCur(e.target.value)} required />
            <input className="asteria-input" type="password" placeholder="Nuova password (min 6)" value={nw} onChange={e => setNw(e.target.value)} required />
            <input className="asteria-input" type="password" placeholder="Conferma nuova password" value={cf} onChange={e => setCf(e.target.value)} required />
            {mismatch && <span className="asteria-settings__fb is-err">Le password non coincidono</span>}
            <div className="asteria-settings__actions">
                <button type="submit" className="asteria-btn" disabled={m.isPending || mismatch || nw.length < 6}>Aggiorna</button>
                <Feedback m={m} />
            </div>
        </form>
    );
}

function EmailForm({ user }: { user: AuthUser }): ReactNode
{
    const m = usePatch('email');
    const [email, setEmail] = useState(user.mail || '');
    return (
        <form className="asteria-login" style={{ margin: 0, maxWidth: 'none' }} onSubmit={e => { e.preventDefault(); m.mutate({ email }); }}>
            <h2 className="asteria-settings__title">Email</h2>
            <input className="asteria-input" type="email" placeholder="La tua email" value={email} onChange={e => setEmail(e.target.value)} required />
            <div className="asteria-settings__actions">
                <button type="submit" className="asteria-btn" disabled={m.isPending}>Salva email</button>
                <Feedback m={m} />
            </div>
        </form>
    );
}

function MottoForm({ user }: { user: AuthUser }): ReactNode
{
    const m = usePatch('motto');
    const [motto, setMotto] = useState(user.motto || '');
    return (
        <form className="asteria-login" style={{ margin: 0, maxWidth: 'none' }} onSubmit={e => { e.preventDefault(); m.mutate({ motto }); }}>
            <h2 className="asteria-settings__title">Motto</h2>
            <input className="asteria-input" type="text" maxLength={127} placeholder="Il tuo motto" value={motto} onChange={e => setMotto(e.target.value)} />
            <div className="asteria-form__hint">{motto.length}/127</div>
            <div className="asteria-settings__actions">
                <button type="submit" className="asteria-btn" disabled={m.isPending}>Salva motto</button>
                <Feedback m={m} />
            </div>
        </form>
    );
}

function CharacterInfo({ user }: { user: AuthUser }): ReactNode
{
    return (
        <div>
            <h2 className="asteria-settings__title">Personaggio</h2>
            <p className="asteria-article__summary">Username: <strong>{user.username}</strong></p>
            <p className="asteria-form__hint">Il cambio username è gestito dallo staff. Apri un ticket in gioco con <code>:cfh</code>.</p>
        </div>
    );
}

function Stub({ title, text }: { title: string; text: string }): ReactNode
{
    return <div><h2 className="asteria-settings__title">{title}</h2><p className="asteria-article__summary">{text}</p></div>;
}
