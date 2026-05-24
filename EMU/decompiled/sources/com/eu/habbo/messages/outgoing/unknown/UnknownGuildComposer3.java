package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownGuildComposer3.class */
public class UnknownGuildComposer3 extends MessageComposer {
    private final int userId;

    public UnknownGuildComposer3(int i) {
        this.userId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownGuildComposer3);
        this.response.appendInt(Integer.valueOf(this.userId));
        return this.response;
    }
}
