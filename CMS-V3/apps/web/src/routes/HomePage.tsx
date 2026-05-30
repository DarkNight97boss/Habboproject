import type { ReactNode } from 'react';
import { Link } from 'react-router';

/**
 * Home page habbo.it style.
 * Layout: 70% main (ULTIME NOTIZIE + cards) + 30% sidebar (CONSIGLI DI SICUREZZA, GUIDA PER I GENITORI)
 */
export function HomePage(): ReactNode
{
    return (
        <div className="bg-habbo-page">
            {/* Sub-nav nera */}
            <div className="h-[40px] bg-habbo-nav-sub-bg flex items-center px-8 gap-4">
                <SubNavLink to="/" label="NOVITÀ" active />
                <Separator />
                <SubNavLink to="/messages" label="MESSAGGI" />
            </div>

            {/* Main grid */}
            <div className="container mx-auto px-4 py-6 grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-6">
                <main>
                    <h1 className="text-white text-3xl font-bold uppercase tracking-wide mb-4">Ultime notizie</h1>

                    {/* News card grande */}
                    <article className="bg-habbo-card-bg border border-habbo-card-border overflow-hidden mb-4">
                        <div className="aspect-[2.2/1] bg-habbo-blue-light/20 flex items-center justify-center text-white/40 text-xs">
                            [Hero pixel-art Habbo]
                        </div>
                        <div className="p-4">
                            <h2 className="text-white text-2xl font-bold mb-1">BENVENUTO SU HABBO</h2>
                            <p className="text-habbo-page-accent text-sm italic mb-2">
                                {new Date().toLocaleDateString('it-IT', { day: 'numeric', month: 'short', year: 'numeric' })} | Aggiornamenti
                            </p>
                            <p className="text-habbo-page-text leading-relaxed">
                                Il tuo hotel virtuale è pronto. Esplora le stanze, fai nuovi amici e personalizza il tuo Habbo.
                                Clicca su <span className="font-bold text-white">GIOCA</span> in alto a destra per iniziare!
                            </p>
                        </div>
                    </article>

                    {/* News cards più piccole — 2 colonne */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {[
                            { title: 'EVENTO: ROLLER DISCO', date: '01 mag 2026', cat: 'Campagne & Attività', text: 'La pista retrò ti sta chiamando. Entra e dai un\'occhiata!' },
                            { title: 'COMANDI ITALIANI', date: '29 mag 2026', cat: 'Aggiornamenti', text: 'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.' },
                            { title: 'BOT ORACOLO ATTIVO', date: '30 mag 2026', cat: 'Nuove funzionalità', text: 'Entra nella stanza Oracolo e proponi le tue idee per nuove feature.' },
                            { title: 'CHAT BUBBLE STYLES', date: '28 mag 2026', cat: 'Nuove funzionalità', text: '54 stili bolla chat in stile Habbo ufficiale ora disponibili.' }
                        ].map(n => (
                            <article key={n.title} className="bg-habbo-card-bg border border-habbo-card-border p-4">
                                <h3 className="text-white text-lg font-bold mb-1">{n.title}</h3>
                                <p className="text-habbo-page-accent text-xs italic mb-2">{n.date} | {n.cat}</p>
                                <p className="text-habbo-page-text text-sm leading-snug">{n.text}</p>
                            </article>
                        ))}
                    </div>
                </main>

                {/* Sidebar */}
                <aside className="space-y-4">
                    <TipBox title="CONSIGLI DI SICUREZZA">
                        Sentiti sempre al sicuro su internet! Impara come <Link to="/safety" className="underline font-bold">navigare in sicurezza</Link>.
                    </TipBox>

                    <TipBox title="GUIDA PER I GENITORI">
                        Vuoi saperne di più su come ci assicuriamo che i nostri utenti si divertano in un ambiente sicuro?
                        Guarda la nostra <Link to="/parents" className="underline font-bold">Guida per i genitori</Link>.
                    </TipBox>

                    <TipBox title="SERVE AIUTO?">
                        Impara come puoi risolvere tu stesso i problemi, o come ricevere aiuto nella sezione{' '}
                        <Link to="/help" className="underline font-bold">Segnalare un problema</Link>.
                    </TipBox>
                </aside>
            </div>
        </div>
    );
}

function SubNavLink({ to, label, active }: { to: string; label: string; active?: boolean }): ReactNode
{
    return (
        <Link
            to={to}
            className={`uppercase text-sm tracking-wide font-bold transition-colors ${
                active ? 'text-white' : 'text-habbo-nav-sub-text hover:text-white'
            }`}
        >
            {label}
        </Link>
    );
}

function Separator(): ReactNode
{
    return <span className="text-habbo-nav-sub-separator">|</span>;
}

function TipBox({ title, children }: { title: string; children: ReactNode }): ReactNode
{
    return (
        <div className="bg-habbo-sidebar-tip-bg border border-habbo-card-border">
            <div className="bg-habbo-card-header-bg text-white font-bold uppercase px-4 py-2 text-sm tracking-wide">
                {title}
            </div>
            <div className="p-4 text-white/90 text-sm leading-relaxed">
                {children}
            </div>
        </div>
    );
}
