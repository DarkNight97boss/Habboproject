package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/PostItStickyPoleOpenComposer.class */
public class PostItStickyPoleOpenComposer extends MessageComposer {
    private final HabboItem item;

    public PostItStickyPoleOpenComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PostItStickyPoleOpenComposer);
        this.response.appendInt(Integer.valueOf(this.item == null ? -1234 : this.item.getId()));
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}
