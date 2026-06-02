import { useEffect } from 'react';
import { matchPath, useLocation } from 'react-router';
import { SITE_NAME } from '../hooks/useDocumentTitle';

/**
 * Imposta `document.title` dinamicamente a ogni navigazione, in base alla
 * route corrente. Centralizzato e indipendente dallo skin (vale sia per le
 * pagine Asteria che classic). Le pagine data-driven possono sovrascrivere
 * con il titolo reale via useDocumentTitle() una volta caricati i dati
 * (es. il titolo dell'articolo) — quel set avviene DOPO ed ha la priorità.
 */

function pretty(s: string | undefined): string
{
    if(!s) return '';
    return s.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

type Entry = { path: string; title: (p: Record<string, string | undefined>) => string };

// Pattern in ordine: i più specifici (più segmenti) per primi.
const ROUTES: Entry[] = [
    { path: '/profile/:user/photo/:id', title: p => `Foto di ${p.user}` },
    { path: '/profile/:username',       title: p => `${p.username}` },
    { path: '/community/article/:slug', title: p => `${pretty(p.slug)} · News` },
    { path: '/community/category/:category', title: p => p.category === 'all' ? 'News' : `${pretty(p.category)} · News` },
    { path: '/community/photos',        title: () => 'Foto della community' },
    { path: '/community/rooms',         title: () => 'Stanze' },
    { path: '/community/forum',         title: () => 'Forum' },
    { path: '/community',               title: () => 'Community' },
    { path: '/shop/acquisti',           title: () => 'I miei acquisti' },
    { path: '/shop/prepagate',          title: () => 'Codici prepagati' },
    { path: '/shop/success',            title: () => 'Acquisto completato' },
    { path: '/shop/cancel',             title: () => 'Acquisto annullato' },
    { path: '/shop',                    title: () => 'Shop' },
    { path: '/playing-habbo/:slug',     title: p => pretty(p.slug) },
    { path: '/playing-habbo',           title: () => 'Guida' },
    { path: '/help/:slug',              title: p => pretty(p.slug) },
    { path: '/help',                    title: () => 'Centro assistenza' },
    { path: '/habbo-nft',               title: () => 'Collezionabili' },
    { path: '/settings/:section',       title: p => `Impostazioni · ${pretty(p.section)}` },
    { path: '/settings',                title: () => 'Impostazioni' },
    { path: '/admin/:section',          title: p => `Admin · ${pretty(p.section)}` },
    { path: '/admin',                   title: () => 'Admin' },
    { path: '/messaging',               title: () => 'Messaggi' },
    { path: '/leaderboard',             title: () => 'Classifica' },
    { path: '/me',                      title: () => 'Il mio profilo' },
    { path: '/login',                   title: () => 'Accedi' },
    { path: '/registration',            title: () => 'Registrati' },
    { path: '/',                        title: () => 'Hotel virtuale italiano' }
];

export function RouteTitle(): null
{
    const { pathname } = useLocation();
    useEffect(() =>
    {
        let label = '';
        for(const r of ROUTES)
        {
            const m = matchPath({ path: r.path, end: true }, pathname);
            if(m)
            {
                label = r.title(m.params as Record<string, string | undefined>);
                break;
            }
        }
        document.title = label ? `${label} · ${SITE_NAME}` : SITE_NAME;
    }, [pathname]);
    return null;
}
