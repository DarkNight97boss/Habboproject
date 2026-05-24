package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueryDeleteHabboBadge implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(QueryDeleteHabboBadge.class);
   private final String name;
   private final Habbo habbo;

   public QueryDeleteHabboBadge(Habbo habbo, String name) {
      this.name = name;
      this.habbo = habbo;
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM user_badges WHERE users_id = ? AND badge_code = ?");

            try {
               statement.setInt(1, this.habbo.getHabboInfo().getId());
               statement.setString(2, this.name);
               statement.execute();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var8) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }
}
