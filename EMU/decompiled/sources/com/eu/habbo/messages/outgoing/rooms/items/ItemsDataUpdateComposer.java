package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Set;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/ItemsDataUpdateComposer.class */
public class ItemsDataUpdateComposer extends MessageComposer {
    private final Set<HabboItem> items;

    public ItemsDataUpdateComposer(Set<HabboItem> set) {
        this.items = set;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ItemsDataUpdateComposer);
        this.response.appendInt(Integer.valueOf(this.items.size()));
        for (HabboItem habboItem : this.items) {
            this.response.appendInt(Integer.valueOf(habboItem.getId()));
            habboItem.serializeExtradata(this.response);
        }
        return this.response;
    }
}
