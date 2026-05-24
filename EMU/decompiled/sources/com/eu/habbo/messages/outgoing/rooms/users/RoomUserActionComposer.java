package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserActionComposer.class */
public class RoomUserActionComposer extends MessageComposer {
    private RoomUserAction action;
    private RoomUnit roomUnit;

    public RoomUserActionComposer(RoomUnit roomUnit, RoomUserAction roomUserAction) {
        this.roomUnit = roomUnit;
        this.action = roomUserAction;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserActionComposer);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.action.getAction()));
        return this.response;
    }
}
