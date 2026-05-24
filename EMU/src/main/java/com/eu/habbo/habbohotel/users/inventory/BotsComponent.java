package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotsComponent {
   private static final Logger LOGGER = LoggerFactory.getLogger(BotsComponent.class);
   private final THashMap<Integer, Bot> bots = new THashMap();

   public BotsComponent(Habbo habbo) {
      this.loadBots(habbo);
   }

   private void loadBots(Habbo habbo) {
      synchronized (this.bots) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "SELECT users.username AS owner_name, bots.* FROM bots INNER JOIN users ON users.id = bots.user_id WHERE user_id = ? AND room_id = 0 ORDER BY id ASC"
               );

               try {
                  statement.setInt(1, habbo.getHabboInfo().getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        Bot bot = Emulator.getGameEnvironment().getBotManager().loadBot(set);
                        if (bot != null) {
                           this.bots.put(set.getInt("id"), bot);
                        }
                     }
                  } catch (Throwable var12) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var11) {
                           var12.addSuppressed(var11);
                        }
                     }

                     throw var12;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var13) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var10) {
                        var13.addSuppressed(var10);
                     }
                  }

                  throw var13;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var14) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var14.addSuppressed(var9);
                  }
               }

               throw var14;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public Bot getBot(int botId) {
      return (Bot)this.bots.get(botId);
   }

   public void addBot(Bot bot) {
      synchronized (this.bots) {
         this.bots.put(bot.getId(), bot);
      }
   }

   public void removeBot(Bot bot) {
      synchronized (this.bots) {
         this.bots.remove(bot.getId());
      }
   }

   public THashMap<Integer, Bot> getBots() {
      return this.bots;
   }

   public void dispose() {
      synchronized (this.bots) {
         for (Entry<Integer, Bot> map : this.bots.entrySet()) {
            if (map.getValue().needsUpdate()) {
               Emulator.getThreading().run(map.getValue());
            }
         }

         this.bots.clear();
      }
   }
}
