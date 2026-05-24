package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/ItemIntStateComposer.class */
public class ItemIntStateComposer extends MessageComposer {
    private final int id;
    private final int value;

    public ItemIntStateComposer(int i, int i2) {
        this.id = i;
        this.value = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ItemStateComposer2);
        this.response.appendInt(Integer.valueOf(this.id));
        this.response.appendInt(Integer.valueOf(this.value));
        return this.response;
    }
}
