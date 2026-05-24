package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomAddRightsListComposer.class */
public class RoomAddRightsListComposer extends MessageComposer {
    private final Room room;
    private final int userId;
    private final String userName;

    public RoomAddRightsListComposer(Room room, int i, String str) {
        this.room = room;
        this.userId = i;
        this.userName = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomAddRightsListComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.userName);
        return this.response;
    }
}
