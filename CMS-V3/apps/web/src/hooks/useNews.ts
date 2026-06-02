import { useQuery } from '@tanstack/react-query';

/**
 * News/articoli del CMS — consuma l'API pubblica community.
 *   GET /api/v2/community/news?category=<slug>  → { news: NewsItem[] }
 *   GET /api/v2/community/news/:slug             → { article: NewsItem } | 404
 *
 * Sorgente: tabella cms_v3_news (vedi services/news.ts + migration 002).
 */

export interface NewsItem {
    slug: string;
    href: string;
    image: string;
    title: string;
    date: string;
    category: string;
    categoryLabel: string;
    summary: string;
    bodyHtml: string;
}

async function fetchNewsList(category: string | undefined, signal?: AbortSignal): Promise<NewsItem[]>
{
    const qs = category && category !== 'all' ? `?category=${encodeURIComponent(category)}` : '';
    const r = await fetch(`/api/v2/community/news${qs}`, { credentials: 'include', signal });
    if(!r.ok) throw new Error('news_list_' + r.status);
    return (await r.json() as { news: NewsItem[] }).news;
}

async function fetchArticle(slug: string, signal?: AbortSignal): Promise<NewsItem | null>
{
    const r = await fetch(`/api/v2/community/news/${encodeURIComponent(slug)}`, { credentials: 'include', signal });
    if(r.status === 404) return null;
    if(!r.ok) throw new Error('article_' + r.status);
    return (await r.json() as { article: NewsItem }).article;
}

/** Lista articoli (opzionalmente filtrata per categoria). */
export function useNewsList(category?: string)
{
    return useQuery<NewsItem[]>({
        queryKey: ['community', 'news', category ?? 'all'],
        queryFn: ({ signal }) => fetchNewsList(category, signal),
        staleTime: 60_000
    });
}

/** Singolo articolo per slug. `data === null` ⇒ 404 (non esiste). */
export function useArticle(slug: string)
{
    return useQuery<NewsItem | null>({
        queryKey: ['community', 'article', slug],
        queryFn: ({ signal }) => fetchArticle(slug, signal),
        enabled: slug.length > 0,
        retry: false,
        staleTime: 60_000
    });
}
