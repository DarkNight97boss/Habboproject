package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomHeightMapComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomRelativeMapComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestRoomHeightmapEvent.class */
public class RequestRoomHeightmapEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room roomLoadRoom;
        if (this.client.getHabbo().getHabboInfo().getLoadingRoom() <= 0 || (roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.client.getHabbo().getHabboInfo().getLoadingRoom())) == null || roomLoadRoom.getLayout() == null) {
            return;
        }
        this.client.sendResponse(new RoomRelativeMapComposer(roomLoadRoom));
        this.client.sendResponse(new RoomHeightMapComposer(roomLoadRoom));
        Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), roomLoadRoom);
    }
}
