import { type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { avatarUrl } from '../hooks/useAuth';
import './asteria.css';

/**
 * /profile/:username — profilo pubblico in stile Asteria.
 * GET /api/v2/profile/:username → { user, rooms, friends, groups, badges, achievements, photos, counts }.
 */

interface ProfileUser { id: number; username: string; motto: string; look: string; rank: number; accountCreated: number; lastOnline: number; online: boolean; }
interface ProfileRoom { id: number; name: string; description: string; users: number; maxUsers: number; model: string; }
interface ProfileFriend { id: number; username: string; look: string; online: boolean; }
interface ProfileGroup { id: number; name: string; description: string; badge: string; }
interface ProfileBadge { code: string; slot: number; }
interface ProfilePhoto { id: number; timestamp: number; url: string; }
interface ProfileCounts { photos: number; rooms: number; friends: number; groups: number; badges: number; achievements: number; }
interface ProfileData {
    user: ProfileUser;
    rooms: ProfileRoom[]; friends: ProfileFriend[]; groups: ProfileGroup[];
    badges: ProfileBadge[]; photos: ProfilePhoto[]; counts: ProfileCounts;
}

function fmtDate(unixSeconds: number): string
{
    if(!unixSeconds) return '—';
    return new Date(unixSeconds * 1000).toLocaleDateString('it-IT', { day: 'numeric', month: 'long', year: 'numeric' });
}

export function AsteriaProfilePage(): ReactNode
{
    const params = useParams<{ username: string }>();
    const username = params.username ?? '';
    const { data, isLoading, error } = useQuery<ProfileData | null>({
        queryKey: ['profile', username],
        queryFn: async ({ signal }) =>
        {
            const r = await fetch(`/api/v2/profile/${encodeURIComponent(username)}`, { credentials: 'include', signal });
            if(r.status === 404) return null;
            if(!r.ok) throw new Error('profile_' + r.status);
            return r.json() as Promise<ProfileData>;
        },
        enabled: username.length > 0,
        retry: false
    });

    return (
        <AsteriaShell activeNav="community">
            {isLoading && <section className="asteria-profile"><div className="asteria-news__state">Caricamento profilo…</div></section>}

            {!isLoading && (error || data === null) && (
                <section className="asteria-profile asteria-article--missing">
                    <div className="asteria-hero__eyebrow">Profilo</div>
                    <h1 className="asteria-article__title">{username || 'Utente'} non trovato</h1>
                    <p className="asteria-article__summary">Questo Habbo non esiste o ha nascosto il profilo.</p>
                    <div className="asteria-article__back"><Link to="/community/rooms" className="asteria-btn asteria-btn--ghost">Esplora la community</Link></div>
                </section>
            )}

            {data && <ProfileView data={data} />}
        </AsteriaShell>
    );
}

function ProfileView({ data }: { data: ProfileData }): ReactNode
{
    const { user, rooms, friends, groups, badges, photos, counts } = data;
    const isStaff = user.rank >= 5;

    return (
        <>
            <section className="asteria-profile__hero">
                <div className="asteria-profile__avatar" style={{ backgroundImage: `url("${avatarUrl(user.look, { size: 'l' })}")` }} />
                <div className="asteria-profile__id">
                    <div className="asteria-profile__online">
                        <span className={`asteria-dot ${user.online ? 'is-online' : ''}`} />
                        {user.online ? 'Online ora' : 'Offline'}
                    </div>
                    <h1 className="asteria-profile__name">{user.username}</h1>
                    <p className="asteria-profile__motto">{user.motto || '—'}</p>
                    <div className="asteria-profile__tags">
                        <span className="asteria-chip is-active">Rank {user.rank}</span>
                        {isStaff && <span className="asteria-chip is-active">STAFF</span>}
                        <span className="asteria-chip">Iscritto il {fmtDate(user.accountCreated)}</span>
                    </div>
                </div>
            </section>

            <div className="asteria-stats">
                <Stat value={counts.friends} label="Amici" />
                <Stat value={counts.rooms} label="Stanze" />
                <Stat value={counts.groups} label="Gruppi" />
                <Stat value={counts.badges} label="Badge" />
                <Stat value={counts.photos} label="Foto" />
                <Stat value={counts.achievements} label="Achievement" />
            </div>

            {badges.length > 0 && (
                <Section title="Badge" subtitle={`${counts.badges} totali`}>
                    <div className="asteria-profile__badges">
                        {badges.map(b => (
                            <img key={b.code} className="asteria-profile__badge" loading="lazy"
                                 src={`https://images.habbo.com/c_images/album1584/${b.code}.gif`} alt={b.code} title={b.code} />
                        ))}
                    </div>
                </Section>
            )}

            {friends.length > 0 && (
                <Section title="Amici" subtitle={`${counts.friends} totali`}>
                    <div className="asteria-profile__friends">
                        {friends.map(f => (
                            <Link key={f.id} to={`/profile/${encodeURIComponent(f.username)}`} className="asteria-profile__friend">
                                <img loading="lazy" src={avatarUrl(f.look, { size: 's', headOnly: true })} alt={f.username} />
                                <span className="asteria-profile__friend-name">{f.username}</span>
                                {f.online && <span className="asteria-dot is-online" />}
                            </Link>
                        ))}
                    </div>
                </Section>
            )}

            {rooms.length > 0 && (
                <Section title="Stanze" subtitle={`${counts.rooms} totali`}>
                    <div className="asteria-rooms__grid">
                        {rooms.map(r => (
                            <div key={r.id} className="asteria-room">
                                <div className="asteria-room__thumb"><span>{r.users}/{r.maxUsers}</span></div>
                                <div className="asteria-room__body">
                                    <h3 className="asteria-room__name">{r.name}</h3>
                                    <p className="asteria-room__desc">{r.description || '—'}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Section>
            )}

            {groups.length > 0 && (
                <Section title="Gruppi" subtitle={`${counts.groups} totali`}>
                    <div className="asteria-profile__groups">
                        {groups.map(g => (
                            <div key={g.id} className="asteria-profile__group">
                                <strong>{g.name}</strong>
                                <span>{g.description || '—'}</span>
                            </div>
                        ))}
                    </div>
                </Section>
            )}

            {photos.length > 0 && (
                <Section title="Foto" subtitle={`${counts.photos} scatti`}>
                    <div className="asteria-photos__grid">
                        {photos.map(p => (
                            <Link key={p.id} to={`/profile/${encodeURIComponent(user.username)}/photo/${p.id}`} className="asteria-photo">
                                <img loading="lazy" src={p.url} alt={`Foto ${p.id}`} />
                            </Link>
                        ))}
                    </div>
                </Section>
            )}
        </>
    );
}

function Stat({ value, label }: { value: number; label: string }): ReactNode
{
    return <div className="asteria-stat"><div className="asteria-stat__value">{value}</div><div className="asteria-stat__label">{label}</div></div>;
}

function Section({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }): ReactNode
{
    return (
        <section className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">{title}</h2>
                {subtitle && <span className="asteria-section__subtitle">{subtitle}</span>}
            </div>
            {children}
        </section>
    );
}
