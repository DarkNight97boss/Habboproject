import { type FormEvent, type ReactNode, useState } from 'react';
import { Navigate } from 'react-router';
import { AuthedShell, ShopTabs } from '../components/AuthedShell';
import { useAuth } from '../hooks/useAuth';

/**
 * /shop/prepagate — sub-tab "Prepagate" dello shop.
 *
 * Sull'ufficiale habbo.it: form per inserire codice prepagato + lista
 * fornitori (carte ricaricabili, abbonamenti). Habboproject usa codici
 * staff distribuiti su Discord — niente carte di credito reali.
 */
export function ShopPrepaidPage(): ReactNode
{
    const { data: user, isLoading } = useAuth();
    const [code, setCode] = useState('');
    const [msg, setMsg] = useState<string | null>(null);

    if(isLoading) return null;
    if(!user) return <Navigate to="/" replace />;

    async function redeem(ev: FormEvent<HTMLFormElement>): Promise<void>
    {
        ev.preventDefault();
        if(!code.trim()) return;
        // Backend redeem endpoint sarà aggiunto in futuro. Per ora UI demo.
        setMsg('⚠ Sistema codici in arrivo — chiedi un codice allo staff su Discord.');
    }

    return (
        <AuthedShell user={user} tabs={<ShopTabs active="prepagate" />}>
            <main className="wrapper wrapper--content">
                <header className="shop__header">
                    <h1 className="shop__header__title">Inserisci un codice prepagato</h1>
                </header>
                <section>
                    <habbo-compile className="main main--fixed">
                        <div style={{ padding: 24, color: '#fff', background: 'rgba(0,0,0,.25)', borderRadius: 5, maxWidth: 540 }}>
                            <h2 style={{ textTransform: 'uppercase', margin: '0 0 12px' }}>Codice Habboproject</h2>
                            <p style={{ margin: '0 0 16px', opacity: 0.9 }}>
                                I codici prepagati Habboproject si ottengono tramite eventi staff su Discord,
                                Twitch drops, achievement speciali. Inserisci qui il codice per riscattare.
                            </p>
                            <form onSubmit={redeem} style={{ display: 'flex', gap: 8 }}>
                                <input
                                    type="text"
                                    value={code}
                                    onChange={e => setCode(e.target.value.toUpperCase())}
                                    placeholder="HABBO-XXXX-XXXX"
                                    maxLength={32}
                                    autoCapitalize="characters"
                                    autoCorrect="off"
                                    spellCheck={false}
                                    style={{
                                        flex: 1,
                                        padding: '8px 12px',
                                        borderRadius: 5,
                                        border: '2px solid #275d8e',
                                        background: '#ccd8df',
                                        color: '#000',
                                        fontSize: 16,
                                        textTransform: 'uppercase'
                                    }}
                                />
                                <button
                                    type="submit"
                                    style={{
                                        padding: '8px 20px',
                                        background: '#00813e',
                                        color: 'white',
                                        border: '2px solid #8eda55',
                                        borderRadius: 5,
                                        fontWeight: 'bold',
                                        textTransform: 'uppercase',
                                        cursor: 'pointer'
                                    }}
                                >
                                    Riscatta
                                </button>
                            </form>
                            {msg && <p style={{ margin: '12px 0 0', padding: '8px 12px', background: 'rgba(255,255,255,.1)', borderRadius: 3 }}>{msg}</p>}
                            <h3 style={{ textTransform: 'uppercase', margin: '32px 0 8px' }}>Dove ottenerli</h3>
                            <ul style={{ paddingLeft: 20, fontSize: 14, lineHeight: 1.7 }}>
                                <li>Eventi staff settimanali (Discord <code>#eventi</code>)</li>
                                <li>Twitch drops durante le live ufficiali</li>
                                <li>Achievement speciali (vedi <a href="/community/category/aggiornamenti-su-habbo" style={{ color: '#fbd33f' }}>news</a>)</li>
                                <li>Partecipazione attiva al Forum (top contributor mensile)</li>
                            </ul>
                        </div>
                    </habbo-compile>
                    <habbo-web-pages className="aside aside--box aside--fixed">
                        <aside className="static-content">
                            <h3>Habboproject è gratis</h3>
                            <p>Niente carte di credito, niente abbonamenti. Tutto si ottiene <strong>giocando</strong>.</p>
                            <p><a href="/shop" style={{ color: '#fbd33f' }}>← Torna al catalogo</a></p>
                        </aside>
                    </habbo-web-pages>
                </section>
            </main>
        </AuthedShell>
    );
}
