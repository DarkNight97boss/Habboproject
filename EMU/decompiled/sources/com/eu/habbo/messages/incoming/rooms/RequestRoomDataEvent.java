package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestRoomDataEvent.class */
public class RequestRoomDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.packet.readInt().intValue());
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        if (roomLoadRoom != null) {
            boolean z = true;
            if (iIntValue == 0 && iIntValue2 == 1) {
                z = false;
            }
            this.client.sendResponse(new RoomDataComposer(roomLoadRoom, this.client.getHabbo(), true, z));
        }
    }
}
