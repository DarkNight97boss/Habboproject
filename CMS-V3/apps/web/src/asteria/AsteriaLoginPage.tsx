import { useQueryClient } from '@tanstack/react-query';
import { type FormEvent, type ReactNode, useState } from 'react';
import { Navigate } from 'react-router';
import { broadcastAuth, useAuth } from '../hooks/useAuth';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Pagina /login Asteria proprietaria. Layout single-column con form glass
 * centrato sotto un mini-hero. CTA registrazione + recupero password.
 *
 * Già loggato → redirect a /me.
 */
export function AsteriaLoginPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [remember, setRemember] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const qc = useQueryClient();

    if(!isLoading && user) return <Navigate to="/me" replace />;

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
                body: JSON.stringify({ username, password, remember })
            });
            if(!r.ok)
            {
                const j = await r.json().catch(() => ({ error: 'unknown' }));
                if(j.error === 'invalid_credentials') setError('Credenziali errate.');
                else if(j.error === 'account_locked') setError('Account bloccato. Contatta lo staff.');
                else if(j.error === 'rate_limited') setError('Troppi tentativi. Riprova fra 1 minuto.');
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
        <AsteriaShell activeNav="home" hideMarquee>
            <section className="asteria-hero" style={{ paddingTop: 60, paddingBottom: 40 }}>
                <div className="asteria-hero__eyebrow">Accedi</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>
                    Bentornato.
                </h1>
                <p className="asteria-hero__tagline" style={{ fontSize: 16 }}>
                    Entra in Asteria con il tuo account. Niente social-only: usi <em>quello che vuoi</em>.
                </p>

                <form className="asteria-login" onSubmit={login}>
                    {error && <div className="asteria-error">{error}</div>}
                    <label className="asteria-form__label">
                        <span>Nome utente</span>
                        <input
                            type="text"
                            className="asteria-input"
                            placeholder="es. devadmin"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            autoComplete="username"
                            autoFocus
                            required
                        />
                    </label>
                    <label className="asteria-form__label">
                        <span>Password</span>
                        <input
                            type="password"
                            className="asteria-input"
                            placeholder="••••••••"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            autoComplete="current-password"
                            required
                        />
                    </label>
                    <div className="asteria-form__row">
                        <label className="asteria-checkbox">
                            <input type="checkbox" checked={remember} onChange={e => setRemember(e.target.checked)} />
                            <span>Ricordami</span>
                        </label>
                        <a href="/help/password-reset" className="asteria-form__link">Password dimenticata?</a>
                    </div>
                    <button type="submit" disabled={loading} className="asteria-btn asteria-btn--xl" style={{ width: '100%', marginTop: 6 }}>
                        {loading ? 'Accesso in corso…' : 'Entra adesso ↗'}
                    </button>
                    <p style={{ marginTop: 22, fontSize: 12, color: '#64748b', textAlign: 'center', fontFamily: 'JetBrains Mono, monospace', letterSpacing: '0.05em' }}>
                        Nuovo qui? <a href="/registration" style={{ color: '#c4b5fd', textDecoration: 'none', fontWeight: 600 }}>CREA ACCOUNT GRATIS →</a>
                    </p>
                </form>
            </section>
        </AsteriaShell>
    );
}
