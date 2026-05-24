package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/WelcomeGiftComposer.class */
public class WelcomeGiftComposer extends MessageComposer {
    private final String email;
    private final boolean unknownB1;
    private final boolean unknownB2;
    private final int furniId;
    private final boolean unknownB3;

    public WelcomeGiftComposer(String str, boolean z, boolean z2, int i, boolean z3) {
        this.email = str;
        this.unknownB1 = z;
        this.unknownB2 = z2;
        this.furniId = i;
        this.unknownB3 = z3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WelcomeGiftComposer);
        this.response.appendString(this.email);
        this.response.appendBoolean(Boolean.valueOf(this.unknownB1));
        this.response.appendBoolean(Boolean.valueOf(this.unknownB2));
        this.response.appendInt(Integer.valueOf(this.furniId));
        this.response.appendBoolean(Boolean.valueOf(this.unknownB3));
        return this.response;
    }
}
