package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserDropHandItemEvent.class */
public class RoomUserDropHandItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        this.client.getHabbo().getRoomUnit().setHandItem(0);
        if (currentRoom != null) {
            currentRoom.unIdle(this.client.getHabbo());
            currentRoom.sendComposer(new RoomUserHandItemComposer(this.client.getHabbo().getRoomUnit()).compose());
        }
    }
}
