package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomMutedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomMuteEvent.class */
public class RoomMuteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || !currentRoom.isOwner(this.client.getHabbo())) {
            return;
        }
        if (currentRoom.isMuted()) {
            currentRoom.setMuted(false);
            this.client.sendResponse(new RoomMutedComposer(currentRoom));
            currentRoom.sendWhisper("The room has been unmuted!", RoomChatMessageBubbles.ALERT);
        } else {
            currentRoom.setMuted(true);
            this.client.sendResponse(new RoomMutedComposer(currentRoom));
            currentRoom.sendWhisper("The room has been muted!", RoomChatMessageBubbles.ALERT);
        }
    }
}
