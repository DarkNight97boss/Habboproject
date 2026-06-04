/**
 * StaffPanelPage — pannello amministrazione (/admin)
 *
 * Visibile solo a utenti con rank >= 5 (staff Arcturus). Si appoggia
 * sull'AuthedShell per chrome + header, e usa la propria CSS in
 * `styles/admin.css` (palette habbo + contrast AAA).
 *
 * Sezioni (route /admin/:section):
 *   • overview          (default) — dashboard stats + audit log live
 *   • users             — lookup utente + edit (rank, motto, mail, look) + reset password + strike
 *   • bans              — lista ban con search + add ban + unban
 *   • logs              — chat room / chat PM / commandlogs / namechange
 *   • iptool            — IP / machine / username lookup (clone detection)
 *   • wordfilter        — list + upsert + delete
 *   • vouchers          — list + create + delete
 *   • staff             — staff page list (rank >= 5)
 *   • actions           — RCON quick actions (alert, broadcast, credits, badge, ...)
 *
 * Tutto il backend è in apps/api/src/routes/staff.ts protetto da
 * requireRank(5).
 */

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent, type ReactNode } from 'react';
import { Navigate, NavLink, useParams } from 'react-router';
import { SkinPicker } from '../components/SkinPicker';
import { SkinBuilder } from '../components/admin/SkinBuilder';
import { avatarUrl, useAuth } from '../hooks/useAuth';
import '../styles/admin.css';

// =====================================================================
// SHELL
// =====================================================================
export function StaffPanelPage(): ReactNode
{
    const auth = useAuth();
    const user = auth.data;
    const { section = 'overview' } = useParams();

    if(auth.isLoading) return <div style={{ padding: 40, textAlign: 'center' }}>Caricamento…</div>;
    if(!user) return <Navigate to="/" replace />;
    if(user.rank < 5) return <Navigate to="/" replace />;

    // Standalone: NESSUNA chrome habbo/AuthedShell → il pannello è indipendente
    // dallo skin del sito (design proprio in admin.css).
    return (
        <div className="admin-root">
            <div className="admin-topbar">
                <div className="admin-topbar__brand"><span className="admin-topbar__logo">✦</span> Asteria <strong>Admin</strong></div>
                <div className="admin-topbar__right">
                    <span className="admin-topbar__user">{user.username}</span>
                    <span className="admin-header__badge">RANK {user.rank}</span>
                    <a href="/" className="admin-btn admin-btn--ghost admin-btn--small">← Torna al sito</a>
                </div>
            </div>
            <div className="admin-shell">
                <Sidebar />
                <main className="admin-main">
                    <header className="admin-header">
                        <div>
                            <h1 className="admin-header__title">Pannello Amministrazione</h1>
                            <p className="admin-header__subtitle">Strumenti staff · gestione hotel Asteria</p>
                        </div>
                        <div><span className="admin-header__badge">RANK {user.rank}</span></div>
                    </header>

                    {section === 'overview'   && <OverviewSection />}
                    {section === 'users'      && <UsersSection />}
                    {section === 'bans'       && <BansSection />}
                    {section === 'logs'       && <LogsSection />}
                    {section === 'iptool'     && <IpToolSection />}
                    {section === 'wordfilter' && <WordfilterSection />}
                    {section === 'vouchers'   && <VouchersSection />}
                    {section === 'news'       && <NewsSection />}
                    {section === 'staff'      && <StaffListSection />}
                    {section === 'actions'    && <ActionsSection />}
                    {section === 'appearance' && <AppearanceSection />}
                    {section === 'skinbuilder' && <SkinBuilder />}
                    {section === 'analytics'  && <AnalyticsSection />}
                    {section === 'moderation' && <ModerationSection />}
                    {section === 'alerts'     && <AlertsSection />}
                    {section === 'flags'      && <FlagsSection />}
                </main>
            </div>
        </div>
    );
}

// =====================================================================
// SIDEBAR
// =====================================================================
function Sidebar(): ReactNode
{
    const item = (to: string, icon: string, label: string): ReactNode => (
        <li>
            <NavLink to={to} end className={({ isActive }) => isActive ? 'is-active' : ''}>
                <span className="admin-sidebar__icon" aria-hidden>{icon}</span>
                <span>{label}</span>
            </NavLink>
        </li>
    );

    return (
        <nav className="admin-sidebar">
            <div className="admin-sidebar__title">Sezioni</div>

            <div className="admin-sidebar__group">Generale</div>
            <ul className="admin-sidebar__nav">
                {item('/admin', '◉', 'Panoramica')}
                {item('/admin/actions', '⚡', 'Azioni rapide')}
                {item('/admin/appearance', '◈', 'Aspetto sito')}
                {item('/admin/skinbuilder', '✚', 'Skin Builder')}
            </ul>

            <div className="admin-sidebar__group">Moderazione</div>
            <ul className="admin-sidebar__nav">
                {item('/admin/moderation', '🛡', 'Coda CFH')}
                {item('/admin/alerts', '🚨', 'Alert staff')}
                {item('/admin/flags', '🚩', 'Feature flag')}
                {item('/admin/users', '◐', 'Utenti')}
                {item('/admin/bans', '⛔', 'Ban')}
                {item('/admin/logs', '☷', 'Log')}
                {item('/admin/iptool', '◧', 'IP / Clone')}
                {item('/admin/wordfilter', '◊', 'Word filter')}
            </ul>

            <div className="admin-sidebar__group">Contenuti</div>
            <ul className="admin-sidebar__nav">
                {item('/admin/news', '▤', 'News')}
            </ul>

            <div className="admin-sidebar__group">Economia</div>
            <ul className="admin-sidebar__nav">
                {item('/admin/analytics', '📊', 'Analytics')}
                {item('/admin/vouchers', '◆', 'Voucher')}
            </ul>

            <div className="admin-sidebar__group">Team</div>
            <ul className="admin-sidebar__nav">
                {item('/admin/staff', '★', 'Staff')}
            </ul>
        </nav>
    );
}

// =====================================================================
// SECTION — APPEARANCE (tema sito globale)
// =====================================================================
function AppearanceSection(): ReactNode
{
    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Aspetto del sito (Skin Engine)</h2>
            <SkinPicker />
        </div>
    );
}

// =====================================================================
// SECTION — ANALYTICS SHOP (Fase 20)
// =====================================================================
interface ShopAnalytics {
    totals: { orders: number; paid: number; revenueCents: number; conversionPct: number };
    byStatus: { status: string; count: number }[];
    series: { day: string; revenueCents: number; orders: number }[];
    topItems: { name: string; count: number; revenueCents: number }[];
}
interface OrderRow {
    id: number; userId: number; username: string | null; item: string | null;
    status: string; amountCents: number; currency: string;
    createdAt: string; paidAt: string | null; deliveryLog: string | null;
}

function statusTone(s: string): string
{
    if(s === 'paid') return 'success';
    if(s === 'failed') return 'danger';
    if(s === 'refunded') return 'warning';
    return 'neutral';
}
const eur = (cents: number): string => `€ ${(cents / 100).toLocaleString('it-IT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function AnalyticsSection(): ReactNode
{
    const q = useQuery({ queryKey: [ 'staff', 'shop-analytics' ], queryFn: () => apiGet<ShopAnalytics>('/shop/analytics'), refetchInterval: 60_000 });
    const ord = useQuery({ queryKey: [ 'staff', 'shop-orders' ], queryFn: () => apiGet<{ orders: OrderRow[] }>('/shop/orders') });
    const a = q.data;
    const maxRev = Math.max(1, ...(a?.series.map(s => s.revenueCents) ?? [ 0 ]));

    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Analytics shop <span className="admin-card__hint">ricavi &amp; ordini · refresh 60s</span></h2>
            {q.isLoading && <div className="admin-card">Caricamento…</div>}
            {q.isError && <div className="admin-alert admin-alert--error">Errore nel caricamento delle analytics.</div>}
            {a && (
                <>
                    <div className="admin-stats">
                        <div className="admin-stat admin-stat--highlight"><div className="admin-stat__value">{eur(a.totals.revenueCents)}</div><div className="admin-stat__label">Ricavi (pagati)</div></div>
                        <div className="admin-stat"><div className="admin-stat__value">{a.totals.orders.toLocaleString('it-IT')}</div><div className="admin-stat__label">Ordini totali</div></div>
                        <div className="admin-stat"><div className="admin-stat__value">{a.totals.paid.toLocaleString('it-IT')}</div><div className="admin-stat__label">Pagati</div></div>
                        <div className="admin-stat"><div className="admin-stat__value">{a.totals.conversionPct}%</div><div className="admin-stat__label">Conversione</div></div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Ordini per stato</div>
                        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                            {a.byStatus.length === 0 ? <span className="admin-table__empty">Nessun ordine.</span>
                                : a.byStatus.map(s => <span key={s.status} className={`admin-tag admin-tag--${statusTone(s.status)}`}>{s.status}: {s.count}</span>)}
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Ricavi ultimi 30 giorni</div>
                        {a.series.length === 0 ? <div className="admin-table__empty">Nessun ordine pagato nel periodo.</div>
                            : (
                                <div className="an-bars">
                                    {a.series.map(s => (
                                        <div key={s.day} className="an-bar" title={`${s.day} · ${eur(s.revenueCents)} · ${s.orders} ordini`}>
                                            <div className="an-bar__fill" style={{ height: `${Math.max(2, Math.round((s.revenueCents / maxRev) * 100))}%` }} />
                                            <span className="an-bar__lbl">{s.day.slice(5)}</span>
                                        </div>
                                    ))}
                                </div>
                            )}
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Top prodotti</div>
                        <div className="admin-table-wrap">
                            <table className="admin-table">
                                <thead><tr><th>Prodotto</th><th>Vendite</th><th>Ricavi</th></tr></thead>
                                <tbody>
                                    {a.topItems.length === 0 ? <tr><td colSpan={3} className="admin-table__empty">Nessun dato.</td></tr>
                                        : a.topItems.map((t, i) => <tr key={i}><td>{t.name}</td><td>{t.count}</td><td>{eur(t.revenueCents)}</td></tr>)}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Ordini recenti</div>
                        <div className="admin-table-wrap">
                            <table className="admin-table">
                                <thead><tr><th>#</th><th>Utente</th><th>Prodotto</th><th>Stato</th><th>Importo</th><th>Data</th></tr></thead>
                                <tbody>
                                    {(ord.data?.orders ?? []).length === 0 ? <tr><td colSpan={6} className="admin-table__empty">Nessun ordine.</td></tr>
                                        : (ord.data?.orders ?? []).slice(0, 25).map(o => (
                                            <tr key={o.id}>
                                                <td className="admin-table__id">#{o.id}</td>
                                                <td>{o.username ?? '—'}</td>
                                                <td>{o.item ?? '—'}</td>
                                                <td><span className={`admin-tag admin-tag--${statusTone(o.status)}`}>{o.status}</span></td>
                                                <td>{eur(o.amountCents)}</td>
                                                <td className="admin-table__mono">{(o.createdAt ?? '').slice(0, 16).replace('T', ' ')}</td>
                                            </tr>
                                        ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}

// =====================================================================
// SECTION — CODA MODERAZIONE (CFH, read-only — Fase 20)
// =====================================================================
interface ModTicket {
    id: number; state: number; type: number; timestamp: number; score: number; issue: string;
    sender: string | null; senderId: number; reported: string | null; reportedId: number;
    mod: string | null; room: string | null;
}
interface ModQueue { tickets: ModTicket[]; counts: { state: number; count: number }[] }

function ModerationSection(): ReactNode
{
    const q = useQuery({ queryKey: [ 'staff', 'mod-queue' ], queryFn: () => apiGet<ModQueue>('/moderation/queue'), refetchInterval: 30_000 });
    const d = q.data;
    const stateLabel = (s: number): string => s === 0 ? 'Aperto' : s === 1 ? 'Preso' : `Stato ${s}`;

    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Coda moderazione <span className="admin-card__hint">CFH / segnalazioni · refresh 30s</span></h2>
            <div className="admin-alert admin-alert--info" style={{ marginBottom: 14 }}>
                Sola lettura: per intervenire usa <strong>Azioni rapide</strong> / <strong>Ban</strong> o gli strumenti in-game — così non si desincronizza lo stato dei ticket gestito live dall'EMU.
            </div>
            {q.isLoading && <div className="admin-card">Caricamento…</div>}
            {q.isError && <div className="admin-alert admin-alert--error">Errore nel caricamento della coda.</div>}
            {d && (
                <div className="admin-card">
                    <div className="admin-card__title">Segnalazioni aperte <span className="admin-card__hint">{d.tickets.length} in coda</span></div>
                    <div className="admin-table-wrap">
                        <table className="admin-table">
                            <thead><tr><th>#</th><th>Stato</th><th>Segnalante</th><th>Segnalato</th><th>Stanza</th><th>Messaggio</th><th>Quando</th></tr></thead>
                            <tbody>
                                {d.tickets.length === 0 ? <tr><td colSpan={7} className="admin-table__empty">Nessuna segnalazione aperta. 🎉</td></tr>
                                    : d.tickets.map(t => (
                                        <tr key={t.id}>
                                            <td className="admin-table__id">#{t.id}</td>
                                            <td><span className={`admin-tag admin-tag--${t.state === 0 ? 'warning' : 'info'}`}>{stateLabel(t.state)}</span></td>
                                            <td>{t.sender ?? `#${t.senderId}`}</td>
                                            <td><strong>{t.reported ?? `#${t.reportedId}`}</strong></td>
                                            <td>{t.room ?? '—'}</td>
                                            <td className="admin-audit-details" title={t.issue}>{t.issue || '—'}</td>
                                            <td className="admin-table__mono">{fmtDate(t.timestamp)}</td>
                                        </tr>
                                    ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}

// =====================================================================
// SECTION — ALERT STAFF (eventi visibility='staff' prodotti dai guardiani EMU)
// =====================================================================
interface StaffEvent { id: number; actorId: number | null; actor: string; type: string; payload: unknown; ts: number }

const EVENT_META: Record<string, { icon: string; label: string }> = {
    'raid.detected':   { icon: '🚨', label: 'Possibile raid' },
    'macro.suspected': { icon: '🤖', label: 'Sospetta macro/bot' },
    'minor.flag':      { icon: '⚠️', label: 'Possibile minore' }
};

function describeStaffEvent(e: StaffEvent): string
{
    let p: Record<string, unknown> = {};
    try { p = typeof e.payload === 'string' ? JSON.parse(e.payload) : ((e.payload as Record<string, unknown>) ?? {}); }
    catch { p = {}; }
    switch(e.type)
    {
        case 'raid.detected':   return `Stanza “${String(p.room ?? '?')}” · ${String(p.joins ?? '?')} ingressi in ${String(p.seconds ?? '?')}s`;
        case 'macro.suspected': return `Intervallo medio ${String(p.avgIntervalMs ?? '?')}ms · jitter ${String(p.jitterPct ?? '?')}%`;
        default: {
            const keys = Object.keys(p);
            return keys.length ? keys.map(k => `${k}=${String(p[k])}`).join(' · ') : '—';
        }
    }
}

function AlertsSection(): ReactNode
{
    const q = useQuery({ queryKey: ['staff', 'alerts'], queryFn: () => apiGet<{ events: StaffEvent[] }>('/activity'), refetchInterval: 20_000 });
    const d = q.data;
    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Alert staff <span className="admin-card__hint">eventi di moderazione · refresh 20s</span></h2>
            <div className="admin-alert admin-alert--info" style={{ marginBottom: 14 }}>
                Eventi privati allo staff (non nel feed pubblico) prodotti dai guardiani in-game: <strong>raid</strong>, <strong>macro/bot</strong>, ecc. Si popolano solo quando i relativi feature flag sono accesi (vedi <strong>Feature flag</strong>).
            </div>
            {q.isLoading && <div className="admin-card">Caricamento…</div>}
            {q.isError && <Alert kind="error">Errore nel caricamento degli alert.</Alert>}
            {d && (
                <div className="admin-card">
                    <div className="admin-card__title">Ultimi alert <span className="admin-card__hint">{d.events.length}</span></div>
                    <div className="admin-table-wrap">
                        <table className="admin-table">
                            <thead><tr><th>Quando</th><th>Tipo</th><th>Utente</th><th>Dettagli</th></tr></thead>
                            <tbody>
                                {d.events.length === 0 ? <tr><td colSpan={4} className="admin-table__empty">Nessun alert. Tutto tranquillo. 🌙</td></tr>
                                    : d.events.map(e => {
                                        const m = EVENT_META[e.type] ?? { icon: '✨', label: e.type };
                                        return (
                                            <tr key={e.id}>
                                                <td className="admin-table__mono">{fmtDate(e.ts)}</td>
                                                <td><span className="admin-tag admin-tag--warning">{m.icon} {m.label}</span></td>
                                                <td>{e.actor ? <strong>{e.actor}</strong> : (e.actorId ? `#${e.actorId}` : '—')}</td>
                                                <td className="admin-audit-details">{describeStaffEvent(e)}</td>
                                            </tr>
                                        );
                                    })}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}

// =====================================================================
// SECTION — FEATURE FLAG (accensione feature, incl. dark-launch moderazione)
// =====================================================================
interface FlagRow { key: string; enabled: boolean; description: string; audience: string }

// Flag noti di moderazione (dark-launch): gestibili anche se non ancora creati
// nel DB — il PUT li crea on-demand.
const KNOWN_MOD_FLAGS: { key: string; label: string; hint: string }[] = [
    { key: 'raid_detection', label: 'Rilevamento raid', hint: 'Alert staff su flood di ingressi stanza (#20)' },
    { key: 'shadow_mute',    label: 'Shadow-mute',       hint: 'Mute ombra: messaggi visibili solo a mittente + staff (#19)' },
    { key: 'anti_macro',     label: 'Anti-macro / bot',  hint: 'Alert staff su chat a cadenza robotica (#17)' }
];

async function flagsGet(): Promise<{ flags: FlagRow[] }>
{
    const r = await fetch('/api/v2/flags/admin', { credentials: 'include' });
    if(!r.ok) throw new Error(`${r.status}`);
    return (await r.json()) as { flags: FlagRow[] };
}

function FlagToggleBtn({ enabled, busy, onToggle }: { enabled: boolean; busy: boolean; onToggle: () => void }): ReactNode
{
    return (
        <button
            className={`admin-btn admin-btn--small ${enabled ? 'admin-btn--danger' : 'admin-btn--primary'}`}
            disabled={busy}
            onClick={onToggle}
        >{busy ? '…' : (enabled ? 'Disattiva' : 'Attiva')}</button>
    );
}

function FlagsSection(): ReactNode
{
    const qc = useQueryClient();
    const q = useQuery({ queryKey: ['staff', 'flags'], queryFn: flagsGet, refetchInterval: 30_000 });
    const [busy, setBusy] = useState<string | null>(null);
    const [err, setErr] = useState<string | null>(null);

    const toggle = useMutation({
        mutationFn: async ({ key, enabled }: { key: string; enabled: boolean }) =>
        {
            const r = await fetch(`/api/v2/flags/admin/${key}`, {
                method: 'PUT',
                credentials: 'include',
                headers: { 'content-type': 'application/json' },
                body: JSON.stringify({ enabled })
            });
            if(!r.ok) throw new Error(`${r.status}: ${await r.text().catch(() => '')}`);
            return r.json();
        },
        onMutate: (v) => { setBusy(v.key); setErr(null); },
        onError: (e: unknown) => setErr(e instanceof Error ? e.message : 'Errore'),
        onSettled: () => { setBusy(null); void qc.invalidateQueries({ queryKey: ['staff', 'flags'] }); }
    });

    const existing = new Map((q.data?.flags ?? []).map(f => [f.key, f] as const));
    const otherRows = (q.data?.flags ?? []).filter(f => !KNOWN_MOD_FLAGS.some(k => k.key === f.key));

    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Feature flag <span className="admin-card__hint">accensione feature · refresh 30s</span></h2>
            {err && <Alert kind="error">{err}</Alert>}

            <div className="admin-card" style={{ marginBottom: 16 }}>
                <div className="admin-card__title">Moderazione (dark-launch)</div>
                <div className="admin-alert admin-alert--info" style={{ marginBottom: 12 }}>
                    Feature spedite a flag spento. Attivale qui per collaudarle; gli alert compaiono in <strong>Alert staff</strong>.
                </div>
                <div className="admin-table-wrap">
                    <table className="admin-table">
                        <thead><tr><th>Feature</th><th>Stato</th><th>Azione</th></tr></thead>
                        <tbody>
                            {KNOWN_MOD_FLAGS.map(m =>
                            {
                                const row = existing.get(m.key);
                                const on = row?.enabled === true;
                                return (
                                    <tr key={m.key}>
                                        <td><strong>{m.label}</strong><div className="admin-card__hint">{m.hint}</div></td>
                                        <td>{row ? <span className={`admin-tag admin-tag--${on ? 'success' : 'neutral'}`}>{on ? 'ON' : 'OFF'}</span> : <span className="admin-tag admin-tag--neutral">non creato</span>}</td>
                                        <td><FlagToggleBtn enabled={on} busy={busy === m.key} onToggle={() => toggle.mutate({ key: m.key, enabled: !on })} /></td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="admin-card">
                <div className="admin-card__title">Altri flag</div>
                {q.isLoading && <div style={{ padding: 8 }}>Caricamento…</div>}
                <div className="admin-table-wrap">
                    <table className="admin-table">
                        <thead><tr><th>Chiave</th><th>Descrizione</th><th>Audience</th><th>Stato</th><th>Azione</th></tr></thead>
                        <tbody>
                            {otherRows.length === 0 ? <tr><td colSpan={5} className="admin-table__empty">Nessun altro flag.</td></tr>
                                : otherRows.map(f => (
                                    <tr key={f.key}>
                                        <td className="admin-table__mono">{f.key}</td>
                                        <td>{f.description || '—'}</td>
                                        <td>{f.audience}</td>
                                        <td><span className={`admin-tag admin-tag--${f.enabled ? 'success' : 'neutral'}`}>{f.enabled ? 'ON' : 'OFF'}</span></td>
                                        <td><FlagToggleBtn enabled={f.enabled} busy={busy === f.key} onToggle={() => toggle.mutate({ key: f.key, enabled: !f.enabled })} /></td>
                                    </tr>
                                ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

// =====================================================================
// HELPERS
// =====================================================================
async function apiGet<T>(path: string): Promise<T>
{
    const r = await fetch(`/api/v2/staff${path}`, { credentials: 'include' });
    if(!r.ok) throw new Error(`${r.status}`);
    return (await r.json()) as T;
}

async function apiSend<T = { ok?: boolean; error?: string }>(
    path: string,
    method: 'POST' | 'PATCH' | 'DELETE',
    body?: unknown
): Promise<T>
{
    const r = await fetch(`/api/v2/staff${path}`, {
        method,
        credentials: 'include',
        headers: body ? { 'content-type': 'application/json' } : undefined,
        body: body ? JSON.stringify(body) : undefined
    });
    if(!r.ok)
    {
        const txt = await r.text().catch(() => '');
        throw new Error(`${r.status}: ${txt || r.statusText}`);
    }
    return (await r.json()) as T;
}

function fmtDate(ts: number | null | undefined): string
{
    if(!ts) return '—';
    return new Date(ts * 1000).toLocaleString('it-IT', { dateStyle: 'short', timeStyle: 'medium' });
}

function Alert({ kind, children }: { kind: 'success' | 'error' | 'info'; children: ReactNode }): ReactNode
{
    return <div className={`admin-alert admin-alert--${kind}`}>{children}</div>;
}

// =====================================================================
// OVERVIEW
// =====================================================================
interface Stats {
    totalUsers: number; onlineUsers: number; totalRooms: number;
    totalPhotos: number; staffCount: number; activeBans: number; totalChatlogs: number;
}

function OverviewSection(): ReactNode
{
    const q = useQuery<Stats>({
        queryKey: ['staff', 'stats'],
        queryFn: () => apiGet('/stats'),
        refetchInterval: 30_000
    });
    const s = q.data;

    return (
        <>
            <div className="admin-stats">
                <Stat value={s?.onlineUsers} label="Online ora" highlight />
                <Stat value={s?.totalUsers} label="Utenti totali" />
                <Stat value={s?.activeBans} label="Ban attivi" danger />
                <Stat value={s?.totalRooms} label="Stanze pubbliche" />
                <Stat value={s?.totalPhotos} label="Foto" />
                <Stat value={s?.totalChatlogs} label="Chat log" />
                <Stat value={s?.staffCount} label="Staff" />
            </div>

            <AuditLogTable />
        </>
    );
}

function Stat({ value, label, highlight, danger }: { value: number | undefined; label: string; highlight?: boolean; danger?: boolean }): ReactNode
{
    const cls = ['admin-stat'];
    if(highlight) cls.push('admin-stat--highlight');
    if(danger) cls.push('admin-stat--danger');
    return (
        <div className={cls.join(' ')}>
            <div className="admin-stat__value">{value === undefined ? '—' : value.toLocaleString('it-IT')}</div>
            <div className="admin-stat__label">{label}</div>
        </div>
    );
}

interface AuditEntry {
    id: number; userId: number | null; username: string | null;
    action: string; ip: string; userAgent: string;
    details: unknown; createdAt: number;
}

function AuditLogTable(): ReactNode
{
    const q = useQuery<{ entries: AuditEntry[] }>({
        queryKey: ['staff', 'audit-log'],
        queryFn: () => apiGet('/audit-log?limit=50'),
        refetchInterval: 15_000
    });
    const entries = q.data?.entries ?? [];

    return (
        <section className="admin-card">
            <h2 className="admin-card__title">
                Audit log
                <span className="admin-card__hint">ultime 50 azioni — refresh 15s</span>
            </h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th>Quando</th>
                            <th>Attore</th>
                            <th>Azione</th>
                            <th>IP</th>
                            <th>Dettagli</th>
                        </tr>
                    </thead>
                    <tbody>
                        {entries.length === 0 && (
                            <tr><td colSpan={5} className="admin-table__empty">
                                {q.isLoading ? 'Caricamento…' : 'Nessuna azione registrata.'}
                            </td></tr>
                        )}
                        {entries.map(e => (
                            <tr key={e.id}>
                                <td>{fmtDate(e.createdAt)}</td>
                                <td>{e.username ?? `#${e.userId ?? '?'}`}</td>
                                <td className="admin-table__mono">{e.action}</td>
                                <td className="admin-table__mono">{e.ip}</td>
                                <td className="admin-audit-details">{e.details ? JSON.stringify(e.details) : '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// =====================================================================
// USERS
// =====================================================================
interface UserInfo {
    id: number; username: string; mail: string; motto: string; look: string;
    gender: string; rank: number; credits: number; diamonds: number; duckets: number;
    accountCreated: number; lastLogin: number; lastOnline: number;
    online: boolean; ipCurrent: string | null; ipRegister: string | null;
    machineId: string | null; mailVerified: boolean;
}

function UsersSection(): ReactNode
{
    const [search, setSearch] = useState('');
    const [submitted, setSubmitted] = useState('');
    const qc = useQueryClient();

    const q = useQuery<{ user: UserInfo }>({
        queryKey: ['staff', 'user', submitted],
        queryFn: () => apiGet(`/user/${encodeURIComponent(submitted)}`),
        enabled: submitted.length > 0,
        retry: false
    });

    const updateMut = useMutation<{ ok: boolean }, Error, { userId: number; changes: Partial<UserInfo> }>({
        mutationFn: ({ userId, changes }) => apiSend(`/user/${userId}`, 'PATCH', changes),
        onSuccess: () =>
        {
            void qc.invalidateQueries({ queryKey: ['staff', 'user', submitted] });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        }
    });

    const passwordMut = useMutation<{ ok: boolean }, Error, { userId: number; newPassword: string }>({
        mutationFn: ({ userId, newPassword }) => apiSend(`/user/${userId}/reset-password`, 'POST', { newPassword }),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });

    const strikeMut = useMutation<{ ok: boolean }, Error, { userId: number; reason: string }>({
        mutationFn: ({ userId, reason }) => apiSend(`/user/${userId}/strike`, 'POST', { reason }),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });

    function onSearch(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        setSubmitted(search.trim());
    }

    const user = q.data?.user;

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Cerca utente</h2>
                <form onSubmit={onSearch} className="admin-search">
                    <input
                        className="admin-input"
                        type="text"
                        placeholder="Username esatto…"
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                        required
                    />
                    <button type="submit" className="admin-btn admin-btn--primary">Cerca</button>
                </form>

                {submitted && q.isError && <Alert kind="error">Utente «{submitted}» non trovato.</Alert>}
            </section>

            {user && (
                <>
                    <section className="admin-card">
                        <h2 className="admin-card__title">Anagrafica</h2>
                        <div className="admin-user-card">
                            <img
                                className="admin-user-card__avatar"
                                src={avatarUrl(user.look, { size: 'l' })}
                                alt={user.username}
                            />
                            <div>
                                <h3 className="admin-user-card__title">{user.username} <span style={{ color: '#4f6680', fontSize: 14 }}>#{user.id}</span></h3>
                                <p className="admin-user-card__motto">{user.motto || '— nessun motto —'}</p>
                                <div className="admin-user-card__meta">
                                    <span className={`admin-tag ${user.online ? 'admin-tag--success' : 'admin-tag--neutral'}`}>
                                        {user.online ? 'Online' : 'Offline'}
                                    </span>
                                    <span className="admin-tag admin-tag--info">Rank {user.rank}</span>
                                    <span className="admin-tag">{user.credits.toLocaleString('it-IT')} crediti</span>
                                    <span className="admin-tag">{user.diamonds.toLocaleString('it-IT')} diamanti</span>
                                    <span className="admin-tag">{user.duckets.toLocaleString('it-IT')} duckets</span>
                                </div>
                            </div>
                        </div>

                        <div className="admin-row admin-row--2">
                            <div>
                                <span className="admin-field__label">Email</span>
                                <p style={{ margin: 0, fontFamily: 'monospace', fontSize: 13 }}>{user.mail} {user.mailVerified && <span className="admin-tag admin-tag--success" style={{ marginLeft: 6 }}>verificata</span>}</p>
                            </div>
                            <div>
                                <span className="admin-field__label">Account creato</span>
                                <p style={{ margin: 0, fontSize: 13 }}>{fmtDate(user.accountCreated)}</p>
                            </div>
                            <div>
                                <span className="admin-field__label">Ultimo login</span>
                                <p style={{ margin: 0, fontSize: 13 }}>{fmtDate(user.lastLogin)}</p>
                            </div>
                            <div>
                                <span className="admin-field__label">IP corrente</span>
                                <p style={{ margin: 0, fontFamily: 'monospace', fontSize: 13 }}>{user.ipCurrent ?? '—'}</p>
                            </div>
                        </div>
                    </section>

                    <UserEditCard user={user} onUpdate={(changes) => updateMut.mutate({ userId: user.id, changes })} isPending={updateMut.isPending} feedback={updateMut.data ? { ok: true, msg: 'Utente aggiornato.' } : updateMut.error ? { ok: false, msg: updateMut.error.message } : null} />

                    <UserPasswordCard
                        onReset={(pwd) => passwordMut.mutate({ userId: user.id, newPassword: pwd })}
                        isPending={passwordMut.isPending}
                        feedback={passwordMut.data ? { ok: true, msg: 'Password resettata.' } : passwordMut.error ? { ok: false, msg: passwordMut.error.message } : null}
                    />

                    <UserStrikeCard
                        onStrike={(reason) => strikeMut.mutate({ userId: user.id, reason })}
                        isPending={strikeMut.isPending}
                        feedback={strikeMut.data ? { ok: true, msg: 'Strike registrato.' } : strikeMut.error ? { ok: false, msg: strikeMut.error.message } : null}
                    />
                </>
            )}
        </>
    );
}

function UserEditCard({ user, onUpdate, isPending, feedback }: {
    user: UserInfo;
    onUpdate: (changes: { motto?: string; mail?: string; rank?: number; look?: string }) => void;
    isPending: boolean;
    feedback: { ok: boolean; msg: string } | null;
}): ReactNode
{
    const [motto, setMotto] = useState(user.motto);
    const [mail, setMail] = useState(user.mail);
    const [rank, setRank] = useState(user.rank);
    const [look, setLook] = useState(user.look);

    function submit(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        const changes: { motto?: string; mail?: string; rank?: number; look?: string } = {};
        if(motto !== user.motto) changes.motto = motto;
        if(mail !== user.mail) changes.mail = mail;
        if(rank !== user.rank) changes.rank = rank;
        if(look !== user.look) changes.look = look;
        if(Object.keys(changes).length === 0) return;
        onUpdate(changes);
    }

    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Modifica utente</h2>
            <form onSubmit={submit}>
                <div className="admin-row admin-row--2">
                    <label className="admin-field">
                        <span className="admin-field__label">Motto</span>
                        <input className="admin-input" value={motto} onChange={e => setMotto(e.target.value)} maxLength={127} />
                    </label>
                    <label className="admin-field">
                        <span className="admin-field__label">Email</span>
                        <input className="admin-input" type="email" value={mail} onChange={e => setMail(e.target.value)} />
                    </label>
                    <label className="admin-field">
                        <span className="admin-field__label">Rank</span>
                        <input className="admin-input" type="number" min={1} max={10} value={rank} onChange={e => setRank(Number(e.target.value))} />
                    </label>
                    <label className="admin-field">
                        <span className="admin-field__label">Look (figure string)</span>
                        <input className="admin-input" value={look} onChange={e => setLook(e.target.value)} style={{ fontFamily: 'monospace', fontSize: 12 }} />
                    </label>
                </div>
                <button type="submit" disabled={isPending} className="admin-btn admin-btn--primary">
                    {isPending ? 'Salvataggio…' : 'Salva modifiche'}
                </button>
                {feedback && <Alert kind={feedback.ok ? 'success' : 'error'}>{feedback.msg}</Alert>}
            </form>
        </section>
    );
}

function UserPasswordCard({ onReset, isPending, feedback }: {
    onReset: (newPassword: string) => void;
    isPending: boolean;
    feedback: { ok: boolean; msg: string } | null;
}): ReactNode
{
    const [pwd, setPwd] = useState('');
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Reset password</h2>
            <form onSubmit={e => { e.preventDefault(); onReset(pwd); setPwd(''); }}>
                <label className="admin-field">
                    <span className="admin-field__label">Nuova password (min 8 char)</span>
                    <input className="admin-input" type="text" value={pwd} onChange={e => setPwd(e.target.value)} minLength={8} required />
                </label>
                <button type="submit" disabled={isPending} className="admin-btn admin-btn--danger">
                    {isPending ? 'Reset…' : 'Resetta password'}
                </button>
                {feedback && <Alert kind={feedback.ok ? 'success' : 'error'}>{feedback.msg}</Alert>}
            </form>
        </section>
    );
}

function UserStrikeCard({ onStrike, isPending, feedback }: {
    onStrike: (reason: string) => void;
    isPending: boolean;
    feedback: { ok: boolean; msg: string } | null;
}): ReactNode
{
    const [reason, setReason] = useState('');
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Strike utente</h2>
            <form onSubmit={e => { e.preventDefault(); onStrike(reason); setReason(''); }}>
                <label className="admin-field">
                    <span className="admin-field__label">Motivo dello strike</span>
                    <input className="admin-input" value={reason} onChange={e => setReason(e.target.value)} maxLength={255} required />
                </label>
                <button type="submit" disabled={isPending} className="admin-btn admin-btn--ghost">
                    {isPending ? 'Registrazione…' : 'Registra strike'}
                </button>
                {feedback && <Alert kind={feedback.ok ? 'success' : 'error'}>{feedback.msg}</Alert>}
            </form>
        </section>
    );
}

// =====================================================================
// BANS
// =====================================================================
interface BanRow {
    id: number; userId: number; targetUsername: string | null;
    staffUsername: string | null; ip: string; machineId: string;
    type: string; reason: string; timestamp: number; expireAt: number;
}

function BansSection(): ReactNode
{
    const [search, setSearch] = useState('');
    const qc = useQueryClient();

    const q = useQuery<{ bans: BanRow[] }>({
        queryKey: ['staff', 'bans', search],
        queryFn: () => apiGet(`/bans?limit=100${search ? '&q=' + encodeURIComponent(search) : ''}`),
        refetchInterval: 30_000
    });

    const addMut = useMutation<{ ok: boolean }, Error, { username: string; reason: string; type: string; hours: number }>({
        mutationFn: (b) => apiSend('/bans', 'POST', b),
        onSuccess: () =>
        {
            void qc.invalidateQueries({ queryKey: ['staff', 'bans'] });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        }
    });

    const delMut = useMutation<{ ok: boolean }, Error, number>({
        mutationFn: (id) => apiSend(`/bans/${id}`, 'DELETE'),
        onSuccess: () =>
        {
            void qc.invalidateQueries({ queryKey: ['staff', 'bans'] });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        }
    });

    const [banUsername, setBanUsername] = useState('');
    const [banReason, setBanReason] = useState('');
    const [banType, setBanType] = useState('account');
    const [banHours, setBanHours] = useState(24);

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Aggiungi ban</h2>
                <form onSubmit={e =>
                {
                    e.preventDefault();
                    addMut.mutate({ username: banUsername, reason: banReason, type: banType, hours: banHours });
                    setBanUsername(''); setBanReason('');
                }}>
                    <div className="admin-row admin-row--4">
                        <label className="admin-field">
                            <span className="admin-field__label">Username</span>
                            <input className="admin-input" value={banUsername} onChange={e => setBanUsername(e.target.value)} required />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Motivo</span>
                            <input className="admin-input" value={banReason} onChange={e => setBanReason(e.target.value)} maxLength={200} required />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Tipo</span>
                            <select className="admin-select" value={banType} onChange={e => setBanType(e.target.value)}>
                                <option value="account">Account</option>
                                <option value="ip">IP</option>
                                <option value="machine">Machine ID</option>
                                <option value="super">Super (tutti)</option>
                            </select>
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Durata (ore)</span>
                            <input className="admin-input" type="number" min={1} max={87600} value={banHours} onChange={e => setBanHours(Number(e.target.value))} required />
                        </label>
                    </div>
                    <button type="submit" disabled={addMut.isPending} className="admin-btn admin-btn--danger">
                        {addMut.isPending ? 'Ban…' : 'Banna utente'}
                    </button>
                    {addMut.error && <Alert kind="error">{addMut.error.message}</Alert>}
                    {addMut.data && <Alert kind="success">Ban registrato.</Alert>}
                </form>
            </section>

            <section className="admin-card">
                <h2 className="admin-card__title">
                    Ban attivi
                    <span className="admin-card__hint">refresh 30s</span>
                </h2>
                <div className="admin-search">
                    <input className="admin-input" placeholder="Filtra username/IP/motivo…" value={search} onChange={e => setSearch(e.target.value)} />
                </div>
                <div className="admin-table-wrap">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Tipo</th>
                                <th>Motivo</th>
                                <th>Staff</th>
                                <th>Scadenza</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {(q.data?.bans ?? []).length === 0 && (
                                <tr><td colSpan={7} className="admin-table__empty">{q.isLoading ? 'Caricamento…' : 'Nessun ban attivo.'}</td></tr>
                            )}
                            {q.data?.bans.map(b => (
                                <tr key={b.id}>
                                    <td className="admin-table__id">#{b.id}</td>
                                    <td>{b.targetUsername ?? `user#${b.userId}`}</td>
                                    <td><span className="admin-tag admin-tag--warning">{b.type}</span></td>
                                    <td>{b.reason}</td>
                                    <td>{b.staffUsername ?? '—'}</td>
                                    <td>{fmtDate(b.expireAt)}</td>
                                    <td>
                                        <button className="admin-btn admin-btn--small admin-btn--ghost" onClick={() => delMut.mutate(b.id)} disabled={delMut.isPending}>
                                            Rimuovi
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        </>
    );
}

// =====================================================================
// LOGS (chat-room, chat-pm, commands, namechange)
// =====================================================================
type LogTab = 'chat-room' | 'chat-pm' | 'commands' | 'namechange';

function LogsSection(): ReactNode
{
    const [tab, setTab] = useState<LogTab>('chat-room');
    const [search, setSearch] = useState('');

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Filtra log</h2>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 12 }}>
                    {[
                        { id: 'chat-room', label: 'Chat stanza' },
                        { id: 'chat-pm', label: 'Chat privati' },
                        { id: 'commands', label: 'Comandi staff' },
                        { id: 'namechange', label: 'Cambi username' }
                    ].map(t => (
                        <button
                            key={t.id}
                            type="button"
                            onClick={() => setTab(t.id as LogTab)}
                            className={`admin-btn admin-btn--small ${tab === t.id ? '' : 'admin-btn--ghost'}`}
                        >
                            {t.label}
                        </button>
                    ))}
                </div>
                <input
                    className="admin-input"
                    placeholder="Filtra per username (esatto)…"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    style={{ maxWidth: 320 }}
                />
            </section>

            {tab === 'chat-room' && <ChatRoomLogTable filterUser={search} />}
            {tab === 'chat-pm' && <ChatPmLogTable filterUser={search} />}
            {tab === 'commands' && <CommandLogTable filterUser={search} />}
            {tab === 'namechange' && <NameChangeLogTable filterUser={search} />}
        </>
    );
}

function ChatRoomLogTable({ filterUser }: { filterUser: string }): ReactNode
{
    const q = useQuery<{ entries: { roomId: number; roomName: string | null; fromUsername: string | null; message: string; timestamp: number }[] }>({
        queryKey: ['staff', 'logs', 'chat-room', filterUser],
        queryFn: () => apiGet(`/logs/chat-room${filterUser ? '?user=' + encodeURIComponent(filterUser) : ''}`)
    });
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Chat stanza</h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead><tr><th>Quando</th><th>Utente</th><th>Stanza</th><th>Messaggio</th></tr></thead>
                    <tbody>
                        {(q.data?.entries ?? []).length === 0 && <tr><td colSpan={4} className="admin-table__empty">Nessun log.</td></tr>}
                        {q.data?.entries.map((e, i) => (
                            <tr key={i}>
                                <td>{fmtDate(e.timestamp)}</td>
                                <td>{e.fromUsername ?? '—'}</td>
                                <td className="admin-table__mono">#{e.roomId} {e.roomName ?? ''}</td>
                                <td>{e.message}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

function ChatPmLogTable({ filterUser }: { filterUser: string }): ReactNode
{
    const q = useQuery<{ entries: { id: number; fromUsername: string | null; toUsername: string | null; message: string; timestamp: number }[] }>({
        queryKey: ['staff', 'logs', 'chat-pm', filterUser],
        queryFn: () => apiGet(`/logs/chat-pm${filterUser ? '?user=' + encodeURIComponent(filterUser) : ''}`)
    });
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Chat privati</h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead><tr><th>Quando</th><th>Da</th><th>A</th><th>Messaggio</th></tr></thead>
                    <tbody>
                        {(q.data?.entries ?? []).length === 0 && <tr><td colSpan={4} className="admin-table__empty">Nessun log.</td></tr>}
                        {q.data?.entries.map(e => (
                            <tr key={e.id}>
                                <td>{fmtDate(e.timestamp)}</td>
                                <td>{e.fromUsername ?? '—'}</td>
                                <td>{e.toUsername ?? '—'}</td>
                                <td>{e.message}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

function CommandLogTable({ filterUser }: { filterUser: string }): ReactNode
{
    const q = useQuery<{ entries: { username: string | null; command: string; params: string; success: boolean; timestamp: number }[] }>({
        queryKey: ['staff', 'logs', 'commands', filterUser],
        queryFn: () => apiGet(`/logs/commands${filterUser ? '?user=' + encodeURIComponent(filterUser) : ''}`)
    });
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Comandi staff (in-game)</h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead><tr><th>Quando</th><th>Staff</th><th>Comando</th><th>Parametri</th><th>Esito</th></tr></thead>
                    <tbody>
                        {(q.data?.entries ?? []).length === 0 && <tr><td colSpan={5} className="admin-table__empty">Nessun log.</td></tr>}
                        {q.data?.entries.map((e, i) => (
                            <tr key={i}>
                                <td>{fmtDate(e.timestamp)}</td>
                                <td>{e.username ?? '—'}</td>
                                <td className="admin-table__mono">:{e.command}</td>
                                <td>{e.params}</td>
                                <td>{e.success ? <span className="admin-tag admin-tag--success">OK</span> : <span className="admin-tag admin-tag--danger">FAIL</span>}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

function NameChangeLogTable({ filterUser }: { filterUser: string }): ReactNode
{
    const q = useQuery<{ entries: { currentUsername: string | null; oldName: string; newName: string; timestamp: number }[] }>({
        queryKey: ['staff', 'logs', 'namechange', filterUser],
        queryFn: () => apiGet(`/logs/namechange${filterUser ? '?user=' + encodeURIComponent(filterUser) : ''}`)
    });
    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Cambi username</h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead><tr><th>Quando</th><th>Username attuale</th><th>Vecchio</th><th>Nuovo</th></tr></thead>
                    <tbody>
                        {(q.data?.entries ?? []).length === 0 && <tr><td colSpan={4} className="admin-table__empty">Nessun log.</td></tr>}
                        {q.data?.entries.map((e, i) => (
                            <tr key={i}>
                                <td>{fmtDate(e.timestamp)}</td>
                                <td>{e.currentUsername ?? '—'}</td>
                                <td>{e.oldName}</td>
                                <td>{e.newName}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// =====================================================================
// IP TOOL
// =====================================================================
function IpToolSection(): ReactNode
{
    const [ip, setIp] = useState('');
    const [machine, setMachine] = useState('');
    const [username, setUsername] = useState('');
    const [submitted, setSubmitted] = useState<{ ip?: string; machine?: string; user?: string } | null>(null);

    const q = useQuery<{ matches: { id: number; username: string; mail: string; rank: number; online: boolean; ipCurrent: string | null; ipRegister: string | null; machineId: string | null; lastLogin: number }[] }>({
        queryKey: ['staff', 'ip-lookup', submitted],
        queryFn: () =>
        {
            const params = new URLSearchParams();
            if(submitted?.ip) params.set('ip', submitted.ip);
            if(submitted?.machine) params.set('machine', submitted.machine);
            if(submitted?.user) params.set('user', submitted.user);
            return apiGet(`/ip-lookup?${params.toString()}`);
        },
        enabled: submitted !== null
    });

    function submit(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        const q: { ip?: string; machine?: string; user?: string } = {};
        if(ip.trim()) q.ip = ip.trim();
        if(machine.trim()) q.machine = machine.trim();
        if(username.trim()) q.user = username.trim();
        if(Object.keys(q).length === 0) return;
        setSubmitted(q);
    }

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Ricerca clone (IP / Machine / Username)</h2>
                <form onSubmit={submit}>
                    <div className="admin-row admin-row--3">
                        <label className="admin-field">
                            <span className="admin-field__label">Indirizzo IP</span>
                            <input className="admin-input" value={ip} onChange={e => setIp(e.target.value)} placeholder="es. 192.0.2.10" />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Machine ID</span>
                            <input className="admin-input" value={machine} onChange={e => setMachine(e.target.value)} />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Username (parte da)</span>
                            <input className="admin-input" value={username} onChange={e => setUsername(e.target.value)} />
                        </label>
                    </div>
                    <button type="submit" className="admin-btn admin-btn--primary">Cerca match</button>
                </form>
            </section>

            {submitted && (
                <section className="admin-card">
                    <h2 className="admin-card__title">Risultati</h2>
                    <div className="admin-table-wrap">
                        <table className="admin-table">
                            <thead><tr><th>ID</th><th>Username</th><th>Rank</th><th>IP corrente</th><th>IP registrazione</th><th>Machine ID</th><th>Ultimo login</th></tr></thead>
                            <tbody>
                                {(q.data?.matches ?? []).length === 0 && <tr><td colSpan={7} className="admin-table__empty">Nessun match.</td></tr>}
                                {q.data?.matches.map(m => (
                                    <tr key={m.id}>
                                        <td className="admin-table__id">#{m.id}</td>
                                        <td>{m.username} {m.online && <span className="admin-tag admin-tag--success" style={{ marginLeft: 4 }}>online</span>}</td>
                                        <td>{m.rank}</td>
                                        <td className="admin-table__mono">{m.ipCurrent ?? '—'}</td>
                                        <td className="admin-table__mono">{m.ipRegister ?? '—'}</td>
                                        <td className="admin-table__mono">{m.machineId ?? '—'}</td>
                                        <td>{fmtDate(m.lastLogin)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}
        </>
    );
}

// =====================================================================
// WORD FILTER
// =====================================================================
interface WordfilterRow { word: string; replacement: string; hide: boolean; report: boolean; mute: number; }

function WordfilterSection(): ReactNode
{
    const qc = useQueryClient();
    const q = useQuery<{ entries: WordfilterRow[] }>({
        queryKey: ['staff', 'wordfilter'],
        queryFn: () => apiGet('/wordfilter')
    });

    const upsertMut = useMutation<{ ok: boolean }, Error, WordfilterRow>({
        mutationFn: (b) => apiSend('/wordfilter', 'POST', b),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'wordfilter'] })
    });
    const delMut = useMutation<{ ok: boolean }, Error, string>({
        mutationFn: (w) => apiSend(`/wordfilter/${encodeURIComponent(w)}`, 'DELETE'),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'wordfilter'] })
    });

    const [word, setWord] = useState('');
    const [replacement, setReplacement] = useState('bobba');
    const [hide, setHide] = useState(false);
    const [report, setReport] = useState(false);
    const [mute, setMute] = useState(0);

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Aggiungi parola</h2>
                <form onSubmit={e =>
                {
                    e.preventDefault();
                    upsertMut.mutate({ word, replacement, hide, report, mute });
                    setWord('');
                }}>
                    <div className="admin-row admin-row--3">
                        <label className="admin-field">
                            <span className="admin-field__label">Parola</span>
                            <input className="admin-input" value={word} onChange={e => setWord(e.target.value)} required />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Rimpiazzo</span>
                            <input className="admin-input" value={replacement} onChange={e => setReplacement(e.target.value)} maxLength={16} />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Mute (minuti)</span>
                            <input className="admin-input" type="number" min={0} max={1440} value={mute} onChange={e => setMute(Number(e.target.value))} />
                        </label>
                    </div>
                    <div style={{ display: 'flex', gap: 16, marginBottom: 12 }}>
                        <label style={{ fontSize: 13 }}><input type="checkbox" checked={hide} onChange={e => setHide(e.target.checked)} /> Nascondi messaggio</label>
                        <label style={{ fontSize: 13 }}><input type="checkbox" checked={report} onChange={e => setReport(e.target.checked)} /> Auto-report</label>
                    </div>
                    <button type="submit" disabled={upsertMut.isPending} className="admin-btn admin-btn--primary">Salva regola</button>
                    {upsertMut.error && <Alert kind="error">{upsertMut.error.message}</Alert>}
                </form>
            </section>

            <section className="admin-card">
                <h2 className="admin-card__title">Parole filtrate ({q.data?.entries.length ?? 0})</h2>
                <div className="admin-table-wrap">
                    <table className="admin-table">
                        <thead><tr><th>Parola</th><th>Rimpiazzo</th><th>Nascondi</th><th>Auto-report</th><th>Mute</th><th></th></tr></thead>
                        <tbody>
                            {(q.data?.entries ?? []).length === 0 && <tr><td colSpan={6} className="admin-table__empty">Vuoto.</td></tr>}
                            {q.data?.entries.map(e => (
                                <tr key={e.word}>
                                    <td className="admin-table__mono">{e.word}</td>
                                    <td>{e.replacement}</td>
                                    <td>{e.hide ? <span className="admin-tag admin-tag--warning">sì</span> : '—'}</td>
                                    <td>{e.report ? <span className="admin-tag admin-tag--warning">sì</span> : '—'}</td>
                                    <td>{e.mute > 0 ? `${e.mute}m` : '—'}</td>
                                    <td>
                                        <button className="admin-btn admin-btn--small admin-btn--ghost" onClick={() => delMut.mutate(e.word)} disabled={delMut.isPending}>
                                            Elimina
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        </>
    );
}

// =====================================================================
// VOUCHERS
// =====================================================================
interface VoucherRow {
    id: number; code: string; credits: number; points: number;
    pointsType: number; catalogItemId: number; amount: number; limit: number;
}

function VouchersSection(): ReactNode
{
    const qc = useQueryClient();
    const q = useQuery<{ vouchers: VoucherRow[] }>({
        queryKey: ['staff', 'vouchers'],
        queryFn: () => apiGet('/vouchers')
    });

    const addMut = useMutation<{ ok: boolean }, Error, { code: string; credits: number; points: number; pointsType: number; limit: number }>({
        mutationFn: (b) => apiSend('/vouchers', 'POST', b),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'vouchers'] })
    });

    const delMut = useMutation<{ ok: boolean }, Error, number>({
        mutationFn: (id) => apiSend(`/vouchers/${id}`, 'DELETE'),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'vouchers'] })
    });

    const [code, setCode] = useState('');
    const [credits, setCredits] = useState(0);
    const [points, setPoints] = useState(0);
    const [pointsType, setPointsType] = useState(0);  // 0 = duckets, 5 = diamanti
    const [limit, setLimit] = useState(-1);

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Genera voucher</h2>
                <form onSubmit={e =>
                {
                    e.preventDefault();
                    addMut.mutate({ code, credits, points, pointsType, limit });
                    setCode('');
                }}>
                    <div className="admin-row admin-row--3">
                        <label className="admin-field">
                            <span className="admin-field__label">Codice (3-10 caratteri)</span>
                            <input className="admin-input" value={code} onChange={e => setCode(e.target.value)} pattern="[A-Za-z0-9_\-]+" minLength={3} maxLength={10} required />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Crediti</span>
                            <input className="admin-input" type="number" min={0} value={credits} onChange={e => setCredits(Number(e.target.value))} />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Punti</span>
                            <input className="admin-input" type="number" min={0} value={points} onChange={e => setPoints(Number(e.target.value))} />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Tipo punti</span>
                            <select className="admin-select" value={pointsType} onChange={e => setPointsType(Number(e.target.value))}>
                                <option value={0}>Duckets (0)</option>
                                <option value={5}>Diamanti (5)</option>
                                <option value={101}>Loyalty (101)</option>
                            </select>
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Limite usi (-1 = illimitato)</span>
                            <input className="admin-input" type="number" min={-1} value={limit} onChange={e => setLimit(Number(e.target.value))} />
                        </label>
                    </div>
                    <button type="submit" disabled={addMut.isPending} className="admin-btn admin-btn--primary">Crea voucher</button>
                    {addMut.error && <Alert kind="error">{addMut.error.message}</Alert>}
                    {addMut.data && <Alert kind="success">Voucher creato.</Alert>}
                </form>
            </section>

            <section className="admin-card">
                <h2 className="admin-card__title">Voucher esistenti ({q.data?.vouchers.length ?? 0})</h2>
                <div className="admin-table-wrap">
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Codice</th><th>Crediti</th><th>Punti</th><th>Tipo</th><th>Quantità</th><th>Limite</th><th></th></tr></thead>
                        <tbody>
                            {(q.data?.vouchers ?? []).length === 0 && <tr><td colSpan={8} className="admin-table__empty">Nessun voucher.</td></tr>}
                            {q.data?.vouchers.map(v => (
                                <tr key={v.id}>
                                    <td className="admin-table__id">#{v.id}</td>
                                    <td className="admin-table__mono"><b>{v.code}</b></td>
                                    <td>{v.credits.toLocaleString('it-IT')}</td>
                                    <td>{v.points.toLocaleString('it-IT')}</td>
                                    <td>{v.pointsType}</td>
                                    <td>{v.amount}</td>
                                    <td>{v.limit === -1 ? '∞' : v.limit}</td>
                                    <td>
                                        <button className="admin-btn admin-btn--small admin-btn--ghost" onClick={() => delMut.mutate(v.id)} disabled={delMut.isPending}>
                                            Elimina
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        </>
    );
}

// =====================================================================
// NEWS MANAGEMENT (porting news.php + editnews.php)
// =====================================================================
interface NewsRow {
    id: number; slug: string; title: string;
    category: string; categoryLabel: string;
    summary: string; bodyHtml: string; image: string;
    authorId: number | null; authorUsername: string | null;
    published: boolean; createdAt: number; updatedAt: number;
    date: string; href: string;
}

const NEWS_CATEGORIES: { value: string; label: string }[] = [
    { value: 'aggiornamenti-su-habbo', label: 'Aggiornamenti su Habbo' },
    { value: 'campagne-attivita',      label: 'Campagne & Attività' },
    { value: 'nuove-funzionalita',     label: 'Nuove funzionalità' },
    { value: 'eventi-community',       label: 'Eventi community' },
    { value: 'avvisi-importanti',      label: 'Avvisi importanti' }
];

function NewsSection(): ReactNode
{
    const [editingId, setEditingId] = useState<number | null>(null);
    const [creating, setCreating] = useState(false);

    if(editingId !== null) return <NewsEditor newsId={editingId} onClose={() => setEditingId(null)} />;
    if(creating) return <NewsEditor newsId={null} onClose={() => setCreating(false)} />;

    return <NewsList onEdit={setEditingId} onCreate={() => setCreating(true)} />;
}

function NewsList({ onEdit, onCreate }: { onEdit: (id: number) => void; onCreate: () => void }): ReactNode
{
    const qc = useQueryClient();
    const q = useQuery<{ news: NewsRow[] }>({
        queryKey: ['staff', 'news'],
        queryFn: () => apiGet('/news')
    });

    const togglePublish = useMutation<{ ok: boolean }, Error, { id: number; published: boolean }>({
        mutationFn: ({ id, published }) => apiSend(`/news/${id}`, 'PATCH', { published }),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'news'] })
    });

    const delMut = useMutation<{ ok: boolean }, Error, number>({
        mutationFn: (id) => apiSend(`/news/${id}`, 'DELETE'),
        onSuccess: () =>
        {
            void qc.invalidateQueries({ queryKey: ['staff', 'news'] });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
        }
    });

    function confirmDelete(id: number, title: string): void
    {
        if(window.confirm(`Eliminare definitivamente l'articolo «${title}»? L'azione non è reversibile.`))
        {
            delMut.mutate(id);
        }
    }

    return (
        <section className="admin-card">
            <h2 className="admin-card__title">
                Articoli ({q.data?.news.length ?? 0})
                <button type="button" onClick={onCreate} className="admin-btn admin-btn--small admin-btn--primary">
                    + Nuovo articolo
                </button>
            </h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Titolo</th>
                            <th>Categoria</th>
                            <th>Slug</th>
                            <th>Autore</th>
                            <th>Stato</th>
                            <th>Creato</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {(q.data?.news ?? []).length === 0 && (
                            <tr><td colSpan={8} className="admin-table__empty">
                                {q.isLoading ? 'Caricamento…' : 'Nessuna news. Clicca "Nuovo articolo" per crearne uno.'}
                            </td></tr>
                        )}
                        {q.data?.news.map(n => (
                            <tr key={n.id}>
                                <td className="admin-table__id">#{n.id}</td>
                                <td><b>{n.title}</b></td>
                                <td><span className="admin-tag admin-tag--info">{n.categoryLabel}</span></td>
                                <td className="admin-table__mono">{n.slug}</td>
                                <td>{n.authorUsername ?? '—'}</td>
                                <td>
                                    {n.published
                                        ? <span className="admin-tag admin-tag--success">pubblicato</span>
                                        : <span className="admin-tag admin-tag--warning">bozza</span>}
                                </td>
                                <td>{fmtDate(n.createdAt)}</td>
                                <td style={{ whiteSpace: 'nowrap' }}>
                                    <button type="button" className="admin-btn admin-btn--small admin-btn--ghost" onClick={() => onEdit(n.id)}>Modifica</button>{' '}
                                    <button type="button" className="admin-btn admin-btn--small admin-btn--ghost" onClick={() => togglePublish.mutate({ id: n.id, published: !n.published })} disabled={togglePublish.isPending}>
                                        {n.published ? 'Bozza' : 'Pubblica'}
                                    </button>{' '}
                                    <button type="button" className="admin-btn admin-btn--small admin-btn--danger" onClick={() => confirmDelete(n.id, n.title)} disabled={delMut.isPending}>
                                        Elimina
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

function NewsEditor({ newsId, onClose }: { newsId: number | null; onClose: () => void }): ReactNode
{
    const qc = useQueryClient();
    const isNew = newsId === null;

    // Per la modifica scarico l'articolo via /staff/news/:slug → ho solo l'id.
    // Lookup via lista (già cached): seleziono dalla query 'staff news'.
    const listQ = useQuery<{ news: NewsRow[] }>({
        queryKey: ['staff', 'news'],
        queryFn: () => apiGet('/news'),
        enabled: !isNew
    });
    const existing = isNew ? null : listQ.data?.news.find(n => n.id === newsId);

    const [slug, setSlug] = useState(existing?.slug ?? '');
    const [title, setTitle] = useState(existing?.title ?? '');
    const [category, setCategory] = useState(existing?.category ?? NEWS_CATEGORIES[0]!.value);
    const [summary, setSummary] = useState(existing?.summary ?? '');
    const [bodyHtml, setBodyHtml] = useState(existing?.bodyHtml ?? '');
    const [image, setImage] = useState(existing?.image ?? '');
    const [published, setPublished] = useState(existing?.published ?? true);

    // Se l'articolo arriva dopo (lista caricata in async), ripopola gli stati.
    // Trick: usiamo un effect — qui niente useEffect import perché preferisco
    // mantenere il file leggero; basta una key sul componente.
    // → l'unica via pulita resta: condiziona il rendering finché 'existing' non è disponibile.
    if(!isNew && !existing) return <div style={{ padding: 20 }}>Caricamento articolo…</div>;

    const saveMut = useMutation<{ ok: boolean; id?: number; error?: string }, Error, void>({
        mutationFn: () =>
        {
            const cat = NEWS_CATEGORIES.find(c => c.value === category) ?? NEWS_CATEGORIES[0]!;
            const payload = {
                title, category, categoryLabel: cat.label,
                summary, bodyHtml, image, published,
                ...(slug ? { slug } : {})
            };
            if(isNew) return apiSend('/news', 'POST', payload);
            return apiSend(`/news/${newsId}`, 'PATCH', payload);
        },
        onSuccess: (r) =>
        {
            if(r.error)
            {
                window.alert(`Errore: ${r.error}`);
                return;
            }
            void qc.invalidateQueries({ queryKey: ['staff', 'news'] });
            void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] });
            onClose();
        }
    });

    function submit(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        saveMut.mutate();
    }

    const cat = NEWS_CATEGORIES.find(c => c.value === category) ?? NEWS_CATEGORIES[0]!;

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">
                    {isNew ? 'Nuovo articolo' : `Modifica articolo #${newsId}`}
                    <button type="button" className="admin-btn admin-btn--small admin-btn--ghost" onClick={onClose}>← Torna alla lista</button>
                </h2>

                <form onSubmit={submit}>
                    <div className="admin-row admin-row--2">
                        <label className="admin-field">
                            <span className="admin-field__label">Titolo</span>
                            <input className="admin-input" value={title} onChange={e => setTitle(e.target.value)} minLength={3} maxLength={200} required />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Slug URL {isNew && <em style={{ fontWeight: 400, color: '#4f6680' }}>— omettere per auto-generare dal titolo</em>}</span>
                            <input className="admin-input" value={slug} onChange={e => setSlug(e.target.value)} maxLength={80} pattern="[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?" placeholder="es. note-sulla-mega-patch" style={{ fontFamily: 'monospace', fontSize: 13 }} />
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Categoria</span>
                            <select className="admin-select" value={category} onChange={e => setCategory(e.target.value)}>
                                {NEWS_CATEGORIES.map(c => (
                                    <option key={c.value} value={c.value}>{c.label}</option>
                                ))}
                            </select>
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Immagine (URL relativo o assoluto)</span>
                            <input className="admin-input" value={image} onChange={e => setImage(e.target.value)} maxLength={255} placeholder="/assets/habbo/web_images/..." style={{ fontFamily: 'monospace', fontSize: 12 }} />
                        </label>
                    </div>

                    <label className="admin-field">
                        <span className="admin-field__label">Sommario <em style={{ fontWeight: 400, color: '#4f6680' }}>— testo mostrato nella card di anteprima (max 500 char)</em></span>
                        <textarea className="admin-textarea" value={summary} onChange={e => setSummary(e.target.value)} minLength={10} maxLength={500} required rows={3} />
                    </label>

                    <label className="admin-field">
                        <span className="admin-field__label">
                            Corpo articolo HTML
                            <em style={{ fontWeight: 400, color: '#4f6680' }}> — usa &lt;h2&gt;, &lt;p&gt;, &lt;ul&gt;, &lt;strong&gt;, &lt;code&gt;…</em>
                        </span>
                        <textarea
                            className="admin-textarea"
                            value={bodyHtml}
                            onChange={e => setBodyHtml(e.target.value)}
                            minLength={10}
                            required
                            rows={14}
                            style={{ fontFamily: 'monospace', fontSize: 13, lineHeight: 1.5 }}
                        />
                    </label>

                    <label style={{ display: 'inline-flex', alignItems: 'center', gap: 6, marginBottom: 12, fontSize: 14 }}>
                        <input type="checkbox" checked={published} onChange={e => setPublished(e.target.checked)} />
                        <span>Pubblicato (visibile su /community/news)</span>
                    </label>

                    <div style={{ display: 'flex', gap: 8 }}>
                        <button type="submit" disabled={saveMut.isPending} className="admin-btn admin-btn--primary">
                            {saveMut.isPending ? 'Salvataggio…' : isNew ? 'Crea articolo' : 'Salva modifiche'}
                        </button>
                        <button type="button" onClick={onClose} className="admin-btn admin-btn--ghost">Annulla</button>
                    </div>
                    {saveMut.error && <Alert kind="error">{saveMut.error.message}</Alert>}
                </form>
            </section>

            <section className="admin-card">
                <h2 className="admin-card__title">
                    Anteprima
                    <span className="admin-card__hint">come apparirà su /community/article/{slug || '…'}</span>
                </h2>
                <div style={{ background: '#fff', border: '1px solid #c5d2e1', borderRadius: 8, padding: 20 }}>
                    <div style={{ marginBottom: 10 }}>
                        <span className="admin-tag admin-tag--info">{cat.label}</span>
                    </div>
                    <h1 style={{ margin: '8px 0 6px', fontSize: 22, color: '#0c3a65' }}>{title || '— titolo —'}</h1>
                    <p style={{ margin: '0 0 14px', color: '#4f6680', fontStyle: 'italic' }}>{summary || '— sommario —'}</p>
                    {image && (
                        <img src={image} alt="" style={{ maxWidth: '100%', borderRadius: 6, marginBottom: 14, display: 'block' }} onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                    )}
                    <div
                        style={{ color: '#11243a', lineHeight: 1.55 }}
                        dangerouslySetInnerHTML={{ __html: bodyHtml || '<em style="color:#4f6680">— corpo articolo —</em>' }}
                    />
                </div>
            </section>
        </>
    );
}

// =====================================================================
// STAFF LIST
// =====================================================================
function StaffListSection(): ReactNode
{
    const q = useQuery<{ staff: { id: number; username: string; motto: string; look: string; rank: number; online: boolean; lastOnline: number }[] }>({
        queryKey: ['staff', 'staff-list'],
        queryFn: () => apiGet('/staff-list')
    });

    return (
        <section className="admin-card">
            <h2 className="admin-card__title">Membri staff (rank ≥ 5)</h2>
            <div className="admin-table-wrap">
                <table className="admin-table">
                    <thead><tr><th>Avatar</th><th>Username</th><th>Rank</th><th>Motto</th><th>Online</th><th>Ultimo accesso</th></tr></thead>
                    <tbody>
                        {(q.data?.staff ?? []).length === 0 && <tr><td colSpan={6} className="admin-table__empty">{q.isLoading ? 'Caricamento…' : 'Nessuno staff.'}</td></tr>}
                        {q.data?.staff.map(s => (
                            <tr key={s.id}>
                                <td><img src={avatarUrl(s.look, { headOnly: true, size: 's' })} alt={s.username} style={{ width: 32, height: 32, imageRendering: 'pixelated' }} /></td>
                                <td><b>{s.username}</b></td>
                                <td><span className="admin-tag admin-tag--info">Rank {s.rank}</span></td>
                                <td style={{ fontStyle: 'italic', color: '#4f6680' }}>{s.motto}</td>
                                <td>{s.online ? <span className="admin-tag admin-tag--success">online</span> : <span className="admin-tag admin-tag--neutral">offline</span>}</td>
                                <td>{fmtDate(s.lastOnline)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// =====================================================================
// QUICK ACTIONS (RCON azioni dirette)
// =====================================================================
type ActionKind = 'alert' | 'credits' | 'duckets' | 'diamonds' | 'badge' | 'disconnect' | 'mute';

function ActionsSection(): ReactNode
{
    const qc = useQueryClient();

    const userActionMut = useMutation<{ ok: boolean; error?: string }, Error, { kind: ActionKind; payload: Record<string, unknown> }>({
        mutationFn: ({ kind, payload }) => apiSend(`/${kind === 'badge' ? 'badge' : kind}`, 'POST', payload),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });
    const broadcastMut = useMutation<{ ok: boolean }, Error, string>({
        mutationFn: (m) => apiSend('/hotel-alert', 'POST', { message: m }),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });
    const catalogMut = useMutation<{ ok: boolean }, Error, void>({
        mutationFn: () => apiSend('/refresh-catalog', 'POST', {}),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });
    const wordfilterMut = useMutation<{ ok: boolean }, Error, void>({
        mutationFn: () => apiSend('/refresh-wordfilter', 'POST', {}),
        onSuccess: () => void qc.invalidateQueries({ queryKey: ['staff', 'audit-log'] })
    });

    const [kind, setKind] = useState<ActionKind>('alert');
    const [username, setUsername] = useState('');
    const [message, setMessage] = useState('');
    const [amount, setAmount] = useState(100);
    const [badge, setBadge] = useState('ACH_BasicClub1');
    const [minutes, setMinutes] = useState(5);

    const [broadcast, setBroadcast] = useState('');

    function submitUser(e: FormEvent<HTMLFormElement>): void
    {
        e.preventDefault();
        const payload: Record<string, unknown> = { username };
        if(kind === 'alert') payload.message = message;
        else if(kind === 'credits' || kind === 'duckets' || kind === 'diamonds') payload.amount = amount;
        else if(kind === 'badge') payload.badge = badge;
        else if(kind === 'mute') payload.minutes = minutes;
        userActionMut.mutate({ kind, payload });
    }

    return (
        <>
            <section className="admin-card">
                <h2 className="admin-card__title">Azione su utente</h2>
                <form onSubmit={submitUser}>
                    <div className="admin-row admin-row--2">
                        <label className="admin-field">
                            <span className="admin-field__label">Tipo azione</span>
                            <select className="admin-select" value={kind} onChange={e => setKind(e.target.value as ActionKind)}>
                                <option value="alert">Alert popup in-game</option>
                                <option value="credits">Crediti (+/-)</option>
                                <option value="duckets">Duckets (+/-)</option>
                                <option value="diamonds">Diamanti (+/-)</option>
                                <option value="badge">Assegna badge</option>
                                <option value="disconnect">Disconnetti (kick)</option>
                                <option value="mute">Mute (minuti)</option>
                            </select>
                        </label>
                        <label className="admin-field">
                            <span className="admin-field__label">Username target</span>
                            <input className="admin-input" value={username} onChange={e => setUsername(e.target.value)} required />
                        </label>
                    </div>
                    {kind === 'alert' && (
                        <label className="admin-field">
                            <span className="admin-field__label">Messaggio</span>
                            <textarea className="admin-textarea" value={message} onChange={e => setMessage(e.target.value)} required />
                        </label>
                    )}
                    {(kind === 'credits' || kind === 'duckets' || kind === 'diamonds') && (
                        <label className="admin-field">
                            <span className="admin-field__label">Quantità (negativa = togli)</span>
                            <input className="admin-input" type="number" value={amount} onChange={e => setAmount(Number(e.target.value))} required />
                        </label>
                    )}
                    {kind === 'badge' && (
                        <label className="admin-field">
                            <span className="admin-field__label">Codice badge</span>
                            <input className="admin-input" value={badge} onChange={e => setBadge(e.target.value)} required />
                        </label>
                    )}
                    {kind === 'mute' && (
                        <label className="admin-field">
                            <span className="admin-field__label">Durata (minuti)</span>
                            <input className="admin-input" type="number" min={1} max={1440} value={minutes} onChange={e => setMinutes(Number(e.target.value))} required />
                        </label>
                    )}
                    <button type="submit" disabled={userActionMut.isPending} className="admin-btn admin-btn--primary">
                        {userActionMut.isPending ? 'Invio…' : 'Esegui via RCON'}
                    </button>
                    {userActionMut.data?.ok && <Alert kind="success">Azione completata.</Alert>}
                    {userActionMut.data?.error && <Alert kind="error">{userActionMut.data.error}</Alert>}
                    {userActionMut.error && <Alert kind="error">{userActionMut.error.message}</Alert>}
                </form>
            </section>

            <section className="admin-card">
                <h2 className="admin-card__title">Azione su hotel</h2>
                <form onSubmit={e => { e.preventDefault(); broadcastMut.mutate(broadcast); setBroadcast(''); }}>
                    <label className="admin-field">
                        <span className="admin-field__label">Broadcast a tutti gli utenti</span>
                        <textarea className="admin-textarea" value={broadcast} onChange={e => setBroadcast(e.target.value)} required placeholder="Messaggio popup visualizzato a tutti gli utenti collegati…" />
                    </label>
                    <button type="submit" disabled={broadcastMut.isPending} className="admin-btn admin-btn--danger">
                        {broadcastMut.isPending ? 'Invio…' : 'Invia broadcast'}
                    </button>
                </form>

                <div style={{ display: 'flex', gap: 8, marginTop: 14, flexWrap: 'wrap' }}>
                    <button type="button" className="admin-btn admin-btn--ghost" disabled={catalogMut.isPending} onClick={() => catalogMut.mutate()}>
                        Ricarica catalogo
                    </button>
                    <button type="button" className="admin-btn admin-btn--ghost" disabled={wordfilterMut.isPending} onClick={() => wordfilterMut.mutate()}>
                        Ricarica wordfilter
                    </button>
                </div>
                {(broadcastMut.data?.ok || catalogMut.data?.ok || wordfilterMut.data?.ok) && <Alert kind="success">Azione hotel eseguita.</Alert>}
            </section>
        </>
    );
}
