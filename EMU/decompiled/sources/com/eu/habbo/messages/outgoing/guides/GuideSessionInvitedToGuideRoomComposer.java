package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionInvitedToGuideRoomComposer.class */
public class GuideSessionInvitedToGuideRoomComposer extends MessageComposer {
    private final Room room;

    public GuideSessionInvitedToGuideRoomComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(219);
        this.response.appendInt(Integer.valueOf(this.room != null ? this.room.getId() : 0));
        this.response.appendString(this.room != null ? this.room.getName() : Emulator.PREVIEW);
        return this.response;
    }
}
