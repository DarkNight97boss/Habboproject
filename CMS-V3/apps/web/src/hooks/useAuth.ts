import { useQuery } from '@tanstack/react-query';

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

/**
 * Hook auth: chiama GET /api/v2/me con i cookie httpOnly già emessi
 * dal login/register. Restituisce null se non autenticato (401).
 * Cache 30s, no refetch on focus.
 */
export function useAuth()
{
    return useQuery<AuthUser | null>({
        queryKey: ['auth', 'me'],
        queryFn: async () =>
        {
            const r = await fetch('/api/v2/me', { credentials: 'include' });
            if(r.status === 401) return null;
            if(!r.ok) throw new Error('me_failed_' + r.status);
            return r.json() as Promise<AuthUser>;
        },
        staleTime: 30_000,
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
