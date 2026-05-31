import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';
import { NotFoundPage } from './NotFoundPage';

/**
 * Pagina /profile/:user/photo/:id — singola foto fullsize.
 *
 * Card grande con: img full, creator avatar+name, room name, date,
 * like button + count.
 *
 * Backend: GET /api/v2/community/photos/:id
 *   → { photo: { id, userId, username, userLook, roomId, roomName,
 *                timestamp, url, likes } }
 *   404 se non esiste.
 */
export function PhotoDetailPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const { id, user: pathUser } = useParams<{ id: string; user: string }>();

    const photoQuery = useQuery({
        queryKey: ['community', 'photo', id],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/community/photos/${encodeURIComponent(id ?? '')}`);
            if(r.status === 404) return null;
            if(!r.ok) throw new Error('photo_failed');
            return (await r.json() as { photo: Photo }).photo;
        },
        enabled: !!id,
        retry: false
    });

    if(authLoading || photoQuery.isLoading) return null;
    if(photoQuery.data === null) return <NotFoundPage />;
    if(!photoQuery.data) return null;

    const photo = photoQuery.data;
    const date = formatPhotoDate(photo.timestamp);
    const profileHref = `/profile/${encodeURIComponent(photo.username)}`;

    const body = (
        <main className="wrapper wrapper--content">
            <section>
                <habbo-compile className="main main--fixed">
                    <article style={{ maxWidth: 600, margin: '0 auto' }}>
                        <header className="news-header news-header--single">
                            <h1 className="news-header__wrapper news-header__title">
                                Foto di {photo.username}
                            </h1>
                            <aside className="news-header__wrapper news-header__info">
                                <time className="news-header__date">{date}</time>
                                {photo.roomName && (
                                    <ul className="news-header__categories">
                                        <li className="news-header__category">
                                            <a className="news-header__category__link" href={`/gioca?room=${photo.roomId}`}>
                                                📍 {photo.roomName}
                                            </a>
                                        </li>
                                    </ul>
                                )}
                            </aside>
                        </header>
                        <div style={{
                            background: '#0c3a65',
                            padding: 16,
                            borderRadius: 5,
                            textAlign: 'center',
                            marginBottom: 12
                        }}>
                            <img src={photo.url} alt="" style={{ maxWidth: '100%', height: 'auto' }} />
                        </div>
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '12px 16px',
                            background: 'rgba(0,0,0,.25)',
                            borderRadius: 3
                        }}>
                            <habbo-avatar className="card__creator">
                                <a className="avatar" href={profileHref} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                    <habbo-imager className="avatar__image">
                                        <img
                                            className="imager"
                                            src={avatarUrl(photo.userLook, { headOnly: true, size: 'b' })}
                                            alt={photo.username}
                                        />
                                    </habbo-imager>
                                    <h6 className="avatar__title" style={{ margin: 0 }}>{photo.username}</h6>
                                </a>
                            </habbo-avatar>
                            <habbo-like className="card__like">
                                <div className="like__action">
                                    <a><span>Mi piace</span><span className="like__count" style={{ marginLeft: 6 }}>{photo.likes}</span></a>
                                </div>
                            </habbo-like>
                        </div>
                        <div className="news-footer" style={{ marginTop: 16 }}>
                            <a href={pathUser ? `/profile/${encodeURIComponent(pathUser)}` : '/community/photos'}>
                                ← Torna a tutte le foto
                            </a>
                        </div>
                    </article>
                </habbo-compile>
            </section>
        </main>
    );

    if(user) return <AuthedShell user={user}>{body}</AuthedShell>;
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

interface Photo
{
    id: number;
    userId: number;
    username: string;
    userLook: string;
    roomId: number;
    roomName: string;
    timestamp: number;
    url: string;
    likes: number;
}

function formatPhotoDate(ts: number): string
{
    const d = new Date(ts * 1000);
    return d.toLocaleDateString('it-IT', { day: '2-digit', month: 'short', year: 'numeric' });
}
