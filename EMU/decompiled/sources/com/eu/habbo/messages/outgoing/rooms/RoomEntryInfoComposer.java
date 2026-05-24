package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomEntryInfoComposer.class */
public class RoomEntryInfoComposer extends MessageComposer {
    private final Room room;

    public RoomEntryInfoComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(-1);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendString(this.room.getOwnerName());
        return this.response;
    }
}
