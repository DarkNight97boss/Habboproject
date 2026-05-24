package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomWordFilterModifyEvent.class */
public class RoomWordFilterModifyEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        boolean z = this.packet.readBoolean();
        String string = this.packet.readString();
        if (string.length() > 25) {
            string = string.substring(0, 24);
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || currentRoom.getId() != iIntValue) {
            return;
        }
        if (!currentRoom.isOwner(this.client.getHabbo())) {
            ScripterManager.scripterDetected(this.client, String.format("User (%s) tried to change wordfilter for a not owned room.", this.client.getHabbo().getHabboInfo().getUsername()));
        } else if (z) {
            currentRoom.addToWordFilter(string);
        } else {
            currentRoom.removeFromWordFilter(string);
        }
    }
}
