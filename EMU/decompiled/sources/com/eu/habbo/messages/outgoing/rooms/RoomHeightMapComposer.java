package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomHeightMapComposer.class */
public class RoomHeightMapComposer extends MessageComposer {
    private final Room room;

    public RoomHeightMapComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomHeightMapComposer);
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(this.room.getWallHeight()));
        this.response.appendString(this.room.getLayout().getRelativeMap());
        return this.response;
    }
}
