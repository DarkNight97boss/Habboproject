package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/PongComposer.class */
public class PongComposer extends MessageComposer {
    private final int id;

    public PongComposer(int i) {
        this.id = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(10);
        this.response.appendInt(Integer.valueOf(this.id));
        return this.response;
    }
}
