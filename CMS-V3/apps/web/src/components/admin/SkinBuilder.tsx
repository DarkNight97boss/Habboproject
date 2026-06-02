/**
 * SkinBuilder — editor visuale di temi (Fase 19).
 *
 * Sezione admin (/admin/skinbuilder) indipendente dallo skin del sito.
 * Permette allo staff di:
 *   • clonare uno skin esistente come punto di partenza
 *   • modificare ogni token del manifest (colori, tipografia, spaziatura,
 *     raggi, ombre, animazioni, sfondo) con anteprima LIVE
 *   • salvare come nuova custom skin (POST /api/v2/skins) o aggiornarne una
 *     (PATCH /api/v2/skins/:id), eliminarla (DELETE) e attivarla (PUT site/skin)
 *
 * L'anteprima è una mini-pagina scoped con stili inline derivati dal draft:
 * NON applica lo skin globalmente, così il pannello admin resta intatto.
 */
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type CSSProperties, type ReactNode } from 'react';
import type { SkinManifest } from '../../lib/skin';

// ---------------------------------------------------------------------------
//  API
// ---------------------------------------------------------------------------
interface SkinEntry { id: number; slug: string; name: string; description: string; isBuiltin: boolean; isCustom: boolean }
interface SkinListResponse { skins: SkinManifest[]; entries: SkinEntry[] }

async function fetchSkins(): Promise<SkinListResponse>
{
    const r = await fetch(`/api/v2/skins?_=${Date.now()}`, { credentials: 'include', cache: 'no-store' });
    if(!r.ok) throw new Error(`skins_${r.status}`);
    return (await r.json()) as SkinListResponse;
}

async function saveSkin(manifest: SkinManifest, editingId: number | null): Promise<{ id?: number; slug: string }>
{
    const url = editingId ? `/api/v2/skins/${editingId}` : '/api/v2/skins';
    const r = await fetch(url, {
        method: editingId ? 'PATCH' : 'POST',
        credentials: 'include',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ manifest })
    });
    if(!r.ok)
    {
        const e = await r.json().catch(() => ({})) as { error?: string };
        throw new Error(e.error || `save_${r.status}`);
    }
    return (await r.json()) as { id?: number; slug: string };
}

async function deleteSkin(id: number): Promise<void>
{
    const r = await fetch(`/api/v2/skins/${id}`, { method: 'DELETE', credentials: 'include' });
    if(!r.ok)
    {
        const e = await r.json().catch(() => ({})) as { error?: string };
        throw new Error(e.error || `delete_${r.status}`);
    }
}

async function activateSkin(slug: string): Promise<void>
{
    const r = await fetch('/api/v2/site/skin', {
        method: 'PUT', credentials: 'include',
        headers: { 'content-type': 'application/json' }, body: JSON.stringify({ slug })
    });
    if(!r.ok) throw new Error(`activate_${r.status}`);
}

// ---------------------------------------------------------------------------
//  Default manifest (per "nuova da zero")
// ---------------------------------------------------------------------------
const DEFAULT_MANIFEST: SkinManifest = {
    version: 1,
    meta: { slug: '', name: 'Nuova skin', description: '', swatches: [ '#6366f1', '#22d3ee', '#0b0f17', '#f3f7fc' ] },
    tokens: {
        colors: {
            'page-bg': '#0b0f17', 'page-fg': '#e6edf6', 'header-bg': '#101622', 'header-fg': '#ffffff',
            'card-bg': '#151c28', 'card-border': '#283447', 'card-header-bg': '#1e2738', 'card-header-fg': '#f3f7fc',
            'accent': '#6366f1', 'link': '#22d3ee', 'link-hover': '#67e8f9',
            'button-bg': '#6366f1', 'button-fg': '#ffffff', 'button-border': '#6366f1',
            'success': '#10b981', 'warning': '#f59e0b', 'error': '#f43f5e', 'text-muted': '#8b9bb4'
        },
        typography: {
            fontFamily: '"Inter", system-ui, sans-serif',
            fontFamilyHeading: '"Inter", system-ui, sans-serif',
            sizeScale: 1, weightHeading: 700, letterSpacingHeading: '0'
        },
        spacing: { unit: 8, densityScale: 1 },
        radius: { base: 12, button: 10, input: 8 },
        shadow: { style: 'soft', card: '0 4px 16px rgba(0,0,0,0.3)', button: '0 2px 8px rgba(99,102,241,0.4)' },
        animation: { speed: 'normal', easing: 'ease-out' }
    },
    background: { type: 'solid', value: '#0b0f17' }
};

const clone = (m: SkinManifest): SkinManifest => JSON.parse(JSON.stringify(m)) as SkinManifest;

/** Setta una prop stringa OPZIONALE: scrive se non vuota, altrimenti rimuove
 *  la chiave (necessario con exactOptionalPropertyTypes + validazione min(1)). */
function setOptional(obj: Record<string, unknown>, key: string, v: string): void
{
    const t = v.trim();
    if(t) obj[key] = t;
    else delete obj[key];
}

// ---------------------------------------------------------------------------
//  Field helpers
// ---------------------------------------------------------------------------
function Field({ label, children, hint }: { label: string; children: ReactNode; hint?: string }): ReactNode
{
    return (
        <label className="admin-field">
            <span className="admin-field__label">{label}{hint ? <em className="sb-hint"> · {hint}</em> : null}</span>
            {children}
        </label>
    );
}

function Text({ label, value, onChange, placeholder, mono }: { label: string; value: string; onChange: (v: string) => void; placeholder?: string; mono?: boolean }): ReactNode
{
    return (
        <Field label={label}>
            <input className="admin-input" style={mono ? { fontFamily: 'var(--admin-font-mono)' } : undefined}
                value={value} placeholder={placeholder} onChange={e => onChange(e.target.value)} />
        </Field>
    );
}

function Num({ label, value, min, max, step, onChange, suffix }: { label: string; value: number; min: number; max: number; step: number; onChange: (n: number) => void; suffix?: string }): ReactNode
{
    return (
        <Field label={`${label}: ${value}${suffix ?? ''}`}>
            <input type="range" className="sb-range" min={min} max={max} step={step} value={value}
                onChange={e => onChange(Number(e.target.value))} />
        </Field>
    );
}

function Select({ label, value, options, onChange }: { label: string; value: string; options: readonly { value: string; label: string }[]; onChange: (v: string) => void }): ReactNode
{
    return (
        <Field label={label}>
            <select className="admin-select" value={value} onChange={e => onChange(e.target.value)}>
                {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
        </Field>
    );
}

const isHex = (v: string): boolean => /^#[0-9a-fA-F]{3,8}$/.test(v.trim());

function Color({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }): ReactNode
{
    return (
        <div className="sb-color">
            <input type="color" className="sb-color__pick" value={isHex(value) ? value.slice(0, 7) : '#000000'}
                onChange={e => onChange(e.target.value)} title={label} aria-label={label} />
            <div className="sb-color__body">
                <span className="sb-color__label">{label}</span>
                <input className="admin-input sb-color__text" value={value} onChange={e => onChange(e.target.value)} />
            </div>
        </div>
    );
}

// ---------------------------------------------------------------------------
//  Live preview (scoped, inline styles dal draft)
// ---------------------------------------------------------------------------
function Preview({ m }: { m: SkinManifest }): ReactNode
{
    const c = m.tokens.colors;
    const bg = m.background && (m.background.type === 'gradient' || m.background.type === 'solid')
        ? m.background.value
        : (c['page-bg'] ?? '#0b0f17');
    const radius = m.tokens.radius;
    const page: CSSProperties = {
        background: bg, color: c['page-fg'] ?? '#e6edf6',
        fontFamily: m.tokens.typography.fontFamily,
        fontSize: `${14 * m.tokens.typography.sizeScale}px`,
        padding: m.tokens.spacing.unit * 2
    };
    const heading: CSSProperties = {
        fontFamily: m.tokens.typography.fontFamilyHeading || m.tokens.typography.fontFamily,
        fontWeight: m.tokens.typography.weightHeading,
        letterSpacing: m.tokens.typography.letterSpacingHeading || '0',
        color: c.accent ?? c['page-fg'] ?? '#fff', margin: '0 0 6px'
    };
    const card: CSSProperties = {
        background: c['card-bg'] ?? '#151c28',
        border: `1px solid ${c['card-border'] ?? '#283447'}`,
        borderRadius: radius.base, boxShadow: m.tokens.shadow.card,
        padding: m.tokens.spacing.unit * 2, marginTop: m.tokens.spacing.unit * 1.5
    };
    const button: CSSProperties = {
        background: c['button-bg'] ?? c.accent ?? '#6366f1', color: c['button-fg'] ?? '#fff',
        border: `1px solid ${c['button-border'] ?? c['button-bg'] ?? '#6366f1'}`,
        borderRadius: radius.button, boxShadow: m.tokens.shadow.button,
        padding: '7px 14px', fontWeight: 600, cursor: 'default', fontFamily: 'inherit'
    };

    return (
        <div className="sb-preview-frame">
            <div className="sb-preview-bar"><span /><span /><span /></div>
            <div style={page}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: c['header-bg'] ?? 'transparent', color: c['header-fg'] ?? c['page-fg'], padding: `${m.tokens.spacing.unit}px ${m.tokens.spacing.unit * 1.5}px`, borderRadius: radius.base }}>
                    <strong style={{ fontFamily: heading.fontFamily, fontWeight: m.tokens.typography.weightHeading }}>{m.meta.name || 'Anteprima'}</strong>
                    <span style={{ fontSize: '0.85em', opacity: 0.85 }}>Home · News · Shop</span>
                </div>
                <div style={card}>
                    <h3 style={heading}>Titolo di esempio</h3>
                    <p style={{ margin: '0 0 4px' }}>Testo del corpo con un <a href="#preview" onClick={e => e.preventDefault()} style={{ color: c.link ?? '#22d3ee', textDecoration: 'none' }}>link</a> in linea.</p>
                    <p style={{ margin: '0 0 12px', color: c['text-muted'] ?? '#8b9bb4', fontSize: '0.9em' }}>Testo secondario / muted.</p>
                    <div style={{ display: 'flex', gap: 8 }}>
                        <button type="button" style={button}>Pulsante</button>
                        <span style={{ alignSelf: 'center', color: c.success ?? '#10b981', fontSize: '0.85em' }}>● success</span>
                        <span style={{ alignSelf: 'center', color: c.warning ?? '#f59e0b', fontSize: '0.85em' }}>● warning</span>
                        <span style={{ alignSelf: 'center', color: c.error ?? '#f43f5e', fontSize: '0.85em' }}>● error</span>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: 6, marginTop: m.tokens.spacing.unit * 1.5 }}>
                    {m.meta.swatches.map((s, i) => <span key={i} style={{ width: 28, height: 28, borderRadius: radius.input, background: s, border: '1px solid rgba(255,255,255,0.15)' }} title={s} />)}
                </div>
            </div>
        </div>
    );
}

// ---------------------------------------------------------------------------
//  Main
// ---------------------------------------------------------------------------
export function SkinBuilder(): ReactNode
{
    const qc = useQueryClient();
    const list = useQuery<SkinListResponse>({ queryKey: [ 'skins', 'manage' ], queryFn: fetchSkins });
    const [ draft, setDraft ] = useState<SkinManifest>(DEFAULT_MANIFEST);
    const [ editingId, setEditingId ] = useState<number | null>(null);
    const [ msg, setMsg ] = useState<{ k: 'success' | 'error' | 'info'; t: string } | null>(null);

    /** Update immutabile annidato via clone + producer. */
    const edit = (fn: (d: SkinManifest) => void): void => setDraft(prev => { const next = clone(prev); fn(next); return next; });

    const manifestBySlug = (slug: string): SkinManifest | undefined => list.data?.skins.find(s => s.meta.slug === slug);

    const loadBase = (slug: string): void =>
    {
        const m = manifestBySlug(slug);
        if(!m) return;
        const next = clone(m);
        next.meta.name = `${m.meta.name} (copia)`;
        setDraft(next);
        setEditingId(null);
        setMsg({ k: 'info', t: `Clonato da "${m.meta.name}". Modifica e salva come nuova skin.` });
    };

    const loadForEdit = (entry: SkinEntry): void =>
    {
        const m = manifestBySlug(entry.slug);
        if(!m) return;
        setDraft(clone(m));
        setEditingId(entry.id);
        setMsg({ k: 'info', t: `Modifica di "${entry.name}".` });
    };

    const newFromScratch = (): void => { setDraft(clone(DEFAULT_MANIFEST)); setEditingId(null); setMsg(null); };

    const saveMut = useMutation({
        mutationFn: () => saveSkin(draft, editingId),
        onSuccess: (res) => {
            if(res.id) setEditingId(res.id);
            setMsg({ k: 'success', t: editingId ? 'Modifiche salvate.' : `Skin creata (slug: ${res.slug}).` });
            void qc.invalidateQueries({ queryKey: [ 'skins' ] });
        },
        onError: (e: Error) => setMsg({ k: 'error', t: `Errore salvataggio: ${e.message}` })
    });

    const deleteMut = useMutation({
        mutationFn: (id: number) => deleteSkin(id),
        onSuccess: () => { setEditingId(null); setDraft(clone(DEFAULT_MANIFEST)); setMsg({ k: 'success', t: 'Skin eliminata.' }); void qc.invalidateQueries({ queryKey: [ 'skins' ] }); },
        onError: (e: Error) => setMsg({ k: 'error', t: `Errore eliminazione: ${e.message}` })
    });

    const activateMut = useMutation({
        mutationFn: (slug: string) => activateSkin(slug),
        onSuccess: () => { setMsg({ k: 'success', t: 'Skin attivata sul sito.' }); void qc.invalidateQueries({ queryKey: [ 'skins' ] }); },
        onError: (e: Error) => setMsg({ k: 'error', t: `Errore attivazione: ${e.message}` })
    });

    const colorKeys = Object.keys(draft.tokens.colors);
    const customEntries = list.data?.entries.filter(e => e.isCustom) ?? [];

    return (
        <div className="admin-section">
            <h2 className="admin-section__title">Skin Builder <span className="admin-card__hint">crea e modifica temi del sito</span></h2>

            {/* Barra: base + custom skins */}
            <div className="admin-card">
                <div className="admin-row admin-row--3" style={{ alignItems: 'end' }}>
                    <Field label="Parti da una skin esistente (clona)">
                        <select className="admin-select" value="" onChange={e => { if(e.target.value) loadBase(e.target.value); }}>
                            <option value="">— scegli —</option>
                            {(list.data?.entries ?? []).map(e => <option key={e.id} value={e.slug}>{e.name}{e.isBuiltin ? ' (builtin)' : ''}</option>)}
                        </select>
                    </Field>
                    <div><button type="button" className="admin-btn admin-btn--ghost" onClick={newFromScratch}>＋ Nuova da zero</button></div>
                    <div style={{ textAlign: 'right', color: 'var(--admin-fg-muted)', fontSize: 12 }}>
                        {editingId ? `Modifica skin #${editingId}` : 'Creazione nuova skin'}
                    </div>
                </div>

                {customEntries.length > 0 && (
                    <div style={{ marginTop: 14 }}>
                        <div className="admin-field__label">Skin personalizzate</div>
                        <div className="sb-chips">
                            {customEntries.map(e => (
                                <div key={e.id} className="sb-chip">
                                    <button type="button" className="sb-chip__name" onClick={() => loadForEdit(e)} title="Modifica">{e.name}</button>
                                    <button type="button" className="sb-chip__act" onClick={() => activateMut.mutate(e.slug)} disabled={activateMut.isPending} title="Attiva sul sito">▶</button>
                                    <button type="button" className="sb-chip__del" onClick={() => { if(window.confirm(`Eliminare "${e.name}"?`)) deleteMut.mutate(e.id); }} disabled={deleteMut.isPending} title="Elimina">✕</button>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {msg && <div className={`admin-alert admin-alert--${msg.k === 'info' ? 'info' : msg.k}`} style={{ marginBottom: 14 }}>{msg.t}</div>}

            {/* Editor + preview */}
            <div className="sb-layout">
                <div className="sb-editor">
                    <div className="admin-card">
                        <div className="admin-card__title">Identità</div>
                        <Text label="Nome" value={draft.meta.name} onChange={v => edit(d => { d.meta.name = v; })} />
                        <Text label="Descrizione" value={draft.meta.description} onChange={v => edit(d => { d.meta.description = v; })} />
                        <div className="admin-row admin-row--4">
                            {draft.meta.swatches.map((s, i) => (
                                <Color key={i} label={`Swatch ${i + 1}`} value={s} onChange={v => edit(d => { d.meta.swatches[i] = v; })} />
                            ))}
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Colori <span className="admin-card__hint">diventano --color-&lt;chiave&gt;</span></div>
                        <div className="sb-colors-grid">
                            {colorKeys.map(k => (
                                <Color key={k} label={k} value={draft.tokens.colors[k] ?? ''} onChange={v => edit(d => { d.tokens.colors[k] = v; })} />
                            ))}
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Tipografia</div>
                        <Text label="Font famiglia" value={draft.tokens.typography.fontFamily} onChange={v => edit(d => { d.tokens.typography.fontFamily = v; })} placeholder='"Inter", sans-serif' />
                        <Text label="Font titoli" value={draft.tokens.typography.fontFamilyHeading ?? ''} onChange={v => edit(d => setOptional(d.tokens.typography as unknown as Record<string, unknown>, 'fontFamilyHeading', v))} />
                        <Text label="URL Google Fonts (opz.)" value={draft.tokens.typography.fontUrl ?? ''} onChange={v => edit(d => setOptional(d.tokens.typography as unknown as Record<string, unknown>, 'fontUrl', v))} placeholder="https://fonts.googleapis.com/css2?family=..." mono />
                        <div className="admin-row admin-row--3">
                            <Select label="Scala dimensioni" value={String(draft.tokens.typography.sizeScale)} options={[ { value: '0.875', label: 'Compatta' }, { value: '1', label: 'Normale' }, { value: '1.125', label: 'Comoda' }, { value: '1.25', label: 'Poster' } ]} onChange={v => edit(d => { d.tokens.typography.sizeScale = Number(v); })} />
                            <Select label="Peso titoli" value={String(draft.tokens.typography.weightHeading)} options={[ { value: '400', label: 'Normale' }, { value: '700', label: 'Bold' }, { value: '900', label: 'Black' } ]} onChange={v => edit(d => { d.tokens.typography.weightHeading = Number(v); })} />
                            <Text label="Letter-spacing titoli" value={draft.tokens.typography.letterSpacingHeading ?? ''} onChange={v => edit(d => setOptional(d.tokens.typography as unknown as Record<string, unknown>, 'letterSpacingHeading', v))} placeholder="0.02em" />
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Spaziatura &amp; raggi</div>
                        <div className="admin-row admin-row--2">
                            <Num label="Unità spazio" value={draft.tokens.spacing.unit} min={2} max={20} step={1} suffix="px" onChange={n => edit(d => { d.tokens.spacing.unit = n; })} />
                            <Num label="Densità" value={draft.tokens.spacing.densityScale} min={0.5} max={2} step={0.05} onChange={n => edit(d => { d.tokens.spacing.densityScale = n; })} />
                        </div>
                        <div className="admin-row admin-row--3">
                            <Num label="Raggio card" value={draft.tokens.radius.base} min={0} max={32} step={1} suffix="px" onChange={n => edit(d => { d.tokens.radius.base = n; })} />
                            <Num label="Raggio button" value={draft.tokens.radius.button} min={0} max={32} step={1} suffix="px" onChange={n => edit(d => { d.tokens.radius.button = n; })} />
                            <Num label="Raggio input" value={draft.tokens.radius.input} min={0} max={32} step={1} suffix="px" onChange={n => edit(d => { d.tokens.radius.input = n; })} />
                        </div>
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Ombre &amp; animazione</div>
                        <div className="admin-row admin-row--2">
                            <Select label="Stile ombra" value={draft.tokens.shadow.style} options={[ { value: 'flat', label: 'Flat' }, { value: 'soft', label: 'Soft' }, { value: 'neon-glow', label: 'Neon glow' }, { value: 'pixel', label: 'Pixel' } ]} onChange={v => edit(d => { d.tokens.shadow.style = v as SkinManifest['tokens']['shadow']['style']; })} />
                            <Select label="Velocità animazioni" value={draft.tokens.animation.speed} options={[ { value: 'instant', label: 'Istantanea' }, { value: 'fast', label: 'Veloce' }, { value: 'normal', label: 'Normale' }, { value: 'slow', label: 'Lenta' } ]} onChange={v => edit(d => { d.tokens.animation.speed = v as SkinManifest['tokens']['animation']['speed']; })} />
                        </div>
                        <Text label="Box-shadow card" value={draft.tokens.shadow.card} onChange={v => edit(d => { d.tokens.shadow.card = v; })} mono />
                        <Text label="Box-shadow button" value={draft.tokens.shadow.button} onChange={v => edit(d => { d.tokens.shadow.button = v; })} mono />
                        <Text label="Easing" value={draft.tokens.animation.easing} onChange={v => edit(d => { d.tokens.animation.easing = v; })} placeholder="ease-out" />
                    </div>

                    <div className="admin-card">
                        <div className="admin-card__title">Sfondo pagina</div>
                        <div className="admin-row admin-row--2">
                            <Select label="Tipo" value={draft.background?.type ?? 'solid'} options={[ { value: 'solid', label: 'Tinta unita' }, { value: 'gradient', label: 'Gradiente' }, { value: 'pattern', label: 'Pattern' }, { value: 'animated', label: 'Animato' } ]} onChange={v => edit(d => { if(!d.background) d.background = { type: 'solid', value: '#0b0f17' }; d.background.type = v as NonNullable<SkinManifest['background']>['type']; })} />
                            <Text label="Animazione (se animato)" value={draft.background?.animation ?? ''} onChange={v => edit(d => { if(!d.background) d.background = { type: 'animated', value: '' }; setOptional(d.background as unknown as Record<string, unknown>, 'animation', v); })} placeholder="snow / stars / cobweb" />
                        </div>
                        <Text label="Valore CSS (colore / gradient / url)" value={draft.background?.value ?? ''} onChange={v => edit(d => { if(!d.background) d.background = { type: 'solid', value: v }; else d.background.value = v; })} mono />
                    </div>

                    <div className="sb-actions">
                        <button type="button" className="admin-btn admin-btn--primary" disabled={saveMut.isPending || !draft.meta.name.trim()} onClick={() => saveMut.mutate()}>
                            {saveMut.isPending ? 'Salvataggio…' : editingId ? '💾 Salva modifiche' : '💾 Salva come nuova skin'}
                        </button>
                        {editingId && (
                            <button type="button" className="admin-btn admin-btn--danger" disabled={deleteMut.isPending} onClick={() => { if(window.confirm('Eliminare questa skin?')) deleteMut.mutate(editingId); }}>🗑 Elimina</button>
                        )}
                    </div>
                </div>

                <div className="sb-preview-col">
                    <div className="admin-card__title" style={{ marginBottom: 8 }}>Anteprima live</div>
                    <Preview m={draft} />
                </div>
            </div>
        </div>
    );
}
