import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';
import { NotFoundPage } from './NotFoundPage';

/**
 * Pagina /profile/:username — replica fedele habbo.it/profile.
 *
 * Struttura DOM ufficiale:
 *   <main.wrapper--content>
 *     <header.profile__header>            (banner BG isometric pattern)
 *       <habbo-avatar.profile__avatar>
 *         <img class=imager (full body, size=l)>
 *       <h1.profile__title>{username}
 *     <div.profile__section>
 *       <div.profile__card__wrapper--rooms>      (se ha stanze)
 *       <div.profile__card__wrapper--groups>     (se in gruppi)
 *       <div.profile__card__wrapper--friends>    (se ha amici)
 *       <div.profile__card__wrapper--badges>     (badges visibili in slot)
 *       <div.profile__card__wrapper--achievements> (top achievements)
 *     <p.profile__registered>Registrato su Habbo il {date}
 */
export function ProfilePage(): ReactNode
{
    const { data: authUser, isLoading: authLoading } = useAuth();
    const { username } = useParams<{ username: string }>();

    const profileQuery = useQuery({
        queryKey: ['profile', username],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/profile/${encodeURIComponent(username ?? '')}`);
            if(r.status === 404) return null;
            if(!r.ok) throw new Error('profile_failed');
            return r.json() as Promise<ProfileResponse>;
        },
        enabled: !!username,
        retry: false
    });

    if(authLoading || profileQuery.isLoading) return null;
    if(profileQuery.data === null) return <NotFoundPage />;
    if(!profileQuery.data) return null;

    const data = profileQuery.data;
    const regDate = data.user.accountCreated > 0
        ? new Date(data.user.accountCreated * 1000).toLocaleDateString('it-IT', { day: '2-digit', month: 'long', year: 'numeric' })
        : '—';

    // La CSS ufficiale habbo.it ha regole `.profile__card__wrapper--rooms/
    // --groups/--friends/--badges` (float:left, width:50%, teaser image
    // ::before in cima alla card) ma NON include una variante --achievements.
    // Sull'ufficiale gli achievement sono dentro il client, non nel profilo
    // web pubblico. Non li mostriamo qui per rispettare la fedeltà 1:1.
    const body = (
        <main className="wrapper wrapper--content">
            <ProfileBanner user={data.user} />
            <div className="profile__section">
                {data.rooms.length > 0 && <RoomsCard rooms={data.rooms} totalCount={data.counts.rooms} />}
                {data.groups.length > 0 && <GroupsCard groups={data.groups} totalCount={data.counts.groups} />}
                {data.friends.length > 0 && <FriendsCard friends={data.friends} totalCount={data.counts.friends} />}
                {data.badges.length > 0 && <BadgesCard badges={data.badges} totalCount={data.counts.badges} />}
                {data.rooms.length === 0 && data.groups.length === 0 && data.friends.length === 0 && data.badges.length === 0 && (
                    <habbo-empty-results>
                        <span>Questo profilo non ha ancora attività pubbliche.</span>
                    </habbo-empty-results>
                )}
            </div>
            <p className="profile__registered" style={{ textAlign: 'center', marginTop: 24, color: '#7ecaee', textTransform: 'uppercase' }}>
                Registrato su Habbo il <strong>{regDate}</strong>
            </p>
        </main>
    );

    if(authUser) return <AuthedShell user={authUser}>{body}</AuthedShell>;
    return (
        <>
            <div className="content">
                <header className="header__wrapper wrapper">
                    <a href="/" className="header__habbo__logo">
                        <h1 className="header__habbo__name">Habbo</h1>
                    </a>
                </header>
                {body}
            </div>
        </>
    );
}

// ============================================================
// PROFILE BANNER
// ============================================================

interface ProfileUser
{
    id: number;
    username: string;
    motto: string;
    look: string;
    rank: number;
    accountCreated: number;
    lastOnline: number;
    online: boolean;
}

function ProfileBanner({ user }: { user: ProfileUser }): ReactNode
{
    // Layout banner mirror habbo.it/profile/<user>:
    //   <header.profile__header>            ← CSS ufficiale applica bg
    //                                         profile.69262798.png + height
    //     <div.profile__header__content.wrapper>
    //       <habbo-avatar.profile__header__avatar>
    //         <img/>                        ← avatar fullbody size=l
    //       <div.profile__header__user>
    //         <h1.profile__header__name>{username}
    //         <p.profile__header__motto>{motto}
    return (
        <header className="profile__header">
            <div className="profile__header__content wrapper" style={{ display: 'flex', alignItems: 'center', gap: 24, padding: '20px 12px', minHeight: 130 }}>
                <habbo-avatar className="profile__header__avatar">
                    <img
                        src={avatarUrl(user.look, { headOnly: false, size: 'l' })}
                        alt={user.username}
                        style={{ imageRendering: 'pixelated' }}
                    />
                </habbo-avatar>
                <div className="profile__header__user" style={{ flex: 1, color: '#fff', textShadow: '0 2px 4px rgba(0,0,0,.5)' }}>
                    <h1 className="profile__header__name" style={{ margin: 0, fontSize: 32, textTransform: 'uppercase' }}>
                        {user.username}
                    </h1>
                    {user.motto && (
                        <p className="profile__header__motto" style={{ margin: '6px 0', fontStyle: 'italic', fontSize: 14 }}>
                            "{user.motto}"
                        </p>
                    )}
                    <p style={{ margin: '4px 0 0', fontSize: 12, opacity: 0.85 }}>
                        {user.online ? '🟢 Online' : '⚪ Offline'}
                    </p>
                </div>
            </div>
        </header>
    );
}

// ============================================================
// CARDS (Stanze / Gruppi / Amici / Badge / Achievement)
// ============================================================

function Card({ wrapperClass, title, children, footer }: {
    wrapperClass: string;
    title: string;
    children: ReactNode;
    footer?: ReactNode;
}): ReactNode
{
    return (
        <div className={`profile__card__wrapper--${wrapperClass}`}>
            <section className="profile__card__aligner">
                <div className="profile__card">
                    <h2 className="profile__card__title">{title}</h2>
                    {children}
                    {footer && (
                        <div className="profile__card__footer">
                            {footer}
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}

function VediTutto({ href }: { href: string }): ReactNode
{
    return (
        <habbo-profile-modal>
            <a className="profile-modal__link" href={href}>Vedi tutto</a>
        </habbo-profile-modal>
    );
}

function RoomsCard({ rooms, totalCount }: { rooms: Room[]; totalCount: number }): ReactNode
{
    return (
        <Card wrapperClass="rooms" title="Stanze" footer={totalCount > rooms.length ? <VediTutto href="#" /> : null}>
            <habbo-room-list className="item-list--grid">
                <ul>
                    {rooms.slice(0, 6).map(r => (
                        <li key={r.id} className="item item--room">
                            <a className="item__content" href={`/gioca?room=${r.id}`}>
                                <div className="item__title">{r.name}</div>
                                {r.description && <div className="item__description">{r.description}</div>}
                            </a>
                        </li>
                    ))}
                </ul>
            </habbo-room-list>
        </Card>
    );
}

function GroupsCard({ groups, totalCount }: { groups: Group[]; totalCount: number }): ReactNode
{
    return (
        <Card wrapperClass="groups" title="Gruppi" footer={totalCount > groups.length ? <VediTutto href="#" /> : null}>
            <div className="item-list--grid">
                <ul>
                    {groups.slice(0, 6).map(g => (
                        <li key={g.id} className="item item--group">
                            <a className="item__content" href="#">
                                <div className="item__title">{g.name}</div>
                                {g.description && <div className="item__description">{g.description.substring(0, 60)}</div>}
                            </a>
                        </li>
                    ))}
                </ul>
            </div>
        </Card>
    );
}

function FriendsCard({ friends, totalCount }: { friends: Friend[]; totalCount: number }): ReactNode
{
    return (
        <Card wrapperClass="friends" title="Amici" footer={totalCount > friends.length ? <VediTutto href="#" /> : null}>
            <ul style={{ display: 'flex', flexWrap: 'wrap', gap: 12, listStyle: 'none', padding: 0, margin: 0, justifyContent: 'center' }}>
                {friends.slice(0, 6).map(f => (
                    <li key={f.id} style={{ textAlign: 'center' }}>
                        <a href={`/profile/${encodeURIComponent(f.username)}`} style={{ display: 'block', textDecoration: 'none' }}>
                            <habbo-imager>
                                <img
                                    src={avatarUrl(f.look, { headOnly: true, size: 'b' })}
                                    alt={f.username}
                                    style={{ imageRendering: 'pixelated' }}
                                />
                            </habbo-imager>
                            <div style={{ fontSize: 12, color: '#fff', marginTop: 4 }}>
                                {f.online && <span style={{ color: '#5fde5f' }}>● </span>}
                                {f.username}
                            </div>
                        </a>
                    </li>
                ))}
            </ul>
        </Card>
    );
}

function BadgesCard({ badges, totalCount }: { badges: Badge[]; totalCount: number }): ReactNode
{
    return (
        <Card wrapperClass="badges" title="Badge" footer={totalCount > badges.length ? <VediTutto href="#" /> : null}>
            <ul style={{ display: 'flex', gap: 10, listStyle: 'none', padding: 0, margin: 0, justifyContent: 'center', flexWrap: 'wrap' }}>
                {badges.map(b => (
                    <li key={b.code} title={b.code}>
                        <img
                            src={`https://images.habbo.com/c_images/album1584/${b.code}.gif`}
                            alt={b.code}
                            style={{ imageRendering: 'pixelated' }}
                            onError={e => { (e.currentTarget as HTMLImageElement).style.display = 'none'; }}
                        />
                    </li>
                ))}
            </ul>
        </Card>
    );
}

function AchievementsCard({ achievements, totalCount }: { achievements: Achievement[]; totalCount: number }): ReactNode
{
    return (
        <Card wrapperClass="achievements" title="Achievement" footer={totalCount > achievements.length ? <VediTutto href="#" /> : null}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, color: '#fff' }}>
                {achievements.slice(0, 6).map(a => (
                    <li key={a.achievement_name} style={{ padding: '4px 0', borderBottom: '1px solid rgba(255,255,255,0.1)', fontSize: 13 }}>
                        <strong>{a.achievement_name.replace(/^ACH_/, '').replace(/_/g, ' ')}</strong>
                        <span style={{ opacity: 0.7, marginLeft: 8 }}>livello {a.progress}</span>
                    </li>
                ))}
            </ul>
        </Card>
    );
}

// ============================================================
// TYPES
// ============================================================

interface Room
{
    id: number;
    name: string;
    description: string;
    users: number;
    maxUsers: number;
    model: string;
}

interface Friend
{
    id: number;
    username: string;
    look: string;
    online: boolean;
}

interface Group
{
    id: number;
    name: string;
    description: string;
    badge: string;
}

interface Badge
{
    code: string;
    slot: number;
}

interface Achievement
{
    achievement_name: string;
    progress: number;
}

interface ProfileResponse
{
    user: ProfileUser;
    rooms: Room[];
    friends: Friend[];
    groups: Group[];
    badges: Badge[];
    achievements: Achievement[];
    counts: {
        photos: number;
        rooms: number;
        friends: number;
        groups: number;
        badges: number;
        achievements: number;
    };
}
