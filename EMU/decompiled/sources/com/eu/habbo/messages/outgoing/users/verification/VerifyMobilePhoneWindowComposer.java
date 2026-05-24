package com.eu.habbo.messages.outgoing.users.verification;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/verification/VerifyMobilePhoneWindowComposer.class */
public class VerifyMobilePhoneWindowComposer extends MessageComposer {
    private final int unknownInt1;
    private final int unknownInt2;
    private final int unknownInt3;

    public VerifyMobilePhoneWindowComposer(int i, int i2, int i3) {
        this.unknownInt1 = i;
        this.unknownInt2 = i2;
        this.unknownInt3 = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2890);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendInt(Integer.valueOf(this.unknownInt2));
        this.response.appendInt(Integer.valueOf(this.unknownInt3));
        return this.response;
    }
}
