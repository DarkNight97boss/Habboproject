import net from 'node:net';
import { randomBytes } from 'node:crypto';
import { env } from '../env.js';

/**
 * Bridge RCON/MUS verso l'EMU Arcturus su porta 3001 (default).
 *
 * Wire format ispezionato in EMU RCONServerHandler.java:
 *   {
 *     "key":   "<command>",       // es. "alertuser", "givecredits", ...
 *     "data":  { ... },           // payload del comando (forma per-command)
 *     "token": "<token>",         // se rcon.token configurato in EMU
 *     "nonce": "<random-hex>",    // se rcon.anti_replay.enabled=true
 *     "ts":    <unix-seconds>     // se rcon.anti_replay.enabled=true
 *   }
 *
 * Comandi supportati dall'EMU (vedi RCONServer.java addRCONMessage):
 *   alertuser, hotelalert, disconnect, forwarduser,
 *   givebadge, givecredits, givepixels (duckets), givepoints (diamonds),
 *   sendgift, sendroombundle, setrank, updatewordfilter, updatecatalog,
 *   executecommand (⚠ full RCE — staff-only, audit log),
 *   progressachievement, updateuser, friendrequest,
 *   imagehotelalert, imagealertuser, stalkuser, staffalert, modticket,
 *   talkuser, changeroomowner, muteuser, giverespect, ignoreuser,
 *   setmotto, giveuserclothing, modifysubscription, changeusername.
 *
 * L'EMU chiude il socket dopo la risposta one-shot. Whitelist IP è
 * `rcon.allowed=127.0.0.1` (loopback only) → sicuro perché CMS-V3 e EMU
 * girano sulla stessa box. NON esporre questa porta esternamente.
 */

interface RconCommandShape
{
    key: string;
    data: Record<string, unknown>;
}

export interface RconResult
{
    ok: boolean;
    response: string;
    errorMessage?: string;
}

const HOST = env.RCON_HOST ?? '127.0.0.1';
const PORT = env.RCON_PORT ?? 3001;
const TOKEN = env.RCON_TOKEN ?? '';
const TIMEOUT_MS = 3000;

export async function rconSend(cmd: RconCommandShape): Promise<RconResult>
{
    return new Promise((resolve) =>
    {
        const payload: Record<string, unknown> = {
            key: cmd.key,
            data: cmd.data
        };
        if(TOKEN) payload.token = TOKEN;
        // Anti-replay (defense-in-depth): EMU lo richiede solo se
        // rcon.anti_replay.enabled=true. Lo includiamo sempre, non costa nulla.
        payload.nonce = randomBytes(12).toString('hex');
        payload.ts = Math.floor(Date.now() / 1000);

        const json = JSON.stringify(payload);
        const sock = new net.Socket();
        let response = '';
        let settled = false;

        const finish = (ok: boolean, err?: string): void =>
        {
            if(settled) return;
            settled = true;
            try { sock.destroy(); } catch { /* ignore */ }
            resolve({ ok, response, errorMessage: err });
        };

        sock.setTimeout(TIMEOUT_MS);
        sock.on('timeout', () => finish(false, 'rcon_timeout'));
        sock.on('error', (e) => finish(false, `rcon_error: ${e.message}`));
        sock.on('data', (chunk) => { response += chunk.toString('utf8'); });
        sock.on('close', () =>
        {
            const trimmed = response.trim();
            // L'EMU risponde "OK" su successo, "ERROR" se non autorizzato
            // o se il comando non esiste. Habbo_not_found = utente offline.
            finish(trimmed !== '' && trimmed !== 'ERROR', undefined);
        });

        sock.connect(PORT, HOST, () =>
        {
            sock.write(json);
        });
    });
}

// =================================================================
// Helper tipizzati per i comandi più usati dal CMS-V3.
// =================================================================

export const rcon = {
    /** Popup alert in-game al singolo utente. */
    alertUser: (userId: number, message: string) =>
        rconSend({ key: 'alertuser', data: { user_id: userId, message } }),

    /** Alert broadcast a tutti gli utenti online. */
    hotelAlert: (message: string) =>
        rconSend({ key: 'hotelalert', data: { message } }),

    /** +crediti real-time (l'EMU aggiorna la sessione, no DB-poll delay). */
    giveCredits: (userId: number, credits: number) =>
        rconSend({ key: 'givecredits', data: { user_id: userId, credits } }),

    /** +duckets (pixels in Arcturus). */
    giveDuckets: (userId: number, pixels: number) =>
        rconSend({ key: 'givepixels', data: { user_id: userId, pixels } }),

    /** +diamanti (points in Arcturus). */
    giveDiamonds: (userId: number, points: number) =>
        rconSend({ key: 'givepoints', data: { user_id: userId, points } }),

    /** Assegna un badge. */
    giveBadge: (userId: number, badgeCode: string) =>
        rconSend({ key: 'givebadge', data: { user_id: userId, badge: badgeCode } }),

    /** Imposta il rank dell'utente. */
    setRank: (userId: number, rank: number) =>
        rconSend({ key: 'setrank', data: { user_id: userId, rank } }),

    /** Forza disconnect (kick) dell'utente. */
    disconnect: (userId: number) =>
        rconSend({ key: 'disconnect', data: { user_id: userId } }),

    /** Mute temporaneo (in minuti). */
    muteUser: (userId: number, minutes: number) =>
        rconSend({ key: 'muteuser', data: { user_id: userId, minutes } }),

    /** Refresh catalogo (l'EMU rilegge catalog_pages, catalog_items). */
    updateCatalog: () =>
        rconSend({ key: 'updatecatalog', data: {} }),

    /** Refresh wordfilter (l'EMU rilegge wordfilter table). */
    updateWordfilter: () =>
        rconSend({ key: 'updatewordfilter', data: {} }),

    /** Forza l'avatar a entrare in una stanza specifica. */
    forwardUser: (userId: number, roomId: number) =>
        rconSend({ key: 'forwarduser', data: { user_id: userId, room_id: roomId } }),

    /** Cambia il motto dell'utente. */
    setMotto: (userId: number, motto: string) =>
        rconSend({ key: 'setmotto', data: { user_id: userId, motto } }),

    /** Re-load dei dati dell'utente dal DB (es. dopo UPDATE diretto). */
    updateUser: (userId: number) =>
        rconSend({ key: 'updateuser', data: { user_id: userId } })
};
