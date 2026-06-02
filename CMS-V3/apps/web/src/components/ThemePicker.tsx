import { type ReactNode } from 'react';
import { useSetTheme, useTheme } from '../hooks/useTheme';
import { THEMES, type ThemeId } from '../lib/theme';

/**
 * Selettore tema SITO (staff only — usato in StaffPanelPage /admin/appearance).
 *
 * Layout: griglia card cliccabili con preview swatch.
 * Comportamento: click → mutation PUT /api/v2/site/theme → invalida cache →
 * tutti i visitatori vedono il nuovo tema al prossimo poll (max ~60s) o
 * quando re-focusano la tab.
 *
 * NB: il backend (rank >= 5) è la vera enforcement; questo componente è
 * SOLO renderizzato dentro StaffPanelPage che è già protetto.
 */
export function ThemePicker(): ReactNode
{
    const current = useTheme();
    const mutation = useSetTheme();

    function select(themeId: ThemeId): void
    {
        if(themeId === current || mutation.isPending) return;
        mutation.mutate(themeId);
    }

    return (
        <div>
            <p style={{ margin: '0 0 16px', opacity: 0.85 }}>
                Scegli il tema per <strong>tutto il sito</strong>. Tutti gli utenti vedranno il nuovo
                aspetto entro 60 secondi (o al loro prossimo refresh).
            </p>

            {mutation.isError && (
                <div style={{ padding: 12, background: 'rgba(220, 38, 38, 0.15)', border: '1px solid #dc2626', borderRadius: 4, marginBottom: 16, color: '#fff' }}>
                    Errore: {mutation.error.message}
                </div>
            )}

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                gap: 12
            }}>
                {THEMES.map(t => (
                    <ThemeCard
                        key={t.id}
                        themeId={t.id}
                        name={t.name}
                        description={t.description}
                        swatches={t.swatches}
                        active={current === t.id}
                        disabled={mutation.isPending}
                        onSelect={select}
                    />
                ))}
            </div>

            <p style={{ marginTop: 20, fontSize: 12, opacity: 0.7 }}>
                Il cambio si applica subito sul tuo browser e si propaga a tutti gli altri
                visitatori entro ~1 minuto (cache CDN + polling client). Per testare un tema
                prima di confermare, fai il rollback subito dopo.
            </p>
        </div>
    );
}

interface ThemeCardProps {
    themeId: ThemeId;
    name: string;
    description: string;
    swatches: readonly [string, string, string, string];
    active: boolean;
    disabled: boolean;
    onSelect: (id: ThemeId) => void;
}

function ThemeCard(props: ThemeCardProps): ReactNode
{
    const { themeId, name, description, swatches, active, disabled, onSelect } = props;
    return (
        <button
            type="button"
            onClick={() => onSelect(themeId)}
            disabled={disabled}
            style={{
                textAlign: 'left',
                padding: 12,
                background: active ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.20)',
                border: active ? '2px solid var(--theme-accent, #FFB900)' : '2px solid rgba(255,255,255,0.10)',
                borderRadius: 6,
                cursor: disabled ? 'wait' : (active ? 'default' : 'pointer'),
                opacity: disabled && !active ? 0.5 : 1,
                color: 'inherit',
                font: 'inherit',
                display: 'flex',
                flexDirection: 'column',
                gap: 8,
                transition: 'border-color 0.15s, background 0.15s'
            }}
            aria-pressed={active}
        >
            <div style={{ display: 'flex', gap: 4, height: 32 }}>
                {swatches.map((c, i) => (
                    <div key={i} style={{
                        flex: 1,
                        background: c,
                        borderRadius: 3,
                        border: '1px solid rgba(0,0,0,0.20)'
                    }} />
                ))}
            </div>
            <div>
                <strong style={{ display: 'block', fontSize: 14 }}>
                    {name}
                    {active && <span style={{
                        marginLeft: 8,
                        fontSize: 10,
                        padding: '2px 6px',
                        background: 'var(--theme-accent, #FFB900)',
                        color: 'var(--theme-accent-fg, #0a3b6e)',
                        borderRadius: 3,
                        verticalAlign: 'middle'
                    }}>ATTIVO</span>}
                </strong>
                <small style={{ display: 'block', opacity: 0.75, marginTop: 2, fontSize: 12 }}>
                    {description}
                </small>
            </div>
        </button>
    );
}
