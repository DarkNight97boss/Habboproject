package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestRoomLoadEvent.class */
public class RequestRoomLoadEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        if (this.client.getHabbo().getHabboInfo().getLoadingRoom() != 0 || this.client.getHabbo().getHabboStats().roomEnterTimestamp + 1000 >= System.currentTimeMillis()) {
            return;
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            Emulator.getGameEnvironment().getRoomManager().logExit(this.client.getHabbo());
            currentRoom.removeHabbo(this.client.getHabbo(), true);
            this.client.getHabbo().getHabboInfo().setCurrentRoom(null);
        }
        if (this.client.getHabbo().getRoomUnit() != null && this.client.getHabbo().getRoomUnit().isTeleporting) {
            this.client.getHabbo().getRoomUnit().isTeleporting = false;
        }
        Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), iIntValue, string);
    }
}
