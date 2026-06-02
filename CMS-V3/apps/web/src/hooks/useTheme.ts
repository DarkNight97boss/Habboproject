import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { type ThemeId, applyTheme, fetchActiveTheme, readCachedTheme, setActiveTheme } from '../lib/theme';

/**
 * Hook per leggere il tema attivo (server-side, sync ~60s).
 *
 * Tutti i visitatori (anon e auth) vedono lo STESSO tema — quello scelto
 * dallo staff via `useSetTheme`. Polling 60s + refetch on focus per sync
 * near-real-time se lo staff cambia da un'altra device.
 *
 * Anti-FOUC: `placeholderData` usa l'ultimo valore cached in localStorage,
 * così il primo render ha già un tema verosimile senza aspettare la fetch.
 */
export function useTheme(): ThemeId
{
    const { data } = useQuery<ThemeId>({
        queryKey: ['site', 'theme'],
        queryFn: ({ signal }) => fetchActiveTheme(signal),
        placeholderData: readCachedTheme(),
        staleTime: 30_000,
        refetchInterval: 60_000,
        refetchOnWindowFocus: true,
        retry: 1
    });
    // Side-effect: ogni cambio di data → applica al DOM. fetchActiveTheme già
    // lo fa al primo fetch, ma se la cache invalida e arriva un nuovo valore
    // dal polling, dobbiamo riallineare il <html data-theme>.
    useEffect(() =>
    {
        if(data) applyTheme(data);
    }, [data]);
    return data ?? readCachedTheme();
}

/**
 * Hook mutation per cambiare il tema (staff only — backend enforce rank >= 5).
 * Invalida la query 'site/theme' on success per propagate ad altri componenti
 * (e altre tab via React Query default refetchOnWindowFocus).
 */
export function useSetTheme()
{
    const qc = useQueryClient();
    return useMutation<void, Error, ThemeId>({
        mutationFn: setActiveTheme,
        onSuccess: (_, themeId) =>
        {
            // Aggiorna subito la cache senza aspettare il refetch (UI snappy).
            qc.setQueryData(['site', 'theme'], themeId);
        }
    });
}
