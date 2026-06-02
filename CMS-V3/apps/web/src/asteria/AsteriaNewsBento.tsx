import { type CSSProperties, type ReactNode } from 'react';
import { Link } from 'react-router';
import { useNewsList, type NewsItem } from '../hooks/useNews';

/**
 * Bento grid news condivisa fra HomePage e MePage.
 *
 * Consuma le news REALI da /api/v2/community/news (hook useNewsList). Se l'API
 * non ha ancora risposto o è vuota, mostra un fallback statico così la home
 * non resta mai vuota. Le card linkano al dettaglio /community/article/:slug.
 */

type BentoSize = 'feat' | 'default' | 'wide';
const SIZES: BentoSize[] = ['feat', 'default', 'default', 'wide', 'default', 'default'];

interface BentoCard
{
    slug: string;
    cat: string;
    title: string;
    excerpt: string;
    date: string;
    cover: CSSProperties;
    size: BentoSize;
}

const GRADIENTS = [
    'linear-gradient(135deg, #a855f7 0%, #06b6d4 50%, #ec4899 100%)',
    'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
    'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)',
    'linear-gradient(135deg, #f59e0b 0%, #ec4899 100%)',
    'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
    'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)'
];

function coverFor(item: NewsItem, i: number): CSSProperties
{
    if(item.image) return { backgroundImage: `url("${item.image}")`, backgroundSize: 'cover', backgroundPosition: 'center' };
    return { background: GRADIENTS[i % GRADIENTS.length] as string };
}

/** Fallback statico (mostrato solo finché l'API non risponde). */
const FALLBACK: BentoCard[] = [
    { slug: 'welcome', cat: 'Annuncio', title: 'Il futuro arriva su Asteria.', excerpt: 'Un mondo sociale pixel-perfect, gestito dalla community italiana.', date: '—', cover: { background: GRADIENTS[0] as string }, size: 'feat' },
    { slug: 'roller-disco', cat: 'Live', title: 'Roller Disco è iniziato', excerpt: 'Pista retro in mainsquare.', date: '—', cover: { background: GRADIENTS[1] as string }, size: 'default' },
    { slug: 'comandi-italiani', cat: 'Update', title: 'Comandi italiani', excerpt: '140 comandi tradotti.', date: '—', cover: { background: GRADIENTS[2] as string }, size: 'default' },
    { slug: 'oracolo', cat: 'Bot', title: 'Oracolo è attivo', excerpt: 'Raccoglie le tue idee e bug report.', date: '—', cover: { background: GRADIENTS[3] as string }, size: 'wide' }
];

export function AsteriaNewsBento({ title = 'In evidenza', subtitle = 'Novità, eventi, sicurezza' }: { title?: string; subtitle?: string }): ReactNode
{
    const { data } = useNewsList();

    const cards: BentoCard[] = (data && data.length > 0)
        ? data.slice(0, 6).map((n, i) => ({
            slug: n.slug,
            cat: n.categoryLabel,
            title: n.title,
            excerpt: n.summary,
            date: n.date,
            cover: coverFor(n, i),
            size: SIZES[i] ?? 'default'
        }))
        : FALLBACK;

    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">{title}</h2>
                <span className="asteria-section__subtitle">{subtitle}</span>
            </div>
            <div className="asteria-bento">
                {cards.map(n => (
                    <Link key={n.slug} to={`/community/article/${n.slug}`}
                          className={`asteria-bento__card asteria-bento__card--${n.size}`}>
                        <div className="asteria-bento__cover" style={n.cover}>
                            <span className="asteria-bento__category">{n.cat}</span>
                        </div>
                        <div className="asteria-bento__body">
                            <h3 className="asteria-bento__title">{n.title}</h3>
                            <p className="asteria-bento__excerpt">{n.excerpt}</p>
                            <div className="asteria-bento__meta">{n.date}</div>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}
