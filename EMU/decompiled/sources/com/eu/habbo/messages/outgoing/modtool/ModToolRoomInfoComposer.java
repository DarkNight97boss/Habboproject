package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolRoomInfoComposer.class */
public class ModToolRoomInfoComposer extends MessageComposer {
    private final Room room;

    public ModToolRoomInfoComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolRoomInfoComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendInt(Integer.valueOf(this.room.getCurrentHabbos().size()));
        this.response.appendBoolean(Boolean.valueOf(this.room.getHabbo(this.room.getOwnerId()) != null));
        this.response.appendInt(Integer.valueOf(this.room.getOwnerId()));
        this.response.appendString(this.room.getOwnerName());
        this.response.appendBoolean(true);
        this.response.appendString(this.room.getName());
        this.response.appendString(this.room.getDescription());
        this.response.appendInt(Integer.valueOf(this.room.getTags().split(";").length));
        for (int i = 0; i < this.room.getTags().split(";").length; i++) {
            this.response.appendString(this.room.getTags().split(";")[i]);
        }
        return this.response;
    }
}
