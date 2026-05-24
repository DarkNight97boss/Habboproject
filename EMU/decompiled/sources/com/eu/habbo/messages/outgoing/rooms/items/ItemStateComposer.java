package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/ItemStateComposer.class */
public class ItemStateComposer extends MessageComposer {
    private final HabboItem item;

    public ItemStateComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ItemStateComposer);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        try {
            this.response.appendInt(Integer.valueOf(Integer.valueOf(this.item.getExtradata()).intValue()));
        } catch (Exception e) {
            this.response.appendInt((Integer) 0);
        }
        return this.response;
    }
}
