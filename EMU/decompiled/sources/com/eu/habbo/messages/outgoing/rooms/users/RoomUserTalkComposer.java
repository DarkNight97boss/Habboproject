package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserTalkComposer.class */
public class RoomUserTalkComposer extends MessageComposer {
    private final RoomChatMessage roomChatMessage;

    public RoomUserTalkComposer(RoomChatMessage roomChatMessage) {
        this.roomChatMessage = roomChatMessage;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserTalkComposer);
        if (this.roomChatMessage.getMessage().isEmpty()) {
            return null;
        }
        this.roomChatMessage.serialize(this.response);
        return this.response;
    }
}
