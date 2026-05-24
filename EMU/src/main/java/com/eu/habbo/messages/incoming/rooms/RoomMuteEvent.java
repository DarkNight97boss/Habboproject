package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomMutedComposer;

public class RoomMuteEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
      if (room != null && room.isOwner(this.client.getHabbo())) {
         if (room.isMuted()) {
            room.setMuted(false);
            this.client.sendResponse(new RoomMutedComposer(room));
            room.sendWhisper("The room has been unmuted!", RoomChatMessageBubbles.ALERT);
         } else {
            room.setMuted(true);
            this.client.sendResponse(new RoomMutedComposer(room));
            room.sendWhisper("The room has been muted!", RoomChatMessageBubbles.ALERT);
         }
      }
   }
}
