package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/ReloadRecyclerComposer.class */
public class ReloadRecyclerComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ReloadRecyclerComposer);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
