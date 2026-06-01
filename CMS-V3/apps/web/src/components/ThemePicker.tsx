import { type ReactNode } from 'react';
import { useTheme } from '../hooks/useTheme';
import { THEMES, type ThemeId } from '../lib/theme';

/**
 * Selettore tema con preview swatches.
 *
 * Layout: griglia di card cliccabili — una card per tema. Card mostra:
 *   - nome + descrizione
 *   - 4 swatch colorati (palette preview)
 *   - badge "Attivo" se è il tema corrente
 *
 * On click → applica tema immediatamente (no Salva button).
 */
export function ThemePicker(): ReactNode
{
    const [current, setCurrent] = useTheme();

    return (
        <div style={{ padding: 16, color: 'var(--theme-page-fg, #fff)' }}>
            <h2 style={{ textTransform: 'uppercase', margin: '0 0 8px' }}>Aspetto</h2>
            <p style={{ margin: '0 0 20px', opacity: 0.85 }}>
                Scegli come vuoi vedere il sito. Il cambio è istantaneo e si applica solo
                al tuo browser (puoi avere un tema diverso per ogni dispositivo).
            </p>

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
                        onSelect={setCurrent}
                    />
                ))}
            </div>

            <p style={{ marginTop: 20, fontSize: 12, opacity: 0.7 }}>
                Vuoi proporre un tema? Aprilo come <code>:cfh</code> al team o suggeriscilo
                allo staff. I temi più votati entrano nel pool ufficiale.
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
    onSelect: (id: ThemeId) => void;
}

function ThemeCard(props: ThemeCardProps): ReactNode
{
    const { themeId, name, description, swatches, active, onSelect } = props;
    return (
        <button
            type="button"
            onClick={() => onSelect(themeId)}
            style={{
                textAlign: 'left',
                padding: 12,
                background: active ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.20)',
                border: active ? '2px solid var(--theme-accent, #FFB900)' : '2px solid rgba(255,255,255,0.10)',
                borderRadius: 6,
                cursor: 'pointer',
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
