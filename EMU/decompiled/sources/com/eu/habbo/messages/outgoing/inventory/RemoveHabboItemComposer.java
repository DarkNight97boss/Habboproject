package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/RemoveHabboItemComposer.class */
public class RemoveHabboItemComposer extends MessageComposer {
    private final int itemId;

    public RemoveHabboItemComposer(int i) {
        this.itemId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RemoveHabboItemComposer);
        this.response.appendInt(Integer.valueOf(this.itemId));
        return this.response;
    }
}
