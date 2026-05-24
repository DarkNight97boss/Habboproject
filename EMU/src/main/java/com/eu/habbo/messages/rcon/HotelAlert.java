package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import com.google.gson.Gson;
import java.util.Map.Entry;

public class HotelAlert extends RCONMessage<HotelAlert.JSONHotelAlert> {
   public HotelAlert() {
      super(HotelAlert.JSONHotelAlert.class);
   }

   public void handle(Gson gson, HotelAlert.JSONHotelAlert object) {
      ServerMessage serverMessage;
      if (object.url.isEmpty()) {
         serverMessage = new GenericAlertComposer(object.message).compose();
      } else {
         serverMessage = new StaffAlertWithLinkComposer(object.message, object.url).compose();
      }

      if (serverMessage != null) {
         for (Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
            Habbo habbo = set.getValue();
            if (!habbo.getHabboStats().blockStaffAlerts) {
               habbo.getClient().sendResponse(serverMessage);
            }
         }
      }
   }

   static class JSONHotelAlert {
      public String message;
      public String url = "";
   }
}
