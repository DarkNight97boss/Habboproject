package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomRemoveRightsEvent.class */
public class RoomRemoveRightsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue()).removeRights(this.client.getHabbo().getHabboInfo().getId());
    }
}
