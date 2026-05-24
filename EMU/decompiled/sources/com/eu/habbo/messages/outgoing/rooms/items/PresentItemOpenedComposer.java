package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/PresentItemOpenedComposer.class */
public class PresentItemOpenedComposer extends MessageComposer {
    private final HabboItem item;
    private final String text;
    private final boolean unknown;

    public PresentItemOpenedComposer(HabboItem habboItem, String str, boolean z) {
        this.item = habboItem;
        this.text = str;
        this.unknown = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(56);
        this.response.appendString(this.item.getBaseItem().getType().code.toLowerCase());
        this.response.appendInt(Integer.valueOf(this.item.getBaseItem().getSpriteId()));
        this.response.appendString(this.item.getBaseItem().getName());
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendString(this.item.getBaseItem().getType().code.toLowerCase());
        this.response.appendBoolean(Boolean.valueOf(this.unknown));
        this.response.appendString(this.text);
        return this.response;
    }
}
