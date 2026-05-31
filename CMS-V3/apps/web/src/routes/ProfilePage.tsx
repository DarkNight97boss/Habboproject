import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';
import { NotFoundPage } from './NotFoundPage';

/**
 * Pagina /profile/:username — replica DOM fedele habbo.it/profile/<user>.
 *
 * Struttura ufficiale ispezionata (es. habbo.it/profile/Avaren):
 *
 *   <habbo-header-small class="profile__header">
 *     <header.header__wrapper.wrapper>                                 ← logo + user-menu
 *     <habbo-navigation>                                               ← nav HOME/COMMUNITY/…
 *     <habbo-profile-header>                                           ← BANNER PROFILO
 *       <div.profile-header__avatar>
 *         <div.profile-header__link>
 *           <habbo-imager.profile-header__image>
 *             <img.imager>                                             ← avatar head-only round
 *       <div.profile-header__details>
 *         <div><h1>{username}</h1><div.profile__motto>{motto}</div></div>
 *
 *   <main.wrapper--content>
 *     <div.profile__section>
 *       <div.profile__card__wrapper--badges>   <h2>Distintivi
 *       <div.profile__card__wrapper--friends>  <h2><span>Amici</span>
 *                                                    <span.profile__friends__count>(N di 1000)</span>
 *       <div.profile__card__wrapper--rooms>    <h2>Stanze
 *       <div.profile__card__wrapper--groups>   <h2>Gruppi
 *
 * Layout 2-col floated via CSS ufficiale (.profile__card__wrapper--*
 * { float:left; width:50% }). Ogni card ha un teaser ::before
 * (illustration profilo specifica) sopra.
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

    // Order ufficiale habbo.it Avaren: Distintivi, Amici, Stanze, Gruppi,
    // poi sezione Foto (full-width sotto), poi "Registrato su Habbo il <date>"
    // + 3 cuoricini decorativi.
    const body = (
        <main className="wrapper wrapper--content">
            <div className="profile__section">
                {data.badges.length > 0 && <BadgesCard badges={data.badges} totalCount={data.counts.badges} />}
                {data.friends.length > 0 && <FriendsCard friends={data.friends} totalCount={data.counts.friends} />}
                {data.rooms.length > 0 && <RoomsCard rooms={data.rooms} totalCount={data.counts.rooms} />}
                {data.groups.length > 0 && <GroupsCard groups={data.groups} totalCount={data.counts.groups} />}
                {data.rooms.length === 0 && data.groups.length === 0 && data.friends.length === 0 && data.badges.length === 0 && (
                    <habbo-empty-results>
                        <span>Questo profilo non ha ancora attività pubbliche.</span>
                    </habbo-empty-results>
                )}
            </div>
            {data.photos.length > 0 && <PhotosSection photos={data.photos} username={data.user.username} />}
            <p className="profile__registered" style={{ textAlign: 'center', marginTop: 24, color: '#7ecaee', textTransform: 'uppercase' }}>
                Registrato su Habbo il <strong>{regDate}</strong>
            </p>
            {/* 3 cuoricini decorativi come l'ufficiale Avaren in fondo */}
            <div style={{ textAlign: 'center', margin: '16px 0', fontSize: 22, letterSpacing: 8 }}>
                <span style={{ color: '#e84d6e' }}>♥</span>
                <span style={{ color: '#e84d6e' }}>♥</span>
                <span style={{ color: '#e84d6e' }}>♥</span>
            </div>
        </main>
    );

    // Inietta il banner profilo come <habbo-profile-header> dentro l'header
    // chrome (vedi AuthedShell.extraHeaderContent). La class 'profile__header'
    // applica il bg isometric pattern via CSS ufficiale.
    const profileBanner = <ProfileHeader user={data.user} />;

    if(authUser)
    {
        return (
            <AuthedShell
                user={authUser}
                extraHeaderClass="profile__header"
                extraHeaderContent={profileBanner}
            >
                {body}
            </AuthedShell>
        );
    }

    return (
        <>
            <div className="content">
                <header className="header__wrapper wrapper">
                    <a href="/" className="header__habbo__logo">
                        <h1 className="header__habbo__name">Habbo</h1>
                    </a>
                </header>
                {profileBanner}
                {body}
            </div>
        </>
    );
}

// ============================================================
// PROFILE HEADER (dentro <habbo-header-small.profile__header>)
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

function ProfileHeader({ user }: { user: ProfileUser }): ReactNode
{
    return (
        <habbo-profile-header>
            <div className="profile-header__avatar">
                <div className="profile-header__link">
                    <habbo-imager className="profile-header__image">
                        <img
                            src={avatarUrl(user.look, { headOnly: true, size: 'l' })}
                            alt={user.username}
                            className="imager"
                        />
                    </habbo-imager>
                </div>
            </div>
            <div className="profile-header__details">
                <div>
                    <h1>{user.username}</h1>
                    {user.motto && <div className="profile__motto">{user.motto}</div>}
                </div>
            </div>
        </habbo-profile-header>
    );
}

// ============================================================
// CARDS
// ============================================================

function Card({ wrapperClass, title, children, footer }: {
    wrapperClass: string;
    title: ReactNode;
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

function BadgesCard({ badges, totalCount }: { badges: Badge[]; totalCount: number }): ReactNode
{
    return (
        <Card
            wrapperClass="badges"
            title="Distintivi"
            footer={totalCount > badges.length ? <VediTutto href="#" /> : null}
        >
            <habbo-badge-list className="item-list--grid">
                <ul style={{ display: 'flex', gap: 10, listStyle: 'none', padding: 0, margin: 0, justifyContent: 'center', flexWrap: 'wrap' }}>
                    {badges.map(b => (
                        <li key={b.code} title={b.code} style={{ textAlign: 'center' }}>
                            <img
                                src={`https://images.habbo.com/c_images/album1584/${b.code}.gif`}
                                alt={b.code}
                                style={{ imageRendering: 'pixelated', display: 'block', margin: '0 auto' }}
                                onError={e => { (e.currentTarget as HTMLImageElement).style.display = 'none'; }}
                            />
                        </li>
                    ))}
                </ul>
            </habbo-badge-list>
        </Card>
    );
}

function FriendsCard({ friends, totalCount }: { friends: Friend[]; totalCount: number }): ReactNode
{
    // Title con count come ufficiale: "Amici (5 di 1000)" — 1000 è il max
    // friend slot Arcturus standard. Sostituirlo con env var se diverso.
    return (
        <Card
            wrapperClass="friends"
            title={<>
                <span>Amici</span>
                <span className="profile__friends__count"> ({totalCount} di 1000)</span>
            </>}
            footer={totalCount > friends.length ? <VediTutto href="#" /> : null}
        >
            <habbo-friend-list className="item-list--grid">
                <ul style={{ display: 'flex', flexWrap: 'wrap', gap: 12, listStyle: 'none', padding: 0, margin: 0, justifyContent: 'center' }}>
                    {friends.slice(0, 6).map(f => (
                        <li key={f.id} style={{ textAlign: 'center' }}>
                            <a href={`/profile/${encodeURIComponent(f.username)}`} style={{ display: 'block', textDecoration: 'none' }}>
                                <habbo-imager>
                                    <img
                                        src={avatarUrl(f.look, { headOnly: false, size: 'm' })}
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
            </habbo-friend-list>
        </Card>
    );
}

function RoomsCard({ rooms, totalCount }: { rooms: Room[]; totalCount: number }): ReactNode
{
    return (
        <Card
            wrapperClass="rooms"
            title="Stanze"
            footer={totalCount > rooms.length ? <VediTutto href="#" /> : null}
        >
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

// ============================================================
// PHOTOS SECTION (full-width sotto le 4 cards)
// ============================================================

interface PhotoSummary
{
    id: number;
    timestamp: number;
    url: string;
}

function PhotosSection({ photos, username }: { photos: PhotoSummary[]; username: string }): ReactNode
{
    return (
        <section className="profile__foto" style={{ marginTop: 24 }}>
            <h2 style={{ color: '#fff', textTransform: 'uppercase', borderBottom: '2px solid rgba(255,255,255,.2)', paddingBottom: 8, marginBottom: 12 }}>
                Foto
            </h2>
            <ul style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(4, 1fr)',
                gap: 12,
                listStyle: 'none',
                padding: 0,
                margin: 0
            }}>
                {photos.map(p => (
                    <li key={p.id}>
                        <a href={`/profile/${encodeURIComponent(username)}/photo/${p.id}`} style={{ display: 'block', position: 'relative' }}>
                            <img
                                src={p.url}
                                alt=""
                                loading="lazy"
                                style={{ width: '100%', height: 'auto', borderRadius: 3, display: 'block' }}
                            />
                            <div style={{
                                position: 'absolute',
                                bottom: 0,
                                left: 0,
                                right: 0,
                                padding: '4px 8px',
                                background: 'linear-gradient(transparent, rgba(0,0,0,.7))',
                                color: '#fff',
                                fontSize: 12,
                                display: 'flex',
                                justifyContent: 'space-between'
                            }}>
                                <span>{formatPhotoDateShort(p.timestamp)}</span>
                            </div>
                        </a>
                    </li>
                ))}
            </ul>
            <div style={{ textAlign: 'right', marginTop: 8 }}>
                <a href="/community/photos" style={{ color: '#fbd33f', textTransform: 'uppercase', fontWeight: 'bold' }}>
                    Foto da Habbo &raquo;
                </a>
            </div>
        </section>
    );
}

function formatPhotoDateShort(ts: number): string
{
    const d = new Date(ts * 1000);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yy = String(d.getFullYear()).slice(-2);
    return `${dd}/${mm}/${yy}`;
}

function GroupsCard({ groups, totalCount }: { groups: Group[]; totalCount: number }): ReactNode
{
    return (
        <Card
            wrapperClass="groups"
            title="Gruppi"
            footer={totalCount > groups.length ? <VediTutto href="#" /> : null}
        >
            <habbo-group-list className="item-list--grid">
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
            </habbo-group-list>
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

interface ProfileResponse
{
    user: ProfileUser;
    rooms: Room[];
    friends: Friend[];
    groups: Group[];
    badges: Badge[];
    photos: PhotoSummary[];
    counts: {
        photos: number;
        rooms: number;
        friends: number;
        groups: number;
        badges: number;
        achievements: number;
    };
}
