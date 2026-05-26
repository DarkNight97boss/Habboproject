package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Result of a staff MFA verification attempt.
 *
 * Body:
 *   boolean success - true: code accepted, staff powers unlocked (modal closes).
 *                     false: code rejected (modal stays open, shows the message).
 *   String  message - localized text to show the user.
 */
public class StaffMfaResultComposer extends MessageComposer {

    private final boolean success;
    private final String message;

    public StaffMfaResultComposer(boolean success, String message) {
        this.success = success;
        this.message = message == null ? "" : message;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.StaffMfaResultComposer);
        this.response.appendBoolean(this.success);
        this.response.appendString(this.message);
        return this.response;
    }
}
