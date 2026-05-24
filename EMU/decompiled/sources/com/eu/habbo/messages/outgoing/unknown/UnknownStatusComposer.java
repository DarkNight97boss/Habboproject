package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownStatusComposer.class */
public class UnknownStatusComposer extends MessageComposer {
    public final int STATUS_ZERO = 0;
    public final int STATUS_ONE = 1;
    private final int status;

    public UnknownStatusComposer(int i) {
        this.status = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownStatusComposer);
        this.response.appendInt(Integer.valueOf(this.status));
        return this.response;
    }
}
