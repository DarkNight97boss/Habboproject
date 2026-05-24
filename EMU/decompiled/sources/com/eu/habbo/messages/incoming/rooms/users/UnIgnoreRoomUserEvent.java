package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/UnIgnoreRoomUserEvent.class */
public class UnIgnoreRoomUserEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habbo = currentRoom.getHabbo(this.packet.readString())) == null || !habbo.getHabboStats().allowTalk()) {
            return;
        }
        this.client.getHabbo().getHabboStats().unignoreUser(habbo.getHabboInfo().getId());
        this.client.sendResponse(new RoomUserIgnoredComposer(habbo, 3));
    }
}
