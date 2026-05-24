package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestHeightmapEvent.class */
public class RequestHeightmapEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room;
        if (this.client.getHabbo().getHabboInfo().getLoadingRoom() <= 0 || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.client.getHabbo().getHabboInfo().getLoadingRoom())) == null) {
            return;
        }
        Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), room);
    }
}
