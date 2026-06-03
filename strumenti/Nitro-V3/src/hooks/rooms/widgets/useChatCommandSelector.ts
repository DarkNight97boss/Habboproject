import { AvailableCommandsEvent, GetCommunication } from '@nitrots/nitro-renderer';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { CommandDefinition, LocalizeText } from '../../../api';
import { createNitroStore } from '../../../state/createNitroStore';
import { useMessageEvent } from '../../events';
import { useHasPermission } from '../../session/useSessionSnapshots';

// Risolve la descrizione di un comando: preferisce la traduzione i18n (se la
// chiave esiste nei testi caricati), altrimenti usa il fallback italiano. I
// testi runtime del client arrivano da `gamedata/UITexts.json` e NON includono
// le chiavi `chatcmd.*` — senza fallback si vedrebbe la chiave grezza
// (es. "chatcmd.client.floor"), che è il bug segnalato.
const resolveDescription = (descriptionKey: string, fallback: string): string =>
{
    const localized = LocalizeText(descriptionKey);
    return (!localized || localized === descriptionKey) ? fallback : localized;
};

// Comandi client-only: sempre disponibili, gestiti dal client stesso.
// `label` = descrizione italiana di fallback.
const CLIENT_COMMANDS: { key: string; descriptionKey: string; label: string }[] = [
    // Effetti stanza
    { key: 'shake',       descriptionKey: 'chatcmd.client.shake',      label: 'Fa tremare la stanza' },
    { key: 'rotate',      descriptionKey: 'chatcmd.client.rotate',     label: 'Ruota la visuale della stanza' },
    { key: 'zoom',        descriptionKey: 'chatcmd.client.zoom',       label: 'Zoom della stanza' },
    { key: 'flip',        descriptionKey: 'chatcmd.client.flip',       label: 'Specchia la visuale' },
    { key: 'iddqd',       descriptionKey: 'chatcmd.client.iddqd',      label: 'Modalità dio (easter egg)' },
    { key: 'screenshot',  descriptionKey: 'chatcmd.client.screenshot', label: 'Cattura uno screenshot' },
    { key: 'togglefps',   descriptionKey: 'chatcmd.client.togglefps',  label: 'Mostra/nasconde gli FPS' },
    // Espressioni
    { key: 'd',           descriptionKey: 'chatcmd.client.laugh',      label: 'Ridi (espressione)' },
    { key: 'kiss',        descriptionKey: 'chatcmd.client.kiss',       label: 'Manda un bacio (espressione)' },
    { key: 'jump',        descriptionKey: 'chatcmd.client.jump',       label: 'Salta (espressione)' },
    { key: 'idle',        descriptionKey: 'chatcmd.client.idle',       label: 'Vai in stato di inattività' },
    { key: 'sign',        descriptionKey: 'chatcmd.client.sign',       label: 'Mostra un cartello (1-19)' },
    // Gestione stanza
    { key: 'furni',       descriptionKey: 'chatcmd.client.furni',      label: 'Info sui furni della stanza' },
    { key: 'chooser',     descriptionKey: 'chatcmd.client.chooser',    label: 'Elenco degli utenti nella stanza' },
    { key: 'floor',       descriptionKey: 'chatcmd.client.floor',      label: 'Apre l\'editor del pavimento' },
    { key: 'bcfloor',     descriptionKey: 'chatcmd.client.floor',      label: 'Editor del pavimento (Builders Club)' },
    { key: 'pickall',     descriptionKey: 'chatcmd.client.pickall',    label: 'Raccoglie tutti i tuoi furni' },
    { key: 'ejectall',    descriptionKey: 'chatcmd.client.ejectall',   label: 'Rimanda i furni altrui in inventario' },
    { key: 'settings',    descriptionKey: 'chatcmd.client.settings',   label: 'Apre le impostazioni della stanza' },
    // Info
    { key: 'client',      descriptionKey: 'chatcmd.client.info',       label: 'Informazioni sul client' },
    { key: 'nitro',       descriptionKey: 'chatcmd.client.info',       label: 'Informazioni sul client' },
];

// Comandi staff/admin (lato server Arcturus). Mostrati SOLO se l'utente ha il
// permesso adeguato, così la lista resta pulita per i normali utenti e fa da
// autocompletamento per lo staff.
//   tier 'staff' → gated su `acc_supporttool` (moderazione)
//   tier 'admin' → gated su `acc_supertool`   (economia / server / poteri forti)
// Entrambe le chiavi `acc_*` sono nella whitelist di fallback per securityLevel
// (useSessionSnapshots), quindi il gating funziona sia che il server emetta la
// permission-map sia che NON la emetta. Le keyword sono quelle reali registrate
// nell'emulatore (emulator_texts → commands.keys.*); il server ri-valida
// comunque il permesso all'esecuzione, quindi questo è solo un aiuto visivo.
type StaffTier = 'staff' | 'admin';
const STAFF_COMMANDS: { key: string; label: string; tier: StaffTier }[] = [
    // --- Moderazione (staff) ---
    { key: 'ban',         label: 'Banna un utente (<utente> <secondi>)',     tier: 'staff' },
    { key: 'unban',       label: 'Rimuove il ban (<utente>)',                tier: 'staff' },
    { key: 'ipban',       label: 'Ban per indirizzo IP (<utente> [motivo])', tier: 'staff' },
    { key: 'mute',        label: 'Muta un utente (<utente>)',                tier: 'staff' },
    { key: 'unmute',      label: 'Toglie il mute (<utente>)',                tier: 'staff' },
    { key: 'kickall',     label: 'Caccia tutti dalla stanza ([messaggio])',  tier: 'staff' },
    { key: 'softkick',    label: 'Caccia silenziosa dalla stanza',           tier: 'staff' },
    { key: 'freeze',      label: 'Congela un utente (<utente>)',             tier: 'staff' },
    { key: 'alert',       label: 'Avviso a un utente (<utente> <messaggio>)',tier: 'staff' },
    { key: 'roomalert',   label: 'Avviso a tutta la stanza (<messaggio>)',   tier: 'staff' },
    { key: 'sa',          label: 'Avviso allo staff (<messaggio>)',          tier: 'staff' },
    { key: 'staffonline', label: 'Mostra lo staff online ([rank minimo])',   tier: 'staff' },
    { key: 'summon',      label: 'Convoca un utente da te (<utente>)',       tier: 'staff' },
    { key: 'teleport',    label: 'Teletrasporto tra due punti',              tier: 'staff' },
    { key: 'invisible',   label: 'Diventa invisibile',                       tier: 'staff' },
    { key: 'userinfo',    label: 'Informazioni su un utente (<utente>)',     tier: 'staff' },
    { key: 'follow',      label: 'Segui un utente (<utente>)',               tier: 'staff' },
    { key: 'coords',      label: 'Mostra le tue coordinate',                 tier: 'staff' },
    // --- Amministrazione (admin) ---
    { key: 'superban',    label: 'Super ban (<utente> [motivo])',            tier: 'admin' },
    { key: 'machineban',  label: 'Ban per macchina (<utente> [motivo])',     tier: 'admin' },
    { key: 'giverank',    label: 'Assegna un rank (<utente> <rank>)',        tier: 'admin' },
    { key: 'credits',     label: 'Dai crediti (<utente> <quantità>)',        tier: 'admin' },
    { key: 'duckets',     label: 'Dai duckets (<utente> <quantità>)',        tier: 'admin' },
    { key: 'points',      label: 'Dai diamanti/punti (<utente> <quantità>)', tier: 'admin' },
    { key: 'masscredits', label: 'Crediti a tutti (<quantità>)',             tier: 'admin' },
    { key: 'massduckets', label: 'Duckets a tutti (<quantità>)',             tier: 'admin' },
    { key: 'masspoints',  label: 'Punti a tutti (<quantità> [tipo])',        tier: 'admin' },
    { key: 'badge',       label: 'Dai un distintivo (<utente> <badge>)',     tier: 'admin' },
    { key: 'massbadge',   label: 'Distintivo a tutti (<badge>)',             tier: 'admin' },
    { key: 'gift',        label: 'Regala un oggetto (<utente> <id>)',        tier: 'admin' },
    { key: 'massgift',    label: 'Regalo a tutti (<id>)',                    tier: 'admin' },
    { key: 'ha',          label: 'Avviso hotel a tutti (<messaggio>)',       tier: 'admin' },
    { key: 'event',       label: 'Annuncia un evento (<messaggio>)',         tier: 'admin' },
    { key: 'makesay',     label: 'Fai dire una frase (<utente> <testo>)',    tier: 'admin' },
    { key: 'makeshout',   label: 'Fai gridare una frase (<utente> <testo>)', tier: 'admin' },
    { key: 'sayall',      label: 'Fai parlare tutta la stanza (<testo>)',    tier: 'admin' },
    { key: 'transform',   label: 'Trasformati (<nome> <razza> <colore>)',    tier: 'admin' },
    { key: 'trash',       label: 'Tornado: svuota la stanza',                tier: 'admin' },
    { key: 'setpoll',     label: 'Avvia un sondaggio (<id>)',                tier: 'admin' },
    { key: 'update_catalog',   label: 'Ricarica il catalogo',                tier: 'admin' },
    { key: 'update_navigator', label: 'Ricarica il navigatore',              tier: 'admin' },
    { key: 'update_texts',     label: 'Ricarica i testi dell\'hotel',        tier: 'admin' },
    { key: 'shutdown',    label: 'Arresta l\'emulatore',                     tier: 'admin' },
];

/**
 * Server-pushed command cache. Lives in a Zustand store (instead of
 * module-level `let` variables) so the React Compiler can analyze the
 * surrounding hook cleanly, and so a future test can `setState({…})`
 * a deterministic fixture without monkey-patching the module.
 *
 * The `isListenerRegistered` flag prevents the renderer from getting
 * two AvailableCommandsEvent listeners — one from the module-level
 * pre-mount registration (which captures the server's reply that lands
 * during login, BEFORE any React widget mounts) and one from the
 * in-hook `useMessageEvent` (which covers later rank-change refreshes).
 */
interface ChatCommandStore
{
    serverCommands: CommandDefinition[];
    isListenerRegistered: boolean;
    setServerCommands: (commands: CommandDefinition[]) => void;
    markListenerRegistered: () => void;
}

const useChatCommandStore = createNitroStore<ChatCommandStore>()((set) => ({
    serverCommands: [],
    isListenerRegistered: false,
    setServerCommands: (commands) => set({ serverCommands: commands }),
    markListenerRegistered: () => set({ isListenerRegistered: true })
}));

const ensureGlobalListener = (): void =>
{
    if(useChatCommandStore.getState().isListenerRegistered) return;

    try
    {
        const event = new AvailableCommandsEvent((event: AvailableCommandsEvent) =>
        {
            const parser = event.getParser();
            useChatCommandStore.getState().setServerCommands(parser.commands.map(cmd => ({ key: cmd.key, description: cmd.description })));
        });

        GetCommunication().registerMessageEvent(event);
        useChatCommandStore.getState().markListenerRegistered();
    }
    catch
    {
        // Communication not ready yet — the in-hook useMessageEvent
        // below covers later mounts.
    }
};

// Try once at module load so the server's response landing before any
// React mount still hits the cache.
ensureGlobalListener();

export const useChatCommandSelector = (chatValue: string) =>
{
    const serverCommands = useChatCommandStore(s => s.serverCommands);
    const setServerCommands = useChatCommandStore(s => s.setServerCommands);
    // Gating dei comandi staff/admin. Affidabile sia con permission-map sia con
    // il fallback per securityLevel (entrambe le chiavi sono whitelisted).
    const isStaff = useHasPermission('acc_supporttool');
    const isAdmin = useHasPermission('acc_supertool');
    const [ selectedIndex, setSelectedIndex ] = useState(0);
    const [ dismissed, setDismissed ] = useState(false);

    useEffect(() =>
    {
        // Cover the case where the module-level registration failed
        // because GetCommunication() wasn't ready at import time.
        ensureGlobalListener();
    }, []);

    // Late updates (rank change, etc.) — go through the store so all
    // consumers see the same data.
    useMessageEvent<AvailableCommandsEvent>(AvailableCommandsEvent, event =>
    {
        const parser = event.getParser();
        setServerCommands(parser.commands.map(cmd => ({ key: cmd.key, description: cmd.description })));
    });

    const allCommands = useMemo(() =>
    {
        const merged: CommandDefinition[] = [ ...serverCommands ];

        const pushUnique = (key: string, description: string) =>
        {
            if(merged.some(cmd => cmd.key === key)) return;
            merged.push({ key, description });
        };

        for(const clientCmd of CLIENT_COMMANDS)
        {
            pushUnique(clientCmd.key, resolveDescription(clientCmd.descriptionKey, clientCmd.label));
        }

        for(const staffCmd of STAFF_COMMANDS)
        {
            const allowed = staffCmd.tier === 'admin' ? isAdmin : isStaff;
            if(allowed) pushUnique(staffCmd.key, staffCmd.label);
        }

        return merged.sort((a, b) => a.key.localeCompare(b.key));
    }, [ serverCommands, isStaff, isAdmin ]);

    const filterText = useMemo(() =>
    {
        if(!chatValue.startsWith(':') || chatValue.includes(' ')) return '';

        return chatValue.slice(1).toLowerCase();
    }, [ chatValue ]);

    const filteredCommands = useMemo(() =>
    {
        if(!filterText && !chatValue.startsWith(':')) return [];

        return allCommands.filter(cmd => cmd.key.toLowerCase().startsWith(filterText));
    }, [ allCommands, filterText, chatValue ]);

    const isVisible = useMemo(() =>
    {
        return chatValue.startsWith(':') && !chatValue.includes(' ') && filteredCommands.length > 0 && !dismissed;
    }, [ chatValue, filteredCommands, dismissed ]);

    const moveUp = useCallback(() =>
    {
        setSelectedIndex(prev => (prev <= 0 ? filteredCommands.length - 1 : prev - 1));
    }, [ filteredCommands.length ]);

    const moveDown = useCallback(() =>
    {
        setSelectedIndex(prev => (prev >= filteredCommands.length - 1 ? 0 : prev + 1));
    }, [ filteredCommands.length ]);

    const selectCurrent = useCallback((): CommandDefinition | null =>
    {
        if(selectedIndex >= 0 && selectedIndex < filteredCommands.length)
        {
            return filteredCommands[selectedIndex];
        }

        return null;
    }, [ selectedIndex, filteredCommands ]);

    const close = useCallback(() =>
    {
        setDismissed(true);
    }, []);

    // Reset dismissed when chatValue changes to a new command start
    useEffect(() =>
    {
        if(chatValue === ':' || chatValue === '') setDismissed(false);
    }, [ chatValue ]);

    // Reset selectedIndex when filtered list changes
    useEffect(() =>
    {
        setSelectedIndex(0);
    }, [ filterText ]);

    return { isVisible, filteredCommands, selectedIndex, setSelectedIndex, moveUp, moveDown, selectCurrent, close };
};
