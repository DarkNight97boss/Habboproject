import { type CSSProperties, type ReactNode } from 'react';
import { Link, useParams } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { AsteriaArticleCard } from './AsteriaArticlesPage';
import { useArticle, useNewsList } from '../hooks/useNews';
import './asteria.css';

/**
 * /community/article/:slug — lettura articolo singolo in stile Asteria.
 *
 * Fetch GET /api/v2/community/news/:slug. `data === null` ⇒ 404.
 * Mostra hero (cover + categoria + titolo + data + summary), il corpo HTML
 * dell'articolo e una sezione "continua a leggere" con altri articoli.
 */
export function AsteriaArticlePage(): ReactNode
{
    const params = useParams<{ slug: string }>();
    const slug = params.slug ?? '';
    const { data, isLoading, error } = useArticle(slug);
    const { data: all } = useNewsList();
    const related = (all ?? []).filter(n => n.slug !== slug).slice(0, 3);

    return (
        <AsteriaShell activeNav="community">
            {isLoading && (
                <section className="asteria-article">
                    <div className="asteria-news__state">Caricamento articolo…</div>
                </section>
            )}

            {!isLoading && (error || data === null) && (
                <section className="asteria-article asteria-article--missing">
                    <div className="asteria-hero__eyebrow">Errore 404</div>
                    <h1 className="asteria-article__title">Articolo non trovato</h1>
                    <p className="asteria-article__summary">L'articolo che cerchi non esiste o è stato rimosso.</p>
                    <div className="asteria-article__back">
                        <Link to="/community/category/all" className="asteria-btn asteria-btn--ghost">← Tutte le news</Link>
                    </div>
                </section>
            )}

            {data && (
                <>
                    <article className="asteria-article">
                        <div className="asteria-hero__eyebrow">{data.categoryLabel}</div>
                        <h1 className="asteria-article__title">{data.title}</h1>
                        <div className="asteria-article__meta">
                            <time>{data.date}</time>
                            <Link to={`/community/category/${data.category}`} className="asteria-chip is-active">
                                {data.categoryLabel}
                            </Link>
                        </div>

                        {data.image && (
                            <div className="asteria-article__cover" style={coverImage(data.image)} role="img" aria-label={data.title} />
                        )}

                        <p className="asteria-article__summary">{data.summary}</p>

                        {/* bodyHtml è contenuto curato dallo staff (NON user input).
                            Se in futuro gli articoli diventano user-generated, sanitizzare
                            con DOMPurify prima del render. */}
                        <div
                            className="asteria-article__body"
                            dangerouslySetInnerHTML={{ __html: data.bodyHtml /* nosemgrep */ }}
                        />

                        <div className="asteria-article__back">
                            <Link to="/community/category/all" className="asteria-btn asteria-btn--ghost">← Tutte le news</Link>
                            <a href="/api/v2/auth/play" className="asteria-btn">▶ Entra nel hotel</a>
                        </div>
                    </article>

                    {related.length > 0 && (
                        <section className="asteria-section">
                            <div className="asteria-section__head">
                                <h2 className="asteria-section__title">Continua a leggere</h2>
                                <span className="asteria-section__subtitle">Altre novità</span>
                            </div>
                            <div className="asteria-news__grid">
                                {related.map(n => <AsteriaArticleCard key={n.slug} item={n} />)}
                            </div>
                        </section>
                    )}
                </>
            )}
        </AsteriaShell>
    );
}

function coverImage(url: string): CSSProperties
{
    return { backgroundImage: `url("${url}")` };
}
