import { type ReactNode } from 'react';

/**
 * Bento grid news condivisa fra HomePage e MePage.
 *
 * Mock content per ora (le news vere arriveranno da /api/v2/community/news in
 * iter successivo). 6 slot con layout asimmetrico 1 feat × 5 default.
 */

const NEWS_BENTO = [
    {
        slug: 'benvenuto', cat: 'Annuncio', title: 'Il futuro arriva su Asteria.',
        excerpt: 'Un mondo sociale pixel-perfect, gestito dalla community italiana.',
        date: '01.06.2026',
        cover: 'linear-gradient(135deg, #a855f7 0%, #06b6d4 50%, #ec4899 100%)',
        size: 'feat' as const
    },
    {
        slug: 'roller-disco', cat: 'Live', title: 'Roller Disco è iniziato',
        excerpt: 'Pista retro in mainsquare. Premi per chi resta fino a chiusura.',
        date: '30.05.2026',
        cover: 'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
        size: 'default' as const
    },
    {
        slug: 'comandi', cat: 'Update', title: 'Comandi italiani',
        excerpt: '140 comandi tradotti: :bando, :tira, :spingi, :silenzia.',
        date: '29.05.2026',
        cover: 'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)',
        size: 'default' as const
    },
    {
        slug: 'oracolo', cat: 'Bot', title: 'Oracolo è attivo nelle stanze',
        excerpt: 'Raccoglie le tue idee e bug report. Scrivi :oracolo nella chat.',
        date: '28.05.2026',
        cover: 'linear-gradient(135deg, #f59e0b 0%, #ec4899 100%)',
        size: 'wide' as const
    },
    {
        slug: 'staff-mfa', cat: 'Sicurezza', title: 'MFA staff obbligatoria',
        excerpt: '2FA via authenticator app per tutti gli admin.',
        date: '20.05.2026',
        cover: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
        size: 'default' as const
    },
    {
        slug: 'nitro-v3', cat: 'Client', title: 'Nitro V3 prod',
        excerpt: 'React 19 + Vite, glass UI, hotbar custom, +50% perf.',
        date: '15.05.2026',
        cover: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)',
        size: 'default' as const
    }
];

export function AsteriaNewsBento({ title = 'In evidenza', subtitle = 'Novità, eventi, sicurezza' }: { title?: string; subtitle?: string }): ReactNode
{
    return (
        <div className="asteria-section">
            <div className="asteria-section__head">
                <h2 className="asteria-section__title">{title}</h2>
                <span className="asteria-section__subtitle">{subtitle}</span>
            </div>
            <div className="asteria-bento">
                {NEWS_BENTO.map(n => (
                    <a key={n.slug} href={`/community/article/${n.slug}`}
                       className={`asteria-bento__card asteria-bento__card--${n.size}`}>
                        <div className="asteria-bento__cover" style={{ background: n.cover }}>
                            <span className="asteria-bento__category">{n.cat}</span>
                        </div>
                        <div className="asteria-bento__body">
                            <h3 className="asteria-bento__title">{n.title}</h3>
                            <p className="asteria-bento__excerpt">{n.excerpt}</p>
                            <div className="asteria-bento__meta">{n.date}</div>
                        </div>
                    </a>
                ))}
            </div>
        </div>
    );
}
