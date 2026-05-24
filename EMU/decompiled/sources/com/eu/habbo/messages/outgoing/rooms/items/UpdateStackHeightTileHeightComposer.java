package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/UpdateStackHeightTileHeightComposer.class */
public class UpdateStackHeightTileHeightComposer extends MessageComposer {
    private final HabboItem item;
    private final int height;

    public UpdateStackHeightTileHeightComposer(HabboItem habboItem, int i) {
        this.item = habboItem;
        this.height = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateStackHeightTileHeightComposer);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendInt(Integer.valueOf(this.height));
        return this.response;
    }
}
