package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionRequesterRoomComposer.class */
public class GuideSessionRequesterRoomComposer extends MessageComposer {
    private final Room room;

    public GuideSessionRequesterRoomComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionRequesterRoomComposer);
        this.response.appendInt(Integer.valueOf(this.room != null ? this.room.getId() : 0));
        return this.response;
    }
}
