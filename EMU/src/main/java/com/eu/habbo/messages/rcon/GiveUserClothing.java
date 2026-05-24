package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.users.UserClothesComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GiveUserClothing extends RCONMessage<GiveUserClothing.JSONGiveUserClothing> {
   private static final Logger LOGGER = LoggerFactory.getLogger(GiveUserClothing.class);

   public GiveUserClothing() {
      super(GiveUserClothing.JSONGiveUserClothing.class);
   }

   public void handle(Gson gson, GiveUserClothing.JSONGiveUserClothing object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users_clothing (user_id, clothing_id) VALUES (?, ?)");

            try {
               statement.setInt(1, object.user_id);
               statement.setInt(2, object.clothing_id);
               statement.execute();
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      if (habbo != null) {
         GameClient client = habbo.getClient();
         if (client != null) {
            habbo.getInventory().getWardrobeComponent().getClothing().add(object.clothing_id);
            client.sendResponse(new UserClothesComposer(habbo));
            client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FIGURESET_REDEEMED.key));
         }
      }
   }

   static class JSONGiveUserClothing {
      public int user_id;
      public int clothing_id;
   }
}
