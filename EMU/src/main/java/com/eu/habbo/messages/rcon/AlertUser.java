package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

public class AlertUser extends RCONMessage<AlertUser.JSONAlertUser> {
   public AlertUser() {
      super(AlertUser.JSONAlertUser.class);
   }

   public void handle(Gson gson, AlertUser.JSONAlertUser object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         habbo.alert(object.message);
      }

      this.status = 2;
   }

   static class JSONAlertUser {
      int user_id;
      String message;
   }
}
