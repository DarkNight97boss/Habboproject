package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreUser extends RCONMessage<IgnoreUser.JSONIgnoreUser> {
   private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreUser.class);

   public IgnoreUser() {
      super(IgnoreUser.JSONIgnoreUser.class);
   }

   public void handle(Gson gson, IgnoreUser.JSONIgnoreUser object) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(object.user_id);
      if (habbo != null) {
         habbo.getHabboStats().ignoreUser(habbo.getClient(), object.target_id);
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO users_ignored (user_id, target_id) VALUES (?, ?)");

               try {
                  statement.setInt(1, object.user_id);
                  statement.setInt(2, object.target_id);
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

         this.message = "offline";
      }
   }

   static class JSONIgnoreUser {
      public int user_id;
      public int target_id;
   }
}
