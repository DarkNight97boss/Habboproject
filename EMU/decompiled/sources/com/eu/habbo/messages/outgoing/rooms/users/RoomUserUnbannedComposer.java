package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserUnbannedComposer.class */
public class RoomUserUnbannedComposer extends MessageComposer {
    private final Room room;
    private final int userId;

    public RoomUserUnbannedComposer(Room room, int i) {
        this.room = room;
        this.userId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserUnbannedComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendInt(Integer.valueOf(this.userId));
        return this.response;
    }
}
