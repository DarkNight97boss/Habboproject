package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownComposer8.class */
public class UnknownComposer8 extends MessageComposer {
    private final int unknownInt1;
    private final int userId;
    private final int unknownInt2;

    public UnknownComposer8(int i, int i2, int i3) {
        this.unknownInt1 = i;
        this.userId = i2;
        this.unknownInt2 = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownComposer8);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendInt(Integer.valueOf(this.unknownInt2));
        return this.response;
    }
}
