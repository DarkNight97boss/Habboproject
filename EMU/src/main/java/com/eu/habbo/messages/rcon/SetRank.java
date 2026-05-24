package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

public class SetRank extends RCONMessage<SetRank.JSONSetRank> {
   public SetRank() {
      super(SetRank.JSONSetRank.class);
   }

   public void handle(Gson gson, SetRank.JSONSetRank object) {
      try {
         Emulator.getGameEnvironment().getHabboManager().setRank(object.user_id, object.rank);
      } catch (Exception e) {
         this.status = 4;
         this.message = "invalid rank";
         return;
      }

      this.message = "updated offline user";
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         this.message = "updated online user";
      }
   }

   static class JSONSetRank {
      public int user_id;
      public int rank;
   }
}
