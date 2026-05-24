package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestRoomSettingsEvent.class */
public class RequestRoomSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue());
        if (room != null) {
            this.client.sendResponse(new RoomSettingsComposer(room));
        }
    }
}
