package com.eu.habbo.messages.outgoing.users.verification;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/verification/VerifyMobilePhoneDoneComposer.class */
public class VerifyMobilePhoneDoneComposer extends MessageComposer {
    private final int unknownInt1;
    private final int unknownInt2;

    public VerifyMobilePhoneDoneComposer(int i, int i2) {
        this.unknownInt1 = i;
        this.unknownInt2 = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(91);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendInt(Integer.valueOf(this.unknownInt2));
        return this.response;
    }
}
