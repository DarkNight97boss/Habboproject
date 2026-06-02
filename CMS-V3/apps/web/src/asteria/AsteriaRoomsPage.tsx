import { type CSSProperties, type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { AsteriaShell } from './AsteriaShell';
import { avatarUrl } from '../hooks/useAuth';
import './asteria.css';

/** /community/rooms — stanze popolari pubbliche. GET /api/v2/community/rooms?limit=60 */
interface Room {
    id: number; ownerId: number; ownerName: string; ownerLook: string;
    name: string; description: string; model: string; users: number; maxUsers: number;
    state: 'open' | 'locked' | 'password' | 'invisible'; category: number; score: number; staffPicked: boolean;
}

const GRADS = ['linear-gradient(135deg,#a855f7,#06b6d4)', 'linear-gradient(135deg,#ec4899,#a855f7)', 'linear-gradient(135deg,#06b6d4,#10b981)', 'linear-gradient(135deg,#f59e0b,#ec4899)', 'linear-gradient(135deg,#6366f1,#a855f7)'];
function grad(id: number): CSSProperties { return { background: GRADS[id % GRADS.length] as string }; }

export function AsteriaRoomsPage(): ReactNode
{
    const { data, isLoading, error } = useQuery<Room[]>({
        queryKey: ['community', 'rooms'],
        queryFn: async ({ signal }) =>
        {
            const r = await fetch('/api/v2/community/rooms?limit=60', { credentials: 'include', signal });
            if(!r.ok) throw new Error('rooms_' + r.status);
            return (await r.json() as { rooms: Room[] }).rooms;
        },
        staleTime: 30_000
    });
    const rooms = data ?? [];
    return (
        <AsteriaShell activeNav="community">
            <section className="asteria-news">
                <header className="asteria-news__head">
                    <div className="asteria-hero__eyebrow">Community · Stanze</div>
                    <h1 className="asteria-news__h1">Stanze popolari</h1>
                    <p className="asteria-news__sub">Le stanze più affollate ora. Entra nel client per visitarle live.</p>
                </header>
                {isLoading && <div className="asteria-news__state">Caricamento stanze…</div>}
                {error && <div className="asteria-news__state asteria-news__state--err">Errore nel caricamento.</div>}
                {!isLoading && !error && rooms.length === 0 && <div className="asteria-news__state">Nessuna stanza attiva al momento.</div>}
                <div className="asteria-rooms__grid">
                    {rooms.map(r => (
                        <div key={r.id} className="asteria-room">
                            <div className="asteria-room__thumb" style={grad(r.id)}>
                                {r.staffPicked && <span className="asteria-room__pick">★ Staff pick</span>}
                                <span className="asteria-room__count">{r.users}/{r.maxUsers}</span>
                                {(r.state === 'locked' || r.state === 'password') && <span className="asteria-room__lock">🔒</span>}
                            </div>
                            <div className="asteria-room__body">
                                <h3 className="asteria-room__name">{r.name}</h3>
                                <p className="asteria-room__desc">{r.description || '—'}</p>
                                <div className="asteria-room__owner">
                                    <img loading="lazy" src={avatarUrl(r.ownerLook, { size: 's', headOnly: true })} alt="" />
                                    <span>{r.ownerName}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
                <div style={{ textAlign: 'center', marginTop: 32 }}>
                    <a href="/api/v2/auth/play" className="asteria-btn asteria-btn--xl">▶ Entra e visita le stanze</a>
                </div>
            </section>
        </AsteriaShell>
    );
}
