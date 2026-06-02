import { type ReactNode } from 'react';
import { useAvailableSkins, useSetSkin, useSkin } from '../hooks/useSkin';
import { type SkinManifest } from '../lib/skin';

/**
 * Skin Engine picker — staff only, scelta dello skin attivo del sito.
 *
 * Card più ricca del ThemePicker v1: oltre alle palette swatches, mostra
 * tipografia/spacing/effects come "tag" così lo staff capisce il vibe del
 * tema senza doverlo provare.
 */
export function SkinPicker(): ReactNode
{
    const current = useSkin();
    const { data: skins, isLoading, error } = useAvailableSkins();
    const mutation = useSetSkin();

    function select(slug: string): void
    {
        if(slug === current?.meta.slug || mutation.isPending) return;
        mutation.mutate(slug);
    }

    if(isLoading) return <p style={{ padding: 16 }}>Caricamento skin disponibili…</p>;
    if(error) return <p style={{ padding: 16, color: 'tomato' }}>Errore: {error.message}</p>;
    if(!skins?.length) return <p style={{ padding: 16 }}>Nessuno skin disponibile.</p>;

    return (
        <div>
            <p style={{ margin: '0 0 16px', opacity: 0.85 }}>
                Scegli lo skin per <strong>tutto il sito</strong>. Cambia colori, tipografia, bordi,
                effetti, layout e asset. Tutti gli utenti vedono il nuovo aspetto entro 60 secondi.
            </p>

            {mutation.isError && (
                <div style={{ padding: 12, background: 'rgba(220,38,38,.15)', border: '1px solid #dc2626', borderRadius: 4, marginBottom: 16, color: '#fff' }}>
                    Errore: {mutation.error.message}
                </div>
            )}

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                gap: 14
            }}>
                {skins.map(s => (
                    <SkinCard
                        key={s.meta.slug}
                        skin={s}
                        active={current?.meta.slug === s.meta.slug}
                        disabled={mutation.isPending}
                        onSelect={() => select(s.meta.slug)}
                    />
                ))}
            </div>

            <p style={{ marginTop: 24, fontSize: 12, opacity: 0.7 }}>
                Tip: per testare uno skin prima di confermarlo agli utenti, attivalo, controlla, poi
                fai rollback. Il polling client lo propaga in ~60s, niente reload richiesto.
            </p>
        </div>
    );
}

function SkinCard(props: { skin: SkinManifest; active: boolean; disabled: boolean; onSelect: () => void }): ReactNode
{
    const { skin, active, disabled, onSelect } = props;
    const tags = describeSkin(skin);

    return (
        <button
            type="button"
            onClick={onSelect}
            disabled={disabled}
            style={{
                textAlign: 'left',
                padding: 14,
                background: active ? 'rgba(255,255,255,0.10)' : 'rgba(0,0,0,0.20)',
                border: active ? '2px solid var(--color-accent, #FFB900)' : '2px solid rgba(255,255,255,0.10)',
                borderRadius: 8,
                cursor: disabled ? 'wait' : (active ? 'default' : 'pointer'),
                opacity: disabled && !active ? 0.5 : 1,
                color: 'inherit',
                font: 'inherit',
                display: 'flex',
                flexDirection: 'column',
                gap: 10,
                transition: 'border-color .15s, background .15s'
            }}
            aria-pressed={active}
        >
            <div style={{ display: 'flex', gap: 4, height: 32 }}>
                {skin.meta.swatches.map((c, i) => (
                    <div key={i} style={{ flex: 1, background: c, borderRadius: 3, border: '1px solid rgba(0,0,0,.2)' }} />
                ))}
            </div>
            <div>
                <strong style={{ display: 'block', fontSize: 14 }}>
                    {skin.meta.name}
                    {active && <span style={{
                        marginLeft: 8, fontSize: 10, padding: '2px 6px',
                        background: 'var(--color-accent, #FFB900)',
                        color: 'var(--color-accent-fg, #0a3b6e)',
                        borderRadius: 3, verticalAlign: 'middle'
                    }}>ATTIVO</span>}
                </strong>
                <small style={{ display: 'block', opacity: 0.75, marginTop: 2, fontSize: 12 }}>
                    {skin.meta.description}
                </small>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                {tags.map(t => (
                    <span key={t} style={{
                        fontSize: 10, padding: '2px 8px',
                        background: 'rgba(255,255,255,.08)',
                        border: '1px solid rgba(255,255,255,.15)',
                        borderRadius: 10
                    }}>{t}</span>
                ))}
            </div>
        </button>
    );
}

/**
 * Riduce il manifest a 3-4 tag descrittivi per la card preview.
 * Es: ['Pixel font', 'Squadrato', 'Niente ombre'] per Classico 2008.
 */
function describeSkin(s: SkinManifest): string[]
{
    const tags: string[] = [];

    const font = s.tokens.typography.fontFamily;
    if(font.includes('Press Start')) tags.push('Pixel font');
    else if(font.includes('Inter')) tags.push('Sans moderno');
    else if(font.includes('system-ui')) tags.push('System font');
    else if(font.includes('Ubuntu')) tags.push('Ubuntu');

    const r = s.tokens.radius.base;
    if(r === 0) tags.push('Squadrato');
    else if(r <= 6) tags.push('Soft');
    else if(r <= 14) tags.push('Rounded');
    else tags.push('Pill');

    const shadow = s.tokens.shadow.style;
    if(shadow === 'flat')          tags.push('Flat');
    else if(shadow === 'soft')     tags.push('Ombre soft');
    else if(shadow === 'neon-glow') tags.push('Neon glow');
    else if(shadow === 'pixel')    tags.push('Pixel shadow');

    const bg = s.background?.type;
    if(bg === 'gradient') tags.push('Gradient');
    else if(bg === 'animated') tags.push('Animato');
    else if(bg === 'pattern') tags.push('Pattern');

    if(s.layout?.variant && s.layout.variant !== 'classic') tags.push(`Layout ${s.layout.variant}`);

    return tags.slice(0, 4);
}
