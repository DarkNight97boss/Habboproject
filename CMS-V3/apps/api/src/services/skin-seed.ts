/**
 * Skin Engine — seed dei 3 manifest BUILTIN.
 *
 * Auto-eseguito al boot dell'API se la tabella cms_v3_skins è vuota.
 * I 3 builtin sono:
 *   1. habbo-classico-2008  — pixel font + squadrato + palette Habbo originale
 *   2. habbo-modern         — Ubuntu + rounded + glassmorphism + blu/cyan moderno
 *   3. habbo-dark-pro       — system-ui + rounded + neon glow + dark mode
 *
 * Lo SHAPE deve coincidere con `SkinManifest` in apps/web/src/lib/skin.ts.
 * Se si modifica lì, va aggiornato anche qui (FUTURE: estrarre il tipo in
 * packages/design-tokens condiviso).
 */

import { dbExecute, dbQuery } from '../db/pool.js';

interface BuiltinSkin {
    slug: string;
    name: string;
    description: string;
    manifest: unknown; // full manifest JSON (vedi lib/skin.ts)
}

// ============================================================
//   1. HABBO CLASSICO 2008 — pixel font + squadrato + giallo
// ============================================================
const HABBO_CLASSICO_2008: BuiltinSkin = {
    slug: 'habbo-classico-2008',
    name: 'Habbo Classico 2008',
    description: 'Lo stile originale habbo.it: pixel font, tutto squadrato, niente ombre. Pura nostalgia.',
    manifest: {
        version: 1,
        meta: {
            slug: 'habbo-classico-2008',
            name: 'Habbo Classico 2008',
            description: 'Lo stile originale habbo.it: pixel font, tutto squadrato, niente ombre. Pura nostalgia.',
            swatches: ['#0c3a65', '#0a3b6e', '#FFB900', '#7ECAEE']
        },
        tokens: {
            colors: {
                'page-bg':        '#0c3a65',
                'page-fg':        '#7ECAEE',
                'header-bg':      '#0a3b6e',
                'header-fg':      '#FFFFFF',
                'navtop-bg':      '#E8F0F5',
                'navtop-fg':      '#0a3b6e',
                'navsub-bg':      '#0A1620',
                'navsub-fg':      '#7ECAEE',
                'card-bg':        '#102f4d',
                'card-border':    '#A1B5C8',
                'card-header-bg': '#2A6495',
                'card-header-fg': '#FFFFFF',
                'accent':         '#FFB900',
                'accent-fg':      '#0a3b6e',
                'button-bg':      '#0F7DBC',
                'button-fg':      '#FFFFFF',
                'button-border':  '#2A6495',
                'link':           '#FFB900',
                'link-hover':     '#FFEA00',
                'text-muted':     '#A1B5C8',
                'success':        '#8EDA55',
                'warning':        '#FF8F3A',
                'error':          '#D37871'
            },
            typography: {
                fontFamily: '"Ubuntu", "Trebuchet MS", "Lucida Grande", Tahoma, sans-serif',
                fontFamilyHeading: '"Press Start 2P", "Ubuntu", monospace',
                fontUrl: 'https://fonts.googleapis.com/css2?family=Press+Start+2P&family=Ubuntu:wght@400;700&display=swap',
                sizeScale: 1.0,
                weightHeading: 400,
                letterSpacingHeading: '0.05em'
            },
            spacing: { unit: 8, densityScale: 1.0 },
            radius:  { base: 0, button: 0, input: 0 },
            shadow:  {
                style: 'pixel',
                card:   'none',
                button: '0 2px 0 rgba(0,0,0,0.30)'
            },
            animation: { speed: 'instant', easing: 'ease-out' }
        },
        layout: { variant: 'classic' },
        background: { type: 'solid', value: '#0c3a65' }
    }
};

// ============================================================
//   2. HABBO MODERN — Ubuntu + rounded + glassmorphism
// ============================================================
const HABBO_MODERN: BuiltinSkin = {
    slug: 'habbo-modern',
    name: 'Habbo Modern',
    description: 'Look 2026: card morbide, vetro smerigliato, animazioni fluide. Per chi vuole sentirsi su un sito nuovo.',
    manifest: {
        version: 1,
        meta: {
            slug: 'habbo-modern',
            name: 'Habbo Modern',
            description: 'Look 2026: card morbide, vetro smerigliato, animazioni fluide.',
            swatches: ['#1e3a8a', '#3b82f6', '#06b6d4', '#e0f2fe']
        },
        tokens: {
            colors: {
                'page-bg':        '#1e3a8a',
                'page-fg':        '#e0f2fe',
                'header-bg':      '#1e40af',
                'header-fg':      '#FFFFFF',
                'navtop-bg':      'rgba(255,255,255,0.95)',
                'navtop-fg':      '#1e3a8a',
                'navsub-bg':      'rgba(15,23,42,0.85)',
                'navsub-fg':      '#7dd3fc',
                'card-bg':        'rgba(255,255,255,0.08)',
                'card-border':    'rgba(255,255,255,0.15)',
                'card-header-bg': '#3b82f6',
                'card-header-fg': '#FFFFFF',
                'accent':         '#06b6d4',
                'accent-fg':      '#0c4a6e',
                'button-bg':      '#3b82f6',
                'button-fg':      '#FFFFFF',
                'button-border':  '#1d4ed8',
                'link':           '#7dd3fc',
                'link-hover':     '#bae6fd',
                'text-muted':     '#94a3b8',
                'success':        '#22c55e',
                'warning':        '#f59e0b',
                'error':          '#ef4444'
            },
            typography: {
                fontFamily: '"Inter", "Ubuntu", system-ui, -apple-system, sans-serif',
                fontFamilyHeading: '"Inter", system-ui, sans-serif',
                fontUrl: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap',
                sizeScale: 1.0,
                weightHeading: 600,
                letterSpacingHeading: '-0.02em'
            },
            spacing: { unit: 8, densityScale: 1.1 },
            radius:  { base: 12, button: 8, input: 6 },
            shadow:  {
                style: 'soft',
                card:   '0 4px 24px rgba(0,0,0,0.25), 0 1px 2px rgba(0,0,0,0.10)',
                button: '0 2px 8px rgba(59,130,246,0.40)'
            },
            animation: { speed: 'normal', easing: 'cubic-bezier(0.4, 0, 0.2, 1)' }
        },
        layout: { variant: 'modern' },
        background: {
            type: 'gradient',
            value: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #0c4a6e 100%)'
        }
    }
};

// ============================================================
//   3. HABBO DARK PRO — system-ui + neon glow
// ============================================================
const HABBO_DARK_PRO: BuiltinSkin = {
    slug: 'habbo-dark-pro',
    name: 'Habbo Dark Pro',
    description: 'Nottambuli only: nero AMOLED, neon cyan e viola, glow ovunque. Ottimo per occhi stanchi.',
    manifest: {
        version: 1,
        meta: {
            slug: 'habbo-dark-pro',
            name: 'Habbo Dark Pro',
            description: 'Nottambuli only: nero AMOLED, neon cyan e viola, glow ovunque.',
            swatches: ['#0a0a0f', '#1a1a26', '#22d3ee', '#a855f7']
        },
        tokens: {
            colors: {
                'page-bg':        '#0a0a0f',
                'page-fg':        '#cbd5e1',
                'header-bg':      '#14141c',
                'header-fg':      '#FFFFFF',
                'navtop-bg':      '#1a1a26',
                'navtop-fg':      '#cbd5e1',
                'navsub-bg':      '#050507',
                'navsub-fg':      '#94a3b8',
                'card-bg':        '#1a1a26',
                'card-border':    '#2a2a3a',
                'card-header-bg': '#22d3ee',
                'card-header-fg': '#0a0a0f',
                'accent':         '#22d3ee',
                'accent-fg':      '#0a0a0f',
                'button-bg':      '#a855f7',
                'button-fg':      '#FFFFFF',
                'button-border':  '#7e22ce',
                'link':           '#22d3ee',
                'link-hover':     '#67e8f9',
                'text-muted':     '#64748b',
                'success':        '#4ade80',
                'warning':        '#fbbf24',
                'error':          '#f87171'
            },
            typography: {
                fontFamily: 'system-ui, -apple-system, "Segoe UI", "Roboto", sans-serif',
                fontFamilyHeading: 'system-ui, -apple-system, "Segoe UI", sans-serif',
                sizeScale: 1.0,
                weightHeading: 700,
                letterSpacingHeading: '0'
            },
            spacing: { unit: 8, densityScale: 1.0 },
            radius:  { base: 8, button: 6, input: 4 },
            shadow:  {
                style: 'neon-glow',
                card:   '0 0 24px rgba(34, 211, 238, 0.20), 0 0 1px rgba(34, 211, 238, 0.40)',
                button: '0 0 16px rgba(168, 85, 247, 0.50)'
            },
            animation: { speed: 'fast', easing: 'ease-out' }
        },
        layout: { variant: 'classic' },
        background: {
            type: 'gradient',
            value: 'radial-gradient(ellipse at top, #1a1a26 0%, #0a0a0f 70%)'
        }
    }
};

const BUILTIN_SKINS: BuiltinSkin[] = [HABBO_CLASSICO_2008, HABBO_MODERN, HABBO_DARK_PRO];

/**
 * Idempotente: per ogni builtin INSERT se manca, UPDATE manifest se è
 * cambiato (per evolverli senza intervento manuale).
 */
export async function ensureSkinsSeeded(): Promise<void>
{
    for(const s of BUILTIN_SKINS)
    {
        await dbExecute(
            "INSERT INTO cms_v3_skins (slug, name, description, manifest_json, is_builtin) " +
            "VALUES (?, ?, ?, ?, 1) " +
            "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), manifest_json = VALUES(manifest_json), is_builtin = 1",
            [s.slug, s.name, s.description, JSON.stringify(s.manifest)]
        );
    }
}

/** Lista per debug/CLI. */
export async function listSeededSkins(): Promise<{ slug: string; name: string }[]>
{
    return dbQuery<{ slug: string; name: string }>(
        "SELECT slug, name FROM cms_v3_skins ORDER BY is_builtin DESC, name ASC"
    );
}
