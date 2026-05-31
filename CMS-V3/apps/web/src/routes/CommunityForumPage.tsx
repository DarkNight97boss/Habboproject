import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell, CommunityTabs } from '../components/AuthedShell';
import { avatarUrl, useAuth } from '../hooks/useAuth';

/**
 * Pagina /community/forum — discussion board interno Habboproject.
 *
 * NB: l'ufficiale habbo.it ha /community/fansite ma è 404. Habboproject
 * sostituisce con un forum interno (group forums Arcturus o tabella
 * custom). Per ora stub MVP con header + empty-state.
 *
 * Struttura mirrorata da /community/rooms (stessa "ossatura"
 * header+content), classi rinominate forum__*.
 */
export function CommunityForumPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const threadsQuery = useQuery({
        queryKey: ['community', 'forum', 'threads'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/community/forum/threads');
            if(!r.ok) throw new Error('forum_failed');
            return r.json() as Promise<{ threads: Thread[]; total: number }>;
        },
        staleTime: 30_000
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user} tabs={<CommunityTabs active="forum" />}>
            <main>
                <header className="rooms__header">
                    <div className="rooms__header__container wrapper">
                        <div className="rooms__header__image__wrapper">
                            <div className="rooms__header__image" />
                        </div>
                        <div className="rooms__header__content">
                            <h1 className="rooms__header__title">Forum</h1>
                            <p>Discussioni della community Habboproject: idee, proposte, eventi.</p>
                        </div>
                    </div>
                </header>
                <section className="wrapper wrapper--content rooms-wrapper">
                    {threadsQuery.isLoading
                        ? <EmptyState message="Caricamento discussioni…" />
                        : threadsQuery.error
                        ? <EmptyState message="Errore nel caricamento del forum." />
                        : (threadsQuery.data?.threads.length ?? 0) === 0
                        ? <EmptyState message="Nessuna discussione ancora. Avvia tu la prima — apri il client, entra in un gruppo e crea un thread!" />
                        : threadsQuery.data!.threads.map(t => <ThreadItem key={t.id} thread={t} />)}
                </section>
            </main>
        </AuthedShell>
    );
}

interface Thread
{
    id: number;
    title: string;
    body: string;
    authorId: number;
    authorName: string;
    authorLook: string;
    repliesCount: number;
    createdAt: number;
    lastReplyAt: number;
}

function ThreadItem({ thread }: { thread: Thread }): ReactNode
{
    return (
        <div className="room-item">
            <a className="room-item__link" href={`/community/forum/thread/${thread.id}`}>
                <h2 className="room-item__title">{thread.title}</h2>
            </a>
            <p className="room-item__description">{thread.body.substring(0, 200)}{thread.body.length > 200 ? '…' : ''}</p>
            <div>
                <habbo-avatar className="room-item__owner--user">
                    <a className="avatar" href={`/profile/${encodeURIComponent(thread.authorName)}`}>
                        <habbo-imager className="avatar__image">
                            <img
                                className="imager"
                                src={avatarUrl(thread.authorLook, { headOnly: true, size: 'b' })}
                                alt={thread.authorName}
                            />
                        </habbo-imager>
                        <h6 className="avatar__title">{thread.authorName} · {thread.repliesCount} risposte</h6>
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
