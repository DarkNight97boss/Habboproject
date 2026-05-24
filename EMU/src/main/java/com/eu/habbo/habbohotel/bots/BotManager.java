package com.eu.habbo.habbohotel.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.generic.alerts.BotErrorComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.AddBotComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveBotComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer;
import com.eu.habbo.plugin.events.bots.BotPickUpEvent;
import com.eu.habbo.plugin.events.bots.BotPlacedEvent;
import gnu.trove.map.hash.THashMap;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);
   private static final THashMap<String, Class<? extends Bot>> botDefenitions = new THashMap();
   public static int MINIMUM_CHAT_SPEED = 7;
   public static int MAXIMUM_CHAT_SPEED = 604800;
   public static int MAXIMUM_CHAT_LENGTH = 120;
   public static int MAXIMUM_NAME_LENGTH = 15;
   public static int MAXIMUM_BOT_INVENTORY_SIZE = 25;

   public BotManager() throws Exception {
      long millis = System.currentTimeMillis();
      addBotDefinition("generic", Bot.class);
      addBotDefinition("bartender", ButlerBot.class);
      addBotDefinition("visitor_log", VisitorBot.class);
      this.reload();
      LOGGER.info("Bot Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public static void addBotDefinition(String type, Class<? extends Bot> botClazz) throws Exception {
      botClazz.getDeclaredConstructor(ResultSet.class).setAccessible(true);
      botDefenitions.put(type, botClazz);
   }

   public boolean reload() {
      for (Entry<String, Class<? extends Bot>> set : botDefenitions.entrySet()) {
         try {
            Method m = set.getValue().getMethod("initialise");
            m.setAccessible(true);
            m.invoke(null);
         } catch (NoSuchMethodException e) {
            LOGGER.info("Bot Manager -> Failed to execute initialise method upon bot type '" + set.getKey() + "'. No Such Method!");
            return false;
         } catch (Exception e) {
            LOGGER.info("Bot Manager -> Failed to execute initialise method upon bot type '" + set.getKey() + "'. Error: " + e.getMessage());
            return false;
         }
      }

      return true;
   }

   public Bot createBot(THashMap<String, String> data, String type) {
      Bot bot = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO bots (user_id, room_id, name, motto, figure, gender, type) VALUES (0, 0, ?, ?, ?, ?, ?)", 1
            );

            try {
               statement.setString(1, (String)data.get("name"));
               statement.setString(2, (String)data.get("motto"));
               statement.setString(3, (String)data.get("figure"));
               statement.setString(4, ((String)data.get("gender")).toUpperCase());
               statement.setString(5, type);
               statement.execute();
               ResultSet set = statement.getGeneratedKeys();

               try {
                  if (set.next()) {
                     try {
                        PreparedStatement stmt = connection.prepareStatement(
                           "SELECT users.username AS owner_name, bots.* FROM bots LEFT JOIN users ON bots.user_id = users.id WHERE bots.id = ? LIMIT 1"
                        );

                        try {
                           stmt.setInt(1, set.getInt(1));
                           ResultSet resultSet = stmt.executeQuery();

                           try {
                              if (resultSet.next()) {
                                 bot = this.loadBot(resultSet);
                              }
                           } catch (Throwable var16) {
                              if (resultSet != null) {
                                 try {
                                    resultSet.close();
                                 } catch (Throwable var15) {
                                    var16.addSuppressed(var15);
                                 }
                              }

                              throw var16;
                           }

                           if (resultSet != null) {
                              resultSet.close();
                           }
                        } catch (Throwable var17) {
                           if (stmt != null) {
                              try {
                                 stmt.close();
                              } catch (Throwable var14) {
                                 var17.addSuppressed(var14);
                              }
                           }

                           throw var17;
                        }

                        if (stmt != null) {
                           stmt.close();
                        }
                     } catch (SQLException e) {
                        LOGGER.error("Caught SQL exception", e);
                     }
                  }
               } catch (Throwable var19) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var13) {
                        var19.addSuppressed(var13);
                     }
                  }

                  throw var19;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var20) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var12) {
                     var20.addSuppressed(var12);
                  }
               }

               throw var20;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var21) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var21.addSuppressed(var11);
               }
            }

            throw var21;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return bot;
   }

   public void placeBot(Bot bot, Habbo habbo, Room room, RoomTile location) {
      BotPlacedEvent event = new BotPlacedEvent(bot, location, habbo);
      Emulator.getPluginManager().fireEvent(event);
      if (!event.isCancelled()) {
         if (room != null && bot != null && habbo != null) {
            if (room.getOwnerId() != habbo.getHabboInfo().getId()
               && !habbo.hasPermission(Permission.ACC_ANYROOMOWNER)
               && !habbo.hasPermission(Permission.ACC_PLACEFURNI)) {
               habbo.getClient()
                  .sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            } else {
               if (room.getCurrentBots().size() >= Room.MAXIMUM_BOTS && !habbo.hasPermission(Permission.ACC_UNLIMITED_BOTS)) {
                  habbo.getClient().sendResponse(new BotErrorComposer(2));
                  return;
               }

               if (room.hasHabbosAt(location.x, location.y)
                  || !location.isWalkable() && location.state != RoomTileState.SIT && location.state != RoomTileState.LAY) {
                  return;
               }

               if (room.hasBotsAt(location.x, location.y)) {
                  habbo.getClient().sendResponse(new BotErrorComposer(3));
                  return;
               }

               RoomUnit roomUnit = new RoomUnit();
               roomUnit.setRotation(RoomUserRotation.SOUTH);
               roomUnit.setLocation(location);
               double stackHeight = location.getStackHeight();
               roomUnit.setPreviousLocationZ(stackHeight);
               roomUnit.setZ(stackHeight);
               roomUnit.setPathFinderRoom(room);
               roomUnit.setRoomUnitType(RoomUnitType.BOT);
               roomUnit.setCanWalk(room.isAllowBotsWalk());
               bot.setRoomUnit(roomUnit);
               bot.setRoom(room);
               bot.needsUpdate(true);
               room.addBot(bot);
               Emulator.getThreading().run(bot);
               room.sendComposer(new RoomUsersComposer(bot).compose());
               room.sendComposer(new RoomUserStatusComposer(bot.getRoomUnit()).compose());
               habbo.getInventory().getBotsComponent().removeBot(bot);
               habbo.getClient().sendResponse(new RemoveBotComposer(bot));
               bot.onPlace(habbo, room);
               HabboItem topItem = room.getTopItemAt(location.x, location.y);
               if (topItem != null) {
                  try {
                     topItem.onWalkOn(bot.getRoomUnit(), room, null);
                  } catch (Exception e) {
                     LOGGER.error("Caught exception", e);
                  }
               }

               bot.cycle(false);
            }
         }
      }
   }

   public void pickUpBot(int botId, Habbo habbo) {
      if (habbo.getHabboInfo().getCurrentRoom() != null) {
         this.pickUpBot(habbo.getHabboInfo().getCurrentRoom().getBot(Math.abs(botId)), habbo);
      }
   }

   public void pickUpBot(Bot bot, Habbo habbo) {
      HabboInfo receiverInfo = habbo == null ? Emulator.getGameEnvironment().getHabboManager().getHabboInfo(bot.getOwnerId()) : habbo.getHabboInfo();
      if (bot != null) {
         BotPickUpEvent pickedUpEvent = new BotPickUpEvent(bot, habbo);
         Emulator.getPluginManager().fireEvent(pickedUpEvent);
         if (pickedUpEvent.isCancelled()) {
            return;
         }

         if (habbo == null || bot.getOwnerId() == habbo.getHabboInfo().getId() || habbo.hasPermission(Permission.ACC_ANYROOMOWNER)) {
            if (habbo != null
               && !habbo.hasPermission(Permission.ACC_UNLIMITED_BOTS)
               && habbo.getInventory().getBotsComponent().getBots().size() >= MAXIMUM_BOT_INVENTORY_SIZE) {
               habbo.alert(Emulator.getTexts().getValue("error.bots.max.inventory").replace("%amount%", MAXIMUM_BOT_INVENTORY_SIZE + ""));
               return;
            }

            bot.onPickUp(habbo, receiverInfo.getCurrentRoom());
            receiverInfo.getCurrentRoom().removeBot(bot);
            bot.stopFollowingHabbo();
            bot.setOwnerId(receiverInfo.getId());
            bot.setOwnerName(receiverInfo.getUsername());
            bot.needsUpdate(true);
            Emulator.getThreading().run(bot);
            Habbo receiver = habbo == null ? Emulator.getGameEnvironment().getHabboManager().getHabbo(receiverInfo.getId()) : habbo;
            if (receiver != null) {
               receiver.getInventory().getBotsComponent().addBot(bot);
               receiver.getClient().sendResponse(new AddBotComposer(bot));
            }
         }
      }
   }

   public Bot loadBot(ResultSet set) {
      try {
         String type = set.getString("type");
         Class<? extends Bot> botClazz = (Class<? extends Bot>)botDefenitions.get(type);
         if (botClazz != null) {
            return botClazz.getDeclaredConstructor(ResultSet.class).newInstance(set);
         }

         LOGGER.error("Unknown Bot Type: " + type);
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
      }

      return null;
   }

   public boolean deleteBot(Bot bot) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var4;
         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM bots WHERE id = ? LIMIT 1");

            try {
               statement.setInt(1, bot.getId());
               var4 = statement.execute();
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

         return var4;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public void dispose() {
      for (Entry<String, Class<? extends Bot>> set : botDefenitions.entrySet()) {
         try {
            Method m = set.getValue().getMethod("dispose");
            m.setAccessible(true);
            m.invoke(null);
         } catch (NoSuchMethodException e) {
            LOGGER.info("Bot Manager -> Failed to execute dispose method upon bot type '" + set.getKey() + "'. No Such Method!");
         } catch (Exception e) {
            LOGGER.info("Bot Manager -> Failed to execute dispose method upon bot type '" + set.getKey() + "'. Error: " + e.getMessage());
         }
      }
   }
}
