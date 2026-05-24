package com.eu.habbo.messages.outgoing.rooms.items.rentablespaces;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/rentablespaces/RentableSpaceUnknown2Composer.class */
public class RentableSpaceUnknown2Composer extends MessageComposer {
    private final int itemId;

    public RentableSpaceUnknown2Composer(int i) {
        this.itemId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RentableSpaceUnknown2Composer);
        this.response.appendInt(Integer.valueOf(this.itemId));
        return this.response;
    }
}
