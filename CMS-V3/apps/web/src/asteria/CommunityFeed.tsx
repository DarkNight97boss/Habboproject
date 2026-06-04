import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';

/**
 * Feed di community PUBBLICO (#11), riusabile. Mostra gli ultimi eventi
 * dall'event-bus (registrazioni, stanze, gruppi, foto, streak, acquisti).
 * Non renderizza nulla se il flag `activity_feed` è OFF o se il feed è vuoto,
 * così non lascia sezioni vuote in pagina. Usato sulla homepage (visibile a
 * tutti) e riutilizzabile altrove.
 */

interface FeedEvent
{
    id: number;
    actor_name: string;
    type: string;
    payload: unknown;
    created_at: string;
}

function renderEvent(e: FeedEvent): { icon: string; text: ReactNode }
{
    const who = e.actor_name || 'Qualcuno';
    const p = (e.payload && typeof e.payload === 'object') ? e.payload as Record<string, unknown> : {};
    switch(e.type)
    {
        case 'user.registered': return { icon: '🎉', text: <><strong>{who}</strong> si è unito ad Asteria</> };
        case 'streak.claimed':  return { icon: '🔥', text: <><strong>{who}</strong> ha riscattato uno streak di {String(p.streak ?? '?')} giorni</> };
        case 'shop.purchase':   return { icon: '🛒', text: <><strong>{who}</strong> ha acquistato {String(p.item ?? 'un articolo')}</> };
        case 'room.created':    return { icon: '🏠', text: <><strong>{who}</strong> ha creato la stanza {p.room ? <em>«{String(p.room)}»</em> : 'una stanza'}</> };
        case 'guild.created':   return { icon: '🛡️', text: <><strong>{who}</strong> ha fondato il gruppo {p.guild ? <em>«{String(p.guild)}»</em> : ''}</> };
        case 'photo.posted':    return { icon: '📸', text: <><strong>{who}</strong> ha pubblicato una foto</> };
        case 'achievement.unlocked': return { icon: '🏆', text: <><strong>{who}</strong> ha sbloccato un obiettivo</> };
        default:                return { icon: '✨', text: <><strong>{who}</strong> · {e.type}</> };
    }
}

function timeAgo(iso: string): string
{
    const t = new Date(iso).getTime();
    if(!t) return '';
    const s = Math.floor((Date.now() - t) / 1000);
    if(s < 60) return 'ora';
    if(s < 3600) return `${Math.floor(s / 60)}m fa`;
    if(s < 86400) return `${Math.floor(s / 3600)}h fa`;
    return `${Math.floor(s / 86400)}g fa`;
}

export function CommunityFeed({ limit = 10 }: { limit?: number }): ReactNode
{
    const { data, isLoading } = useQuery<{ enabled: boolean; events: FeedEvent[] }>({
        queryKey: ['community', 'activity', limit],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/activity/feed?limit=${limit}`, { credentials: 'include' });
            if(!r.ok) throw new Error('feed_failed');
            return r.json();
        },
        staleTime: 20_000
    });

    if(isLoading) return null;
    if(!data?.enabled) return null;
    const events = data.events || [];
    if(!events.length) return null;

    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">Attività di community</h2>
                <span className="asteria-section__subtitle">Cosa succede ora ad Asteria</span>
            </div>
            <div className="asteria-feed">
                {events.map(e =>
                {
                    const { icon, text } = renderEvent(e);
                    return (
                        <div key={e.id} className="asteria-feed__item">
                            <span className="asteria-feed__icon">{icon}</span>
                            <span className="asteria-feed__text">{text}</span>
                            <span className="asteria-feed__time">{timeAgo(e.created_at)}</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
