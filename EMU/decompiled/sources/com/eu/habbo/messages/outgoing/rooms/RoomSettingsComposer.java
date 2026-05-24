package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomSettingsComposer.class */
public class RoomSettingsComposer extends MessageComposer {
    private final Room room;

    public RoomSettingsComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomSettingsComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendString(this.room.getName());
        this.response.appendString(this.room.getDescription());
        this.response.appendInt(Integer.valueOf(this.room.getState().getState()));
        this.response.appendInt(Integer.valueOf(this.room.getCategory()));
        this.response.appendInt(Integer.valueOf(this.room.getUsersMax()));
        this.response.appendInt(Integer.valueOf(this.room.getUsersMax()));
        if (this.room.getTags().isEmpty()) {
            this.response.appendInt((Integer) 0);
        } else {
            this.response.appendInt(Integer.valueOf(this.room.getTags().split(";").length));
            for (String str : this.room.getTags().split(";")) {
                this.response.appendString(str);
            }
        }
        this.response.appendInt(Integer.valueOf(this.room.getTradeMode()));
        this.response.appendInt(Integer.valueOf(this.room.isAllowPets() ? 1 : 0));
        this.response.appendInt(Integer.valueOf(this.room.isAllowPetsEat() ? 1 : 0));
        this.response.appendInt(Integer.valueOf(this.room.isAllowWalkthrough() ? 1 : 0));
        this.response.appendInt(Integer.valueOf(this.room.isHideWall() ? 1 : 0));
        this.response.appendInt(Integer.valueOf(this.room.getWallSize()));
        this.response.appendInt(Integer.valueOf(this.room.getFloorSize()));
        this.response.appendInt(Integer.valueOf(this.room.getChatMode()));
        this.response.appendInt(Integer.valueOf(this.room.getChatWeight()));
        this.response.appendInt(Integer.valueOf(this.room.getChatSpeed()));
        this.response.appendInt(Integer.valueOf(this.room.getChatDistance()));
        this.response.appendInt(Integer.valueOf(this.room.getChatProtection()));
        this.response.appendBoolean(false);
        this.response.appendInt(Integer.valueOf(this.room.getMuteOption()));
        this.response.appendInt(Integer.valueOf(this.room.getKickOption()));
        this.response.appendInt(Integer.valueOf(this.room.getBanOption()));
        return this.response;
    }
}
