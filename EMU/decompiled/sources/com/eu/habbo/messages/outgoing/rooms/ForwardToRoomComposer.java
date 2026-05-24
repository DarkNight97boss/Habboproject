package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/ForwardToRoomComposer.class */
public class ForwardToRoomComposer extends MessageComposer {
    private final int roomId;

    public ForwardToRoomComposer(int i) {
        this.roomId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ForwardToRoomComposer);
        this.response.appendInt(Integer.valueOf(this.roomId));
        return this.response;
    }
}
