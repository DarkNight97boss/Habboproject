package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GivePixels extends RCONMessage<GivePixels.JSONGivePixels> {
   private static final Logger LOGGER = LoggerFactory.getLogger(GivePixels.class);

   public GivePixels() {
      super(GivePixels.JSONGivePixels.class);
   }

   public void handle(Gson gson, GivePixels.JSONGivePixels object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         habbo.givePixels(object.pixels);
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "UPDATE users_currency SET users_currency.amount = users_currency.amount + ? WHERE users_currency.user_id = ? AND users_currency.type = 0"
               );

               try {
                  statement.setInt(1, object.pixels);
                  statement.setInt(2, object.user_id);
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
            this.status = 4;
            LOGGER.error("Caught SQL exception", e);
         }

         this.message = "offline";
      }
   }

   static class JSONGivePixels {
      public int user_id;
      public int pixels;
   }
}
