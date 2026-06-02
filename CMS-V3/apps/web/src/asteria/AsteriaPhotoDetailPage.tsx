import { type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { avatarUrl } from '../hooks/useAuth';
import './asteria.css';

/** /profile/:user/photo/:id — dettaglio foto. GET /api/v2/community/photos/:id */
interface PhotoFull { id: number; userId: number; username: string; userLook: string; roomId: number; roomName: string; timestamp: number; url: string; likes: number; }
interface OtherPhoto { id: number; timestamp: number; url: string; }
interface PhotoResp { photo: PhotoFull; otherPhotos: OtherPhoto[]; }

export function AsteriaPhotoDetailPage(): ReactNode
{
    const params = useParams<{ user: string; id: string }>();
    const id = params.id ?? '';
    const { data, isLoading, error } = useQuery<PhotoResp | null>({
        queryKey: ['community', 'photo', id],
        queryFn: async ({ signal }) =>
        {
            const r = await fetch(`/api/v2/community/photos/${encodeURIComponent(id)}`, { credentials: 'include', signal });
            if(r.status === 404) return null;
            if(!r.ok) throw new Error('photo_' + r.status);
            return r.json() as Promise<PhotoResp>;
        },
        enabled: id.length > 0, retry: false
    });

    return (
        <AsteriaShell activeNav="community">
            {isLoading && <section className="asteria-article"><div className="asteria-news__state">Caricamento foto…</div></section>}
            {!isLoading && (error || data === null) && (
                <section className="asteria-article asteria-article--missing">
                    <div className="asteria-hero__eyebrow">Foto</div>
                    <h1 className="asteria-article__title">Foto non trovata</h1>
                    <div className="asteria-article__back"><Link to="/community/photos" className="asteria-btn asteria-btn--ghost">← Galleria</Link></div>
                </section>
            )}
            {data && (
                <section className="asteria-photo-detail">
                    <div className="asteria-photo-detail__stage">
                        <img src={data.photo.url} alt={`Foto di ${data.photo.username}`} />
                    </div>
                    <div className="asteria-photo-detail__meta">
                        <Link to={`/profile/${encodeURIComponent(data.photo.username)}`} className="asteria-photo-detail__author">
                            <img src={avatarUrl(data.photo.userLook, { size: 'm', headOnly: true })} alt="" />
                            <div>
                                <strong>{data.photo.username}</strong>
                                <span>{data.photo.roomName || 'Stanza sconosciuta'}</span>
                            </div>
                        </Link>
                        <span className="asteria-chip is-active">♥ {data.photo.likes}</span>
                    </div>
                    {data.otherPhotos.length > 0 && (
                        <section className="asteria-section">
                            <div className="asteria-section__head"><h2 className="asteria-section__title">Altri scatti di {data.photo.username}</h2></div>
                            <div className="asteria-photos__grid">
                                {data.otherPhotos.map(o => (
                                    <Link key={o.id} to={`/profile/${encodeURIComponent(data.photo.username)}/photo/${o.id}`} className="asteria-photo">
                                        <img loading="lazy" src={o.url} alt={`Foto ${o.id}`} />
                                    </Link>
                                ))}
                            </div>
                        </section>
                    )}
                </section>
            )}
        </AsteriaShell>
    );
}
