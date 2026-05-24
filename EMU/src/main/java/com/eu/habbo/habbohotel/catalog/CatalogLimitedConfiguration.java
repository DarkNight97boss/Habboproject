package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogLimitedConfiguration implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(CatalogLimitedConfiguration.class);
   private final int itemId;
   private final LinkedList<Integer> limitedNumbers;
   private int totalSet;

   public CatalogLimitedConfiguration(int itemId, LinkedList<Integer> availableNumbers, int totalSet) {
      this.itemId = itemId;
      this.totalSet = totalSet;
      this.limitedNumbers = availableNumbers;
      if (Emulator.getConfig().getBoolean("catalog.ltd.random", true)) {
         Collections.shuffle(this.limitedNumbers);
      } else {
         Collections.reverse(this.limitedNumbers);
      }
   }

   public int getNumber() {
      synchronized (this.limitedNumbers) {
         int num = this.limitedNumbers.pop();
         if (this.limitedNumbers.isEmpty()) {
            Emulator.getGameEnvironment()
               .getCatalogManager()
               .moveCatalogItem(
                  Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(this.itemId), Emulator.getConfig().getInt("catalog.ltd.page.soldout")
               );
         }

         return num;
      }
   }

   public void limitedSold(int catalogItemId, Habbo habbo, HabboItem item) {
      synchronized (this.limitedNumbers) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "UPDATE catalog_items_limited SET user_id = ?, timestamp = ?, item_id = ? WHERE catalog_item_id = ? AND number = ? AND user_id = 0 LIMIT 1"
               );

               try {
                  statement.setInt(1, habbo.getHabboInfo().getId());
                  statement.setInt(2, Emulator.getIntUnixTimestamp());
                  statement.setInt(3, item.getId());
                  statement.setInt(4, catalogItemId);
                  statement.setInt(5, item.getLimitedSells());
                  statement.execute();
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
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
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
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

   public void generateNumbers(int starting, int amount) {
      synchronized (this.limitedNumbers) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO catalog_items_limited (catalog_item_id, number) VALUES (?, ?)");

               try {
                  statement.setInt(1, this.itemId);

                  for (int i = starting; i <= amount; i++) {
                     statement.setInt(2, i);
                     statement.addBatch();
                     this.limitedNumbers.push(i);
                  }

                  statement.executeBatch();
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
            } catch (Throwable var12) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
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

         this.totalSet += amount;
         if (Emulator.getConfig().getBoolean("catalog.ltd.random", true)) {
            Collections.shuffle(this.limitedNumbers);
         } else {
            Collections.reverse(this.limitedNumbers);
         }
      }
   }

   public int available() {
      return this.limitedNumbers.size();
   }

   public int getTotalSet() {
      return this.totalSet;
   }

   public void setTotalSet(int totalSet) {
      this.totalSet = totalSet;
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE catalog_items SET limited_stack = ?, limited_sells = ? WHERE id = ?");

            try {
               statement.setInt(1, this.totalSet);
               statement.setInt(2, this.totalSet - this.available());
               statement.setInt(3, this.itemId);
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
