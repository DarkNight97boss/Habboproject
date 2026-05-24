package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTypingComposer;

public class RoomUserStartTypingEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
         if (this.client.getHabbo().getRoomUnit() != null) {
            this.client
               .getHabbo()
               .getHabboInfo()
               .getCurrentRoom()
               .sendComposer(new RoomUserTypingComposer(this.client.getHabbo().getRoomUnit(), true).compose());
         }
      }
   }
}
