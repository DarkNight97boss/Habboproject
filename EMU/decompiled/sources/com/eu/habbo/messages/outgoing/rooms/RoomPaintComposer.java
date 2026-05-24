package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomPaintComposer.class */
public class RoomPaintComposer extends MessageComposer {
    private final String type;
    private final String value;

    public RoomPaintComposer(String str, String str2) {
        this.type = str;
        this.value = str2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomPaintComposer);
        this.response.appendString(this.type);
        this.response.appendString(this.value);
        return this.response;
    }
}
