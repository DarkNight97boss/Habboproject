package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetMotto extends RCONMessage<SetMotto.SetMottoJSON> {
   private static final Logger LOGGER = LoggerFactory.getLogger(SetMotto.class);

   public SetMotto() {
      super(SetMotto.SetMottoJSON.class);
   }

   public void handle(Gson gson, SetMotto.SetMottoJSON json) {
      Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
      if (habbo != null) {
         habbo.getHabboInfo().setMotto(json.motto);
         habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users SET motto = ? WHERE id = ? LIMIT 1");

               try {
                  statement.setString(1, json.motto);
                  statement.setInt(2, json.user_id);
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
            LOGGER.error("Caught exception", e);
         }
      }
   }

   static class SetMottoJSON {
      public int user_id;
      public String motto;
   }
}
