package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserRemoveComposer.class */
public class RoomUserRemoveComposer extends MessageComposer {
    private final RoomUnit roomUnit;

    public RoomUserRemoveComposer(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserRemoveComposer);
        this.response.appendString(this.roomUnit.getId() + Emulator.PREVIEW);
        return this.response;
    }
}
