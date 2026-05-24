package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserHandItemComposer.class */
public class RoomUserHandItemComposer extends MessageComposer {
    private final RoomUnit roomUnit;

    public RoomUserHandItemComposer(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1474);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.roomUnit.getHandItem()));
        return this.response;
    }
}
