package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsFullGameStatusComposer.class */
public class SnowWarsFullGameStatusComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 3);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        return this.response;
    }
}
