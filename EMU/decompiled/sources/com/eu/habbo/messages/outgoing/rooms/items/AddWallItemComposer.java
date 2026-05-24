package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/AddWallItemComposer.class */
public class AddWallItemComposer extends MessageComposer {
    private final HabboItem item;
    private final String itemOwnerName;

    public AddWallItemComposer(HabboItem habboItem, String str) {
        this.item = habboItem;
        this.itemOwnerName = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddWallItemComposer);
        this.item.serializeWallData(this.response);
        this.response.appendString(this.itemOwnerName);
        return this.response;
    }
}
