import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell, CommunityTabs } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';

/**
 * Pagina /community/photos — replica DOM ufficiale habbo.it/community/photos.
 *
 * Struttura ispezionata live (Chrome extension):
 *   <div.content>
 *     <habbo-header-small>… (user-menu + GIOCA)
 *     <habbo-tabs> sub-nav: Foto* | Stanze | Fansite | Notizie
 *     <main>
 *       <header.photos__header>
 *         <div.photos__header__container.wrapper>
 *           <div.photos__header__image__wrapper>
 *             <div.photos__header__image>  ← bg: teaser_stories_channels (243px)
 *           <div.photos__header__content>
 *             <h1.photos__header__title>Foto da Habbo</h1>
 *             <p>Dai uno sguardo ad alcuni dei grandi Habbo momenti…</p>
 *       <section.wrapper.wrapper--content>
 *         <habbo-columns-channel>
 *           <div.columns>
 *             {photos.map(p =>
 *               <habbo-card.columns__column>
 *                 <div.card>
 *                   <div.card__content>
 *                     <a.card__link href="/profile/<user>/photo/<id>">
 *                       <div.card__image__aligner>
 *                         <img.card__image.card__image--photo src={p.url}/>
 *                     <div.card__meta>
 *                       <time.card__date>{formatDate(p.timestamp)}</time>
 *                       <habbo-like.card__like>… Mi piace + count
 *                   <div>
 *                     <habbo-avatar.card__creator>
 *                       <a.avatar href="/profile/<user>">
 *                         <habbo-imager.avatar__image><img headOnly/>
 *                         <h6.avatar__title>{p.username}</h6>
 *
 * Backend: GET /api/v2/community/photos → { photos: [...], limit, offset }
 *   sourced da tabella `camera_web` (Arcturus standard), JOIN users.
 */
export function CommunityPhotosPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();

    const photosQuery = useQuery({
        queryKey: ['community', 'photos'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/community/photos?limit=48');
            if(!r.ok) throw new Error('photos_failed');
            return r.json() as Promise<{ photos: Photo[] }>;
        },
        staleTime: 30_000
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user} tabs={<CommunityTabs active="foto" />}>
            <main>
                <PhotosHeader />
                <section className="wrapper wrapper--content">
                    <habbo-columns-channel>
                        {photosQuery.isLoading
                            ? <div className="columns"><EmptyState message="Caricamento…" /></div>
                            : photosQuery.error
                            ? <div className="columns"><EmptyState message="Errore nel caricamento delle foto." /></div>
                            : (photosQuery.data?.photos.length ?? 0) === 0
                            ? <div className="columns"><EmptyState message="Nessuna foto ancora scattata. Apri il client e usa la fotocamera per immortalare il tuo momento!" /></div>
                            : (
                                <div className="columns">
                                    {photosQuery.data!.photos.map(p => <PhotoCard key={p.id} photo={p} />)}
                                </div>
                            )}
                    </habbo-columns-channel>
                </section>
            </main>
        </AuthedShell>
    );
}

function PhotosHeader(): ReactNode
{
    return (
        <header className="photos__header">
            <div className="photos__header__container wrapper">
                <div className="photos__header__image__wrapper">
                    {/* bg-image = teaser_stories_channels.36d165fe.png (già mirrorato in
                        /assets/habbo/assets/images/) applicato dalla CSS ufficiale */}
                    <div className="photos__header__image" />
                </div>
                <div className="photos__header__content">
                    <h1 className="photos__header__title">Foto da Habbo</h1>
                    <p>Dai uno sguardo ad alcuni dei grandi Habbo momenti immortalati in giro per l'Hotel.</p>
                </div>
            </div>
        </header>
    );
}

interface Photo
{
    id: number;
    userId: number;
    username: string;
    userLook: string;
    roomId: number;
    timestamp: number;
    url: string;
    likes: number;
}

function PhotoCard({ photo }: { photo: Photo }): ReactNode
{
    const date = formatPhotoDate(photo.timestamp);
    const photoHref = `/profile/${encodeURIComponent(photo.username)}/photo/${photo.id}?pool=photos`;
    const profileHref = `/profile/${encodeURIComponent(photo.username)}`;
    return (
        <habbo-card className="columns__column">
            <div className="card">
                <div className="card__content">
                    <a className="card__link" href={photoHref}>
                        <div className="card__image__aligner">
                            <img className="card__image card__image--photo" src={photo.url} alt="" loading="lazy" />
                        </div>
                    </a>
                    <div className="card__meta">
                        <time className="card__date">{date}</time>
                        <habbo-like className="card__like">
                            <div className="like__action">
                                <a><span>Mi piace</span><span className="like__count">{photo.likes}</span></a>
                            </div>
                            <div className="like__thumb">
                                <i className="like__icon icon icon--like" />
                            </div>
                        </habbo-like>
                    </div>
                </div>
                <div>
                    <habbo-avatar className="card__creator">
                        <a className="avatar" href={profileHref}>
                            <habbo-imager className="avatar__image">
                                <img
                                    className="imager"
                                    src={avatarUrl(photo.userLook, { headOnly: true, size: 's' })}
                                    alt={photo.username}
                                />
                            </habbo-imager>
                            <h6 className="avatar__title">{photo.username}</h6>
                        </a>
                    </habbo-avatar>
                </div>
            </div>
        </habbo-card>
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

/** Format timestamp (sec) → "dd/MM/YY" come l'ufficiale (es. "31/05/26"). */
function formatPhotoDate(ts: number): string
{
    const d = new Date(ts * 1000);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yy = String(d.getFullYear()).slice(-2);
    return `${dd}/${mm}/${yy}`;
}
