package com.eu.habbo.habbohotel.hotelview;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsList {
   private static final Logger LOGGER = LoggerFactory.getLogger(NewsList.class);
   private final ArrayList<NewsWidget> newsWidgets = new ArrayList<>();

   public NewsList() {
      this.reload();
   }

   public void reload() {
      synchronized (this.newsWidgets) {
         this.newsWidgets.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM hotelview_news ORDER BY id DESC LIMIT 10");

                  try {
                     while (set.next()) {
                        this.newsWidgets.add(new NewsWidget(set));
                     }
                  } catch (Throwable var11) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public ArrayList<NewsWidget> getNewsWidgets() {
      return this.newsWidgets;
   }
}
