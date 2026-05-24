package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsUnknownComposer.class */
public class SnowWarsUnknownComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2869);
        this.response.appendString("snowwar");
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendBoolean(true);
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
