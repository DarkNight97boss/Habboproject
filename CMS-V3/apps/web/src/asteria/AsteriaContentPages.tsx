import { type ReactNode, useState } from 'react';
import { Link, useParams } from 'react-router';
import { AsteriaShell, type AsteriaNavId } from './AsteriaShell';
import { useNotifications } from '../hooks/useAuth';
import './asteria.css';

/* ============================================================
   Layout di contenuto riusabile (guida, help, collezionabili, legal)
   ============================================================ */
interface ContentSection { icon: string; title: string; body: ReactNode; }

function AsteriaContentPage({ activeNav = 'guida', eyebrow, title, intro, sections, children }: {
    activeNav?: AsteriaNavId; eyebrow: string; title: string; intro?: string; sections?: ContentSection[]; children?: ReactNode;
}): ReactNode
{
    return (
        <AsteriaShell activeNav={activeNav}>
            <section className="asteria-content">
                <header className="asteria-content__head">
                    <div className="asteria-hero__eyebrow">{eyebrow}</div>
                    <h1 className="asteria-content__title">{title}</h1>
                    {intro && <p className="asteria-content__intro">{intro}</p>}
                </header>
                {sections && (
                    <div className="asteria-content__grid">
                        {sections.map(s => (
                            <article key={s.title} className="asteria-content__card">
                                <div className="asteria-content__icon">{s.icon}</div>
                                <h3 className="asteria-content__card-title">{s.title}</h3>
                                <div className="asteria-content__card-body">{s.body}</div>
                            </article>
                        ))}
                    </div>
                )}
                {children}
            </section>
        </AsteriaShell>
    );
}

function PlayCta(): ReactNode
{
    return (
        <div className="asteria-content__cta">
            <a href="/gioca" className="asteria-btn asteria-btn--xl">▶ Entra nel hotel</a>
            <Link to="/registration" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Crea account</Link>
        </div>
    );
}

/* ============================================================  GUIDA  */
export function AsteriaGuidePage(): ReactNode
{
    const sections: ContentSection[] = [
        { icon: '🏨', title: 'Cos\'è Asteria', body: 'Un hotel virtuale social in pixel-art: crei un avatar, esplori stanze, chatti e costruisci.' },
        { icon: '👤', title: 'Avatar', body: 'Personalizza look, vestiti e accessori. Tutto gratis e modificabile quando vuoi.' },
        { icon: '🏠', title: 'Stanze', body: 'Visita le stanze pubbliche o costruisci la tua con il furni del catalogo.' },
        { icon: '💬', title: 'Chat & comandi', body: <>140+ comandi in italiano. Leggi <Link to="/community/article/comandi-italiani">la guida comandi</Link>.</> },
        { icon: '🤝', title: 'Social', body: 'Aggiungi amici, crea gruppi, partecipa agli eventi staff.' },
        { icon: '📸', title: 'Fotocamera', body: <>Scatta foto nelle stanze e condividile nella <Link to="/community/photos">galleria</Link>.</> }
    ];
    return (
        <AsteriaContentPage eyebrow="Playing Asteria" title="Come si gioca"
            intro="Tutto quello che ti serve per iniziare nel tuo hotel virtuale." sections={sections}>
            <PlayCta />
        </AsteriaContentPage>
    );
}

/* ============================================================  HELP  */
export function AsteriaHelpPage(): ReactNode
{
    const sections: ContentSection[] = [
        { icon: '🚀', title: 'Inizio rapido', body: <>Crea un account, scegli il look e premi GIOCA. Serve aiuto? <Link to="/playing-habbo">Leggi la guida</Link>.</> },
        { icon: '🔐', title: 'Account', body: <>Gestisci <Link to="/settings/password">password</Link>, <Link to="/settings/email">email</Link> e <Link to="/settings/privacy">privacy</Link>.</> },
        { icon: '💰', title: 'Crediti & valuta', body: <>Crediti, diamanti e duckets si usano per furni e VIP. Comprali nello <Link to="/shop">shop</Link>.</> },
        { icon: '🛡️', title: 'Sicurezza', body: <>Non condividere mai la password. Segnala abusi con <code>:cfh</code> in chat. <Link to="/playing-habbo/safety">Consigli</Link>.</> },
        { icon: '👨‍👩‍👧', title: 'Genitori', body: <>Informazioni per le famiglie nella <Link to="/help/parents">guida genitori</Link>.</> },
        { icon: '🐛', title: 'Bug & idee', body: <>Proponi feature al bot <Link to="/community/article/oracolo">Oracolo</Link> in gioco.</> }
    ];
    return <AsteriaContentPage activeNav="guida" eyebrow="Aiuto" title="Centro assistenza" intro="Domande frequenti e supporto." sections={sections} />;
}

/* ============================================================  COLLEZIONABILI  */
export function AsteriaCollectiblesPage(): ReactNode
{
    const sections: ContentSection[] = [
        { icon: '🎴', title: 'Rari & LTD', body: 'Furni in edizione limitata. Niente blockchain: solo pixel rari salvati nel DB.' },
        { icon: '🏅', title: 'Badge', body: 'Sblocca badge per eventi, achievement e traguardi. Esponili sul profilo.' },
        { icon: '🌸', title: 'Furni del catalogo', body: <>Migliaia di mobili per arredare. Sfoglia lo <Link to="/shop">shop</Link>.</> },
        { icon: '📊', title: 'Trasparenza', body: 'Le quantità dei rari sono pubbliche. Nessun drop nascosto, nessun pay-to-win.' }
    ];
    return (
        <AsteriaContentPage activeNav="collezionabili" eyebrow="Collezionabili" title="Rari, badge & furni"
            intro="Gli oggetti più ambiti di Asteria — collezionali in gioco." sections={sections}>
            <PlayCta />
        </AsteriaContentPage>
    );
}

/* ============================================================  LEGAL / STATIC INFO  */
const STATIC_PAGES: Record<string, { eyebrow: string; title: string; body: ReactNode }> = {
    'safety': { eyebrow: 'Sicurezza', title: 'Sicurezza su Asteria', body: <><p>La tua sicurezza viene prima di tutto. Non condividere mai password o dati personali. Lo staff non te li chiederà mai.</p><p>Usa <code>:cfh &lt;messaggio&gt;</code> per chiamare un moderatore. Blocca e ignora chi ti infastidisce.</p></> },
    'terms-of-service': { eyebrow: 'Legale', title: 'Termini e condizioni', body: <p>Usando Asteria accetti di rispettare le regole della community: niente comportamenti tossici, scam o cheating. Lo staff può sospendere gli account che violano le regole.</p> },
    'privacy-notice': { eyebrow: 'Legale', title: 'Privacy', body: <p>Conserviamo solo i dati necessari al funzionamento del gioco (account, email, log di moderazione). Non vendiamo i tuoi dati. Puoi richiederne la cancellazione contattando lo staff.</p> },
    'dsa': { eyebrow: 'Legale', title: 'Digital Services Act', body: <p>In conformità al DSA UE, puoi segnalare contenuti illegali allo staff. Le decisioni di moderazione sono motivate e contestabili.</p> },
    'parents': { eyebrow: 'Famiglie', title: 'Guida per i genitori', body: <p>Asteria è una community moderata. Consigliamo ai genitori di parlare con i ragazzi della sicurezza online: password segrete, attenzione agli sconosciuti, segnalazione degli abusi.</p> },
    'cookies': { eyebrow: 'Legale', title: 'Cookie', body: <p>Usiamo solo cookie tecnici necessari al login e alla sessione di gioco. Nessun cookie di tracciamento pubblicitario.</p> }
};

export function AsteriaStaticInfoPage(): ReactNode
{
    const params = useParams<{ slug: string }>();
    const page = STATIC_PAGES[params.slug ?? ''];
    if(!page)
    {
        return (
            <AsteriaShell activeNav="guida">
                <section className="asteria-article asteria-article--missing">
                    <div className="asteria-hero__eyebrow">Info</div>
                    <h1 className="asteria-article__title">Pagina non trovata</h1>
                    <div className="asteria-article__back"><Link to="/help" className="asteria-btn asteria-btn--ghost">← Centro assistenza</Link></div>
                </section>
            </AsteriaShell>
        );
    }
    return (
        <AsteriaShell activeNav="guida">
            <article className="asteria-article">
                <div className="asteria-hero__eyebrow">{page.eyebrow}</div>
                <h1 className="asteria-article__title">{page.title}</h1>
                <div className="asteria-article__body">{page.body}</div>
                <div className="asteria-article__back"><Link to="/help" className="asteria-btn asteria-btn--ghost">← Centro assistenza</Link></div>
            </article>
        </AsteriaShell>
    );
}

/* ============================================================  MESSAGGI  */
export function AsteriaMessagingPage(): ReactNode
{
    const { data: notif } = useNotifications(true);
    return (
        <AsteriaContentPage activeNav="home" eyebrow="Inbox" title="Messaggi"
            intro="La messaggistica privata sul web arriverà presto. Per ora usa il messenger in gioco.">
            <div className="asteria-emptybox">
                <div className="asteria-emptybox__icon">✉️</div>
                <p>{notif && notif.unreadMessages > 0
                    ? <>Hai <strong>{notif.unreadMessages}</strong> messaggi non letti nel client.</>
                    : 'Nessun nuovo messaggio.'}</p>
                <a href="/gioca" className="asteria-btn">Apri il messenger in gioco</a>
            </div>
        </AsteriaContentPage>
    );
}

/* ============================================================  FORUM  */
export function AsteriaForumPage(): ReactNode
{
    return (
        <AsteriaContentPage activeNav="community" eyebrow="Community · Forum" title="Forum"
            intro="Il forum dei gruppi e delle discussioni è in arrivo.">
            <div className="asteria-emptybox">
                <div className="asteria-emptybox__icon">💬</div>
                <p>Per ora le discussioni vivono in gioco e su Discord. Il forum web sarà la prossima feature community.</p>
                <Link to="/community/category/all" className="asteria-btn asteria-btn--ghost">Leggi le news</Link>
            </div>
        </AsteriaContentPage>
    );
}

/* ============================================================  PREPAGATE  */
export function AsteriaPrepaidPage(): ReactNode
{
    const [code, setCode] = useState('');
    const [msg, setMsg] = useState<string | null>(null);
    return (
        <AsteriaContentPage activeNav="shop" eyebrow="Shop · Prepagate" title="Riscatta un codice"
            intro="Hai un codice prepagato da un evento o un drop? Inseriscilo qui.">
            <form className="asteria-login" style={{ margin: '0 auto', maxWidth: 460 }}
                  onSubmit={e => { e.preventDefault(); setMsg('Il riscatto codici arriverà a breve — per ora i pacchetti si acquistano nello Shop.'); }}>
                <input className="asteria-input" type="text" placeholder="ASTERIA-XXXX-XXXX" value={code}
                       maxLength={32} onChange={e => setCode(e.target.value.toUpperCase())} style={{ textTransform: 'uppercase', textAlign: 'center', letterSpacing: '0.1em' }} />
                <button type="submit" className="asteria-btn" disabled={code.length < 4}>Riscatta</button>
                {msg && <p className="asteria-form__hint" style={{ textAlign: 'center' }}>{msg}</p>}
            </form>
            <div className="asteria-content__cta"><Link to="/shop" className="asteria-btn asteria-btn--ghost">Vai allo shop</Link></div>
        </AsteriaContentPage>
    );
}
