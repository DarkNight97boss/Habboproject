package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomChatSettingsComposer.class */
public class RoomChatSettingsComposer extends MessageComposer {
    private final Room room;

    public RoomChatSettingsComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomChatSettingsComposer);
        this.response.appendInt(Integer.valueOf(this.room.getChatMode()));
        this.response.appendInt(Integer.valueOf(this.room.getChatWeight()));
        this.response.appendInt(Integer.valueOf(this.room.getChatSpeed()));
        this.response.appendInt(Integer.valueOf(this.room.getChatDistance()));
        this.response.appendInt(Integer.valueOf(this.room.getChatProtection()));
        return this.response;
    }
}
