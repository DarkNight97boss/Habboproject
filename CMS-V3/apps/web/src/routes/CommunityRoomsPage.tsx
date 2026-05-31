import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell, CommunityTabs } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';

/**
 * Pagina /community/rooms — Galleria Stanze.
 *
 * Struttura DOM ufficiale (ispezione live):
 *   <main>
 *     <header.rooms__header>
 *       <div.rooms__header__container.wrapper>
 *         <div.rooms__header__image__wrapper>
 *           <div.rooms__header__image/>  ← bg illustrazione (CSS ufficiale)
 *         <div.rooms__header__content>
 *           <h1.rooms__header__title>Galleria Stanze</h1>
 *           <p>Scopri alcune delle Stanze più popolari di Habbo adesso!</p>
 *     <section.wrapper.wrapper--content.rooms-wrapper>
 *       {rooms.map → <div.room-item>
 *         <a.room-item__link href=/client?room={id}>
 *           <div.room-item__thumbnail/>  ← bg model isometric
 *         <a.room-item__link>
 *           <h2.room-item__title>{room.name}</h2>
 *         <p.room-item__description>{room.description}</p>
 *         <div>
 *           <habbo-avatar.room-item__owner--user>
 *             <a.avatar href=/profile/{owner}>
 *               <habbo-imager.avatar__image><img headOnly/>
 *               <h6.avatar__title>{owner}
 *
 * Backend: GET /api/v2/community/rooms?limit=N → { rooms: [...] }
 *   source `rooms` table ORDER BY users DESC (più popolari per primi).
 */
export function CommunityRoomsPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const roomsQuery = useQuery({
        queryKey: ['community', 'rooms'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/community/rooms?limit=60');
            if(!r.ok) throw new Error('rooms_failed');
            return r.json() as Promise<{ rooms: Room[] }>;
        },
        staleTime: 30_000
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user} tabs={<CommunityTabs active="stanze" />}>
            <main>
                <header className="rooms__header">
                    <div className="rooms__header__container wrapper">
                        <div className="rooms__header__image__wrapper">
                            <div className="rooms__header__image" />
                        </div>
                        <div className="rooms__header__content">
                            <h1 className="rooms__header__title">Galleria Stanze</h1>
                            <p>Scopri alcune delle Stanze più popolari di Habbo adesso!</p>
                        </div>
                    </div>
                </header>
                <section className="wrapper wrapper--content rooms-wrapper">
                    {roomsQuery.isLoading
                        ? <EmptyState message="Caricamento stanze…" />
                        : roomsQuery.error
                        ? <EmptyState message="Errore nel caricamento delle stanze." />
                        : (roomsQuery.data?.rooms.length ?? 0) === 0
                        ? <EmptyState message="Nessuna stanza pubblica al momento. Apri il client e creane una!" />
                        : roomsQuery.data!.rooms.map(r => <RoomItem key={r.id} room={r} />)}
                </section>
            </main>
        </AuthedShell>
    );
}

interface Room
{
    id: number;
    ownerId: number;
    ownerName: string;
    ownerLook: string;
    name: string;
    description: string;
    model: string;
    users: number;
    maxUsers: number;
    state: 'open' | 'locked' | 'password' | 'invisible';
    category: number;
    score: number;
    staffPicked: boolean;
}

function RoomItem({ room }: { room: Room }): ReactNode
{
    const enterHref = `/gioca?room=${room.id}`;
    const profileHref = `/profile/${encodeURIComponent(room.ownerName)}`;
    return (
        <div className="room-item">
            <a className="room-item__link" href={enterHref}>
                {/* room thumbnail: bg-image generato dal model isometric.
                    Per ora la CSS ufficiale fallisce a renderizzarlo (il
                    pattern bg dipende dal model dei nostri tile) — lascio
                    <div .room-item__thumbnail> vuoto, applica fallback bg */}
                <div className="room-item__thumbnail" data-model={room.model} />
            </a>
            <a className="room-item__link" href={enterHref}>
                <h2 className="room-item__title">{room.name}</h2>
            </a>
            <p className="room-item__description">{room.description}</p>
            <div>
                <habbo-avatar className="room-item__owner--user">
                    <a className="avatar" href={profileHref}>
                        <habbo-imager className="avatar__image">
                            <img
                                className="imager"
                                src={avatarUrl(room.ownerLook || 'hd-180-1.ch-210-66.lg-270-82.sh-290-80', { headOnly: true, size: 'b' })}
                                alt={room.ownerName}
                            />
                        </habbo-imager>
                        <h6 className="avatar__title">{room.ownerName}</h6>
                    </a>
                </habbo-avatar>
            </div>
        </div>
    );
}

function EmptyState({ message }: { message: string }): ReactNode
{
    return (
        <habbo-empty-results>
            <span>{message}</span>
        </habbo-empty-results>
    );
}
