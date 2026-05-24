package com.eu.habbo.messages.outgoing.generic;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/testcomposer.class */
public class testcomposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3019);
        this.response.appendInt((Integer) 3);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 2);
        this.response.appendString("Key");
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 2);
        this.response.appendString("Key");
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 2);
        this.response.appendString("Key");
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) 1);
        return this.response;
    }
}
