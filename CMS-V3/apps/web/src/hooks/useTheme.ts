import { useEffect, useState } from 'react';
import { type ThemeId, hydrateTheme, setTheme as persistTheme } from '../lib/theme';

/**
 * Hook React per leggere/cambiare il tema attivo.
 *
 * - Inizializza dal localStorage al primo render (già hydratato in main.tsx).
 * - Ascolta `cms-v3-theme-change` event per sync cross-componente.
 * - Ascolta `storage` event nativo per sync cross-tab.
 *
 * Pattern: NO Zustand global store — non serve perché il theme vive sul DOM
 * (`<html data-theme>`) ed è l'unica sorgente di verità. Lo state React qui
 * è solo per forzare re-render dei componenti che mostrano il tema corrente.
 */
export function useTheme(): readonly [ThemeId, (next: ThemeId) => void]
{
    const [theme, setLocal] = useState<ThemeId>(() => hydrateTheme());

    useEffect(() =>
    {
        function onChange(ev: Event): void
        {
            const next = (ev as CustomEvent<ThemeId>).detail;
            if(next) setLocal(next);
        }
        function onStorage(ev: StorageEvent): void
        {
            if(ev.key === 'cms-v3-theme' && ev.newValue)
            {
                setLocal(ev.newValue as ThemeId);
                // Anche applica al DOM — questo è dall'altra tab, non era applicato qui.
                document.documentElement.setAttribute('data-theme', ev.newValue);
            }
        }
        window.addEventListener('cms-v3-theme-change', onChange);
        window.addEventListener('storage', onStorage);
        return () =>
        {
            window.removeEventListener('cms-v3-theme-change', onChange);
            window.removeEventListener('storage', onStorage);
        };
    }, []);

    function change(next: ThemeId): void
    {
        persistTheme(next);
        setLocal(next);
    }

    return [theme, change] as const;
}
