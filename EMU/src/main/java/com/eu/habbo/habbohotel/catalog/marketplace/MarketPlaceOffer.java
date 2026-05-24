package com.eu.habbo.habbohotel.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketPlaceOffer implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlaceOffer.class);
   public int avarage;
   public int count;
   private int offerId;
   private Item baseItem;
   private int itemId;
   private int price;
   private int limitedStack;
   private int limitedNumber;
   private int timestamp = Emulator.getIntUnixTimestamp();
   private int soldTimestamp = 0;
   private MarketPlaceState state = MarketPlaceState.OPEN;
   private boolean needsUpdate = false;

   public MarketPlaceOffer(ResultSet set, boolean privateOffer) throws SQLException {
      this.offerId = set.getInt("id");
      this.price = set.getInt("price");
      this.timestamp = set.getInt("timestamp");
      this.soldTimestamp = set.getInt("sold_timestamp");
      this.baseItem = Emulator.getGameEnvironment().getItemManager().getItem(set.getInt("base_item_id"));
      this.state = MarketPlaceState.getType(set.getInt("state"));
      this.itemId = set.getInt("item_id");
      if (!set.getString("ltd_data").split(":")[1].equals("0")) {
         this.limitedStack = Integer.valueOf(set.getString("ltd_data").split(":")[0]);
         this.limitedNumber = Integer.valueOf(set.getString("ltd_data").split(":")[1]);
      }

      if (!privateOffer) {
         this.avarage = set.getInt("avg");
         this.count = set.getInt("number");
         this.price = set.getInt("minPrice");
      }
   }

   public MarketPlaceOffer(HabboItem item, int price, Habbo habbo) {
      this.price = price;
      this.baseItem = item.getBaseItem();
      this.itemId = item.getId();
      if (item.getLimitedSells() > 0) {
         this.limitedNumber = item.getLimitedSells();
         this.limitedStack = item.getLimitedStack();
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO marketplace_items (item_id, user_id, price, timestamp, state) VALUES (?, ?, ?, ?, ?)", 1
            );

            try {
               statement.setInt(1, item.getId());
               statement.setInt(2, habbo.getHabboInfo().getId());
               statement.setInt(3, this.price);
               statement.setInt(4, this.timestamp);
               statement.setString(5, this.state.getState() + "");
               statement.execute();
               ResultSet id = statement.getGeneratedKeys();

               try {
                  while (id.next()) {
                     this.offerId = id.getInt(1);
                  }
               } catch (Throwable var12) {
                  if (id != null) {
                     try {
                        id.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (id != null) {
                  id.close();
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

   public static void insert(MarketPlaceOffer offer, Habbo habbo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO marketplace_items VALUES (?, ?, ?, ?, ?, ?)", 1);

            try {
               statement.setInt(1, offer.getItemId());
               statement.setInt(2, habbo.getHabboInfo().getId());
               statement.setInt(3, offer.getPrice());
               statement.setInt(4, offer.getTimestamp());
               statement.setInt(5, offer.getSoldTimestamp());
               statement.setString(6, offer.getState().getState() + "");
               statement.execute();
               ResultSet id = statement.getGeneratedKeys();

               try {
                  while (id.next()) {
                     offer.setOfferId(id.getInt(1));
                  }
               } catch (Throwable var10) {
                  if (id != null) {
                     try {
                        id.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (id != null) {
                  id.close();
               }
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
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
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

   public int getOfferId() {
      return this.offerId;
   }

   public void setOfferId(int offerId) {
      this.offerId = offerId;
   }

   public int getItemId() {
      return this.baseItem.getSpriteId();
   }

   public int getPrice() {
      return this.price;
   }

   public MarketPlaceState getState() {
      return this.state;
   }

   public void setState(MarketPlaceState state) {
      this.state = state;
   }

   public int getTimestamp() {
      return this.timestamp;
   }

   public int getSoldTimestamp() {
      return this.soldTimestamp;
   }

   public void setSoldTimestamp(int soldTimestamp) {
      this.soldTimestamp = soldTimestamp;
   }

   public int getLimitedStack() {
      return this.limitedStack;
   }

   public int getLimitedNumber() {
      return this.limitedNumber;
   }

   public int getSoldItemId() {
      return this.itemId;
   }

   public void needsUpdate(boolean value) {
      this.needsUpdate = value;
   }

   public int getType() {
      if (this.limitedStack > 0) {
         return 3;
      } else {
         return this.baseItem.getType().equals(FurnitureType.WALL) ? 2 : 1;
      }
   }

   @Override
   public void run() {
      if (this.needsUpdate) {
         this.needsUpdate = false;

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE marketplace_items SET state = ?, sold_timestamp = ? WHERE id = ?");

               try {
                  statement.setInt(1, this.state.getState());
                  statement.setInt(2, this.soldTimestamp);
                  statement.setInt(3, this.offerId);
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
}
