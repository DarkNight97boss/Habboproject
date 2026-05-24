package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RoomWordFilterModifyEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      int roomId = this.packet.readInt();
      boolean add = this.packet.readBoolean();
      String word = this.packet.readString();
      if (word.length() > 25) {
         word = word.substring(0, 24);
      }

      Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
      if (room != null && room.getId() == roomId) {
         if (!room.isOwner(this.client.getHabbo())) {
            ScripterManager.scripterDetected(
               this.client, String.format("User (%s) tried to change wordfilter for a not owned room.", this.client.getHabbo().getHabboInfo().getUsername())
            );
         } else {
            if (add) {
               room.addToWordFilter(word);
            } else {
               room.removeFromWordFilter(word);
            }
         }
      }
   }
}
