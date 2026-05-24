package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserBanEvent.class */
public class RoomUserBanEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getRoomManager().banUserFromRoom(this.client.getHabbo(), this.packet.readInt().intValue(), this.packet.readInt().intValue(), RoomManager.RoomBanTypes.valueOf(this.packet.readString()));
    }
}
