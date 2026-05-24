package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserTypingComposer.class */
public class RoomUserTypingComposer extends MessageComposer {
    private final RoomUnit roomUnit;
    private final boolean typing;

    public RoomUserTypingComposer(RoomUnit roomUnit, boolean z) {
        this.roomUnit = roomUnit;
        this.typing = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserTypingComposer);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.typing ? 1 : 0));
        return this.response;
    }
}
