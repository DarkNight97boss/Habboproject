import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';
import { NotFoundPage } from './NotFoundPage';

/**
 * Pagina /profile/:username — info pubbliche di un utente.
 *
 * Struttura DOM mirrorata da habbo.it/profile/<user>:
 *   <main.wrapper--content>
 *     <div.profile__section>
 *       <div.profile__card__wrapper--rooms>
 *         <section.profile__card__aligner>
 *           <div.profile__card>
 *             <h2.profile__card__title>Stanze
 *             <habbo-room-list.item-list--grid>
 *               <ul><li.item.item--room><a.item__content>…
 *             <div.profile__card__footer>
 *               <habbo-profile-modal><a.profile-modal__link>Vedi tutto
 *
 * Mostriamo solo la card "Stanze" per ora (MVP). In futuro:
 * Gruppi, Amici, Achievement, Cronologia.
 *
 * Backend: GET /api/v2/profile/:username
 *   → { user, rooms, counts }
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
            return r.json() as Promise<{
                user: { id: number; username: string; motto: string; look: string; rank: number; accountCreated: number; online: boolean };
                rooms: { id: number; name: string; description: string; users: number; maxUsers: number; model: string }[];
                counts: { photos: number; rooms: number; friends: number };
            }>;
        },
        enabled: !!username,
        retry: false
    });

    if(authLoading || profileQuery.isLoading) return null;
    if(profileQuery.data === null) return <NotFoundPage />;
    if(!profileQuery.data) return null;

    const { user, rooms, counts } = profileQuery.data;

    const body = (
        <main className="wrapper wrapper--content">
            <ProfileHeader user={user} counts={counts} />
            <div className="profile__section">
                <div className="profile__card__wrapper--rooms">
                    <section className="profile__card__aligner">
                        <div className="profile__card">
                            <h2 className="profile__card__title">Stanze</h2>
                            {rooms.length === 0
                                ? <habbo-empty-results><span>Nessuna stanza pubblica.</span></habbo-empty-results>
                                : (
                                    <habbo-room-list className="item-list--grid">
                                        <ul>
                                            {rooms.slice(0, 6).map(r => (
                                                <li key={r.id} className="item item--room">
                                                    <a className="item__content" href={`/gioca?room=${r.id}`}>
                                                        <div className="item__title">{r.name}</div>
                                                        <div className="item__description">{r.description}</div>
                                                    </a>
                                                </li>
                                            ))}
                                        </ul>
                                    </habbo-room-list>
                                )}
                            {rooms.length > 6 && (
                                <div className="profile__card__footer">
                                    <a className="profile-modal__link" href="#">Vedi tutto</a>
                                </div>
                            )}
                        </div>
                    </section>
                </div>
            </div>
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

function ProfileHeader({ user, counts }: {
    user: { username: string; motto: string; look: string; online: boolean; accountCreated: number };
    counts: { photos: number; rooms: number; friends: number };
}): ReactNode
{
    const regDate = user.accountCreated > 0
        ? new Date(user.accountCreated * 1000).toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' })
        : '—';
    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: 16,
            padding: '12px 16px',
            background: 'rgba(0,0,0,0.3)',
            borderRadius: 3,
            marginBottom: 16,
            color: '#fff'
        }}>
            <img
                src={avatarUrl(user.look, { headOnly: false, size: 'l' })}
                alt={user.username}
                style={{ height: 110 }}
            />
            <div style={{ flex: 1 }}>
                <h1 style={{ margin: 0, fontSize: 28, textTransform: 'uppercase' }}>{user.username}</h1>
                <p style={{ margin: '4px 0', fontStyle: 'italic', opacity: 0.85 }}>{user.motto || '—'}</p>
                <p style={{ margin: '4px 0', fontSize: 13, opacity: 0.7 }}>
                    {user.online ? '🟢 Online' : '⚪ Offline'} · Iscritto il {regDate}
                </p>
                <p style={{ margin: '6px 0 0', fontSize: 14 }}>
                    <strong>{counts.rooms}</strong> stanze · <strong>{counts.photos}</strong> foto · <strong>{counts.friends}</strong> amici
                </p>
            </div>
        </div>
    );
}
