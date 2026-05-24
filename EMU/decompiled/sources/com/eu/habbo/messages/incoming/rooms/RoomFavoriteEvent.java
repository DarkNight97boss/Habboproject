package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.FavoriteRoomChangedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomFavoriteEvent.class */
public class RoomFavoriteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        boolean z = true;
        if (Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue) != null) {
            if (this.client.getHabbo().getHabboStats().hasFavoriteRoom(iIntValue)) {
                this.client.getHabbo().getHabboStats().removeFavoriteRoom(iIntValue);
                z = false;
            } else if (!this.client.getHabbo().getHabboStats().addFavoriteRoom(iIntValue)) {
                return;
            }
            this.client.sendResponse(new FavoriteRoomChangedComposer(iIntValue, z));
        }
    }
}
