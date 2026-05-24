package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertModToolIssue implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(InsertModToolIssue.class);
   private final ModToolIssue issue;

   public InsertModToolIssue(ModToolIssue issue) {
      this.issue = issue;
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO support_tickets (state, timestamp, score, sender_id, reported_id, room_id, mod_id, issue, category, group_id, thread_id, comment_id, photo_item_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
               1
            );

            try {
               statement.setInt(1, this.issue.state.getState());
               statement.setInt(2, this.issue.timestamp);
               statement.setInt(3, this.issue.priority);
               statement.setInt(4, this.issue.senderId);
               statement.setInt(5, this.issue.reportedId);
               statement.setInt(6, this.issue.roomId);
               statement.setInt(7, this.issue.modId);
               statement.setString(8, this.issue.message);
               statement.setInt(9, this.issue.category);
               statement.setInt(10, this.issue.groupId);
               statement.setInt(11, this.issue.threadId);
               statement.setInt(12, this.issue.commentId);
               statement.setInt(13, this.issue.photoItem != null ? this.issue.photoItem.getId() : -1);
               statement.execute();
               ResultSet key = statement.getGeneratedKeys();

               try {
                  if (key.first()) {
                     this.issue.id = key.getInt(1);
                  }
               } catch (Throwable var14) {
                  if (key != null) {
                     try {
                        key.close();
                     } catch (Throwable var10) {
                        var14.addSuppressed(var10);
                     }
                  }

                  throw var14;
               }

               if (key != null) {
                  key.close();
               }
            } catch (Throwable var15) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var15.addSuppressed(var9);
                  }
               }

               throw var15;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var16) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var16.addSuppressed(var8);
               }
            }

            throw var16;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET cfh_send = cfh_send + 1 WHERE user_id = ?");

            try {
               statement.setInt(1, this.issue.senderId);
               statement.execute();
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var11.addSuppressed(var7);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var12.addSuppressed(var6);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }
}
