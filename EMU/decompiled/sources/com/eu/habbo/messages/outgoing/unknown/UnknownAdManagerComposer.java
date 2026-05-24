package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownAdManagerComposer.class */
public class UnknownAdManagerComposer extends MessageComposer {
    private final boolean unknownBoolean;

    public UnknownAdManagerComposer(boolean z) {
        this.unknownBoolean = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownAdManagerComposer);
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean));
        return this.response;
    }
}
