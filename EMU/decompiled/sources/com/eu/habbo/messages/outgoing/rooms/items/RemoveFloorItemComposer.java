package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/RemoveFloorItemComposer.class */
public class RemoveFloorItemComposer extends MessageComposer {
    private final HabboItem item;
    private final boolean noUser;

    public RemoveFloorItemComposer(HabboItem habboItem) {
        this.item = habboItem;
        this.noUser = false;
    }

    public RemoveFloorItemComposer(HabboItem habboItem, boolean z) {
        this.item = habboItem;
        this.noUser = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RemoveFloorItemComposer);
        this.response.appendString(this.item.getId() + Emulator.PREVIEW);
        this.response.appendBoolean(false);
        this.response.appendInt(Integer.valueOf(this.noUser ? 0 : this.item.getUserId()));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
