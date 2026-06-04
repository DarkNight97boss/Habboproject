import { useQuery, useQueryClient } from '@tanstack/react-query';
import { type ReactNode, useState } from 'react';
import { Navigate } from 'react-router';
import { type AuthUser, avatarUrl, useAuth } from '../hooks/useAuth';
import { AsteriaNewsBento } from './AsteriaNewsBento';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * Dashboard /me Asteria — vista premium del proprio account.
 *
 *  - Hero personale con avatar grande + username + motto + rank badge
 *  - Card statistiche (crediti, diamanti, online time, friends, rooms)
 *  - Quick actions per le destinazioni più visitate
 *  - Recent activity feed
 *  - Order history shop (sintesi)
 *
 * Solo auth — redirect a /login se anon.
 */
export function AsteriaMePage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    if(isLoading) return null;
    if(!user) return <Navigate to="/login" replace />;
    return (
        <AsteriaShell activeNav="home">
            <MePanel user={user} />
            <MeStats user={user} />
            <MeStreak />
            <MeReferral />
            <MeMissions />
            <MeQuickActions />
            <AsteriaNewsBento title="Ultime news" subtitle="Cosa succede in Asteria" />
            <MeActivity />
            <MeFollowedRooms />
            <MeOrders />
        </AsteriaShell>
    );
}

function MePanel({ user }: { user: AuthUser }): ReactNode
{
    return (
        <section className="asteria-me__hero">
            <div className="asteria-me__avatar-frame">
                <img src={avatarUrl(user.look, { size: 'l' })} alt="" />
            </div>
            <div className="asteria-me__info">
                <div className="asteria-hero__eyebrow">Il tuo account</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(40px, 7vw, 96px)', margin: 0 }}>
                    {user.username}
                </h1>
                <p className="asteria-me__motto">{user.motto || '— motto vuoto —'}</p>
                <div className="asteria-me__badges">
                    <span className="asteria-me__badge asteria-me__badge--rank">RANK {user.rank}</span>
                    {user.rank >= 5 && <span className="asteria-me__badge asteria-me__badge--staff">STAFF</span>}
                </div>
                <div className="asteria-hero__cta" style={{ marginTop: 24 }}>
                    <a href="/gioca" className="asteria-btn asteria-btn--xl">▶ Entra nel hotel</a>
                    <a href={`/profile/${user.username}`} className="asteria-btn asteria-btn--ghost asteria-btn--xl">Profilo pubblico</a>
                </div>
            </div>
        </section>
    );
}

function MeStats({ user }: { user: AuthUser }): ReactNode
{
    return (
        <div className="asteria-stats">
            <div className="asteria-stat">
                <div className="asteria-stat__value">{user.credits.toLocaleString('it-IT')}</div>
                <div className="asteria-stat__label">Crediti</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">--</div>
                <div className="asteria-stat__label">Diamanti</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">{formatDate(user.account_created)}</div>
                <div className="asteria-stat__label">Iscritto dal</div>
            </div>
            <div className="asteria-stat">
                <div className="asteria-stat__value">{relativeFrom(user.last_online)}</div>
                <div className="asteria-stat__label">Ultima volta</div>
            </div>
        </div>
    );
}

interface StreakState
{
    enabled: boolean;
    currentStreak?: number;
    longestStreak?: number;
    totalClaims?: number;
    claimableToday?: boolean;
    nextReward?: number | null;
}

/**
 * Widget Daily Streak (#1). Visibile solo se il flag `daily_streak` è ON
 * (l'API risponde {enabled:false} se OFF → il widget non si renderizza).
 * Premio in crediti consegnato server-side via RCON; al claim invalidiamo
 * sia lo stato streak sia ['auth','me'] (crediti aggiornati).
 */
function MeStreak(): ReactNode
{
    const qc = useQueryClient();
    const { data, isLoading } = useQuery<StreakState>({
        queryKey: ['me', 'streak'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/streak', { credentials: 'include' });
            if(!r.ok) throw new Error('streak_fetch_failed');
            return r.json();
        },
        staleTime: 30_000
    });

    const [claiming, setClaiming] = useState(false);
    const [claimed, setClaimed] = useState<{ reward: number; delivered: boolean } | null>(null);
    const [error, setError] = useState(false);

    async function doClaim(): Promise<void>
    {
        if(claiming) return;
        setClaiming(true);
        setError(false);
        try
        {
            const r = await fetch('/api/v2/streak/claim', { method: 'POST', credentials: 'include' });
            const body = await r.json().catch(() => ({}));
            if(!r.ok) throw new Error('claim_failed');
            setClaimed({ reward: Number(body.reward ?? 0), delivered: body.rewardDelivered !== false });
            void qc.invalidateQueries({ queryKey: ['me', 'streak'] });
            void qc.invalidateQueries({ queryKey: ['auth', 'me'] });
        }
        catch { setError(true); }
        finally { setClaiming(false); }
    }

    if(isLoading) return null;
    if(!data?.enabled) return null; // feature flag OFF → widget invisibile

    const streak = data.currentStreak ?? 0;
    const claimable = (data.claimableToday ?? false) && !claimed;

    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">🔥 Streak giornaliero</h2>
                <span className="asteria-section__subtitle">Torna ogni giorno, accumula bonus</span>
            </div>
            <div className="asteria-streak">
                <div className="asteria-streak__count">
                    <div className="asteria-streak__num">{streak}</div>
                    <div className="asteria-streak__unit">giorni di fila</div>
                </div>
                <div className="asteria-streak__meta">
                    <div><strong>{data.longestStreak ?? 0}</strong> record</div>
                    <div><strong>{data.totalClaims ?? 0}</strong> riscatti totali</div>
                </div>
                <div className="asteria-streak__action">
                    {claimed ? (
                        <div className="asteria-streak__done">
                            +{claimed.reward} crediti!{!claimed.delivered && ' (in arrivo a breve)'}
                        </div>
                    ) : claimable ? (
                        <button className="asteria-btn asteria-btn--xl" disabled={claiming} onClick={() => void doClaim()}>
                            {claiming ? '…' : `Riscatta +${data.nextReward ?? 10} crediti`}
                        </button>
                    ) : (
                        <div className="asteria-streak__done">Già riscattato oggi · torna domani</div>
                    )}
                    {error && <div className="asteria-streak__err">Errore, riprova</div>}
                </div>
            </div>
        </div>
    );
}

interface ReferralState
{
    enabled: boolean;
    code: string | null;
    rewardReferrer: number;
    rewardReferred: number;
    invited: { username: string | null; at: number; reward: number }[];
    invitedCount: number;
    alreadyRedeemed: boolean;
}

/**
 * Referral (#15). Mostra il mio codice invito + riscatto (per account recenti).
 * Visibile solo se il flag `referral` è ON (API → {enabled:false} se OFF).
 */
function MeReferral(): ReactNode
{
    const qc = useQueryClient();
    const { data, isLoading } = useQuery<ReferralState>({
        queryKey: ['me', 'referral'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/referral', { credentials: 'include' });
            if(!r.ok) throw new Error('referral_fetch_failed');
            return r.json();
        },
        staleTime: 30_000
    });

    const [code, setCode] = useState('');
    const [busy, setBusy] = useState(false);
    const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);

    async function redeem(): Promise<void>
    {
        if(busy || !code.trim()) return;
        setBusy(true);
        setMsg(null);
        try
        {
            const r = await fetch('/api/v2/referral/redeem', {
                method: 'POST',
                credentials: 'include',
                headers: { 'content-type': 'application/json' },
                body: JSON.stringify({ code: code.trim() })
            });
            const body = await r.json().catch(() => ({}));
            if(!r.ok)
            {
                const errs: Record<string, string> = {
                    already_redeemed: 'Hai già usato un codice invito.',
                    account_too_old: 'I codici invito valgono solo per account recenti.',
                    code_not_found: 'Codice non valido.',
                    cannot_self_refer: 'Non puoi usare il tuo stesso codice.',
                    feature_disabled: 'Funzione non attiva.'
                };
                setMsg({ ok: false, text: errs[String(body.error)] ?? 'Operazione non riuscita.' });
            }
            else
            {
                setMsg({ ok: true, text: `Fatto! +${Number(body.rewardReferred ?? 0)} crediti a te e +${Number(body.rewardReferrer ?? 0)} a chi ti ha invitato.` });
                setCode('');
                void qc.invalidateQueries({ queryKey: ['me', 'referral'] });
                void qc.invalidateQueries({ queryKey: ['auth', 'me'] });
            }
        }
        catch { setMsg({ ok: false, text: 'Errore di rete, riprova.' }); }
        finally { setBusy(false); }
    }

    if(isLoading) return null;
    if(!data?.enabled) return null; // feature flag OFF → invisibile

    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">🎁 Invita un amico</h2>
                <span className="asteria-section__subtitle">+{data.rewardReferrer} crediti a te, +{data.rewardReferred} al tuo amico</span>
            </div>
            <div className="asteria-referral">
                <div>
                    <span className="asteria-referral__label">Il tuo codice</span>
                    <div className="asteria-referral__code">
                        <code className="asteria-referral__value">{data.code ?? '—'}</code>
                        {data.code && (
                            <button
                                className="asteria-btn asteria-btn--ghost"
                                onClick={() => { void navigator.clipboard?.writeText(data.code ?? '').catch(() => undefined); }}
                            >Copia</button>
                        )}
                    </div>
                </div>

                {!data.alreadyRedeemed && (
                    <div className="asteria-referral__redeem">
                        <span className="asteria-referral__label">Hai un codice da un amico?</span>
                        <div className="asteria-referral__row">
                            <input
                                className="asteria-input"
                                value={code}
                                onChange={e => setCode(e.target.value.toUpperCase())}
                                placeholder="ABC123"
                                maxLength={16}
                            />
                            <button className="asteria-btn" disabled={busy || !code.trim()} onClick={() => void redeem()}>
                                {busy ? '…' : 'Riscatta'}
                            </button>
                        </div>
                    </div>
                )}

                {msg && <div className={msg.ok ? 'asteria-referral__ok' : 'asteria-referral__err'}>{msg.text}</div>}

                {data.invitedCount > 0 && (
                    <div className="asteria-referral__meta">
                        <strong>{data.invitedCount}</strong> {data.invitedCount === 1 ? 'amico invitato' : 'amici invitati'}
                    </div>
                )}
            </div>
        </div>
    );
}

interface MissionRow { id: string; title: string; period: 'daily' | 'weekly'; target: number; reward: number; progress: number; claimed: boolean; claimable: boolean }

/**
 * Missioni (#3). Progresso calcolato lato server contando gli activity event.
 * Visibile solo se il flag `missions` è ON (API → {enabled:false} se OFF).
 */
function MeMissions(): ReactNode
{
    const qc = useQueryClient();
    const { data, isLoading } = useQuery<{ enabled: boolean; missions: MissionRow[] }>({
        queryKey: ['me', 'missions'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/missions', { credentials: 'include' });
            if(!r.ok) throw new Error('missions_failed');
            return r.json();
        },
        staleTime: 20_000
    });
    const [busy, setBusy] = useState<string | null>(null);

    async function claim(id: string): Promise<void>
    {
        if(busy) return;
        setBusy(id);
        try
        {
            const r = await fetch(`/api/v2/missions/claim/${id}`, { method: 'POST', credentials: 'include' });
            if(r.ok) void qc.invalidateQueries({ queryKey: ['auth', 'me'] });
        }
        finally
        {
            setBusy(null);
            void qc.invalidateQueries({ queryKey: ['me', 'missions'] });
        }
    }

    if(isLoading) return null;
    if(!data?.enabled) return null; // feature flag OFF → invisibile
    const missions = data.missions || [];
    if(!missions.length) return null;

    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">🎯 Missioni</h2>
                <span className="asteria-section__subtitle">Completa e riscatta crediti</span>
            </div>
            <div className="asteria-missions">
                {missions.map(m =>
                {
                    const pct = m.target > 0 ? Math.round((m.progress / m.target) * 100) : 0;
                    return (
                        <div key={m.id} className="asteria-mission">
                            <div className="asteria-mission__info">
                                <div className="asteria-mission__title">
                                    {m.title}
                                    <span className="asteria-mission__tag">{m.period === 'daily' ? 'Giornaliera' : 'Settimanale'}</span>
                                </div>
                                <div className="asteria-mission__bar"><span style={{ width: `${pct}%` }} /></div>
                                <div className="asteria-mission__prog">{m.progress}/{m.target} · +{m.reward} crediti</div>
                            </div>
                            <div className="asteria-mission__action">
                                {m.claimed
                                    ? <span className="asteria-mission__done">✓ Riscattata</span>
                                    : m.claimable
                                        ? <button className="asteria-btn" disabled={busy === m.id} onClick={() => void claim(m.id)}>{busy === m.id ? '…' : 'Riscatta'}</button>
                                        : <span className="asteria-mission__todo">In corso</span>}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

interface FeedEvent
{
    id: number;
    actor_name: string;
    type: string;
    payload: unknown;
    created_at: string;
}

function renderEvent(e: FeedEvent): { icon: string; text: ReactNode }
{
    const who = e.actor_name || 'Qualcuno';
    const p = (e.payload && typeof e.payload === 'object') ? e.payload as Record<string, unknown> : {};
    switch(e.type)
    {
        case 'user.registered': return { icon: '🎉', text: <><strong>{who}</strong> si è unito ad Asteria</> };
        case 'streak.claimed':  return { icon: '🔥', text: <><strong>{who}</strong> ha riscattato uno streak di {String(p.streak ?? '?')} giorni</> };
        case 'shop.purchase':   return { icon: '🛒', text: <><strong>{who}</strong> ha acquistato {String(p.item ?? 'un articolo')}</> };
        case 'badge.earned':    return { icon: '🏅', text: <><strong>{who}</strong> ha ottenuto un distintivo</> };
        case 'room.created':    return { icon: '🏠', text: <><strong>{who}</strong> ha creato la stanza {p.room ? <em>«{String(p.room)}»</em> : 'una stanza'}</> };
        case 'guild.created':   return { icon: '🛡️', text: <><strong>{who}</strong> ha fondato il gruppo {p.guild ? <em>«{String(p.guild)}»</em> : ''}</> };
        case 'photo.posted':    return { icon: '📸', text: <><strong>{who}</strong> ha pubblicato una foto</> };
        case 'achievement.unlocked': return { icon: '🏆', text: <><strong>{who}</strong> ha sbloccato un obiettivo</> };
        case 'referral.redeemed': return { icon: '🎁', text: <><strong>{who}</strong> si è unito tramite invito</> };
        default:                return { icon: '✨', text: <><strong>{who}</strong> · {e.type}</> };
    }
}

/**
 * Activity feed di community (#11). Consuma /api/v2/activity/feed (event-bus).
 * Visibile solo se il flag `activity_feed` è ON (API → {enabled:false} se OFF).
 * Gli eventi in-game (badge/stanze) arriveranno dal ponte EMU (Fase 2); per ora
 * mostra gli eventi CMS-side (registrazioni, streak, acquisti).
 */
function MeActivity(): ReactNode
{
    const { data, isLoading } = useQuery<{ enabled: boolean; events: FeedEvent[] }>({
        queryKey: ['community', 'activity'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/activity/feed?limit=12', { credentials: 'include' });
            if(!r.ok) throw new Error('feed_failed');
            return r.json();
        },
        staleTime: 20_000
    });
    if(isLoading) return null;
    if(!data?.enabled) return null;
    const events = data.events || [];
    if(!events.length) return null;
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">Attività di community</h2>
                <span className="asteria-section__subtitle">Gli ultimi movimenti</span>
            </div>
            <div className="asteria-feed">
                {events.map(e =>
                {
                    const { icon, text } = renderEvent(e);
                    return (
                        <div key={e.id} className="asteria-feed__item">
                            <span className="asteria-feed__icon">{icon}</span>
                            <span className="asteria-feed__text">{text}</span>
                            <span className="asteria-feed__time">{relativeFrom(Math.floor(new Date(e.created_at).getTime() / 1000))}</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

interface FollowedRoom { id: number; name: string; users: number; maxUsers: number; ownerName: string; }

/**
 * Stanze seguite (#12). Visibile se flag `room_follow` ON e l'utente ne segue
 * almeno una. "Entra" porta al client (il deep-link alla stanza specifica
 * arriverà col ponte EMU, Fase 2).
 */
function MeFollowedRooms(): ReactNode
{
    const { data, isLoading } = useQuery<{ enabled: boolean; rooms: FollowedRoom[] }>({
        queryKey: ['me', 'followed-rooms'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/community/rooms/followed', { credentials: 'include' });
            if(!r.ok) throw new Error('followed_failed');
            return r.json();
        },
        staleTime: 20_000
    });
    if(isLoading) return null;
    if(!data?.enabled) return null;
    const rooms = data.rooms || [];
    if(!rooms.length) return null;
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">⭐ Stanze seguite</h2>
                <a href="/community/rooms" className="asteria-section__subtitle" style={{ textDecoration: 'none', color: '#a855f7' }}>SCOPRI STANZE ↗</a>
            </div>
            <div className="asteria-feed">
                {rooms.map(r => (
                    <div key={r.id} className="asteria-feed__item">
                        <span className="asteria-feed__icon">🏠</span>
                        <span className="asteria-feed__text"><strong>{r.name}</strong> · {r.users}/{r.maxUsers} online</span>
                        <a className="asteria-feed__time" href="/gioca" style={{ color: '#22d3ee', textDecoration: 'none' }}>Entra ▶</a>
                    </div>
                ))}
            </div>
        </div>
    );
}

function MeQuickActions(): ReactNode
{
    const actions = [
        { icon: '◉', label: 'Profilo pubblico', href: '/profile' },
        { icon: '⌂', label: 'Le mie stanze', href: '/community/rooms' },
        { icon: '⊕', label: 'Compra crediti', href: '/shop' },
        { icon: '⚙', label: 'Impostazioni', href: '/settings/privacy' },
        { icon: '✉', label: 'Messaggi', href: '/messaging' },
        { icon: '✦', label: 'Collezionabili', href: '/habbo-nft' }
    ];
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">Azioni rapide</h2>
                <span className="asteria-section__subtitle">Tutto a portata di mano</span>
            </div>
            <div className="asteria-quick">
                {actions.map(a => (
                    <a key={a.label} href={a.href}>
                        <span className="asteria-quick__icon">{a.icon}</span>
                        <span>{a.label}</span>
                    </a>
                ))}
            </div>
        </div>
    );
}

interface OrderRow {
    id: number;
    status: string;
    amount_cents: number;
    currency: string;
    created_at: string;
    delivered_at: string | null;
    delivery_log: string | null;
    item_name: string;
    image_gradient: string;
}

function MeOrders(): ReactNode
{
    const { data, isLoading } = useQuery<{ orders: OrderRow[] }>({
        queryKey: ['me', 'orders'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/shop/orders', { credentials: 'include' });
            if(!r.ok) throw new Error('orders_fetch_failed');
            return r.json();
        },
        staleTime: 30_000
    });
    if(isLoading) return null;
    const orders = data?.orders || [];
    if(!orders.length) return null;
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">I tuoi acquisti</h2>
                <a href="/shop" className="asteria-section__subtitle" style={{ textDecoration: 'none', color: '#a855f7' }}>VAI ALLO SHOP ↗</a>
            </div>
            <div className="asteria-orders">
                {orders.slice(0, 6).map(o => (
                    <div key={o.id} className="asteria-order">
                        <div className="asteria-order__cover" style={{ background: o.image_gradient }} />
                        <div className="asteria-order__body">
                            <div className="asteria-order__name">{o.item_name}</div>
                            <div className="asteria-order__meta">
                                {(o.amount_cents / 100).toFixed(2)} {o.currency} · {formatDate(Math.floor(new Date(o.created_at).getTime() / 1000))}
                            </div>
                            <span className={`asteria-order__status asteria-order__status--${o.status}`}>{translateStatus(o.status)}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function translateStatus(s: string): string
{
    return { pending: 'In attesa', paid: 'Pagato', failed: 'Fallito', refunded: 'Rimborsato' }[s] || s;
}

function formatDate(unixSeconds: number): string
{
    if(!unixSeconds) return '—';
    const d = new Date(unixSeconds * 1000);
    return d.toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' });
}

function relativeFrom(unixSeconds: number): string
{
    if(!unixSeconds) return '—';
    const ageMs = Date.now() - unixSeconds * 1000;
    if(ageMs < 60_000) return 'ora';
    if(ageMs < 3_600_000) return Math.floor(ageMs / 60_000) + ' min fa';
    if(ageMs < 86_400_000) return Math.floor(ageMs / 3_600_000) + ' h fa';
    if(ageMs < 30 * 86_400_000) return Math.floor(ageMs / 86_400_000) + ' g fa';
    return formatDate(unixSeconds);
}
