/**
 * Design tokens estratti da habbo.it ufficiale (2026-05).
 * Catturati via DOM scan + screenshot analysis.
 * Usati da Tailwind preset (vedi apps/web/tailwind.config.ts).
 */

export const habboColors = {
    /** Sfondo pagina (blu scuro tipico). */
    page: {
        bg: '#0c3a65',
        text: '#7ECAEE',
        accent: '#A1B5C8'
    },
    /** Header in alto col logo. */
    header: {
        bg: '#0a3b6e',
        text: '#FFFFFF'
    },
    /** Bar bianca menu principale (HOME, COMMUNITY, SHOP, ...). */
    navTop: {
        bg: '#E8F0F5',
        text: '#0a3b6e',
        textHover: '#003e6e',
        accent: '#2A6495'
    },
    /** Bar nera sub-menu (FOTO | STANZE | ...). */
    navSub: {
        bg: '#0A1620',
        text: '#7ECAEE',
        textHover: '#FFFFFF',
        separator: '#2A4060'
    },
    /** Card standard. */
    card: {
        bg: '#102f4d',
        headerBg: '#2A6495',
        headerText: '#FFFFFF',
        border: '#A1B5C8'
    },
    /** Box informativi sidebar (CONSIGLI DI SICUREZZA, etc.). */
    sidebar: {
        tipBg: '#1c4575',
        tipHeader: '#2A6495',
        tipText: '#FFFFFF'
    },
    /** Accenti colori per CTA e highlight. */
    accent: {
        yellow: '#FFB900',
        yellowLight: '#FFEA00',
        green: '#8EDA55',
        greenDark: '#00813E',
        orange: '#FF8F3A',
        red: '#923E3A',
        redLight: '#D37871',
        blue: '#0F7DBC',
        blueLight: '#6AA5EC'
    }
} as const;

export const habboType = {
    family: '"Ubuntu", "Trebuchet MS", "Lucida Grande", "Lucida Sans Unicode", "Lucida Sans", Tahoma, sans-serif',
    sizes: {
        xs: '12px',
        sm: '14px',
        base: '16px',
        lg: '18px',
        xl: '24px',
        hero: '36px'
    },
    weights: {
        regular: 400,
        bold: 700
    },
    lineHeight: {
        tight: 1.1,
        normal: 1.4,
        loose: 1.6
    }
} as const;

export const habboSpacing = {
    headerHeight: '74px',
    navTopHeight: '60px',
    navSubHeight: '40px',
    cardPadding: '16px',
    cardRadius: '0px', // habbo.it usa card squadrate
    buttonRadius: '10px',
    buttonBorder: '4px solid'
} as const;

export const habboShadow = {
    card: '0 2px 8px rgba(0,0,0,0.15)',
    button: '0 2px 0 rgba(0,0,0,0.20)',
    modal: '0 8px 32px rgba(0,0,0,0.35)'
} as const;

/** Z-index scale per evitare conflitti tra layer. */
export const habboZ = {
    nav: 100,
    sidebar: 90,
    modal: 1000,
    toast: 2000
} as const;
