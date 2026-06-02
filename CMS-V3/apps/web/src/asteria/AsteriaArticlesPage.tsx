import { type CSSProperties, type ReactNode } from 'react';
import { Link, useParams } from 'react-router';
import { AsteriaShell } from './AsteriaShell';
import { useNewsList, type NewsItem } from '../hooks/useNews';
import './asteria.css';

/**
 * /community/category/:category — lista news/articoli in stile Asteria.
 *
 * Fetcha TUTTI gli articoli una volta (cache 60s), costruisce i chip categoria
 * dai dati reali e filtra client-side in base al param :category. Ogni card
 * linka al dettaglio /community/article/:slug.
 */
export function AsteriaArticlesPage(): ReactNode
{
    const params = useParams<{ category: string }>();
    const active = params.category ?? 'all';
    const { data: all, isLoading, error } = useNewsList();

    const list = all ?? [];
    const categories = buildCategories(list);
    const items = active === 'all' ? list : list.filter(n => n.category === active);

    return (
        <AsteriaShell activeNav="community">
            <section className="asteria-news">
                <header className="asteria-news__head">
                    <div className="asteria-hero__eyebrow">Community · News</div>
                    <h1 className="asteria-news__h1">Novità da Asteria</h1>
                    <p className="asteria-news__sub">Aggiornamenti, eventi e annunci dal team.</p>
                </header>

                <nav className="asteria-news__chips" aria-label="Categorie">
                    <Link to="/community/category/all" className={chipClass(active === 'all')}>Tutte</Link>
                    {categories.map(c => (
                        <Link key={c.slug} to={`/community/category/${c.slug}`} className={chipClass(active === c.slug)}>
                            {c.label}
                        </Link>
                    ))}
                </nav>

                {isLoading && <div className="asteria-news__state">Caricamento articoli…</div>}
                {error && <div className="asteria-news__state asteria-news__state--err">Errore nel caricamento delle news.</div>}
                {!isLoading && !error && items.length === 0 && (
                    <div className="asteria-news__state">Nessun articolo in questa categoria.</div>
                )}

                <div className="asteria-news__grid">
                    {items.map(n => <AsteriaArticleCard key={n.slug} item={n} />)}
                </div>
            </section>
        </AsteriaShell>
    );
}

/** Card articolo riusabile (lista + "continua a leggere" nel dettaglio). */
export function AsteriaArticleCard({ item }: { item: NewsItem }): ReactNode
{
    return (
        <Link to={`/community/article/${item.slug}`} className="asteria-news__card">
            <div className="asteria-news__cover" style={coverStyle(item)}>
                <span className="asteria-news__cat">{item.categoryLabel}</span>
            </div>
            <div className="asteria-news__body">
                <h3 className="asteria-news__title">{item.title}</h3>
                <p className="asteria-news__excerpt">{item.summary}</p>
                <div className="asteria-news__date">{item.date}</div>
            </div>
        </Link>
    );
}

function chipClass(activeChip: boolean): string
{
    return `asteria-chip${activeChip ? ' is-active' : ''}`;
}

function buildCategories(items: NewsItem[]): { slug: string; label: string }[]
{
    const map = new Map<string, string>();
    for(const it of items) if(!map.has(it.category)) map.set(it.category, it.categoryLabel);
    return [...map].map(([slug, label]) => ({ slug, label }));
}

const GRADIENTS = [
    'linear-gradient(135deg, #a855f7 0%, #06b6d4 100%)',
    'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
    'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)',
    'linear-gradient(135deg, #f59e0b 0%, #ec4899 100%)',
    'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)'
];

/** Cover: immagine articolo se presente, altrimenti gradient deterministico. */
function coverStyle(item: NewsItem): CSSProperties
{
    if(item.image) return { backgroundImage: `url("${item.image}")` };
    let h = 0;
    for(const ch of item.slug) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    return { background: GRADIENTS[h % GRADIENTS.length] as string };
}
