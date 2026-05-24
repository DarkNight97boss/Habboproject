package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDeleteHabboItem implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(QueryDeleteHabboItem.class);
   private final int itemId;

   public QueryDeleteHabboItem(int itemId) {
      this.itemId = itemId;
   }

   public QueryDeleteHabboItem(HabboItem item) {
      this.itemId = item.getId();
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM items WHERE id = ?");

            try {
               statement.setInt(1, this.itemId);
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
