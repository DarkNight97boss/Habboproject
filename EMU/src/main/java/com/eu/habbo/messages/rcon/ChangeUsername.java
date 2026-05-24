package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeUsername extends RCONMessage<ChangeUsername.JSON> {
   private static final Logger LOGGER = LoggerFactory.getLogger(ChangeUsername.class);

   public ChangeUsername() {
      super(ChangeUsername.JSON.class);
   }

   public void handle(Gson gson, ChangeUsername.JSON json) {
      try {
         if (json.user_id <= 0) {
            this.status = 2;
            this.message = "User not found";
            return;
         }

         boolean success = true;
         Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(json.user_id);
         if (habbo != null) {
            if (json.canChange) {
               habbo.alert(Emulator.getTexts().getValue("rcon.alert.user.change_username"));
            }

            habbo.getHabboStats().allowNameChange = json.canChange;
            habbo.getClient().sendResponse(new UserDataComposer(habbo));
         } else {
            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  try {
                     PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET allow_name_change = ? WHERE user_id = ? LIMIT 1");

                     try {
                        statement.setBoolean(1, json.canChange);
                        statement.setInt(2, json.user_id);
                        success = statement.executeUpdate() >= 1;
                     } catch (Throwable var11) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var10) {
                              var11.addSuppressed(var10);
                           }
                        }

                        throw var11;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                  } catch (SQLException sqlException) {
                     sqlException.printStackTrace();
                  }
               } catch (Throwable var13) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var9) {
                        var13.addSuppressed(var9);
                     }
                  }

                  throw var13;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException sqlException) {
               sqlException.printStackTrace();
            }
         }

         this.status = success ? 0 : 1;
         this.message = success ? "Sent successfully." : "There was an error updating this user.";
      } catch (Exception e) {
         this.status = 4;
         this.message = "Exception occurred";
         LOGGER.error("Exception occurred", e);
      }
   }

   static class JSON {
      public int user_id;
      public boolean canChange;
   }
}
