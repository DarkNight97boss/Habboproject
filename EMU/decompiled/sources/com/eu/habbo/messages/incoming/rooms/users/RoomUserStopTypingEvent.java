package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTypingComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserStopTypingEvent.class */
public class RoomUserStopTypingEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || this.client.getHabbo().getRoomUnit() == null) {
            return;
        }
        this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserTypingComposer(this.client.getHabbo().getRoomUnit(), false).compose());
    }
}
