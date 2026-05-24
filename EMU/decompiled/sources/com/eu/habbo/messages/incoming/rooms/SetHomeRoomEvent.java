package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserHomeRoomComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/SetHomeRoomEvent.class */
public class SetHomeRoomEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (iIntValue != this.client.getHabbo().getHabboInfo().getHomeRoom()) {
            this.client.getHabbo().getHabboInfo().setHomeRoom(iIntValue);
            this.client.sendResponse(new UserHomeRoomComposer(this.client.getHabbo().getHabboInfo().getHomeRoom(), 0));
        }
    }
}
