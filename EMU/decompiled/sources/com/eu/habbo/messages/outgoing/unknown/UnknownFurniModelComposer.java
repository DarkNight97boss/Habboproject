package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownFurniModelComposer.class */
public class UnknownFurniModelComposer extends MessageComposer {
    private final HabboItem item;
    private final int unknownInt;

    public UnknownFurniModelComposer(HabboItem habboItem, int i) {
        this.item = habboItem;
        this.unknownInt = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownFurniModelComposer);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendInt(Integer.valueOf(this.unknownInt));
        return this.response;
    }
}
