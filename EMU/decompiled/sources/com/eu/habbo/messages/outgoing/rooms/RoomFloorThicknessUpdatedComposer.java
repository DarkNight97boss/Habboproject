package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomFloorThicknessUpdatedComposer.class */
public class RoomFloorThicknessUpdatedComposer extends MessageComposer {
    private final Room room;

    public RoomFloorThicknessUpdatedComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3786);
        this.response.appendBoolean(Boolean.valueOf(this.room.isHideWall()));
        this.response.appendInt(Integer.valueOf(this.room.getFloorSize()));
        this.response.appendInt(Integer.valueOf(this.room.getWallSize()));
        return this.response;
    }
}
