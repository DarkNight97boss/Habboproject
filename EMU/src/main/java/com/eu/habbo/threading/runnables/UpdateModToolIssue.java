package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateModToolIssue implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateModToolIssue.class);
   private final ModToolIssue issue;

   public UpdateModToolIssue(ModToolIssue issue) {
      this.issue = issue;
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE support_tickets SET state = ?, type = ?, mod_id = ?, category = ? WHERE id = ?");

            try {
               statement.setInt(1, this.issue.state.getState());
               statement.setInt(2, this.issue.type.getType());
               statement.setInt(3, this.issue.modId);
               statement.setInt(4, this.issue.category);
               statement.setInt(5, this.issue.id);
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
