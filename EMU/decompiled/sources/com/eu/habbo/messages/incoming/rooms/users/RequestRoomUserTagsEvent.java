package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTagsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RequestRoomUserTagsEvent.class */
public class RequestRoomUserTagsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habboByRoomUnitId;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (habboByRoomUnitId = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboByRoomUnitId(iIntValue)) == null) {
            return;
        }
        this.client.sendResponse(new RoomUserTagsComposer(habboByRoomUnitId));
    }
}
