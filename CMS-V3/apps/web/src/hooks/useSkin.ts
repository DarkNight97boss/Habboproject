import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { type SkinManifest, applySkin, fetchActiveSkin, fetchAvailableSkins, readCachedSkin, setActiveSkin } from '../lib/skin';

/**
 * Hook headless: fetcha lo skin attivo dal server e lo applica al DOM.
 *
 * Polling 60s + refetch on focus per propagation cross-tab e cross-user
 * quando lo staff cambia skin.
 *
 * Anti-FOUC: usa readCachedSkin() come placeholder → primo render ha già
 * uno skin "verosimile" applicato (vedi hydrateSkin() in main.tsx).
 */
export function useSkin(): SkinManifest | null
{
    const { data } = useQuery<SkinManifest>({
        queryKey: ['site', 'skin'],
        queryFn: ({ signal }) => fetchActiveSkin(signal),
        placeholderData: readCachedSkin() ?? undefined,
        staleTime: 30_000,
        refetchInterval: 60_000,
        refetchOnWindowFocus: true,
        retry: 1
    });

    // Side-effect: ogni cambio di manifest → riapplica al DOM.
    // fetchActiveSkin/setActiveSkin lo fanno già al success, questo cattura
    // i polling refetch + cross-tab sync.
    useEffect(() =>
    {
        if(data) applySkin(data);
    }, [data]);

    return data ?? readCachedSkin();
}

/** Lista skin disponibili (per il picker staff). */
export function useAvailableSkins()
{
    return useQuery<SkinManifest[]>({
        queryKey: ['skins', 'list'],
        queryFn: ({ signal }) => fetchAvailableSkins(signal),
        staleTime: 5 * 60_000
    });
}

/** Mutation staff: cambia skin attivo via PUT. */
export function useSetSkin()
{
    const qc = useQueryClient();
    return useMutation<SkinManifest, Error, string>({
        mutationFn: setActiveSkin,
        onSuccess: (manifest) =>
        {
            qc.setQueryData(['site', 'skin'], manifest);
        }
    });
}
