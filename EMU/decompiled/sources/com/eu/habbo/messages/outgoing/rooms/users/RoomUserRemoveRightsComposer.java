package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserRemoveRightsComposer.class */
public class RoomUserRemoveRightsComposer extends MessageComposer {
    private final Room room;
    private final int habboId;

    public RoomUserRemoveRightsComposer(Room room, int i) {
        this.room = room;
        this.habboId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(84);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendInt(Integer.valueOf(this.habboId));
        return this.response;
    }
}
