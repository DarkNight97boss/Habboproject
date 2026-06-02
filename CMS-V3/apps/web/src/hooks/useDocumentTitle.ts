import { useEffect } from 'react';

export const SITE_NAME = 'Asteria';

/**
 * Imposta `document.title` a "<title> · Asteria" finché il componente è
 * montato. Usato dalle pagine data-driven (es. articolo, profilo) per
 * sovrascrivere il titolo di base impostato da <RouteTitle> con il titolo
 * reale, una volta caricati i dati. Passare null/undefined = no-op (resta
 * il titolo di route).
 */
export function useDocumentTitle(title: string | null | undefined): void
{
    useEffect(() =>
    {
        if(title && title.trim().length > 0)
        {
            document.title = `${title} · ${SITE_NAME}`;
        }
    }, [title]);
}
