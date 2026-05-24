package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomSettingsSavedComposer.class */
public class RoomSettingsSavedComposer extends MessageComposer {
    private final Room room;

    public RoomSettingsSavedComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomSettingsSavedComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        return this.response;
    }
}
