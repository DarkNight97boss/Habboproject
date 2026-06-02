import { type ReactNode } from 'react';
import { AsteriaShell, type AsteriaNavId } from './AsteriaShell';
import { AsteriaNewsBento } from './AsteriaNewsBento';
import './asteria.css';

/**
 * AsteriaPlaceholderPage — fallback Asteria-style per route non ancora
 * portate al design proprietario (community, profile, settings, messaging,
 * playing-habbo, help, collezionabili).
 *
 * Renderizza shell completo + sezione "in arrivo" con CTA + news bento.
 * Importante: NESSUNA route deve mostrare il tema habbo quando skin=
 * asteria-nebula. Anche pagine non-ancora-portate devono restare coerenti.
 */
export function AsteriaPlaceholderPage({
    title, subtitle, eyebrow = 'Coming soon', activeNav = 'home'
}: {
    title: string;
    subtitle?: string;
    eyebrow?: string;
    activeNav?: AsteriaNavId;
}): ReactNode
{
    return (
        <AsteriaShell activeNav={activeNav}>
            <section className="asteria-hero" style={{ paddingBottom: 40 }}>
                <div className="asteria-hero__eyebrow">{eyebrow}</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(48px, 10vw, 140px)' }}>
                    {title}
                </h1>
                {subtitle && (
                    <p className="asteria-hero__tagline">{subtitle}</p>
                )}
                <div className="asteria-hero__cta">
                    <a href="/" className="asteria-btn asteria-btn--xl">← Torna alla home</a>
                    <a href="/shop" className="asteria-btn asteria-btn--ghost asteria-btn--xl">Vai allo shop</a>
                </div>
            </section>
            <AsteriaNewsBento title="Intanto leggi le ultime" subtitle="Aggiornamenti dal team" />
        </AsteriaShell>
    );
}
