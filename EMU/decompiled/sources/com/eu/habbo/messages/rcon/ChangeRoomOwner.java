package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ChangeRoomOwner.class */
public class ChangeRoomOwner extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ChangeRoomOwner$JSON.class */
    static class JSON {
        public int room_id;
        public int user_id;
        public String username;

        JSON() {
        }
    }

    public ChangeRoomOwner() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(json.room_id);
        if (roomLoadRoom != null) {
            roomLoadRoom.setOwnerId(json.user_id);
            roomLoadRoom.setOwnerName(json.username);
            roomLoadRoom.setNeedsUpdate(true);
            roomLoadRoom.save();
            Emulator.getGameEnvironment().getRoomManager().unloadRoom(roomLoadRoom);
        }
    }
}
