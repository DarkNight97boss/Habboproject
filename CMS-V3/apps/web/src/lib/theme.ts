/**
 * Theme system: registry + apply/fetch helpers.
 *
 * SORGENTE DI VERITÀ: server (DB cms_v3_site_settings.theme).
 * Solo lo staff (rank >= 5) può cambiare il tema. Tutti gli altri vedono
 * il tema attivo lato server in read-only.
 *
 * Anti-FOUC: prima del fetch async usiamo l'ultimo valore CACHED in
 * localStorage (`cms-v3-theme-cache`) per applicare immediatamente un tema
 * "verosimile" mentre il fetch dell'API gira. Quando arriva la risposta
 * fresca, sostituiamo. È un'ottimizzazione perceptual, non auth.
 *
 * Il CSS effettivo dei tema vive in `styles/themes.css` (caricato in
 * main.tsx).
 */

export type ThemeId = 'habbo-classico' | 'habbo-dark' | 'habbo-sunset' | 'minimal-light';

export interface Theme {
    id: ThemeId;
    name: string;
    description: string;
    /** 4 colori chiave per preview swatches nel ThemePicker. */
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
const CACHE_KEY = 'cms-v3-theme-cache';

/** Applica il tema settando `data-theme` su <html>. Idempotente. */
export function applyTheme(themeId: ThemeId): void
{
    document.documentElement.setAttribute('data-theme', themeId);
}

/** Cache scrittura (non auth — solo perceptual). */
function writeCache(themeId: ThemeId): void
{
    try { window.localStorage.setItem(CACHE_KEY, themeId); }
    catch {/* localStorage può essere bloccato — irrilevante */}
}

/** Cache lettura (per anti-FOUC al boot — DEFAULT se vuota o invalida). */
export function readCachedTheme(): ThemeId
{
    try
    {
        const stored = window.localStorage.getItem(CACHE_KEY);
        if(stored && THEMES.some(t => t.id === stored)) return stored as ThemeId;
    }
    catch {/* fallback default */}
    return DEFAULT_THEME;
}

/**
 * Hydration al boot: applica l'ultimo tema CACHED (verosimile) PRIMA del
 * render React. Poi `useTheme` farà il fetch fresh dall'API e correggerà
 * se il server ha cambiato nel frattempo.
 */
export function hydrateTheme(): ThemeId
{
    const theme = readCachedTheme();
    applyTheme(theme);
    return theme;
}

/**
 * Fetch del tema attivo dal server (lettura pubblica).
 * Aggiorna anche la cache locale per il prossimo boot.
 */
export async function fetchActiveTheme(signal?: AbortSignal): Promise<ThemeId>
{
    const r = await fetch('/api/v2/site/theme', { credentials: 'include', cache: 'no-store', signal });
    if(!r.ok) throw new Error('site_theme_fetch_' + r.status);
    const data = await r.json() as { theme?: string };
    const theme = data.theme && THEMES.some(t => t.id === data.theme) ? data.theme as ThemeId : DEFAULT_THEME;
    writeCache(theme);
    applyTheme(theme);
    return theme;
}

/**
 * Cambia il tema attivo (staff only — PUT con auth cookie).
 * Lato client: applica + cache + return. Lato server: rank >= 5 enforced.
 */
export async function setActiveTheme(themeId: ThemeId): Promise<void>
{
    const r = await fetch('/api/v2/site/theme', {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ theme: themeId })
    });
    if(!r.ok)
    {
        const err = await r.json().catch(() => ({})) as { error?: string };
        throw new Error(err.error || 'theme_change_failed_' + r.status);
    }
    writeCache(themeId);
    applyTheme(themeId);
}
