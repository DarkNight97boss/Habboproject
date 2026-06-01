import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';

export interface AuthUser
{
    id: number;
    username: string;
    mail: string;
    motto: string;
    look: string;
    gender: string;
    credits: number;
    rank: number;
    last_online: number;
    account_created: number;
}

export interface Notifications
{
    unreadMessages: number;
    friendRequests: number;
    total: number;
}

/**
 * BroadcastChannel cross-tab per sync di stato auth.
 * Pattern: ogni tab CMS aperta ascolta `cms-v3-auth` e invalida
 * la cache useAuth quando un'altra tab emette login/logout.
 *
 * Esempio: utente fa logout in tab A → tab B vede `{type:'logout'}` e
 * il suo useAuth() si invalida → re-fetch /me → 401 → user=null →
 * HomePage rerenderizza in stato anonimo.
 */
const AUTH_CHANNEL = typeof BroadcastChannel !== 'undefined'
    ? new BroadcastChannel('cms-v3-auth')
    : null;

export function broadcastAuth(type: 'login' | 'logout'): void
{
    AUTH_CHANNEL?.postMessage({ type, ts: Date.now() });
}

/**
 * Hook auth: chiama GET /api/v2/me con i cookie httpOnly già emessi
 * dal login/register. Restituisce null se non autenticato (401).
 *
 * Polling 15s + refetchOnWindowFocus per sync near-real-time dei
 * crediti/look/motto (utili nel user-menu mentre si gioca su Nitro).
 *
 * BroadcastChannel cross-tab: se un'altra tab CMS fa login/logout,
 * invalida automaticamente la cache.
 */
export function useAuth()
{
    const qc = useQueryClient();

    useEffect(() =>
    {
        if(!AUTH_CHANNEL) return;
        const onMsg = (ev: MessageEvent): void =>
        {
            // Whatever happens nelle altre tab (login/logout), invalida
            // la cache così questa tab rilegge lo stato fresh.
            if(ev.data?.type === 'login' || ev.data?.type === 'logout')
            {
                void qc.invalidateQueries({ queryKey: ['auth', 'me'] });
                void qc.invalidateQueries({ queryKey: ['me', 'notifications'] });
            }
        };
        AUTH_CHANNEL.addEventListener('message', onMsg);
        return () => AUTH_CHANNEL.removeEventListener('message', onMsg);
    }, [qc]);

    return useQuery<AuthUser | null>({
        queryKey: ['auth', 'me'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/me', { credentials: 'include', cache: 'no-store' });
            if(r.status === 401) return null;
            if(!r.ok) throw new Error('me_failed_' + r.status);
            return r.json() as Promise<AuthUser>;
        },
        staleTime: 15_000,
        refetchInterval: 30_000,        // refresh crediti/look ogni 30s
        refetchOnWindowFocus: true,     // appena torni sulla tab, refresh
        retry: false
    });
}

/**
 * Hook notifications: poll /api/v2/me/notifications ogni 15s mentre la
 * tab è attiva. Restituisce null se non autenticato (skip fetch).
 *
 * Total = unreadMessages + friendRequests → numero da mostrare come
 * badge red-dot sul user-menu.
 */
export function useNotifications(enabled: boolean)
{
    return useQuery<Notifications | null>({
        queryKey: ['me', 'notifications'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/me/notifications', { credentials: 'include', cache: 'no-store' });
            if(r.status === 401) return null;
            if(!r.ok) throw new Error('notif_failed_' + r.status);
            return r.json() as Promise<Notifications>;
        },
        enabled,
        staleTime: 10_000,
        refetchInterval: 15_000,        // polling 15s (cheap query)
        refetchOnWindowFocus: true,
        retry: false
    });
}

/**
 * URL avatar da figure string — usa l'imager ufficiale habbo.it come fallback.
 *
 * Param `size`:
 *  - 's' = small (25x25 head, 35x55 body)
 *  - 'm' = medium (default, ~32x32 head, 50x110 body)
 *  - 'l' = large (~50x50 head, 64x110 body)
 *  - 'b' = big (54x62 head — usato in room-item creator/avatar dal CSS ufficiale habbo.it)
 */
export function avatarUrl(figure: string, opts: { size?: 's' | 'm' | 'l' | 'b'; headOnly?: boolean } = {}): string
{
    const params = new URLSearchParams({ figure });
    if(opts.size) params.set('size', opts.size);
    if(opts.headOnly) params.set('headonly', '1');
    return `https://www.habbo.it/habbo-imaging/avatarimage?${params.toString()}`;
}
