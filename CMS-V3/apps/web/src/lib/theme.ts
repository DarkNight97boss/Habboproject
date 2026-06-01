/**
 * Theme system: registry + apply/persistence helpers.
 *
 * Sorgente di verità per i temi disponibili. Il CSS effettivo dell'override
 * vive in `styles/themes.css` (caricato in main.tsx).
 *
 * Persistenza: localStorage chiave `cms-v3-theme`. Per utenti loggati possiamo
 * in futuro syncare con users.theme_preference via PATCH /api/v2/me/theme,
 * ma per ora il client-side è sufficiente (no flash perché applichiamo
 * data-theme PRIMA del render React in main.tsx).
 */

export type ThemeId = 'habbo-classico' | 'habbo-dark' | 'habbo-sunset' | 'minimal-light';

export interface Theme {
    id: ThemeId;
    name: string;
    description: string;
    /** Swatch colors per preview UI nel selettore (palette ridotta a 4 colori chiave). */
    swatches: [string, string, string, string];
}

export const THEMES: readonly Theme[] = [
    {
        id: 'habbo-classico',
        name: 'Habbo Classico',
        description: 'Blu navy + giallo. Lo stile originale habbo.it.',
        swatches: ['#0c3a65', '#0a3b6e', '#FFB900', '#7ECAEE']
    },
    {
        id: 'habbo-dark',
        name: 'Habbo Dark',
        description: 'Nero profondo + cyan elettrico. Per nottambuli.',
        swatches: ['#0a0a0f', '#14141c', '#25b8ee', '#c0d8ee']
    },
    {
        id: 'habbo-sunset',
        name: 'Habbo Sunset',
        description: 'Viola + arancio + giallo. Vibes anni 2008 al tramonto.',
        swatches: ['#2d1a3e', '#ff6b35', '#ffba08', '#ffd4a8']
    },
    {
        id: 'minimal-light',
        name: 'Minimal Light',
        description: 'Bianco + grigi + accent blu. Pulito e leggibile.',
        swatches: ['#f4f6fa', '#FFFFFF', '#2563eb', '#11243a']
    }
] as const;

export const DEFAULT_THEME: ThemeId = 'habbo-classico';
const STORAGE_KEY = 'cms-v3-theme';

/** Applica il tema settando `data-theme` su <html>. Idempotente. */
export function applyTheme(themeId: ThemeId): void
{
    document.documentElement.setAttribute('data-theme', themeId);
}

/** Legge il tema persistito (o ritorna default). Safe contro SSR/localStorage missing. */
export function loadStoredTheme(): ThemeId
{
    try
    {
        const stored = window.localStorage.getItem(STORAGE_KEY);
        if(stored && THEMES.some(t => t.id === stored)) return stored as ThemeId;
    }
    catch
    {
        // localStorage può essere bloccato (private mode, no-cookie consent, ecc.)
    }
    return DEFAULT_THEME;
}

/** Persiste e applica. Chiamato dal ThemePicker su change. */
export function setTheme(themeId: ThemeId): void
{
    try
    {
        window.localStorage.setItem(STORAGE_KEY, themeId);
    }
    catch {/* ignora — il tema vive in memoria comunque */}
    applyTheme(themeId);
    // Notifica altre tab CMS aperte (lo stesso utente potrebbe avere più tab).
    try
    {
        window.dispatchEvent(new CustomEvent('cms-v3-theme-change', { detail: themeId }));
    }
    catch {/* fallback no-op */}
}

/** Hydration al boot: legge da localStorage e applica PRIMA del render React. */
export function hydrateTheme(): ThemeId
{
    const theme = loadStoredTheme();
    applyTheme(theme);
    return theme;
}
