package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.friends.SearchUserEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsEvent;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.threading.runnables.AchievementUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanerThread implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(CleanerThread.class);
   private static final int DELAY = 10000;
   private static final int RELOAD_HALL_OF_FAME = 1800;
   private static final int RELOAD_NEWS_LIST = 3600;
   private static final int REMOVE_INACTIVE_ROOMS = 120;
   private static final int REMOVE_INACTIVE_GUILDS = 60;
   private static final int REMOVE_INACTIVE_TOURS = 600;
   private static final int SAVE_ERROR_LOGS = 30;
   private static final int CLEAR_CACHED_VALUES = 3600;
   private static final int CALLBACK_TIME = 900;
   private static int LAST_HOF_RELOAD = Emulator.getIntUnixTimestamp();
   private static int LAST_NL_RELOAD = Emulator.getIntUnixTimestamp();
   private static int LAST_INACTIVE_ROOMS_CLEARED = Emulator.getIntUnixTimestamp();
   private static int LAST_INACTIVE_GUILDS_CLEARED = Emulator.getIntUnixTimestamp();
   private static int LAST_INACTIVE_TOURS_CLEARED = Emulator.getIntUnixTimestamp();
   private static int LAST_ERROR_LOGS_SAVED = Emulator.getIntUnixTimestamp();
   private static int LAST_DAILY_REFILL = Emulator.getIntUnixTimestamp();
   private static int LAST_CALLBACK = Emulator.getIntUnixTimestamp();
   private static int LAST_HABBO_CACHE_CLEARED = Emulator.getIntUnixTimestamp();

   public CleanerThread() {
      this.databaseCleanup();
      Emulator.getThreading().run(this, 10000L);
      Emulator.getThreading().run(new AchievementUpdater());
   }

   @Override
   public void run() {
      Emulator.getThreading().run(this, 10000L);
      int time = Emulator.getIntUnixTimestamp();
      if (time - LAST_HOF_RELOAD > 1800) {
         Emulator.getGameEnvironment().getHotelViewManager().getHallOfFame().reload();
         LAST_HOF_RELOAD = time;
      }

      if (time - LAST_NL_RELOAD > 3600) {
         Emulator.getGameEnvironment().getHotelViewManager().getNewsList().reload();
         LAST_NL_RELOAD = time;
      }

      if (time - LAST_INACTIVE_ROOMS_CLEARED > 120) {
         Emulator.getGameEnvironment().getRoomManager().clearInactiveRooms();
         LAST_INACTIVE_ROOMS_CLEARED = time;
      }

      if (time - LAST_INACTIVE_GUILDS_CLEARED > 60) {
         Emulator.getGameEnvironment().getGuildManager().clearInactiveGuilds();
         ForumThread.clearCache();
         LAST_INACTIVE_GUILDS_CLEARED = time;
      }

      if (time - LAST_INACTIVE_TOURS_CLEARED > 600) {
         Emulator.getGameEnvironment().getGuideManager().cleanup();
         LAST_INACTIVE_TOURS_CLEARED = time;
      }

      if (time - LAST_ERROR_LOGS_SAVED > 30) {
         Emulator.getDatabaseLogger().save();
         LAST_ERROR_LOGS_SAVED = time;
      }

      if (time - LAST_CALLBACK > 900) {
         LAST_CALLBACK = time;
      }

      if (time - LAST_DAILY_REFILL > Emulator.getConfig().getInt("hotel.refill.daily")) {
         this.refillDailyRespects();
         LAST_DAILY_REFILL = time;
      }

      if (time - LAST_HABBO_CACHE_CLEARED > 3600) {
         this.clearCachedValues();
         LAST_HABBO_CACHE_CLEARED = time;
      }

      SearchRoomsEvent.cachedResults.clear();
      SearchUserEvent.cachedResults.clear();
   }

   void databaseCleanup() {
      this.refillDailyRespects();
      int time = Emulator.getIntUnixTimestamp();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               statement.execute("UPDATE users SET online = '0' WHERE online = '1'");
               statement.execute("UPDATE rooms SET users = '0' WHERE users > 0");
               statement.execute("DELETE FROM room_mutes WHERE ends < " + time);
               statement.execute("DELETE FROM room_bans WHERE ends < " + time);
               statement.execute("DELETE users_favorite_rooms FROM users_favorite_rooms LEFT JOIN rooms ON room_id = rooms.id WHERE rooms.id IS NULL");
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

            statement = connection.prepareStatement(
               "UPDATE users_effects SET total = total - 1 WHERE activation_timestamp + duration < ? AND activation_timestamp > 0 AND duration > 0"
            );

            try {
               ((java.sql.PreparedStatement) statement).setInt(1, Emulator.getIntUnixTimestamp());
               ((java.sql.PreparedStatement) statement).execute();
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }

            statement = connection.createStatement();

            try {
               statement.execute("DELETE FROM users_effects WHERE total <= 0");
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var13.addSuppressed(var6);
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

      LOGGER.info("Database -> Cleaned!");
   }

   public void refillDailyRespects() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET daily_respect_points = ?, daily_pet_respect_points = ?");

            try {
               statement.setInt(1, Emulator.getConfig().getInt("hotel.daily.respect"));
               statement.setInt(2, Emulator.getConfig().getInt("hotel.daily.respect.pets"));
               statement.executeUpdate();
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

      if (Emulator.isReady) {
         for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
            habbo.getHabboStats().respectPointsToGive = Emulator.getConfig().getInt("hotel.daily.respect");
            habbo.getHabboStats().petRespectPointsToGive = Emulator.getConfig().getInt("hotel.daily.respect.pets");
            habbo.getClient().sendResponse(new UserDataComposer(habbo));
         }
      }
   }

   private void clearCachedValues() {
      for (Entry<Integer, Habbo> map : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
         Habbo habbo = map.getValue();

         try {
            if (habbo != null) {
               habbo.clearCaches();
            }
         } catch (Exception e) {
            LOGGER.error("Caught exception", e);
         }
      }
   }
}
