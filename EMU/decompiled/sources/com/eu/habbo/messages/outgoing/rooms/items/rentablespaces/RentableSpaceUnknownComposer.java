package com.eu.habbo.messages.outgoing.rooms.items.rentablespaces;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/rentablespaces/RentableSpaceUnknownComposer.class */
public class RentableSpaceUnknownComposer extends MessageComposer {
    private final int itemId;

    public RentableSpaceUnknownComposer(int i) {
        this.itemId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RentableSpaceUnknownComposer);
        this.response.appendInt(Integer.valueOf(this.itemId));
        return this.response;
    }
}
