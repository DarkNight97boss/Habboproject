import { useQuery } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { Navigate, useParams } from 'react-router';
import { AuthedShell, CommunityTabs } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * Pagina /community/article/:slug — replica DOM ufficiale habbo.it.
 *
 * Struttura ispezionata (habbo.it/community/article/29428/note-sulla-mega-patch…):
 *   <main>
 *     <section.wrapper.wrapper--content>
 *       <habbo-compile.main.main--fixed>
 *         <article>
 *           <header.news-header.news-header--single>
 *             <div.news-header__banner>
 *               <figure.news-header__viewport>
 *                 <img.news-header__image.news-header__image--featured/>
 *             <habbo-social-share/>
 *             <h1.news-header__wrapper.news-header__title>{title}
 *             <aside.news-header__wrapper.news-header__info>
 *               <time.news-header__date>{date}
 *               <ul.news-header__categories>
 *                 <li.news-header__category>
 *                   <a.news-header__category__link>{categoryLabel}
 *             <p.news-header__wrapper.news-header__summary>{summary}
 *           <div.news-article>
 *             {bodyHtml renderizzato come HTML}
 *
 * Backend: GET /api/v2/community/news/:slug → { article: NewsItem }
 *   404 se slug non esiste.
 */
export function CommunityArticlePage(): ReactNode
{
    const { data: user, isLoading: authLoading } = useAuth();
    const params = useParams<{ slug: string }>();
    const slug = params.slug ?? '';

    const articleQuery = useQuery({
        queryKey: ['community', 'article', slug],
        queryFn: async () =>
        {
            const r = await fetch(`/api/v2/community/news/${encodeURIComponent(slug)}`);
            if(r.status === 404) return null;
            if(!r.ok) throw new Error('article_failed');
            return (await r.json() as { article: Article }).article;
        },
        enabled: slug.length > 0,
        retry: false
    });

    if(authLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    // Article non trovato → 404 page (mantiene chrome consistente).
    if(articleQuery.data === null)
    {
        return (
            <AuthedShell user={user} tabs={<CommunityTabs active="notizie" />}>
                <main className="wrapper wrapper--content not-found">
                    <section className="not-found__content">
                        <h3>Oh bobba! Articolo non trovato.</h3>
                        <div>L'articolo che cerchi non esiste. <a href="/community/category/all"><strong>Torna alle notizie</strong></a>.</div>
                    </section>
                </main>
            </AuthedShell>
        );
    }

    return (
        <AuthedShell user={user} tabs={<CommunityTabs active="notizie" />}>
            <main>
                <section className="wrapper wrapper--content">
                    {/* Layout 2-col: habbo-compile (.main.main--fixed) a sinistra,
                        habbo-web-pages (.aside.aside--box.aside--fixed) a destra.
                        Stessa struttura di habbo.it/community/article/<id>. */}
                    <habbo-compile className="main main--fixed">
                        {articleQuery.isLoading || !articleQuery.data
                            ? <p>Caricamento…</p>
                            : <ArticleContent article={articleQuery.data} />}
                    </habbo-compile>
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

interface Article
{
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

function ArticleContent({ article }: { article: Article }): ReactNode
{
    return (
        <article>
            <header className="news-header news-header--single">
                <div className="news-header__banner">
                    <figure className="news-header__viewport">
                        <img src={article.image} alt={article.title} className="news-header__image news-header__image--featured" />
                    </figure>
                </div>
                <h1 className="news-header__wrapper news-header__title">{article.title}</h1>
                <aside className="news-header__wrapper news-header__info">
                    <time className="news-header__date">{article.date}</time>
                    <ul className="news-header__categories">
                        <li className="news-header__category">
                            <a href={`/community/category/${article.category}`} className="news-header__category__link">{article.categoryLabel}</a>
                        </li>
                    </ul>
                </aside>
                <p className="news-header__wrapper news-header__summary">{article.summary}</p>
            </header>
            {/* bodyHtml proviene dal backend, contenuto trusted (curato dallo
                staff). dangerouslySetInnerHTML è OK qui perché non viene da
                user input. Se in futuro pubblichiamo articoli user-generated
                serve sanitize via DOMPurify. */}
            <div
                className="news-article"
                dangerouslySetInnerHTML={{ __html: article.bodyHtml }}
            />
            <div className="news-footer">
                <a href="/community/category/all">← Torna a tutte le notizie</a>
            </div>
        </article>
    );
}
