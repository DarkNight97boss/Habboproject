import { useQueryClient } from '@tanstack/react-query';
import { type FormEvent, type ReactNode, useState } from 'react';
import { Navigate } from 'react-router';
import { avatarUrl, broadcastAuth, useAuth } from '../hooks/useAuth';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Pagina /registration Asteria proprietaria.
 *
 * Form completo: username + email + password + accept terms. Preview avatar
 * live (look default base) come "carta di prova". Redirect a /me al success.
 *
 * Già loggato → redirect a /me.
 */

const DEFAULT_LOOKS = [
    'hd-180-1.ch-210-66.lg-270-82.sh-290-91',
    'hd-180-2.ch-215-71.lg-270-82.sh-300-1408',
    'hd-185-1.ch-225-78.lg-275-86.sh-295-1408',
    'hd-180-9.ch-220-91.lg-270-82.sh-290-91'
];

export function AsteriaRegistrationPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [look, setLook] = useState(DEFAULT_LOOKS[0]);
    const [gender, setGender] = useState<'M' | 'F'>('M');
    const [terms, setTerms] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const qc = useQueryClient();

    if(!isLoading && user) return <Navigate to="/me" replace />;

    const passwordStrength = computeStrength(password);

    async function register(ev: FormEvent<HTMLFormElement>): Promise<void>
    {
        ev.preventDefault();
        setError(null);
        if(password !== confirm) { setError('Le password non coincidono.'); return; }
        if(passwordStrength.score < 2) { setError('Password troppo debole. Usa almeno 8 char con un numero e una maiuscola.'); return; }
        if(!terms) { setError('Devi accettare termini e privacy.'); return; }
        setLoading(true);
        try
        {
            const r = await fetch('/api/v2/auth/register', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password, look, gender })
            });
            if(!r.ok)
            {
                const j = await r.json().catch(() => ({ error: 'unknown' }));
                const map: Record<string, string> = {
                    username_taken: 'Username già in uso.',
                    email_taken: 'Email già registrata.',
                    invalid_username: 'Username non valido (3-15 caratteri, lettere/numeri).',
                    invalid_email: 'Email non valida.',
                    password_pwned: 'Password compromessa in data breach noti. Usane un\'altra.',
                    rate_limited: 'Troppe registrazioni. Riprova fra 1 ora.'
                };
                setError(map[j.error] || 'Errore registrazione. Riprova.');
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
                <div className="asteria-hero__eyebrow">Registrazione · gratis per sempre</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 8vw, 96px)' }}>
                    Crea il tuo<br />avatar.
                </h1>

                <div className="asteria-register__layout">
                    <div className="asteria-register__preview">
                        <div className="asteria-register__avatar-frame">
                            <img src={avatarUrl(look, { size: 'l' })} alt="anteprima avatar" />
                        </div>
                        <div className="asteria-register__looks">
                            <div className="asteria-form__hint">Scegli un look iniziale (cambi tutto dopo, gratis):</div>
                            <div className="asteria-register__looks-grid">
                                {DEFAULT_LOOKS.map((L, i) => (
                                    <button key={i} type="button"
                                        className={`asteria-register__look-btn ${L === look ? 'is-active' : ''}`}
                                        onClick={() => setLook(L)}>
                                        <img src={avatarUrl(L, { size: 's', headOnly: true })} alt="" />
                                    </button>
                                ))}
                            </div>
                            <div className="asteria-form__hint" style={{ marginTop: 16 }}>Sesso:</div>
                            <div className="asteria-register__looks-grid">
                                <button type="button" className={`asteria-btn ${gender === 'M' ? '' : 'asteria-btn--ghost'}`} onClick={() => setGender('M')}>♂ Maschio</button>
                                <button type="button" className={`asteria-btn ${gender === 'F' ? '' : 'asteria-btn--ghost'}`} onClick={() => setGender('F')}>♀ Femmina</button>
                            </div>
                        </div>
                    </div>

                    <form className="asteria-login" onSubmit={register} style={{ margin: 0, maxWidth: 'none' }}>
                        {error && <div className="asteria-error">{error}</div>}
                        <label className="asteria-form__label">
                            <span>Username</span>
                            <input type="text" className="asteria-input" placeholder="es. tuo_nome"
                                value={username} onChange={e => setUsername(e.target.value)}
                                pattern="[a-zA-Z0-9_.-]{3,15}" minLength={3} maxLength={15} required autoFocus />
                        </label>
                        <label className="asteria-form__label">
                            <span>Email</span>
                            <input type="email" className="asteria-input" placeholder="email@example.com"
                                value={email} onChange={e => setEmail(e.target.value)} required />
                        </label>
                        <label className="asteria-form__label">
                            <span>Password</span>
                            <input type="password" className="asteria-input" placeholder="min 8 caratteri"
                                value={password} onChange={e => setPassword(e.target.value)} minLength={8} required />
                            {password.length > 0 && (
                                <div className="asteria-strength">
                                    <div className={`asteria-strength__bar score-${passwordStrength.score}`} />
                                    <span className="asteria-strength__label">{passwordStrength.label}</span>
                                </div>
                            )}
                        </label>
                        <label className="asteria-form__label">
                            <span>Conferma password</span>
                            <input type="password" className="asteria-input" placeholder="ripeti password"
                                value={confirm} onChange={e => setConfirm(e.target.value)} minLength={8} required />
                        </label>
                        <label className="asteria-checkbox">
                            <input type="checkbox" checked={terms} onChange={e => setTerms(e.target.checked)} required />
                            <span>Accetto <a href="/playing-habbo/terms">termini</a> e <a href="/playing-habbo/privacy">privacy</a>.</span>
                        </label>
                        <button type="submit" disabled={loading} className="asteria-btn asteria-btn--xl" style={{ width: '100%', marginTop: 10 }}>
                            {loading ? 'Creazione…' : 'Crea il mio account ↗'}
                        </button>
                        <p style={{ marginTop: 16, fontSize: 12, color: '#64748b', textAlign: 'center', fontFamily: 'JetBrains Mono, monospace', letterSpacing: '0.05em' }}>
                            Hai già un account? <a href="/login" style={{ color: '#c4b5fd', textDecoration: 'none', fontWeight: 600 }}>ACCEDI →</a>
                        </p>
                    </form>
                </div>
            </section>
        </AsteriaShell>
    );
}

function computeStrength(pw: string): { score: 0 | 1 | 2 | 3 | 4; label: string }
{
    if(pw.length === 0) return { score: 0, label: '' };
    let score = 0;
    if(pw.length >= 8) score++;
    if(pw.length >= 12) score++;
    if(/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if(/\d/.test(pw)) score++;
    if(/[^a-zA-Z0-9]/.test(pw)) score++;
    score = Math.min(4, score) as 0 | 1 | 2 | 3 | 4;
    const labels: Record<number, string> = { 0: 'Troppo corta', 1: 'Debole', 2: 'Media', 3: 'Buona', 4: 'Eccellente' };
    return { score, label: labels[score] };
}
