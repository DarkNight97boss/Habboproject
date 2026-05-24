package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseLogger {
   private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseLogger.class);
   private final ConcurrentLinkedQueue<DatabaseLoggable> loggables = new ConcurrentLinkedQueue<>();

   public void store(DatabaseLoggable loggable) {
      this.loggables.add(loggable);
   }

   public void save() {
      if (Emulator.getDatabase() != null && Emulator.getDatabase().getDataSource() != null) {
         if (!this.loggables.isEmpty()) {
            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  while (!this.loggables.isEmpty()) {
                     DatabaseLoggable loggable = this.loggables.remove();
                     PreparedStatement statement = connection.prepareStatement(loggable.getQuery());

                     try {
                        loggable.log(statement);
                        statement.executeBatch();
                     } catch (Throwable var8) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var7) {
                              var8.addSuppressed(var7);
                           }
                        }

                        throw var8;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                  }
               } catch (Throwable var9) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException e) {
               LOGGER.error("Exception caught while saving loggables to database.", e);
            }
         }
      }
   }
}
