package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.google.gson.Gson;

public class ForwardUser extends RCONMessage<ForwardUser.ForwardUserJSON> {
   public ForwardUser() {
      super(ForwardUser.ForwardUserJSON.class);
   }

   public void handle(Gson gson, ForwardUser.ForwardUserJSON object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(object.room_id);
         if (room != null) {
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
               Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo, habbo.getHabboInfo().getCurrentRoom());
            }

            habbo.getClient().sendResponse(new ForwardToRoomComposer(object.room_id));
            Emulator.getGameEnvironment().getRoomManager().enterRoom(habbo, object.room_id, "", true);
         } else {
            this.status = 3;
         }
      }

      this.status = 2;
   }

   static class ForwardUserJSON {
      public int user_id;
      public int room_id;
   }
}
