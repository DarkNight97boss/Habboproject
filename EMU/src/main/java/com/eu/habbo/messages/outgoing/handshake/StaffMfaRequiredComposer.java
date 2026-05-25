package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Tells the nitro client to show the Habbo-style staff MFA popup.
 *
 * Body:
 *   boolean enrolled  - true: just ask for a 6-digit code.
 *                       false: first-time enrollment, show QR + ask for first code.
 *   String  secret    - Base32 secret (empty when enrolled, so it is never re-sent).
 *   String  otpauthUri- otpauth:// provisioning URI for the QR (empty when enrolled).
 */
public class StaffMfaRequiredComposer extends MessageComposer {

    private final boolean enrolled;
    private final String secret;
    private final String otpauthUri;

    public StaffMfaRequiredComposer(boolean enrolled, String secret, String otpauthUri) {
        this.enrolled = enrolled;
        this.secret = secret == null ? "" : secret;
        this.otpauthUri = otpauthUri == null ? "" : otpauthUri;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.StaffMfaRequiredComposer);
        this.response.appendBoolean(this.enrolled);
        this.response.appendString(this.enrolled ? "" : this.secret);
        this.response.appendString(this.enrolled ? "" : this.otpauthUri);
        return this.response;
    }
}
