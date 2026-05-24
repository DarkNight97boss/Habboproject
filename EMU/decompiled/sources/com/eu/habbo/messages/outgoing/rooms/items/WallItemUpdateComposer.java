package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/WallItemUpdateComposer.class */
public class WallItemUpdateComposer extends MessageComposer {
    private final HabboItem item;

    public WallItemUpdateComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WallItemUpdateComposer);
        this.item.serializeWallData(this.response);
        this.response.appendString(this.item.getUserId() + Emulator.PREVIEW);
        return this.response;
    }
}
