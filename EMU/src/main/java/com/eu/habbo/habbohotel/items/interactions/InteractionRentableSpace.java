package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import com.eu.habbo.threading.runnables.ClearRentedSpace;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionRentableSpace extends HabboItem {
   private static final Logger LOGGER = LoggerFactory.getLogger(InteractionRentableSpace.class);
   private int renterId;
   private String renterName;
   private int endTimestamp;

   public InteractionRentableSpace(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
      String[] data = set.getString("extra_data").split(":");
      this.renterName = "Unknown";
      if (data.length == 2) {
         this.renterId = Integer.valueOf(data[0]);
         this.endTimestamp = Integer.valueOf(data[1]);
         if (this.renterId > 0) {
            if (this.isRented()) {
               Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.renterId);
               if (habbo != null) {
                  this.renterName = habbo.getHabboInfo().getUsername();
               } else {
                  try {
                     Connection connection = Emulator.getDatabase().getDataSource().getConnection();

                     try {
                        PreparedStatement statement = connection.prepareStatement("SELECT username FROM users WHERE id = ? LIMIT 1");

                        try {
                           statement.setInt(1, this.renterId);
                           ResultSet row = statement.executeQuery();

                           try {
                              if (row.next()) {
                                 this.renterName = row.getString("username");
                              }
                           } catch (Throwable var13) {
                              if (row != null) {
                                 try {
                                    row.close();
                                 } catch (Throwable var12) {
                                    var13.addSuppressed(var12);
                                 }
                              }

                              throw var13;
                           }

                           if (row != null) {
                              row.close();
                           }
                        } catch (Throwable var14) {
                           if (statement != null) {
                              try {
                                 statement.close();
                              } catch (Throwable var11) {
                                 var14.addSuppressed(var11);
                              }
                           }

                           throw var14;
                        }

                        if (statement != null) {
                           statement.close();
                        }
                     } catch (Throwable var15) {
                        if (connection != null) {
                           try {
                              connection.close();
                           } catch (Throwable var10) {
                              var15.addSuppressed(var10);
                           }
                        }

                        throw var15;
                     }

                     if (connection != null) {
                        connection.close();
                     }
                  } catch (SQLException e) {
                     LOGGER.error("Caught SQL exception", e);
                  }
               }
            } else if (this.getRoomId() > 0) {
               Emulator.getThreading().run(new ClearRentedSpace(this, Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId())));
               this.renterId = 0;
            }
         }
      }
   }

   public InteractionRentableSpace(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
      this.renterName = "";
   }

   @Override
   public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
      if (this.getExtradata().isEmpty()) {
         return false;
      } else {
         Habbo habbo = room.getHabbo(roomUnit);
         if (habbo == null) {
            return true;
         } else if (habbo.getHabboInfo().getId() == room.getId()) {
            return true;
         } else {
            return this.endTimestamp <= Emulator.getIntUnixTimestamp() ? false : this.renterId > 0 && this.renterId == habbo.getHabboInfo().getId();
         }
      }
   }

   @Override
   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      this.sendRentWidget(client.getHabbo());
   }

   @Override
   public boolean isWalkable() {
      return true;
   }

   @Override
   public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
   }

   @Override
   public void serializeExtradata(ServerMessage serverMessage) {
      if (this.getExtradata().isEmpty()) {
         this.setExtradata("0:0");
      }

      serverMessage.appendInt(1 + (this.isLimited() ? 256 : 0));
      if (this.isRented()) {
         serverMessage.appendInt(1);
         serverMessage.appendString("renterId");
         serverMessage.appendString(this.renterId + "");
      } else {
         serverMessage.appendInt(0);
      }

      super.serializeExtradata(serverMessage);
   }

   public void rent(Habbo habbo) {
      if (!this.isRented()) {
         if (!habbo.getHabboStats().isRentingSpace()) {
            if (habbo.getHabboInfo().getCredits() >= this.rentCost()) {
               if (habbo.getHabboStats().getClubExpireTimestamp() >= Emulator.getIntUnixTimestamp()) {
                  this.setRenterId(habbo.getHabboInfo().getId());
                  this.setRenterName(habbo.getHabboInfo().getUsername());
                  this.setEndTimestamp(Emulator.getIntUnixTimestamp() + 604800);
                  habbo.getHabboStats().setRentedItemId(this.getId());
                  habbo.getHabboStats().setRentedTimeEnd(this.endTimestamp);
                  this.needsUpdate(true);
                  this.run();
               }
            }
         }
      }
   }

   public void endRent() {
      this.setEndTimestamp(0);
      Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
      if (room != null) {
         Rectangle rect = RoomLayout.getRectangle(this.getX(), this.getY(), this.getBaseItem().getWidth(), this.getBaseItem().getLength(), this.getRotation());
         THashSet<HabboItem> items = new THashSet();

         for (int i = rect.x; i < rect.x + rect.getWidth(); i++) {
            for (int j = rect.y; j < rect.y + rect.getHeight(); j++) {
               items.addAll(room.getItemsAt(i, j, this.getZ()));
            }
         }

         TObjectHashIterator var15 = items.iterator();

         while (var15.hasNext()) {
            HabboItem item = (HabboItem)var15.next();
            if (item.getUserId() == this.renterId) {
               room.pickUpItem(item, null);
            }
         }

         Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.renterId);
         if (habbo != null) {
            habbo.getHabboStats().setRentedItemId(0);
            habbo.getHabboStats().setRentedTimeEnd(0);
         } else {
            int zero = 0;

            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users_settings SET rent_space_id = ?, rent_space_endtime = ? WHERE user_id = ? LIMIT 1"
                  );

                  try {
                     statement.setInt(1, zero);
                     statement.setInt(2, zero);
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

         this.setRenterId(0);
         this.setRenterName("");
         this.needsUpdate(true);
         this.run();
      }
   }

   @Override
   public String getExtradata() {
      return this.renterId + ":" + this.endTimestamp;
   }

   public int getRenterId() {
      return this.renterId;
   }

   public void setRenterId(int renterId) {
      this.renterId = renterId;
   }

   public String getRenterName() {
      return this.renterName;
   }

   public void setRenterName(String renterName) {
      this.renterName = renterName;
   }

   public int getEndTimestamp() {
      return this.endTimestamp;
   }

   public void setEndTimestamp(int endTimestamp) {
      this.endTimestamp = endTimestamp;
   }

   public boolean isRented() {
      return this.endTimestamp > Emulator.getIntUnixTimestamp();
   }

   public int rentCost() {
      String[] data = this.getBaseItem().getName().replace("hblooza_spacerent", "").split("x");
      if (data.length == 2) {
         int x = Integer.valueOf(data[0]);
         int y = Integer.valueOf(data[1]);
         return 10 * x * y;
      } else {
         return 1337;
      }
   }

   public int getRentErrorCode(Habbo habbo) {
      if (this.isRented() && this.renterId != habbo.getHabboInfo().getId()) {
         return 100;
      } else if (habbo.getHabboStats().isRentingSpace() && habbo.getHabboStats().getRentedItemId() != this.getId()) {
         return 103;
      } else if (habbo.getHabboStats().getClubExpireTimestamp() < Emulator.getIntUnixTimestamp()) {
         return 203;
      } else {
         return this.rentCost() > habbo.getHabboInfo().getCredits() ? 200 : 0;
      }
   }

   public void sendRentWidget(Habbo habbo) {
      habbo.getClient().sendResponse(new RentableSpaceInfoComposer(habbo, this, this.getRentErrorCode(habbo)));
   }
}
