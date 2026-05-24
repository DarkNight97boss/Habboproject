package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GiveCredits extends RCONMessage<GiveCredits.JSONGiveCredits> {
   private static final Logger LOGGER = LoggerFactory.getLogger(GiveCredits.class);

   public GiveCredits() {
      super(GiveCredits.JSONGiveCredits.class);
   }

   public void handle(Gson gson, GiveCredits.JSONGiveCredits object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         habbo.giveCredits(object.credits);
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users SET credits = credits + ? WHERE id = ? LIMIT 1");

               try {
                  statement.setInt(1, object.credits);
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

   static class JSONGiveCredits {
      public int user_id;
      public int credits;
   }
}
