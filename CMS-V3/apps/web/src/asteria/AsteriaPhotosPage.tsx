import { type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { avatarUrl } from '../hooks/useAuth';
import './asteria.css';

/** /community/photos — galleria foto pubblica. GET /api/v2/community/photos?limit=48 */
interface Photo { id: number; userId: number; username: string; userLook: string; roomId: number; timestamp: number; url: string; likes: number; }

export function AsteriaPhotosPage(): ReactNode
{
    const { data, isLoading, error } = useQuery<Photo[]>({
        queryKey: ['community', 'photos'],
        queryFn: async ({ signal }) =>
        {
            const r = await fetch('/api/v2/community/photos?limit=48', { credentials: 'include', signal });
            if(!r.ok) throw new Error('photos_' + r.status);
            return (await r.json() as { photos: Photo[] }).photos;
        },
        staleTime: 60_000
    });
    const photos = data ?? [];
    return (
        <AsteriaShell activeNav="community">
            <section className="asteria-news">
                <header className="asteria-news__head">
                    <div className="asteria-hero__eyebrow">Community · Foto</div>
                    <h1 className="asteria-news__h1">Scatti dalla community</h1>
                    <p className="asteria-news__sub">Le ultime foto scattate nelle stanze di Asteria.</p>
                </header>
                {isLoading && <div className="asteria-news__state">Caricamento foto…</div>}
                {error && <div className="asteria-news__state asteria-news__state--err">Errore nel caricamento.</div>}
                {!isLoading && !error && photos.length === 0 && <div className="asteria-news__state">Ancora nessuna foto. Scatta la prima in gioco con la fotocamera! 📸</div>}
                <div className="asteria-photos__grid">
                    {photos.map(p => (
                        <Link key={p.id} to={`/profile/${encodeURIComponent(p.username)}/photo/${p.id}`} className="asteria-photo asteria-photo--card">
                            <img loading="lazy" src={p.url} alt={`Foto di ${p.username}`} className="asteria-photo__img" />
                            <div className="asteria-photo__meta">
                                <img className="asteria-photo__avatar" src={avatarUrl(p.userLook, { size: 's', headOnly: true })} alt="" loading="lazy" />
                                <span className="asteria-photo__user">{p.username}</span>
                                <span className="asteria-photo__likes">♥ {p.likes}</span>
                            </div>
                        </Link>
                    ))}
                </div>
            </section>
        </AsteriaShell>
    );
}
