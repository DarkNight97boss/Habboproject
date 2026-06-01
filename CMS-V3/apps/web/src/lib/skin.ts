/**
 * Skin Engine — manifest format + apply pipeline.
 *
 * Uno SKIN è un manifest JSON che descrive ogni aspetto visibile del CMS:
 * non solo i colori, ma tipografia, spacing, bordi, ombre, animazioni,
 * asset (logo/bg), layout structure, background layers.
 *
 * Pipeline applySkin(manifest):
 *   1. Scrive ogni token come CSS custom property su :root
 *   2. Setta data-skin + data-layout su <html>
 *   3. Carica font esterni via FontFace API (se fontUrl presente)
 *   4. Carica pattern/bg asset via <link rel=preload>
 *
 * Sorgente di verità: server (cms_v3_skins.manifest_json). Solo staff può
 * cambiare skin attivo (PUT /api/v2/site/skin con rank >= 5).
 */

// ============================================================
//   MANIFEST SHAPE
// ============================================================

export interface SkinManifest {
    /** Schema version per migrare manifest vecchi se il formato cambia. */
    version: number;

    meta: {
        slug: string;
        name: string;
        description: string;
        /** 4 colori chiave per preview swatches nel SkinPicker. */
        swatches: [string, string, string, string];
    };

    tokens: {
        /**
         * Color palette completa. Ogni chiave diventa `--color-<key>` su :root.
         * Convenzioni: `page-bg`, `page-fg`, `header-bg`, `card-bg`,
         * `accent`, `link`, `success`, `warning`, `error`, `text-muted`.
         */
        colors: Record<string, string>;

        typography: {
            /** Stack font primario. Es. `'"Ubuntu", system-ui, sans-serif'` */
            fontFamily: string;
            /** Stack font per titoli/heading. Default = fontFamily. */
            fontFamilyHeading?: string;
            /** URL CSS Google Fonts (https://fonts.googleapis.com/css2?family=...) — caricato lazy. */
            fontUrl?: string;
            /** Moltiplicatore size 0.875 = compact, 1 = normale, 1.125 = comfortable, 1.25 = poster. */
            sizeScale: number;
            /** Peso heading: 400 normale, 700 bold, 900 black. */
            weightHeading: number;
            /** Letter-spacing per heading (es. "0.02em"). */
            letterSpacingHeading?: string;
        };

        spacing: {
            /** Unità base in px (4 = tight, 8 = comfortable, 12 = spacious). */
            unit: number;
            /** Moltiplicatore globale densità: 0.75 = compact, 1 = normal, 1.5 = roomy. */
            densityScale: number;
        };

        radius: {
            /** Radius card/box in px. 0 = squadrato, 8 = soft, 16 = rounded, 24 = pill. */
            base: number;
            /** Radius button. */
            button: number;
            /** Radius input/form. */
            input: number;
        };

        shadow: {
            /** Stile shadow: 'flat'=nessuna, 'soft'=blurred, 'neon-glow'=colored glow, 'pixel'=hard pixel offset. */
            style: 'flat' | 'soft' | 'neon-glow' | 'pixel';
            /** CSS box-shadow per card. */
            card: string;
            /** CSS box-shadow per button (hover). */
            button: string;
        };

        animation: {
            /** Velocità transizione: 'instant' = 0ms, 'fast' = 100ms, 'normal' = 200ms, 'slow' = 400ms. */
            speed: 'instant' | 'fast' | 'normal' | 'slow';
            /** Easing CSS (es. 'ease-out', 'cubic-bezier(...)'). */
            easing: string;
        };
    };

    /** Override asset (URL completi). Optional — se mancante usa default app. */
    assets?: {
        logoUrl?: string;
        heroBackgroundUrl?: string;
        patternUrl?: string;
        faviconUrl?: string;
    };

    /** Variante layout strutturale. Fase 3 implementerà. */
    layout?: {
        variant?: 'classic' | 'modern' | 'compact' | 'magazine';
    };

    /** Background del body — layer composto. */
    background?: {
        type: 'solid' | 'gradient' | 'pattern' | 'animated';
        /** Valore CSS dipendente dal type. */
        value: string;
        /** Per type='animated', nome animazione registrata (es. 'snow', 'stars', 'cobweb'). */
        animation?: string;
    };
}

// ============================================================
//   APPLY PIPELINE
// ============================================================

/** Scrive una mappa di CSS custom properties sul :root. */
function setCssVars(vars: Record<string, string | number>): void
{
    const root = document.documentElement;
    for(const [key, val] of Object.entries(vars))
    {
        root.style.setProperty(key, String(val));
    }
}

/** Speed token → millisecondi. */
const SPEED_MS: Record<SkinManifest['tokens']['animation']['speed'], number> = {
    instant: 0,
    fast: 100,
    normal: 200,
    slow: 400
};

/**
 * Applica completamente uno skin manifest al DOM.
 * Idempotente: chiamarlo 2x con lo stesso manifest non duplica nulla.
 */
export function applySkin(manifest: SkinManifest): void
{
    const root = document.documentElement;

    // 1) Identifier su <html> per CSS selectors mirati [data-skin="..."]
    root.setAttribute('data-skin', manifest.meta.slug);

    // 2) Layout variant (Fase 3 usa questo per swap grid)
    root.setAttribute('data-layout', manifest.layout?.variant ?? 'classic');

    // 3) Colors → --color-<key>
    const colorVars: Record<string, string> = {};
    for(const [k, v] of Object.entries(manifest.tokens.colors))
    {
        colorVars[`--color-${k}`] = v;
    }

    // 4) Typography
    const fontVars: Record<string, string | number> = {
        '--font-family-base': manifest.tokens.typography.fontFamily,
        '--font-family-heading': manifest.tokens.typography.fontFamilyHeading || manifest.tokens.typography.fontFamily,
        '--font-size-scale': manifest.tokens.typography.sizeScale,
        '--font-weight-heading': manifest.tokens.typography.weightHeading,
        '--font-letter-spacing-heading': manifest.tokens.typography.letterSpacingHeading || '0'
    };

    // 5) Spacing
    const spacingVars = {
        '--spacing-unit': `${manifest.tokens.spacing.unit}px`,
        '--spacing-density': manifest.tokens.spacing.densityScale
    };

    // 6) Radius
    const radiusVars = {
        '--radius-base': `${manifest.tokens.radius.base}px`,
        '--radius-button': `${manifest.tokens.radius.button}px`,
        '--radius-input': `${manifest.tokens.radius.input}px`
    };

    // 7) Shadow
    const shadowVars = {
        '--shadow-style': manifest.tokens.shadow.style,
        '--shadow-card': manifest.tokens.shadow.card,
        '--shadow-button': manifest.tokens.shadow.button
    };

    // 8) Animation
    const animVars = {
        '--anim-speed': `${SPEED_MS[manifest.tokens.animation.speed]}ms`,
        '--anim-easing': manifest.tokens.animation.easing
    };

    // 9) Assets (URL come CSS vars — usabili in url(var(--asset-...)))
    const assetVars: Record<string, string> = {};
    if(manifest.assets?.logoUrl)            assetVars['--asset-logo']         = `url("${manifest.assets.logoUrl}")`;
    if(manifest.assets?.heroBackgroundUrl)  assetVars['--asset-hero-bg']      = `url("${manifest.assets.heroBackgroundUrl}")`;
    if(manifest.assets?.patternUrl)         assetVars['--asset-pattern']      = `url("${manifest.assets.patternUrl}")`;

    // 10) Background body
    const bgVars: Record<string, string> = {};
    if(manifest.background)
    {
        bgVars['--bg-type'] = manifest.background.type;
        bgVars['--bg-value'] = manifest.background.value;
        if(manifest.background.animation)
        {
            bgVars['--bg-animation'] = manifest.background.animation;
        }
    }

    // Scrive TUTTO atomicamente (un solo browser reflow)
    setCssVars({ ...colorVars, ...fontVars, ...spacingVars, ...radiusVars, ...shadowVars, ...animVars, ...assetVars, ...bgVars });

    // 11) Carica font esterno on-demand (no FOUT perché applichiamo fallback già)
    if(manifest.tokens.typography.fontUrl)
    {
        injectFontStylesheet(manifest.tokens.typography.fontUrl);
    }

    // 12) Favicon swap (opt)
    if(manifest.assets?.faviconUrl)
    {
        let link = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
        if(!link)
        {
            link = document.createElement('link');
            link.rel = 'icon';
            document.head.appendChild(link);
        }
        link.href = manifest.assets.faviconUrl;
    }
}

/** Inietta <link rel=stylesheet> per Google Fonts una sola volta per URL. */
const loadedFontUrls = new Set<string>();
function injectFontStylesheet(url: string): void
{
    if(loadedFontUrls.has(url)) return;
    loadedFontUrls.add(url);
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = url;
    document.head.appendChild(link);
}

// ============================================================
//   API CLIENT (fetch helpers)
// ============================================================

const SKIN_CACHE_KEY = 'cms-v3-skin-cache';

/** Cache locale dell'ultimo skin manifest applicato — anti-FOUC. */
export function readCachedSkin(): SkinManifest | null
{
    try
    {
        const raw = window.localStorage.getItem(SKIN_CACHE_KEY);
        if(!raw) return null;
        return JSON.parse(raw) as SkinManifest;
    }
    catch { return null; }
}

function writeCachedSkin(manifest: SkinManifest): void
{
    try { window.localStorage.setItem(SKIN_CACHE_KEY, JSON.stringify(manifest)); }
    catch {/* localStorage può essere bloccato */}
}

/**
 * Hydration al boot: applica subito lo skin cached (anti-FOUC). Poi
 * useSkin React Query fetcherà fresh dall'API e ri-applicherà se cambiato.
 */
export function hydrateSkin(): void
{
    const cached = readCachedSkin();
    if(cached) applySkin(cached);
}

/** Fetch skin attivo dal server (lettura pubblica, cacheable 60s edge). */
export async function fetchActiveSkin(signal?: AbortSignal): Promise<SkinManifest>
{
    const r = await fetch('/api/v2/site/skin', { credentials: 'include', cache: 'no-store', signal });
    if(!r.ok) throw new Error('site_skin_fetch_' + r.status);
    const data = await r.json() as { manifest: SkinManifest };
    writeCachedSkin(data.manifest);
    applySkin(data.manifest);
    return data.manifest;
}

/** Lista skin disponibili (per il picker staff). */
export async function fetchAvailableSkins(signal?: AbortSignal): Promise<SkinManifest[]>
{
    const r = await fetch('/api/v2/skins', { credentials: 'include', cache: 'no-store', signal });
    if(!r.ok) throw new Error('skins_list_fetch_' + r.status);
    const data = await r.json() as { skins: SkinManifest[] };
    return data.skins;
}

/** Cambia skin attivo (staff only — backend enforce rank >= 5). */
export async function setActiveSkin(slug: string): Promise<SkinManifest>
{
    const r = await fetch('/api/v2/site/skin', {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slug })
    });
    if(!r.ok)
    {
        const err = await r.json().catch(() => ({})) as { error?: string };
        throw new Error(err.error || 'skin_change_failed_' + r.status);
    }
    const data = await r.json() as { manifest: SkinManifest };
    writeCachedSkin(data.manifest);
    applySkin(data.manifest);
    return data.manifest;
}
