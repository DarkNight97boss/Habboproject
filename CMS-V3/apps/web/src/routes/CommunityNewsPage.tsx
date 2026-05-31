import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate, useParams } from 'react-router';
import { AuthedShell, CommunityTabs } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /community/category/:category — lista notizie.
 *
 * Struttura DOM ufficiale (ispezione live habbo.it/community/category/all):
 *   <main>
 *     <section.wrapper.wrapper--content>
 *       <habbo-compile.main.main--fixed>
 *         <section>
 *           <header.news-category__header>
 *             <span>Vedere Notizie riguardanti:</span>
 *             <nav.news-category__navigation>
 *               <ul.news-category__list>
 *                 <li.news-category__item>
 *                   <a.news-category__link[.--active]>Tutto/Campagne…
 *           {news.map → <article.news-header.news-header--column>… (stesso
 *             markup di HomePage NewsList)
 *       <habbo-web-pages.aside.aside--box.aside--fixed>… sidebar
 *
 * Backend: GET /api/v2/community/news?category=<cat> → { news: [...] }
 */
export function CommunityNewsPage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const params = useParams<{ category?: string }>();
    const activeCat = params.category ?? 'all';

    const newsQuery = useQuery({
        queryKey: ['community', 'news', activeCat],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/community/news?category=${encodeURIComponent(activeCat)}`);
            if(!r.ok) throw new Error('news_failed');
            return r.json() as Promise<{ news: NewsItem[] }>;
        },
        staleTime: 30_000
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    return (
        <AuthedShell user={user} tabs={<CommunityTabs active="notizie" />}>
            <main className="wrapper wrapper--content">
                <section>
                    <div className="main main--fixed">
                        <habbo-compile>
                            <section>
                                <CategoryFilter active={activeCat} />
                                {newsQuery.isLoading ? <EmptyState message="Caricamento…" />
                                    : newsQuery.error ? <EmptyState message="Errore." />
                                    : (newsQuery.data?.news.length ?? 0) === 0
                                        ? <EmptyState message="Nessuna notizia in questa categoria." />
                                        : newsQuery.data!.news.map(n => <NewsArticle key={n.href} news={n} />)}
                            </section>
                        </habbo-compile>
                    </div>
                    <habbo-web-pages className="aside aside--box aside--fixed">
                        <aside className="static-content">
                            <h3>Consigli di sicurezza</h3>
                            <p>Sentiti sempre al sicuro su internet! Impara come <a href="/playing-habbo/safety">navigare in sicurezza</a>.</p>
                        </aside>
                    </habbo-web-pages>
                </section>
            </main>
        </AuthedShell>
    );
}

const CATEGORIES = [
    { key: 'all', label: 'Tutto' },
    { key: 'campagne-attivita', label: 'Campagne & Attività' },
    { key: 'offerte-speciali', label: 'Offerte Speciali' },
    { key: 'sicurezza', label: 'Sicurezza' },
    { key: 'aggiornamenti-su-habbo', label: 'Aggiornamenti su Habbo' },
    { key: 'nuove-funzionalita', label: 'Nuove funzionalità' },
    { key: 'ambasciatori', label: 'Ambasciatori' }
];

function CategoryFilter({ active }: { active: string }): ReactNode
{
    return (
        <header className="news-category__header">
            <span>Vedere Notizie riguardanti:</span>
            <nav className="news-category__navigation">
                <ul className="news-category__list">
                    {CATEGORIES.map(c => (
                        <li key={c.key} className="news-category__item">
                            <a
                                href={`/community/category/${c.key}`}
                                className={`news-category__link${c.key === active ? ' news-category__link--active' : ''}`}
                            >
                                {c.label}
                            </a>
                        </li>
                    ))}
                </ul>
            </nav>
        </header>
    );
}

interface NewsItem
{
    href: string;
    image: string;
    title: string;
    date: string;
    category: string;
    categoryLabel: string;
    summary: string;
}

function NewsArticle({ news }: { news: NewsItem }): ReactNode
{
    return (
        <article className="news-header news-header--column">
            <a href={news.href} className="news-header__link news-header__banner">
                <figure className="news-header__viewport">
                    <img src={news.image} alt={news.title} className="news-header__image news-header__image--featured" />
                    <img src={news.image} alt={news.title} className="news-header__image news-header__image--thumbnail" />
                </figure>
            </a>
            <a href={news.href} className="news-header__link news-header__wrapper">
                <h2 className="news-header__title">{news.title}</h2>
            </a>
            <aside className="news-header__wrapper news-header__info">
                <time className="news-header__date">{news.date}</time>
                <ul className="news-header__categories">
                    <li className="news-header__category">
                        <a className="news-header__category__link">{news.categoryLabel}</a>
                    </li>
                </ul>
            </aside>
            <p className="news-header__wrapper news-header__summary">{news.summary}</p>
        </article>
    );
}

function EmptyState({ message }: { message: string }): ReactNode
{
    return (
        <habbo-empty-results>
            <span>{message}</span>
        </habbo-empty-results>
    );
}
