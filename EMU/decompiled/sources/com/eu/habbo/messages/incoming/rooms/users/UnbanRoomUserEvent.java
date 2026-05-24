package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/UnbanRoomUserEvent.class */
public class UnbanRoomUserEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue());
        if (room == null || !room.isOwner(this.client.getHabbo())) {
            return;
        }
        room.unbanHabbo(iIntValue);
    }
}
