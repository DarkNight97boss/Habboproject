package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserShoutComposer.class */
public class RoomUserShoutComposer extends MessageComposer {
    private RoomChatMessage roomChatMessage;

    public RoomUserShoutComposer(RoomChatMessage roomChatMessage) {
        this.roomChatMessage = roomChatMessage;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (this.roomChatMessage.getMessage().isEmpty()) {
            return null;
        }
        this.response.init(1036);
        this.roomChatMessage.serialize(this.response);
        return this.response;
    }
}
