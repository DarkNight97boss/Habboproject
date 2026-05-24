package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GiveRespect extends RCONMessage<GiveRespect.JSONGiveRespect> {
   private static final Logger LOGGER = LoggerFactory.getLogger(GiveRespect.class);

   public GiveRespect() {
      super(GiveRespect.JSONGiveRespect.class);
   }

   public void handle(Gson gson, GiveRespect.JSONGiveRespect object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         HabboStats var10000 = habbo.getHabboStats();
         var10000.respectPointsReceived = var10000.respectPointsReceived + object.respect_received;
         var10000 = habbo.getHabboStats();
         var10000.respectPointsGiven = var10000.respectPointsGiven + object.respect_given;
         var10000 = habbo.getHabboStats();
         var10000.respectPointsToGive = var10000.respectPointsToGive + object.daily_respects;
         habbo.getClient().sendResponse(new UserDataComposer(habbo));
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "UPDATE users_settings SET respects_given = respects_give + ?, respects_received = respects_received + ?, daily_respect_points = daily_respect_points + ? WHERE user_id = ? LIMIT 1"
               );

               try {
                  statement.setInt(1, object.respect_received);
                  statement.setInt(2, object.respect_given);
                  statement.setInt(3, object.daily_respects);
                  statement.setInt(4, object.user_id);
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

   static class JSONGiveRespect {
      public int user_id;
      public int respect_given = 0;
      public int respect_received = 0;
      public int daily_respects = 0;
   }
}
