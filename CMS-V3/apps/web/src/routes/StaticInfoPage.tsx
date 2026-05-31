import { type ReactNode } from 'react';
import { useParams } from 'react-router';
import { AuthedShell } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';
import { NotFoundPage } from './NotFoundPage';

/**
 * Pagine statiche informative — replica il layout
 * `<main.wrapper--content><section><div.main--fixed/><aside/></section>`
 * delle pagine 2-col di habbo.it (es. T&C, privacy, sicurezza).
 *
 * Contenuti CUSTOM Habboproject: i T&C/privacy/cookies di Sulake non
 * sono copiabili (sono testi legali proprietari). Scriviamo noi i nostri,
 * brevi e nello spirito community-friendly.
 *
 * Route: /playing-habbo/:slug, /help/:slug
 * Lookup in PAGES; se slug non trovato → NotFoundPage.
 */

interface StaticPage
{
    title: string;
    introHtml: string;
    bodyHtml: string;
}

const PAGES: Record<string, StaticPage> = {
    // ===== /playing-habbo/* =====
    'safety': {
        title: 'Sicurezza su Habboproject',
        introHtml: '<p>Habboproject è un\'esperienza community-driven. Per goderla in sicurezza, segui queste regole d\'oro.</p>',
        bodyHtml: `
            <h2>Le regole d'oro</h2>
            <ul>
                <li><strong>Non condividere mai la tua password</strong> con nessuno — nemmeno con chi dice di essere staff. Lo staff non chiede MAI la password.</li>
                <li><strong>Non cliccare link sospetti</strong> in chat. Il nostro filtro anti-link blocca URL da utenti non-staff, ma stai sempre all'erta.</li>
                <li><strong>Non rivelare dati personali</strong> (telefono, indirizzo, scuola, foto reali). Habbo è anonimo per design.</li>
                <li><strong>Segnala abusi</strong> con il bottone "Supporto al Giocatore" in basso a sinistra del client, o usa <code>:cfh</code> in chat.</li>
                <li><strong>Rispetta gli altri</strong>: insulti razziali, sessuali, di odio = ban permanente immediato.</li>
            </ul>
            <h2>Tutele tecniche attive</h2>
            <ul>
                <li>JWT httpOnly + refresh rotation con reuse detection</li>
                <li>bcrypt cost 12 sulle password + check HIBP al registro</li>
                <li>Rate limiting su login (5/min) e registrazione (3/ora)</li>
                <li>Anti-link obfuscation server-side (blocca <code>g o o g l e . i t</code>)</li>
                <li>MFA Google Authenticator obbligatorio per ruoli staff</li>
                <li>Audit log HMAC di tutte le azioni sensibili</li>
            </ul>
            <p><a href="mailto:advertising@habboproject"><strong>Contatta lo staff per qualsiasi dubbio.</strong></a></p>
        `
    },
    'terms-of-service': {
        title: 'Termini e Condizioni',
        introHtml: '<p>Usando Habboproject accetti le seguenti condizioni. Le abbiamo scritte in italiano normale, non in legalese.</p>',
        bodyHtml: `
            <h2>Cos'è Habboproject</h2>
            <p>Habboproject è un server retro non ufficiale di Habbo Hotel, gestito da una community
               italiana di appassionati a scopo educativo e di intrattenimento. Non siamo
               affiliati a Sulake Oy. HABBO® è un marchio registrato di Sulake.</p>
            <h2>Cosa puoi fare</h2>
            <ul>
                <li>Creare un account gratis e giocare</li>
                <li>Creare stanze, fare amicizie, partecipare a eventi</li>
                <li>Guadagnare crediti/diamanti giocando (Daily Streak, achievements)</li>
                <li>Segnalare bug e proporre feature (Oracolo)</li>
            </ul>
            <h2>Cosa NON puoi fare</h2>
            <ul>
                <li>Multi-account (1 utente = 1 account; il fingerprint browser blocca i casi più comuni)</li>
                <li>Hack/exploit/cheat — ban permanente + ip-ban</li>
                <li>Bestemmie, insulti razziali/sessuali/di odio</li>
                <li>Spam, scam, phishing, link malevoli</li>
                <li>Vendere/scambiare account o oggetti per soldi reali</li>
                <li>Impersonare staff o altri utenti</li>
            </ul>
            <h2>Sospensione e ban</h2>
            <p>Lo staff può sospendere/bannare un account in qualsiasi momento per violazione di queste
               regole. Le decisioni sono inappellabili ma puoi sempre contattarci per fare appello via
               <code>:cfh</code> o email.</p>
            <h2>Disclaimer</h2>
            <p>Habboproject è fornito "as-is". Non garantiamo uptime 24/7 né integrità eterna dei dati.
               Faremo del nostro meglio. Non siamo responsabili per dispute fra utenti.</p>
        `
    },
    'privacy-notice': {
        title: 'Politica sulla Privacy',
        introHtml: '<p>Habboproject raccoglie il minimo indispensabile per farti giocare. Niente tracking di terze parti, niente analytics aggressive.</p>',
        bodyHtml: `
            <h2>Dati che raccogliamo</h2>
            <ul>
                <li><strong>Username e password</strong> (hash bcrypt, mai in chiaro)</li>
                <li><strong>Email</strong> (opzionale, per recupero password — NO marketing)</li>
                <li><strong>Data di nascita</strong> (per check &ge;16 anni)</li>
                <li><strong>IP address</strong> (per anti-abuse, audit log, fraud detection)</li>
                <li><strong>User-agent + fingerprint browser</strong> (anti-multiaccount)</li>
                <li><strong>Game data</strong> (look, motto, crediti, stanze create, foto scattate, messaggi)</li>
            </ul>
            <h2>Dove vivono i dati</h2>
            <p>MariaDB su server self-hosted in Italia. Backup giornalieri criptati.
               Niente cloud Google/AWS/Azure per il database principale.</p>
            <h2>Chi può vedere cosa</h2>
            <ul>
                <li>Tu vedi i tuoi dati nel pannello /settings (in arrivo)</li>
                <li>Lo staff vede log moderazione + IP per anti-abuse</li>
                <li>Audit log staff con HMAC firma (no tampering, traceability)</li>
                <li><strong>Mai venduti</strong> a terze parti</li>
            </ul>
            <h2>I tuoi diritti GDPR</h2>
            <ul>
                <li>Accesso ai tuoi dati: <code>:dati</code> in chat o email</li>
                <li>Cancellazione account: <code>:elimina-account</code> (purge 30gg)</li>
                <li>Export dati: scaricabile dal pannello /settings</li>
                <li>Rettifica: aggiorna direttamente dal client/pannello</li>
            </ul>
            <p><strong>Contatto DPO:</strong> <a href="mailto:advertising@habboproject">advertising@habboproject</a></p>
        `
    },
    'dsa': {
        title: 'Regolamento sui Servizi Digitali (DSA)',
        introHtml: '<p>Habboproject è soggetto al Regolamento UE 2022/2065 sui Servizi Digitali. Ecco come ci adeguiamo.</p>',
        bodyHtml: `
            <h2>Punto di contatto unico</h2>
            <p>Per autorità competenti, utenti, ricercatori — un solo indirizzo:
               <a href="mailto:advertising@habboproject">advertising@habboproject</a></p>
            <h2>Rappresentante legale UE</h2>
            <p>Habboproject è gestito interamente da volontari nell'Unione Europea (Italia).
               Non opera al di fuori dell'UE.</p>
            <h2>Trasparency report</h2>
            <p>Pubblichiamo annualmente:</p>
            <ul>
                <li>Numero di segnalazioni utenti ricevute (CFH)</li>
                <li>Numero di sanzioni applicate (mute / kick / ban)</li>
                <li>Tempi medi di risposta dello staff</li>
                <li>Decisioni di moderazione automatica vs umana</li>
            </ul>
            <h2>Sistema interno di gestione reclami</h2>
            <p>Ogni decisione di moderazione può essere appellata via <code>:cfh-appello</code> in chat
               entro 30 giorni. L'appello viene esaminato da uno staff diverso da chi ha preso la
               decisione originale.</p>
        `
    },
    // ===== /help/* =====
    'parents': {
        title: 'Guida per i Genitori',
        introHtml: '<p>Vostro figlio gioca su Habboproject? Ecco cosa dovete sapere.</p>',
        bodyHtml: `
            <h2>Cos'è Habboproject</h2>
            <p>Una community italiana ispirata al gioco "Habbo Hotel" — un mondo virtuale dove
               i ragazzi creano avatar pixel-art, costruiscono stanze e socializzano in chat.</p>
            <h2>Età minima</h2>
            <p><strong>16 anni</strong> (verifica al registro). Niente eccezioni.</p>
            <h2>Cosa monitoriamo</h2>
            <ul>
                <li>Chat tossica / insulti — auto-moderazione + staff manuale</li>
                <li>Link a siti esterni — bloccati per non-staff (anti-phishing)</li>
                <li>Tentativi di adescamento — segnaliamo immediatamente alle autorità</li>
                <li>Dati personali condivisi — il sistema avvisa e blocca</li>
            </ul>
            <h2>Cosa NON facciamo</h2>
            <ul>
                <li>Tracking pubblicitario di vostro figlio</li>
                <li>Vendita dati a terzi</li>
                <li>Microtransazioni con soldi reali — il gioco è gratuito al 100%</li>
                <li>Loot box, gambling, meccaniche addictive</li>
            </ul>
            <h2>Come potete intervenire</h2>
            <p>Se vedete comportamenti sospetti contattateci a
               <a href="mailto:advertising@habboproject">advertising@habboproject</a>.
               Risposta entro 24h.</p>
            <p>Per cancellare l'account di vostro figlio: stesso indirizzo, allegate documento
               d'identità che provi la tutela legale.</p>
        `
    },
    'cookies': {
        title: 'Politica sui Cookies',
        introHtml: '<p>Usiamo pochi cookie, tutti tecnici. Niente tracking di terze parti.</p>',
        bodyHtml: `
            <h2>Cookie che usiamo</h2>
            <table>
                <thead><tr><th>Nome</th><th>Tipo</th><th>Durata</th><th>Scopo</th></tr></thead>
                <tbody>
                    <tr><td><code>cms_v3_access</code></td><td>Strettamente necessario</td><td>15 min</td><td>Sessione utente (JWT httpOnly)</td></tr>
                    <tr><td><code>cms_v3_refresh</code></td><td>Strettamente necessario</td><td>14 giorni</td><td>Refresh token rotation</td></tr>
                </tbody>
            </table>
            <p>Tutti i cookie sono <code>HttpOnly + SameSite=Lax</code> (più <code>Secure</code> in production).
               <strong>Non condivisi con terze parti.</strong></p>
            <h2>Cookie che NON usiamo</h2>
            <ul>
                <li>❌ Google Analytics</li>
                <li>❌ Facebook Pixel</li>
                <li>❌ Pubblicità retargeting</li>
                <li>❌ Cookie cross-site di marketing</li>
            </ul>
            <h2>Come gestirli</h2>
            <p>I cookie di sessione vengono cancellati al logout. Per cancellarli manualmente: impostazioni
               del browser → privacy → cancella dati per <code>127.0.0.1</code>.</p>
        `
    }
};

export function StaticInfoPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const params = useParams<{ slug: string }>();
    const slug = params.slug ?? '';
    const page = PAGES[slug];

    if(isLoading) return null;
    if(!page) return <NotFoundPage />;

    const body = (
        <main className="wrapper wrapper--content">
            <section>
                <habbo-compile className="main main--fixed">
                    <article>
                        <header className="news-header news-header--single">
                            <h1 className="news-header__wrapper news-header__title">{page.title}</h1>
                        </header>
                        <div
                            className="news-article"
                            dangerouslySetInnerHTML={{ __html: `${page.introHtml}${page.bodyHtml}` }}
                        />
                    </article>
                </habbo-compile>
                <habbo-web-pages className="aside aside--box aside--fixed">
                    <aside className="static-content">
                        <h3>Hai bisogno d'aiuto?</h3>
                        <p>Contatta lo staff via <a href="mailto:advertising@habboproject">email</a> o usa <code>:cfh</code> nel client.</p>
                    </aside>
                </habbo-web-pages>
            </section>
        </main>
    );

    if(user) return <AuthedShell user={user}>{body}</AuthedShell>;

    // Fallback per anonimi (le pagine legali sono pubbliche).
    return (
        <>
            <div className="content">
                <header className="header__wrapper wrapper">
                    <a href="/" className="header__habbo__logo">
                        <h1 className="header__habbo__name">Habbo</h1>
                    </a>
                </header>
                {body}
            </div>
        </>
    );
}
