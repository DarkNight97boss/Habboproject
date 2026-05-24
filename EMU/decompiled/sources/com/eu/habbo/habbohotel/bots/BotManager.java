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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/bots/BotManager.class */
public class BotManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);
    private static final THashMap<String, Class<? extends Bot>> botDefenitions = new THashMap<>();
    public static int MINIMUM_CHAT_SPEED = 7;
    public static int MAXIMUM_CHAT_SPEED = 604800;
    public static int MAXIMUM_CHAT_LENGTH = 120;
    public static int MAXIMUM_NAME_LENGTH = 15;
    public static int MAXIMUM_BOT_INVENTORY_SIZE = 25;

    public BotManager() throws Exception {
        long jCurrentTimeMillis = System.currentTimeMillis();
        addBotDefinition("generic", Bot.class);
        addBotDefinition("bartender", ButlerBot.class);
        addBotDefinition("visitor_log", VisitorBot.class);
        reload();
        LOGGER.info("Bot Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public static void addBotDefinition(String str, Class<? extends Bot> cls) throws Exception {
        cls.getDeclaredConstructor(ResultSet.class).setAccessible(true);
        botDefenitions.put(str, cls);
    }

    public boolean reload() {
        for (Map.Entry entry : botDefenitions.entrySet()) {
            try {
                Method method = ((Class) entry.getValue()).getMethod("initialise", new Class[0]);
                method.setAccessible(true);
                method.invoke(null, new Object[0]);
            } catch (NoSuchMethodException e) {
                LOGGER.info("Bot Manager -> Failed to execute initialise method upon bot type '" + ((String) entry.getKey()) + "'. No Such Method!");
                return false;
            } catch (Exception e2) {
                LOGGER.info("Bot Manager -> Failed to execute initialise method upon bot type '" + ((String) entry.getKey()) + "'. Error: " + e2.getMessage());
                return false;
            }
        }
        return true;
    }

    public Bot createBot(THashMap<String, String> tHashMap, String str) {
        Bot botLoadBot = null;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO bots (user_id, room_id, name, motto, figure, gender, type) VALUES (0, 0, ?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setString(1, (String) tHashMap.get("name"));
                    preparedStatementPrepareStatement.setString(2, (String) tHashMap.get("motto"));
                    preparedStatementPrepareStatement.setString(3, (String) tHashMap.get("figure"));
                    preparedStatementPrepareStatement.setString(4, ((String) tHashMap.get("gender")).toUpperCase());
                    preparedStatementPrepareStatement.setString(5, str);
                    preparedStatementPrepareStatement.execute();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            try {
                                PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT users.username AS owner_name, bots.* FROM bots LEFT JOIN users ON bots.user_id = users.id WHERE bots.id = ? LIMIT 1");
                                try {
                                    preparedStatementPrepareStatement2.setInt(1, generatedKeys.getInt(1));
                                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement2.executeQuery();
                                    try {
                                        if (resultSetExecuteQuery.next()) {
                                            botLoadBot = loadBot(resultSetExecuteQuery);
                                        }
                                        if (resultSetExecuteQuery != null) {
                                            resultSetExecuteQuery.close();
                                        }
                                        if (preparedStatementPrepareStatement2 != null) {
                                            preparedStatementPrepareStatement2.close();
                                        }
                                    } catch (Throwable th) {
                                        if (resultSetExecuteQuery != null) {
                                            try {
                                                resultSetExecuteQuery.close();
                                            } catch (Throwable th2) {
                                                th.addSuppressed(th2);
                                            }
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    if (preparedStatementPrepareStatement2 != null) {
                                        try {
                                            preparedStatementPrepareStatement2.close();
                                        } catch (Throwable th4) {
                                            th3.addSuppressed(th4);
                                        }
                                    }
                                    throw th3;
                                }
                            } catch (SQLException e) {
                                LOGGER.error("Caught SQL exception", e);
                            }
                        }
                        if (generatedKeys != null) {
                            generatedKeys.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th5) {
                        if (generatedKeys != null) {
                            try {
                                generatedKeys.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                } catch (Throwable th7) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th8) {
                            th7.addSuppressed(th8);
                        }
                    }
                    throw th7;
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
        return botLoadBot;
    }

    public void placeBot(Bot bot, Habbo habbo, Room room, RoomTile roomTile) {
        BotPlacedEvent botPlacedEvent = new BotPlacedEvent(bot, roomTile, habbo);
        Emulator.getPluginManager().fireEvent(botPlacedEvent);
        if (botPlacedEvent.isCancelled() || room == null || bot == null || habbo == null) {
            return;
        }
        if (room.getOwnerId() != habbo.getHabboInfo().getId() && !habbo.hasPermission(Permission.ACC_ANYROOMOWNER) && !habbo.hasPermission(Permission.ACC_PLACEFURNI)) {
            habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }
        if (room.getCurrentBots().size() >= Room.MAXIMUM_BOTS && !habbo.hasPermission(Permission.ACC_UNLIMITED_BOTS)) {
            habbo.getClient().sendResponse(new BotErrorComposer(2));
            return;
        }
        if (room.hasHabbosAt(roomTile.x, roomTile.y)) {
            return;
        }
        if (roomTile.isWalkable() || roomTile.state == RoomTileState.SIT || roomTile.state == RoomTileState.LAY) {
            if (room.hasBotsAt(roomTile.x, roomTile.y)) {
                habbo.getClient().sendResponse(new BotErrorComposer(3));
                return;
            }
            RoomUnit roomUnit = new RoomUnit();
            roomUnit.setRotation(RoomUserRotation.SOUTH);
            roomUnit.setLocation(roomTile);
            double stackHeight = roomTile.getStackHeight();
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
            HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y);
            if (topItemAt != null) {
                try {
                    topItemAt.onWalkOn(bot.getRoomUnit(), room, null);
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
            bot.cycle(false);
        }
    }

    public void pickUpBot(int i, Habbo habbo) {
        if (habbo.getHabboInfo().getCurrentRoom() != null) {
            pickUpBot(habbo.getHabboInfo().getCurrentRoom().getBot(Math.abs(i)), habbo);
        }
    }

    public void pickUpBot(Bot bot, Habbo habbo) {
        HabboInfo habboInfo = habbo == null ? Emulator.getGameEnvironment().getHabboManager().getHabboInfo(bot.getOwnerId()) : habbo.getHabboInfo();
        if (bot != null) {
            BotPickUpEvent botPickUpEvent = new BotPickUpEvent(bot, habbo);
            Emulator.getPluginManager().fireEvent(botPickUpEvent);
            if (botPickUpEvent.isCancelled()) {
                return;
            }
            if (habbo == null || bot.getOwnerId() == habbo.getHabboInfo().getId() || habbo.hasPermission(Permission.ACC_ANYROOMOWNER)) {
                if (habbo != null && !habbo.hasPermission(Permission.ACC_UNLIMITED_BOTS) && habbo.getInventory().getBotsComponent().getBots().size() >= MAXIMUM_BOT_INVENTORY_SIZE) {
                    habbo.alert(Emulator.getTexts().getValue("error.bots.max.inventory").replace("%amount%", MAXIMUM_BOT_INVENTORY_SIZE + Emulator.PREVIEW));
                    return;
                }
                bot.onPickUp(habbo, habboInfo.getCurrentRoom());
                habboInfo.getCurrentRoom().removeBot(bot);
                bot.stopFollowingHabbo();
                bot.setOwnerId(habboInfo.getId());
                bot.setOwnerName(habboInfo.getUsername());
                bot.needsUpdate(true);
                Emulator.getThreading().run(bot);
                Habbo habbo2 = habbo == null ? Emulator.getGameEnvironment().getHabboManager().getHabbo(habboInfo.getId()) : habbo;
                if (habbo2 != null) {
                    habbo2.getInventory().getBotsComponent().addBot(bot);
                    habbo2.getClient().sendResponse(new AddBotComposer(bot));
                }
            }
        }
    }

    public Bot loadBot(ResultSet resultSet) {
        try {
            String string = resultSet.getString("type");
            Class cls = (Class) botDefenitions.get(string);
            if (cls != null) {
                return (Bot) cls.getDeclaredConstructor(ResultSet.class).newInstance(resultSet);
            }
            LOGGER.error("Unknown Bot Type: " + string);
            return null;
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return null;
        } catch (Exception e2) {
            LOGGER.error("Caught exception", e2);
            return null;
        }
    }

    public boolean deleteBot(Bot bot) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM bots WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, bot.getId());
                    boolean zExecute = preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return zExecute;
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public void dispose() {
        for (Map.Entry entry : botDefenitions.entrySet()) {
            try {
                Method method = ((Class) entry.getValue()).getMethod("dispose", new Class[0]);
                method.setAccessible(true);
                method.invoke(null, new Object[0]);
            } catch (NoSuchMethodException e) {
                LOGGER.info("Bot Manager -> Failed to execute dispose method upon bot type '" + ((String) entry.getKey()) + "'. No Such Method!");
            } catch (Exception e2) {
                LOGGER.info("Bot Manager -> Failed to execute dispose method upon bot type '" + ((String) entry.getKey()) + "'. Error: " + e2.getMessage());
            }
        }
    }
}
