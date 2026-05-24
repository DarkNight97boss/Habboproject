package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserDanceComposer.class */
public class RoomUserDanceComposer extends MessageComposer {
    private final RoomUnit roomUnit;

    public RoomUserDanceComposer(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserDanceComposer);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.roomUnit.getDanceType().getType()));
        return this.response;
    }
}
