import { type ReactNode, useEffect, useRef, useState } from 'react';
import { type AuthUser } from '../hooks/useAuth';

/**
 * Command palette stile Linear/Arc (no deps esterne, lightweight).
 *
 * Trigger Cmd+K (gestito dal parent AsteriaShell). Mostra modal glass con:
 *  - Input search auto-focus
 *  - Lista azioni filtrabili (navigate + commands)
 *  - Frecce ↑↓ per navigare, Enter per selezionare
 *  - Esc per chiudere
 *
 * Zero runtime cost quando chiuso (early return null).
 */

interface PaletteItem {
    id: string;
    label: string;
    shortcut?: string;
    icon: string;
    section: string;
    onSelect: () => void;
    matchOn?: string;  // termini extra per fuzzy match
}

export function CommandPalette({ open, onClose, user }: { open: boolean; onClose: () => void; user: AuthUser | null }): ReactNode
{
    const [query, setQuery] = useState('');
    const [selected, setSelected] = useState(0);
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() =>
    {
        if(!open) { setQuery(''); setSelected(0); return; }
        // Auto-focus input quando si apre
        const t = setTimeout(() => inputRef.current?.focus(), 30);
        return () => clearTimeout(t);
    }, [open]);

    if(!open) return null;

    const nav = (href: string) => () => { window.location.href = href; onClose(); };

    const items: PaletteItem[] = [
        { id: 'home',       label: 'Home',                   icon: '◉', section: 'Naviga', onSelect: nav('/'),                  matchOn: 'index inizio' },
        { id: 'shop',       label: 'Shop · Crediti & Pack',  icon: '⊕', section: 'Naviga', onSelect: nav('/shop'),              matchOn: 'compra acquista pack credit' },
        { id: 'leaderboard',label: 'Leaderboard · Top user', icon: '★', section: 'Naviga', onSelect: nav('/leaderboard'),       matchOn: 'classifica ranking top' },
        { id: 'community',  label: 'Community · Photos',     icon: '◳', section: 'Naviga', onSelect: nav('/community/photos'),  matchOn: 'foto stanze forum' },
        { id: 'guida',      label: 'Guida',                  icon: 'ⓘ', section: 'Naviga', onSelect: nav('/playing-habbo'),     matchOn: 'help info tutorial' },
        ...(user
            ? [
                { id: 'me',       label: 'Il tuo profilo',      icon: '◐', section: 'Account', onSelect: nav('/me'),                  matchOn: 'me account profile' },
                { id: 'play',     label: 'Entra nel hotel',     icon: '▶', section: 'Account', onSelect: nav('/api/v2/auth/play'),    matchOn: 'gioca play hotel' },
                { id: 'settings', label: 'Impostazioni',        icon: '⚙', section: 'Account', onSelect: nav('/settings/privacy'),    matchOn: 'config preferenze' },
                ...(user.rank >= 5 ? [{ id: 'admin', label: 'Pannello admin',     icon: '⚡', section: 'Account', onSelect: nav('/admin'),               matchOn: 'staff housekeeping' }] : [])
            ] as PaletteItem[]
            : [
                { id: 'login', label: 'Accedi',           icon: '↗', section: 'Account', onSelect: nav('/login'),         matchOn: 'login sign in entra' },
                { id: 'reg',   label: 'Crea account',     icon: '＋', section: 'Account', onSelect: nav('/registration'),  matchOn: 'register signup nuovo' }
            ] as PaletteItem[]
        ),
        { id: 'help',  label: 'Help center',  icon: '?', section: 'Supporto', onSelect: nav('/help'),                       matchOn: 'aiuto faq supporto' },
        { id: 'cfh',   label: 'Contatta staff (in gioco)', icon: '✉', section: 'Supporto', onSelect: () => { window.alert('Apri il client e usa il comando :cfh in chat per contattare lo staff.'); onClose(); }, matchOn: 'cfh ticket bug report' }
    ];

    const q = query.trim().toLowerCase();
    const filtered = q.length === 0
        ? items
        : items.filter(it => (it.label + ' ' + (it.matchOn || '')).toLowerCase().includes(q));

    const sectionsMap = new Map<string, PaletteItem[]>();
    filtered.forEach(it =>
    {
        const arr = sectionsMap.get(it.section) || [];
        arr.push(it);
        sectionsMap.set(it.section, arr);
    });
    const flat = Array.from(sectionsMap.values()).flat();

    function onKey(ev: React.KeyboardEvent): void
    {
        if(ev.key === 'ArrowDown') { ev.preventDefault(); setSelected(s => Math.min(s + 1, flat.length - 1)); }
        else if(ev.key === 'ArrowUp') { ev.preventDefault(); setSelected(s => Math.max(s - 1, 0)); }
        else if(ev.key === 'Enter') { ev.preventDefault(); const it = flat[selected]; if(it) it.onSelect(); }
    }

    return (
        <div className="asteria-cmd-backdrop" onClick={onClose}>
            <div className="asteria-cmd" onClick={ev => ev.stopPropagation()}>
                <div className="asteria-cmd__input-row">
                    <span className="asteria-cmd__icon">⌕</span>
                    <input
                        ref={inputRef}
                        type="text"
                        className="asteria-cmd__input"
                        placeholder="Cerca pagine, azioni…"
                        value={query}
                        onChange={e => { setQuery(e.target.value); setSelected(0); }}
                        onKeyDown={onKey}
                        autoComplete="off"
                        spellCheck={false}
                    />
                    <kbd className="asteria-cmd__hint">Esc</kbd>
                </div>
                <div className="asteria-cmd__results">
                    {flat.length === 0 ? (
                        <div className="asteria-cmd__empty">Nessun risultato per "{query}"</div>
                    ) : (
                        Array.from(sectionsMap.entries()).map(([section, sectionItems]) => (
                            <div key={section} className="asteria-cmd__section">
                                <div className="asteria-cmd__section-title">{section}</div>
                                {sectionItems.map(it =>
                                {
                                    const flatIdx = flat.findIndex(x => x.id === it.id);
                                    const isActive = flatIdx === selected;
                                    return (
                                        <button
                                            key={it.id}
                                            type="button"
                                            className={`asteria-cmd__item ${isActive ? 'is-active' : ''}`}
                                            onClick={() => it.onSelect()}
                                            onMouseEnter={() => setSelected(flatIdx)}
                                        >
                                            <span className="asteria-cmd__item-icon">{it.icon}</span>
                                            <span className="asteria-cmd__item-label">{it.label}</span>
                                            {it.shortcut && <kbd className="asteria-cmd__item-shortcut">{it.shortcut}</kbd>}
                                        </button>
                                    );
                                })}
                            </div>
                        ))
                    )}
                </div>
                <div className="asteria-cmd__footer">
                    <span><kbd>↑</kbd><kbd>↓</kbd> naviga</span>
                    <span><kbd>↵</kbd> seleziona</span>
                    <span><kbd>esc</kbd> chiudi</span>
                </div>
            </div>
        </div>
    );
}
