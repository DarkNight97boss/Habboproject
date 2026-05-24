package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomRightsComposer.class */
public class RoomRightsComposer extends MessageComposer {
    private final RoomRightLevels type;

    public RoomRightsComposer(RoomRightLevels roomRightLevels) {
        this.type = roomRightLevels;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomRightsComposer);
        this.response.appendInt(Integer.valueOf(this.type.level));
        return this.response;
    }
}
