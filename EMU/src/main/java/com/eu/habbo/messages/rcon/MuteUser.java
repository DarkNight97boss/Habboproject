package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuteUser extends RCONMessage<MuteUser.JSON> {
   private static final Logger LOGGER = LoggerFactory.getLogger(MuteUser.class);

   public MuteUser() {
      super(MuteUser.JSON.class);
   }

   public void handle(Gson gson, MuteUser.JSON json) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
      if (habbo != null) {
         if (json.duration == 0) {
            habbo.unMute();
         } else {
            habbo.mute(json.duration, false);
         }
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET mute_end_timestamp = ? WHERE user_id = ? LIMIT 1");

               try {
                  statement.setInt(1, Emulator.getIntUnixTimestamp() + json.duration);
                  statement.setInt(2, json.user_id);
                  if (statement.executeUpdate() == 0) {
                     this.status = 2;
                  }
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
      }
   }

   static class JSON {
      public int user_id;
      public int duration;
   }
}
