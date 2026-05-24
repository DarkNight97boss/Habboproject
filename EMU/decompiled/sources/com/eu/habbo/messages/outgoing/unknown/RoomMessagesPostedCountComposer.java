package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/RoomMessagesPostedCountComposer.class */
public class RoomMessagesPostedCountComposer extends MessageComposer {
    private final Room room;
    private final int count;

    public RoomMessagesPostedCountComposer(Room room, int i) {
        this.room = room;
        this.count = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomMessagesPostedCountComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendString(this.room.getName());
        this.response.appendInt(Integer.valueOf(this.count));
        return this.response;
    }
}
