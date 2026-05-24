package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.navigator.CanCreateRoomComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomBundleLayout extends SingleBundle {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoomBundleLayout.class);
   public int roomId;
   public Room room;
   private int lastUpdate = 0;
   private boolean loaded = false;

   public RoomBundleLayout(ResultSet set) throws SQLException {
      super(set);
      this.roomId = set.getInt("room_id");
   }

   @Override
   public TIntObjectMap<CatalogItem> getCatalogItems() {
      if (Emulator.getIntUnixTimestamp() - this.lastUpdate < 120) {
         this.lastUpdate = Emulator.getIntUnixTimestamp();
         return super.getCatalogItems();
      }

      if (this.room == null) {
         if (this.roomId > 0) {
            this.room = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.roomId);
            if (this.room != null) {
               this.room.preventUnloading = true;
            }
         } else {
            LOGGER.error("No room id specified for room bundle " + this.getPageName() + "(" + this.getId() + ")");
         }
      }

      if (this.room == null) {
         return super.getCatalogItems();
      }

      final CatalogItem[] item = new CatalogItem[]{null};
      super.getCatalogItems().forEachValue(new TObjectProcedure<CatalogItem>() {
         public boolean execute(CatalogItem object) {
            if (object == null) {
               return true;
            }

            item[0] = object;
            return false;
         }
      });
      if (this.room.isPreLoaded()) {
         this.room.loadData();
         this.room.preventUncaching = true;
         this.room.preventUnloading = true;
      }

      if (item[0] != null) {
         item[0].getBundle().clear();
         THashMap<Item, Integer> items = new THashMap();
         TObjectHashIterator data = this.room.getFloorItems().iterator();

         while (data.hasNext()) {
            HabboItem i = (HabboItem)data.next();
            if (!items.contains(i.getBaseItem())) {
               items.put(i.getBaseItem(), 0);
            }

            items.put(i.getBaseItem(), (Integer)items.get(i.getBaseItem()) + 1);
         }

         data = this.room.getWallItems().iterator();

         while (data.hasNext()) {
            HabboItem i = (HabboItem)data.next();
            if (!items.contains(i.getBaseItem())) {
               items.put(i.getBaseItem(), 0);
            }

            items.put(i.getBaseItem(), (Integer)items.get(i.getBaseItem()) + 1);
         }

         if (!item[0].getExtradata().isEmpty()) {
            items.put(Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(item[0].getExtradata())), 1);
         }

         StringBuilder datax = new StringBuilder();

         for (Entry<Item, Integer> set : items.entrySet()) {
            datax.append(set.getKey().getId()).append(":").append(set.getValue()).append(";");
         }

         item[0].setItemId(datax.toString());
         item[0].loadBundle();
      }

      return super.getCatalogItems();
   }

   public void loadItems(Room room) {
      if (this.room != null) {
         this.room.preventUnloading = false;
      }

      this.room = room;
      this.room.preventUnloading = true;
      this.getCatalogItems();
      this.loaded = true;
   }

   public void buyRoom(Habbo habbo) {
      this.buyRoom(habbo, habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername());
   }

   public void buyRoom(Habbo habbo, int userId, String userName) {
      if (!this.loaded) {
         this.loadItems(Emulator.getGameEnvironment().getRoomManager().loadRoom(this.roomId));
      }

      if (habbo != null) {
         int count = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(habbo).size();
         int max = habbo.getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;
         if (count >= max) {
            habbo.getClient().sendResponse(new CanCreateRoomComposer(count, max));
            return;
         }
      }

      if (this.room != null) {
         this.room.save();
         TObjectHashIterator var29 = this.room.getFloorItems().iterator();

         while (var29.hasNext()) {
            HabboItem item = (HabboItem)var29.next();
            item.run();
         }

         var29 = this.room.getWallItems().iterator();

         while (var29.hasNext()) {
            HabboItem item = (HabboItem)var29.next();
            item.run();
         }

         this.getCatalogItems();
         int roomId = 0;

         try {
            label241: {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               label222: {
                  try {
                     PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO rooms (owner_id, owner_name, name, description, model, password, state, users_max, category, paper_floor, paper_wall, paper_landscape, thickness_wall, thickness_floor, moodlight_data, override_model)  (SELECT ?, ?, name, description, model, password, state, users_max, category, paper_floor, paper_wall, paper_landscape, thickness_wall, thickness_floor, moodlight_data, override_model FROM rooms WHERE id = ?)",
                        1
                     );

                     try {
                        statement.setInt(1, userId);
                        statement.setString(2, userName);
                        statement.setInt(3, this.room.getId());
                        statement.execute();
                        ResultSet set = statement.getGeneratedKeys();

                        try {
                           if (set.next()) {
                              roomId = set.getInt(1);
                           }
                        } catch (Throwable var23) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var18) {
                                 var23.addSuppressed(var18);
                              }
                           }

                           throw var23;
                        }

                        if (set != null) {
                           set.close();
                        }
                     } catch (Throwable var26) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var17) {
                              var26.addSuppressed(var17);
                           }
                        }

                        throw var26;
                     }

                     if (statement != null) {
                        statement.close();
                     }

                     if (roomId == 0) {
                        break label222;
                     }

                     statement = connection.prepareStatement(
                        "INSERT INTO items (user_id, room_id, item_id, wall_pos, x, y, z, rot, extra_data, wired_data, limited_data, guild_id) (SELECT ?, ?, item_id, wall_pos, x, y, z, rot, extra_data, wired_data, ?, ? FROM items WHERE room_id = ?)",
                        1
                     );

                     try {
                        statement.setInt(1, userId);
                        statement.setInt(2, roomId);
                        statement.setString(3, "0:0");
                        statement.setInt(4, 0);
                        statement.setInt(5, this.room.getId());
                        statement.execute();
                     } catch (Throwable var22) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var16) {
                              var22.addSuppressed(var16);
                           }
                        }

                        throw var22;
                     }

                     if (statement != null) {
                        statement.close();
                     }

                     if (this.room.hasCustomLayout()) {
                        try {
                           statement = connection.prepareStatement(
                              "INSERT INTO room_models_custom (id, name, door_x, door_y, door_dir, heightmap) (SELECT ?, ?, door_x, door_y, door_dir, heightmap FROM room_models_custom WHERE id = ? LIMIT 1)",
                              1
                           );

                           try {
                              statement.setInt(1, roomId);
                              statement.setString(2, "custom_" + roomId);
                              statement.setInt(3, this.room.getId());
                              statement.execute();
                           } catch (Throwable var20) {
                              if (statement != null) {
                                 try {
                                    statement.close();
                                 } catch (Throwable var19) {
                                    var20.addSuppressed(var19);
                                 }
                              }

                              throw var20;
                           }

                           if (statement != null) {
                              statement.close();
                           }
                        } catch (SQLException e) {
                           LOGGER.error("Caught SQL exception", e);
                        }
                     }

                     if (Emulator.getConfig().getBoolean("bundle.bots.enabled")) {
                        statement = connection.prepareStatement(
                           "INSERT INTO bots (user_id, room_id, name, motto, figure, gender, x, y, z, chat_lines, chat_auto, chat_random, chat_delay, dance, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                           1
                        );

                        try {
                           synchronized (this.room.getCurrentBots()) {
                              statement.setInt(1, userId);
                              statement.setInt(2, roomId);

                              for (Bot bot : this.room.getCurrentBots().valueCollection()) {
                                 statement.setString(3, bot.getName());
                                 statement.setString(4, bot.getMotto());
                                 statement.setString(5, bot.getFigure());
                                 statement.setString(6, bot.getGender().name());
                                 statement.setInt(7, bot.getRoomUnit().getX());
                                 statement.setInt(8, bot.getRoomUnit().getY());
                                 statement.setDouble(9, bot.getRoomUnit().getZ());
                                 StringBuilder text = new StringBuilder();

                                 for (String s : bot.getChatLines()) {
                                    text.append(s).append("\r");
                                 }

                                 statement.setString(10, text.toString());
                                 statement.setString(11, bot.isChatAuto() ? "1" : "0");
                                 statement.setString(12, bot.isChatRandom() ? "1" : "0");
                                 statement.setInt(13, bot.getChatDelay());
                                 statement.setInt(14, bot.getRoomUnit().getDanceType().getType());
                                 statement.setString(15, bot.getType());
                                 statement.addBatch();
                              }
                           }

                           statement.executeBatch();
                        } catch (Throwable var25) {
                           if (statement != null) {
                              try {
                                 statement.close();
                              } catch (Throwable var15) {
                                 var25.addSuppressed(var15);
                              }
                           }

                           throw var25;
                        }

                        if (statement != null) {
                           statement.close();
                        }
                     }
                  } catch (Throwable var27) {
                     if (connection != null) {
                        try {
                           connection.close();
                        } catch (Throwable var14) {
                           var27.addSuppressed(var14);
                        }
                     }

                     throw var27;
                  }

                  if (connection != null) {
                     connection.close();
                  }
                  break label241;
               }

               if (connection != null) {
                  connection.close();
               }

               return;
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }

         Room r = Emulator.getGameEnvironment().getRoomManager().loadRoom(roomId);
         r.setWallHeight(this.room.getWallHeight());
         r.setFloorSize(this.room.getFloorSize());
         r.setWallPaint(this.room.getWallPaint());
         r.setFloorPaint(this.room.getFloorPaint());
         r.setScore(0);
         r.setNeedsUpdate(true);
         THashMap<String, String> keys = new THashMap();
         keys.put("ROOMNAME", r.getName());
         keys.put("ROOMID", r.getId() + "");
         keys.put("OWNER", r.getOwnerName());
         keys.put("image", "${image.library.url}/notifications/room_bundle_" + this.getId() + ".png");
         if (habbo != null) {
            habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.PURCHASING_ROOM.key, keys));
         }
      }
   }
}
