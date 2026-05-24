package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/RoomCategoryUpdateMessageComposer.class */
public class RoomCategoryUpdateMessageComposer extends MessageComposer {
    private final int unknownInt1;

    public RoomCategoryUpdateMessageComposer(int i) {
        this.unknownInt1 = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomCategoryUpdateMessageComposer);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        return this.response;
    }
}
