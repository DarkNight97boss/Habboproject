package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsGenericErrorComposer.class */
public class SnowWarsGenericErrorComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3702);
        this.response.appendInt((Integer) 1);
        return this.response;
    }
}
