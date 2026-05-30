package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.AuditLog;
import com.eu.habbo.core.StaffMfa;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.handshake.StaffMfaResultComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;

/**
 * Client -> server: a staff member submitted a 6-digit Google Authenticator code
 * from the Habbo-style MFA popup. On success we mark the session "elevated" so
 * the gating in CommandHandler / mod-tool stops blocking staff powers.
 *
 * Rate-limited to throttle brute force (1 attempt / 2s; codes are 6 digits).
 */
public class StaffMfaVerifyEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 2000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) {
            return;
        }

        // Nothing to do if the feature is off or this session was never challenged.
        if (!StaffMfa.isEnabled() || !this.client.isMfaRequired()) {
            return;
        }

        if (this.client.isMfaElevated()) {
            this.client.sendResponse(new StaffMfaResultComposer(true, ""));
            return;
        }

        String code = this.packet.readString();
        int userId = this.client.getHabbo().getHabboInfo().getId();
        String username = this.client.getHabbo().getHabboInfo().getUsername();

        boolean ok = StaffMfa.verify(userId, code);
        boolean usedRecoveryCode = false;
        // Fallback: se il TOTP non matcha, prova il codice come recovery monouso
        // (per chi ha perso l'autenticatore). Audit-trail separato in entrambi i casi.
        if (!ok && StaffMfa.verifyRecoveryCode(userId, code)) {
            ok = true;
            usedRecoveryCode = true;
        }

        if (ok) {
            this.client.setMfaElevated(true);
            String action = usedRecoveryCode ? "STAFF_MFA_RECOVERY_OK" : "STAFF_MFA_OK";
            int remaining = usedRecoveryCode ? StaffMfa.remainingRecoveryCodes(userId) : -1;
            AuditLog.record(userId, username, action, "user:" + userId,
                    usedRecoveryCode ? "remaining=" + remaining : "");
            String successMsg;
            if (usedRecoveryCode) {
                successMsg = Emulator.getTexts().getValue("mfa.staff.recovery_success",
                        "Codice di recovery accettato. Codici rimasti: " + remaining
                                + ". Rigenerali al piu' presto con :mfa_codes.");
            } else {
                successMsg = Emulator.getTexts().getValue("mfa.staff.success",
                        "Verification complete. Staff powers unlocked.");
            }
            this.client.sendResponse(new StaffMfaResultComposer(true, successMsg));
            // Refresh permissions/UI now that powers are unlocked.
            this.client.sendResponse(new UserPermissionsComposer(this.client.getHabbo()));
            // Suggerimento sicurezza: se non ci sono codici di recovery (primo enroll
            // o tutti consumati), invita lo staff a rigenerarli SUBITO.
            if (!usedRecoveryCode && StaffMfa.remainingRecoveryCodes(userId) == 0) {
                try {
                    this.client.getHabbo().whisper(
                            "Suggerimento: esegui :mfa_codes per generare i codici di recovery (servono se perdi l'autenticatore).",
                            com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles.ALERT);
                } catch (Throwable ignored) {}
            }
        } else {
            AuditLog.record(userId, username, "STAFF_MFA_FAIL", "user:" + userId, "");
            this.client.sendResponse(new StaffMfaResultComposer(false,
                    Emulator.getTexts().getValue("mfa.staff.failed", "Codice non valido. Riprova.")));
        }
    }
}
