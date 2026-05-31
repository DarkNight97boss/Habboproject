/**
 * StaffPanelPage — pannello amministrazione (/admin)
 *
 * Visibile solo a utenti con rank >= 5 (staff Arcturus).
 * Tutto il backend è già in apps/api/src/routes/staff.ts ed è protetto
 * lato server da requireRank(5): anche se un utente normale scoprisse la
 * route /admin, le chiamate API ritornerebbero 403.
 *
 * Sezioni:
 *  - Stats (GET /api/v2/staff/stats)
 *  - Azioni RCON utente (alert/credits/duckets/diamonds/badge/disconnect/mute)
 *  - Azioni hotel (hotel-alert/refresh-catalog/refresh-wordfilter)
 *  - Audit log (GET /api/v2/staff/audit-log)
 *
 * Convenzioni UI: stessa shell `AuthedShell` delle altre pagine, contenuto
 * stilizzato con classi della CSS ufficiale habbo.it (habbo-web-pages,
 * habbo-card, habbo-tabs, habbo-tab) per coerenza visiva.
 */

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type CSSProperties, type FormEvent, type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

interface StaffStats
{
    totalUsers: number;
    onlineUsers: number;
    totalRooms: number;
    totalPhotos: number;
    staffCount: number;
}

interface AuditEntry
{
    id: number;
    userId: number | null;
    username: string | null;
    action: string;
    ip: string;
    userAgent: string;
    details: unknown;
    createdAt: number;
}

type RconActionResult = { ok: boolean; error?: string } | null;

async function staffPost<T = RconActionResult>(path: string, body: unknown): Promise<T>
{
    const r = await fetch(`/api/v2/staff${path}`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(body)
    });
    if(!r.ok)
    {
        const txt = await r.text().catch(() => '');
        throw new Error(`${r.status}: ${txt || r.statusText}`);
    }
    return (await r.json()) as T;
}

export function StaffPanelPage(): ReactNode
{
    const auth = useAuth();
    const user = auth.data;

    // Loading state: aspetta che useAuth abbia risolto.
    // NB: niente chrome qui perché AuthedShell richiede user non-null.
    if(auth.isLoading)
    {
        return (
            <div style={{ padding: 40, textAlign: 'center' }}>
                <p>Caricamento…</p>
            </div>
        );
    }

    // Non autenticato → home
    if(!user) return <Navigate to="/" replace />;

    // Autenticato ma non staff → 404-like (non riveliamo l'esistenza di /admin)
    if(user.rank < 5) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user}>
            <habbo-web-pages>
                <div className="staff-panel" style={{ padding: '24px 40px', maxWidth: 1200, margin: '0 auto' }}>
                    <header style={{ marginBottom: 24, paddingBottom: 16, borderBottom: '2px solid #5a749b' }}>
                        <h1 style={{ fontSize: 28, fontWeight: 700, color: '#1b3850', marginBottom: 4 }}>
                            Pannello Amministrazione
                        </h1>
                        <p style={{ color: '#5a749b', fontSize: 14 }}>
                            Strumenti RCON live verso l'EMU Arcturus · rank attuale: <strong>{user.rank}</strong>
                        </p>
                    </header>

                    <StatsSection />

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginTop: 24 }}>
                        <UserActionsCard />
                        <HotelActionsCard />
                    </div>

                    <AuditLogSection />
                </div>
            </habbo-web-pages>
        </AuthedShell>
    );
}

// ============================================================
// Stats dashboard
// ============================================================
function StatsSection(): ReactNode
{
    const q = useQuery<StaffStats>({
        queryKey: ['staff', 'stats'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/staff/stats', { credentials: 'include' });
            if(!r.ok) throw new Error('stats_failed');
            return r.json() as Promise<StaffStats>;
        },
        refetchInterval: 30_000
    });

    const stats = q.data;

    return (
        <section>
            <h2 style={{ fontSize: 18, fontWeight: 700, color: '#1b3850', marginBottom: 12 }}>Statistiche Hotel</h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 12 }}>
                <StatCard label="Utenti totali" value={stats?.totalUsers} />
                <StatCard label="Online ora" value={stats?.onlineUsers} highlight />
                <StatCard label="Stanze pubbliche" value={stats?.totalRooms} />
                <StatCard label="Foto totali" value={stats?.totalPhotos} />
                <StatCard label="Staff" value={stats?.staffCount} />
            </div>
        </section>
    );
}

function StatCard({ label, value, highlight }: { label: string; value: number | undefined; highlight?: boolean }): ReactNode
{
    return (
        <div style={{
            background: highlight ? '#1b3850' : '#fff',
            color: highlight ? '#fff' : '#1b3850',
            border: '1px solid #5a749b',
            borderRadius: 8,
            padding: '12px 16px',
            textAlign: 'center'
        }}>
            <div style={{ fontSize: 24, fontWeight: 700 }}>
                {value === undefined ? '—' : value.toLocaleString('it-IT')}
            </div>
            <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: 4 }}>{label}</div>
        </div>
    );
}

// ============================================================
// Azioni utente (alert/credits/duckets/diamonds/badge/kick/mute)
// ============================================================
type UserActionKind = 'alert' | 'credits' | 'duckets' | 'diamonds' | 'badge' | 'disconnect' | 'mute';

function UserActionsCard(): ReactNode
{
    const [kind, setKind] = useState<UserActionKind>('alert');
    const [username, setUsername] = useState('');
    const [message, setMessage] = useState('');
    const [amount, setAmount] = useState('100');
    const [badge, setBadge] = useState('ACH_BasicClub1');
    const [minutes, setMinutes] = useState('5');
    const [feedback, setFeedback] = useState<{ ok: boolean; text: string } | null>(null);

    const qc = useQueryClient();

    const mut = useMutation({
        mutationFn: async (): Promise<RconActionResult> =>
        {
            switch(kind)
            {
                case 'alert':
                    return staffPost('/alert', { username, message });
                case 'credits':
                    return staffPost('/credits', { username, amount: Number(amount) });
                case 'duckets':
                    return staffPost('/duckets', { username, amount: Number(amount) });
                case 'diamonds':
                    return staffPost('/diamonds', { username, amount: Number(amount) });
                case 'badge':
                    return staffPost('/badge', { username, badge });
                case 'disconnect':
                    return staffPost('/disconnect', { username });
                case 'mute':
                    return staffPost('/mute', { username, minutes: Number(minutes) });
            }
        },
        onSuccess: (r) =>
        {
            if(r?.ok) setFeedback({ ok: true, text: `${kind} eseguito su ${username}` });
            else setFeedback({ ok: false, text: r?.error ?? 'errore_rcon' });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        },
        onError: (e: Error) => setFeedback({ ok: false, text: e.message })
    });

    function onSubmit(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        setFeedback(null);
        mut.mutate();
    }

    return (
        <article className="habbo-card" style={{ background: '#fff', border: '1px solid #5a749b', borderRadius: 8, padding: 20 }}>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1b3850', marginBottom: 16 }}>Azioni utente</h3>

            <form onSubmit={onSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <label>
                    <span style={labelStyle}>Tipo azione</span>
                    <select
                        value={kind}
                        onChange={e => setKind(e.target.value as UserActionKind)}
                        style={inputStyle}
                    >
                        <option value="alert">Alert popup in-game</option>
                        <option value="credits">Crediti (+/-)</option>
                        <option value="duckets">Duckets (+/-)</option>
                        <option value="diamonds">Diamanti (+/-)</option>
                        <option value="badge">Assegna badge</option>
                        <option value="disconnect">Disconnetti (kick)</option>
                        <option value="mute">Mute (minuti)</option>
                    </select>
                </label>

                <label>
                    <span style={labelStyle}>Username target</span>
                    <input value={username} onChange={e => setUsername(e.target.value)} required style={inputStyle} />
                </label>

                {kind === 'alert' && (
                    <label>
                        <span style={labelStyle}>Messaggio</span>
                        <textarea value={message} onChange={e => setMessage(e.target.value)} required rows={3} style={{ ...inputStyle, resize: 'vertical' }} />
                    </label>
                )}

                {(kind === 'credits' || kind === 'duckets' || kind === 'diamonds') && (
                    <label>
                        <span style={labelStyle}>Quantità (negativa = togli)</span>
                        <input type="number" value={amount} onChange={e => setAmount(e.target.value)} required style={inputStyle} />
                    </label>
                )}

                {kind === 'badge' && (
                    <label>
                        <span style={labelStyle}>Codice badge (es. ACH_BasicClub1)</span>
                        <input value={badge} onChange={e => setBadge(e.target.value)} required style={inputStyle} />
                    </label>
                )}

                {kind === 'mute' && (
                    <label>
                        <span style={labelStyle}>Durata (minuti)</span>
                        <input type="number" min={1} max={1440} value={minutes} onChange={e => setMinutes(e.target.value)} required style={inputStyle} />
                    </label>
                )}

                <button
                    type="submit"
                    disabled={mut.isPending}
                    style={buttonStyle}
                >
                    {mut.isPending ? 'Invio…' : 'Esegui via RCON'}
                </button>

                {feedback && (
                    <div style={{
                        padding: '8px 12px',
                        borderRadius: 4,
                        background: feedback.ok ? '#d4edda' : '#f8d7da',
                        color: feedback.ok ? '#155724' : '#721c24',
                        fontSize: 13
                    }}>
                        {feedback.text}
                    </div>
                )}
            </form>
        </article>
    );
}

// ============================================================
// Azioni hotel (broadcast/catalog/wordfilter)
// ============================================================
function HotelActionsCard(): ReactNode
{
    const [broadcast, setBroadcast] = useState('');
    const [feedback, setFeedback] = useState<{ ok: boolean; text: string } | null>(null);
    const qc = useQueryClient();

    const broadcastMut = useMutation({
        mutationFn: () => staffPost('/hotel-alert', { message: broadcast }),
        onSuccess: (r) =>
        {
            if(r?.ok)
            {
                setFeedback({ ok: true, text: 'Broadcast inviato a tutto l\'hotel' });
                setBroadcast('');
            }
            else setFeedback({ ok: false, text: r?.error ?? 'errore_rcon' });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        },
        onError: (e: Error) => setFeedback({ ok: false, text: e.message })
    });

    const catalogMut = useMutation({
        mutationFn: () => staffPost('/refresh-catalog', {}),
        onSuccess: (r) =>
        {
            setFeedback(r?.ok ? { ok: true, text: 'Catalogo ricaricato' } : { ok: false, text: r?.error ?? 'errore_rcon' });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        },
        onError: (e: Error) => setFeedback({ ok: false, text: e.message })
    });

    const wordfilterMut = useMutation({
        mutationFn: () => staffPost('/refresh-wordfilter', {}),
        onSuccess: (r) =>
        {
            setFeedback(r?.ok ? { ok: true, text: 'Wordfilter ricaricato' } : { ok: false, text: r?.error ?? 'errore_rcon' });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        },
        onError: (e: Error) => setFeedback({ ok: false, text: e.message })
    });

    return (
        <article className="habbo-card" style={{ background: '#fff', border: '1px solid #5a749b', borderRadius: 8, padding: 20 }}>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1b3850', marginBottom: 16 }}>Azioni hotel</h3>

            <form
                onSubmit={e => { e.preventDefault(); setFeedback(null); broadcastMut.mutate(); }}
                style={{ marginBottom: 20 }}
            >
                <label>
                    <span style={labelStyle}>Broadcast a tutto l'hotel</span>
                    <textarea
                        value={broadcast}
                        onChange={e => setBroadcast(e.target.value)}
                        required
                        rows={3}
                        placeholder="Messaggio popup mostrato a tutti gli utenti collegati…"
                        style={{ ...inputStyle, resize: 'vertical' }}
                    />
                </label>
                <button type="submit" disabled={broadcastMut.isPending} style={{ ...buttonStyle, marginTop: 8 }}>
                    {broadcastMut.isPending ? 'Invio…' : 'Invia broadcast'}
                </button>
            </form>

            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                <button
                    type="button"
                    disabled={catalogMut.isPending}
                    onClick={() => { setFeedback(null); catalogMut.mutate(); }}
                    style={{ ...buttonStyle, background: '#5a749b' }}
                >
                    {catalogMut.isPending ? '…' : 'Ricarica catalogo'}
                </button>
                <button
                    type="button"
                    disabled={wordfilterMut.isPending}
                    onClick={() => { setFeedback(null); wordfilterMut.mutate(); }}
                    style={{ ...buttonStyle, background: '#5a749b' }}
                >
                    {wordfilterMut.isPending ? '…' : 'Ricarica wordfilter'}
                </button>
            </div>

            {feedback && (
                <div style={{
                    marginTop: 12,
                    padding: '8px 12px',
                    borderRadius: 4,
                    background: feedback.ok ? '#d4edda' : '#f8d7da',
                    color: feedback.ok ? '#155724' : '#721c24',
                    fontSize: 13
                }}>
                    {feedback.text}
                </div>
            )}
        </article>
    );
}

// ============================================================
// Audit log viewer
// ============================================================
function AuditLogSection(): ReactNode
{
    const q = useQuery<{ entries: AuditEntry[] }>({
        queryKey: ['staff', 'audit-log'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/staff/audit-log?limit=50', { credentials: 'include' });
            if(!r.ok) throw new Error('audit_failed');
            return r.json() as Promise<{ entries: AuditEntry[] }>;
        },
        refetchInterval: 15_000
    });

    const entries = q.data?.entries ?? [];

    return (
        <section style={{ marginTop: 32 }}>
            <h2 style={{ fontSize: 18, fontWeight: 700, color: '#1b3850', marginBottom: 12 }}>
                Audit log <span style={{ fontSize: 12, fontWeight: 400, color: '#5a749b' }}>(ultime 50 azioni)</span>
            </h2>
            <div style={{ background: '#fff', border: '1px solid #5a749b', borderRadius: 8, overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                    <thead>
                        <tr style={{ background: '#1b3850', color: '#fff' }}>
                            <th style={thStyle}>Quando</th>
                            <th style={thStyle}>Attore</th>
                            <th style={thStyle}>Azione</th>
                            <th style={thStyle}>IP</th>
                            <th style={thStyle}>Dettagli</th>
                        </tr>
                    </thead>
                    <tbody>
                        {entries.length === 0 && (
                            <tr><td colSpan={5} style={{ padding: 24, textAlign: 'center', color: '#5a749b' }}>
                                {q.isLoading ? 'Caricamento…' : 'Nessuna azione registrata.'}
                            </td></tr>
                        )}
                        {entries.map(e => (
                            <tr key={e.id} style={{ borderTop: '1px solid #e0e6ec' }}>
                                <td style={tdStyle}>{new Date(e.createdAt * 1000).toLocaleString('it-IT')}</td>
                                <td style={tdStyle}>{e.username ?? `#${e.userId ?? '?'}`}</td>
                                <td style={{ ...tdStyle, fontFamily: 'monospace', fontSize: 12 }}>{e.action}</td>
                                <td style={{ ...tdStyle, fontFamily: 'monospace', fontSize: 12 }}>{e.ip}</td>
                                <td style={{ ...tdStyle, fontFamily: 'monospace', fontSize: 11, color: '#5a749b', maxWidth: 320, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {e.details ? JSON.stringify(e.details) : '—'}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// ============================================================
// Style helpers (inline così non sporchiamo la CSS ufficiale)
// ============================================================
const labelStyle: CSSProperties = {
    display: 'block',
    fontSize: 12,
    fontWeight: 600,
    color: '#1b3850',
    marginBottom: 4,
    textTransform: 'uppercase',
    letterSpacing: 0.3
};

const inputStyle: CSSProperties = {
    width: '100%',
    padding: '8px 10px',
    border: '1px solid #c5cfdb',
    borderRadius: 4,
    fontSize: 14,
    fontFamily: 'inherit'
};

const buttonStyle: CSSProperties = {
    background: '#fbd33f',
    color: '#1b3850',
    border: 'none',
    borderRadius: 4,
    padding: '10px 16px',
    fontSize: 14,
    fontWeight: 700,
    cursor: 'pointer'
};

const thStyle: CSSProperties = {
    padding: '10px 12px',
    textAlign: 'left',
    fontWeight: 600,
    fontSize: 11,
    textTransform: 'uppercase',
    letterSpacing: 0.5
};

const tdStyle: CSSProperties = {
    padding: '8px 12px',
    color: '#1b3850'
};
