package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.core.StaffMfa;
import com.eu.habbo.messages.incoming.MessageHandler;

/**
 * Client -> server: the MFA modal has mounted and asks whether this session still
 * needs verification. This avoids the login-time race where the proactive popup
 * (sent in SecureLoginEvent) can arrive before the client's React handler is
 * registered. If the session is still locked, we (re)send the challenge popup.
 *
 * Carries no payload.
 */
public class StaffMfaStatusRequestEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 1000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) {
            return;
        }

        if (StaffMfa.isEnabled() && this.client.isStaffMfaLocked()) {
            StaffMfa.sendChallenge(this.client);
        }
    }
}
