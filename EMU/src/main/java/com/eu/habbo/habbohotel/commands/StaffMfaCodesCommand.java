package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.core.StaffMfa;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/**
 * Genera (o rigenera) i codici di recovery MFA per lo staff loggato.
 *
 * Requisiti:
 *  - permesso ACC_SUPPORTTOOL (staff),
 *  - sessione gia' MFA-elevated (per evitare che chi ruba una sessione
 *    pre-MFA possa rigenerare codici e bypassare il TOTP).
 *
 * Comportamento:
 *  - I codici precedenti vengono INVALIDATI (delete prima dell'insert).
 *  - I nuovi sono mostrati UNA volta sola tramite un alert popup; l'utente
 *    deve salvarli/screenshottarli subito perche' il server non li ritiene
 *    in chiaro (solo SHA-256 della forma normalizzata).
 *
 * Audit: STAFF_MFA_RECOVERY_REGENERATED viene inciso nell'AuditLog
 * hash-chained.
 */
public class StaffMfaCodesCommand extends Command {

    public StaffMfaCodesCommand() {
        super(Permission.ACC_SUPPORTTOOL, new String[]{"mfa_codes", "staff_mfa_codes"});
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (gameClient == null || gameClient.getHabbo() == null) {
            return false;
        }

        // Senza una sessione MFA gia' verificata, chiunque rubi un cookie/sessione
        // staff pre-MFA potrebbe rigenerare codici e usarli come bypass del TOTP.
        if (!gameClient.isMfaElevated()) {
            gameClient.getHabbo().whisper(
                    "Devi prima verificare il codice MFA per (ri)generare i codici di recovery.",
                    RoomChatMessageBubbles.ALERT);
            return true;
        }

        int userId = gameClient.getHabbo().getHabboInfo().getId();
        String[] codes = StaffMfa.generateAndStoreRecoveryCodes(userId);
        if (codes == null || codes.length == 0) {
            gameClient.getHabbo().whisper(
                    "Errore nel generare i codici di recovery (vedi log server).",
                    RoomChatMessageBubbles.ALERT);
            return true;
        }

        StringBuilder sb = new StringBuilder("[Codici di recovery MFA]\n\n");
        sb.append("ATTENZIONE: salva ORA questi codici. Non saranno mostrati di nuovo.\n");
        sb.append("I codici precedenti sono stati invalidati.\n\n");
        for (String c : codes) {
            sb.append("  ").append(c).append('\n');
        }
        sb.append("\nUsa uno di questi al posto del TOTP se perdi l'autenticatore.");
        gameClient.getHabbo().alert(sb.toString());
        return true;
    }
}
