package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomPaneComposer.class */
public class RoomPaneComposer extends MessageComposer {
    private final Room room;
    private final boolean roomOwner;

    public RoomPaneComposer(Room room, boolean z) {
        this.room = room;
        this.roomOwner = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomPaneComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendBoolean(Boolean.valueOf(this.roomOwner));
        return this.response;
    }
}
