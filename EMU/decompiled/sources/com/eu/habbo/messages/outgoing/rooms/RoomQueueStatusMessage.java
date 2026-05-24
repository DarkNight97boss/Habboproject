package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomQueueStatusMessage.class */
public class RoomQueueStatusMessage extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomQueueStatusMessage);
        this.response.appendInt((Integer) 1);
        this.response.appendString("TEST");
        this.response.appendInt((Integer) 94);
        this.response.appendInt((Integer) 1);
        this.response.appendString("d");
        this.response.appendInt((Integer) 1);
        return this.response;
    }
}
