package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/ErrorLoginComposer.class */
public class ErrorLoginComposer extends MessageComposer {
    private final int errorCode;

    public ErrorLoginComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(4000);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
