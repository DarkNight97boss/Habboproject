package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/ItemExtraDataComposer.class */
public class ItemExtraDataComposer extends MessageComposer {
    private final HabboItem item;

    public ItemExtraDataComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ItemExtraDataComposer);
        this.response.appendString(this.item.getId() + Emulator.PREVIEW);
        this.item.serializeExtradata(this.response);
        return this.response;
    }
}
