package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.RoomUserPetComposer;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.games.football.FootballGame;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.games.tag.BunnyrunGame;
import com.eu.habbo.habbohotel.games.tag.IceTagGame;
import com.eu.habbo.habbohotel.games.tag.RollerskateGame;
import com.eu.habbo.habbohotel.games.wired.WiredGame;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.navigation.NavigatorFilterComparator;
import com.eu.habbo.habbohotel.navigation.NavigatorFilterField;
import com.eu.habbo.habbohotel.navigation.NavigatorManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetData;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.polls.Poll;
import com.eu.habbo.habbohotel.polls.PollManager;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.incoming.users.UserNuxEvent;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericErrorMessagesComposer;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewComposer;
import com.eu.habbo.messages.outgoing.polls.PollStartComposer;
import com.eu.habbo.messages.outgoing.polls.infobus.SimplePollAnswersComposer;
import com.eu.habbo.messages.outgoing.polls.infobus.SimplePollStartComposer;
import com.eu.habbo.messages.outgoing.rooms.DoorbellAddUserComposer;
import com.eu.habbo.messages.outgoing.rooms.FloodCounterComposer;
import com.eu.habbo.messages.outgoing.rooms.HideDoorbellComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomAccessDeniedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomDataComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomEnterErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomModelComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomOpenComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomPaintComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomPaneComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomScoreComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomThicknessComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RoomFloorItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RoomWallItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetComposer;
import com.eu.habbo.messages.outgoing.rooms.promotions.RoomPromotionMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitIdleComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDanceComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersAddGuildBadgeComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersGuildBadgesComposer;
import com.eu.habbo.messages.outgoing.users.MutedWhisperComposer;
import com.eu.habbo.plugin.events.navigator.NavigatorRoomCreatedEvent;
import com.eu.habbo.plugin.events.rooms.RoomFloorItemsLoadEvent;
import com.eu.habbo.plugin.events.rooms.RoomUncachedEvent;
import com.eu.habbo.plugin.events.rooms.UserVoteRoomEvent;
import com.eu.habbo.plugin.events.users.HabboAddedToRoomEvent;
import com.eu.habbo.plugin.events.users.UserEnterRoomEvent;
import com.eu.habbo.plugin.events.users.UserExitRoomEvent;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomManager.class */
public class RoomManager {
    private static final int page = 0;
    private final THashMap<Integer, RoomCategory> roomCategories;
    private final List<String> mapNames;
    private final ConcurrentHashMap<Integer, Room> activeRooms;
    private final ArrayList<Class<? extends Game>> gameTypes;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomManager.class);
    public static int MAXIMUM_ROOMS_USER = 25;
    public static int MAXIMUM_ROOMS_HC = 35;
    public static int HOME_ROOM_ID = 0;
    public static boolean SHOW_PUBLIC_IN_POPULAR_TAB = false;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomManager$RoomBanTypes.class */
    public enum RoomBanTypes {
        RWUAM_BAN_USER_HOUR(3600),
        RWUAM_BAN_USER_DAY(86400),
        RWUAM_BAN_USER_PERM(315360000);

        public int duration;

        RoomBanTypes(int i) {
            this.duration = i;
        }
    }

    public RoomManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.roomCategories = new THashMap<>();
        this.mapNames = new ArrayList();
        this.activeRooms = new ConcurrentHashMap<>();
        loadRoomCategories();
        loadRoomModels();
        this.gameTypes = new ArrayList<>();
        registerGameType(BattleBanzaiGame.class);
        registerGameType(FreezeGame.class);
        registerGameType(WiredGame.class);
        registerGameType(FootballGame.class);
        registerGameType(BunnyrunGame.class);
        registerGameType(IceTagGame.class);
        registerGameType(RollerskateGame.class);
        LOGGER.info("Room Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public void loadRoomModels() {
        this.mapNames.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM room_models");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.mapNames.add(resultSetExecuteQuery.getString("name"));
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public CustomRoomLayout loadCustomLayout(Room room) {
        CustomRoomLayout customRoomLayout = null;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM room_models_custom WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, room.getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            customRoomLayout = new CustomRoomLayout(resultSetExecuteQuery, room);
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
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
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return customRoomLayout;
    }

    private void loadRoomCategories() {
        this.roomCategories.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM navigator_flatcats");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.roomCategories.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new RoomCategory(resultSetExecuteQuery));
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void loadPublicRooms() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms WHERE is_public = ? OR is_staff_picked = ? ORDER BY id DESC");
                try {
                    preparedStatementPrepareStatement.setString(1, "1");
                    preparedStatementPrepareStatement.setString(2, "1");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            Room room = new Room(resultSetExecuteQuery);
                            room.preventUncaching = true;
                            this.activeRooms.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), room);
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public THashMap<Integer, List<Room>> findRooms(NavigatorFilterField navigatorFilterField, String str, int i, boolean z) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashMap<Integer, List<Room>> tHashMap = new THashMap<>();
        String str2 = navigatorFilterField.databaseQuery + " AND rooms.state NOT LIKE " + (z ? "''" : "'invisible'") + (i >= 0 ? "AND rooms.category = '" + i + "'" : Emulator.PREVIEW) + "  ORDER BY rooms.users, rooms.id DESC LIMIT " + (0 * NavigatorManager.MAXIMUM_RESULTS_PER_PAGE) + Emulator.PREVIEW + ((0 * NavigatorManager.MAXIMUM_RESULTS_PER_PAGE) + NavigatorManager.MAXIMUM_RESULTS_PER_PAGE);
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement(str2);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setString(1, navigatorFilterField.comparator == NavigatorFilterComparator.EQUALS ? str : "%" + str + "%");
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    Room room = this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id")));
                    if (room == null) {
                        room = new Room(resultSetExecuteQuery);
                        this.activeRooms.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), room);
                    }
                    if (!tHashMap.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("category")))) {
                        tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("category")), new ArrayList());
                    }
                    ((List) tHashMap.get(Integer.valueOf(resultSetExecuteQuery.getInt("category")))).add(room);
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
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return tHashMap;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public RoomCategory getCategory(int i) {
        for (RoomCategory roomCategory : this.roomCategories.values()) {
            if (roomCategory.getId() == i) {
                return roomCategory;
            }
        }
        return null;
    }

    public RoomCategory getCategory(String str) {
        for (RoomCategory roomCategory : this.roomCategories.values()) {
            if (roomCategory.getCaption().equalsIgnoreCase(str)) {
                return roomCategory;
            }
        }
        return null;
    }

    public RoomCategory getCategoryBySafeCaption(String str) {
        for (RoomCategory roomCategory : this.roomCategories.values()) {
            if (roomCategory.getCaptionSave().equalsIgnoreCase(str)) {
                return roomCategory;
            }
        }
        return null;
    }

    public List<RoomCategory> roomCategoriesForHabbo(Habbo habbo) {
        ArrayList arrayList = new ArrayList();
        for (RoomCategory roomCategory : this.roomCategories.values()) {
            if (roomCategory.getMinRank() <= habbo.getHabboInfo().getRank().getId()) {
                arrayList.add(roomCategory);
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public boolean hasCategory(int i, Habbo habbo) {
        for (RoomCategory roomCategory : this.roomCategories.values()) {
            if (roomCategory.getId() == i && roomCategory.getMinRank() <= habbo.getHabboInfo().getRank().getId()) {
                return true;
            }
        }
        return false;
    }

    public THashMap<Integer, RoomCategory> getRoomCategories() {
        return this.roomCategories;
    }

    public List<Room> getRoomsByScore() {
        ArrayList arrayList = new ArrayList(this.activeRooms.values());
        arrayList.sort(Room.SORT_SCORE);
        return arrayList;
    }

    public List<Room> getActiveRooms(int i) {
        ArrayList arrayList = new ArrayList();
        for (Room room : this.activeRooms.values()) {
            if (i == room.getCategory() || i == -1) {
                arrayList.add(room);
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public List<Room> getRoomsForHabbo(Habbo habbo) {
        ArrayList arrayList = new ArrayList();
        for (Room room : this.activeRooms.values()) {
            if (room.getOwnerId() == habbo.getHabboInfo().getId()) {
                arrayList.add(room);
            }
        }
        arrayList.sort(Room.SORT_ID);
        return arrayList;
    }

    public List<Room> getRoomsForHabbo(String str) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
        if (habbo != null) {
            return getRoomsForHabbo(habbo);
        }
        ArrayList arrayList = new ArrayList();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms WHERE owner_name = ? ORDER BY id DESC LIMIT 25");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setString(1, str);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    arrayList.add(loadRoom(resultSetExecuteQuery.getInt("id")));
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
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public Room loadRoom(int i) {
        return loadRoom(i, false);
    }

    public Room loadRoom(int i, boolean z) {
        Room room = null;
        if (this.activeRooms.containsKey(Integer.valueOf(i))) {
            Room room2 = this.activeRooms.get(Integer.valueOf(i));
            if (z && room2.isPreLoaded() && !room2.isLoaded()) {
                room2.loadData();
            }
            return room2;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            room = new Room(resultSetExecuteQuery);
                            if (z) {
                                room.loadData();
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (room != null) {
                        this.activeRooms.put(Integer.valueOf(room.getId()), room);
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return room;
    }

    public Room createRoom(int i, String str, String str2, String str3, String str4, int i2, int i3, int i4) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Room roomLoadRoom = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO rooms (owner_id, owner_name, name, description, model, users_max, category, trade_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 1);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setString(2, str);
            preparedStatementPrepareStatement.setString(3, str2);
            preparedStatementPrepareStatement.setString(4, str3);
            preparedStatementPrepareStatement.setString(5, str4);
            preparedStatementPrepareStatement.setInt(6, i2);
            preparedStatementPrepareStatement.setInt(7, i3);
            preparedStatementPrepareStatement.setInt(8, i4);
            preparedStatementPrepareStatement.execute();
            ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
            try {
                if (generatedKeys.next()) {
                    roomLoadRoom = loadRoom(generatedKeys.getInt(1));
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
                return roomLoadRoom;
            } catch (Throwable th) {
                if (generatedKeys != null) {
                    try {
                        generatedKeys.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public Room createRoomForHabbo(Habbo habbo, String str, String str2, String str3, int i, int i2, int i3) {
        Room roomCreateRoom = createRoom(habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername(), str, str2, str3, i, i2, i3);
        Emulator.getPluginManager().fireEvent(new NavigatorRoomCreatedEvent(habbo, roomCreateRoom));
        return roomCreateRoom;
    }

    public void loadRoomsForHabbo(Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms WHERE owner_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (!this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                                this.activeRooms.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new Room(resultSetExecuteQuery));
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void unloadRoomsForHabbo(Habbo habbo) {
        ArrayList<Room> arrayList = new ArrayList();
        for (Room room : this.activeRooms.values()) {
            if (!room.isPublicRoom() && !room.isStaffPromotedRoom() && room.getOwnerId() == habbo.getHabboInfo().getId() && room.getUserCount() == 0 && (this.roomCategories.get(Integer.valueOf(room.getCategory())) == null || !((RoomCategory) this.roomCategories.get(Integer.valueOf(room.getCategory()))).isPublic())) {
                arrayList.add(room);
            }
        }
        for (Room room2 : arrayList) {
            if (!((RoomUncachedEvent) Emulator.getPluginManager().fireEvent(new RoomUncachedEvent(room2))).isCancelled()) {
                room2.dispose();
                this.activeRooms.remove(Integer.valueOf(room2.getId()));
            }
        }
    }

    public void clearInactiveRooms() {
        THashSet tHashSet = new THashSet();
        for (Room room : this.activeRooms.values()) {
            if (!room.isPublicRoom() && !room.isStaffPromotedRoom() && !Emulator.getGameServer().getGameClientManager().containsHabbo(Integer.valueOf(room.getOwnerId())) && room.isPreLoaded()) {
                tHashSet.add(room);
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            Room room2 = (Room) it.next();
            room2.dispose();
            if (room2.getUserCount() == 0) {
                this.activeRooms.remove(Integer.valueOf(room2.getId()));
            }
        }
    }

    public boolean layoutExists(String str) {
        return this.mapNames.contains(str);
    }

    public RoomLayout loadLayout(String str, Room room) {
        RoomLayout roomLayout = null;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM room_models WHERE name LIKE ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setString(1, str);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            roomLayout = new RoomLayout(resultSetExecuteQuery, room);
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
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
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return roomLayout;
    }

    public void unloadRoom(Room room) {
        room.dispose();
    }

    public void uncacheRoom(Room room) {
        this.activeRooms.remove(Integer.valueOf(room.getId()));
    }

    public void voteForRoom(Habbo habbo, Room room) {
        if (habbo.getHabboInfo().getCurrentRoom() == null || room == null || habbo.getHabboInfo().getCurrentRoom() != room || hasVotedForRoom(habbo, room)) {
            return;
        }
        if (((UserVoteRoomEvent) Emulator.getPluginManager().fireEvent(new UserVoteRoomEvent(room, habbo))).isCancelled()) {
            return;
        }
        room.setScore(room.getScore() + 1);
        room.setNeedsUpdate(true);
        habbo.getHabboStats().votedRooms.push(room.getId());
        for (Habbo habbo2 : room.getHabbos()) {
            habbo2.getClient().sendResponse(new RoomScoreComposer(room.getScore(), !hasVotedForRoom(habbo2, room)));
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_votes VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(2, room.getId());
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
        }
    }

    boolean hasVotedForRoom(Habbo habbo, Room room) {
        if (room.getOwnerId() == habbo.getHabboInfo().getId()) {
            return true;
        }
        for (int i : habbo.getHabboStats().votedRooms.toArray()) {
            if (i == room.getId()) {
                return true;
            }
        }
        return false;
    }

    public Room getRoom(int i) {
        return this.activeRooms.get(Integer.valueOf(i));
    }

    public ArrayList<Room> getActiveRooms() {
        return new ArrayList<>(this.activeRooms.values());
    }

    public int loadedRoomsCount() {
        return this.activeRooms.size();
    }

    public void enterRoom(Habbo habbo, int i, String str) {
        enterRoom(habbo, i, str, false, null);
    }

    public void enterRoom(Habbo habbo, int i, String str, boolean z) {
        enterRoom(habbo, i, str, z, null);
    }

    public void enterRoom(Habbo habbo, int i, String str, boolean z, RoomTile roomTile) {
        Room room;
        Room roomLoadRoom = loadRoom(i, true);
        if (roomLoadRoom == null) {
            return;
        }
        if (habbo.getHabboInfo().getLoadingRoom() != 0 && roomLoadRoom.getId() != habbo.getHabboInfo().getLoadingRoom()) {
            habbo.getClient().sendResponse(new HotelViewComposer());
            habbo.getHabboInfo().setLoadingRoom(0);
            return;
        }
        if (((UserEnterRoomEvent) Emulator.getPluginManager().fireEvent(new UserEnterRoomEvent(habbo, roomLoadRoom))).isCancelled() && habbo.getHabboInfo().getCurrentRoom() == null) {
            habbo.getClient().sendResponse(new HotelViewComposer());
            habbo.getHabboInfo().setLoadingRoom(0);
            return;
        }
        if (roomLoadRoom.isBanned(habbo) && !habbo.hasPermission(Permission.ACC_ANYROOMOWNER) && !habbo.hasPermission(Permission.ACC_ENTERANYROOM)) {
            habbo.getClient().sendResponse(new RoomEnterErrorComposer(4));
            return;
        }
        if (habbo.getHabboInfo().getRoomQueueId() != i && (room = Emulator.getGameEnvironment().getRoomManager().getRoom(i)) != null) {
            room.removeFromQueue(habbo);
        }
        if (z || roomLoadRoom.isOwner(habbo) || roomLoadRoom.getState() == RoomState.OPEN || habbo.hasPermission(Permission.ACC_ANYROOMOWNER) || habbo.hasPermission(Permission.ACC_ENTERANYROOM) || roomLoadRoom.hasRights(habbo) || ((roomLoadRoom.getState().equals(RoomState.INVISIBLE) && roomLoadRoom.hasRights(habbo)) || (roomLoadRoom.hasGuild() && roomLoadRoom.getGuildRightLevel(habbo).isGreaterThan(RoomRightLevels.GUILD_RIGHTS)))) {
            openRoom(habbo, roomLoadRoom, roomTile);
            return;
        }
        if (roomLoadRoom.getState() != RoomState.LOCKED) {
            if (roomLoadRoom.getState() != RoomState.PASSWORD) {
                habbo.getClient().sendResponse(new HotelViewComposer());
                habbo.getHabboInfo().setLoadingRoom(0);
                return;
            } else {
                if (roomLoadRoom.getPassword().equalsIgnoreCase(str)) {
                    openRoom(habbo, roomLoadRoom, roomTile);
                    return;
                }
                habbo.getClient().sendResponse(new GenericErrorMessagesComposer(GenericErrorMessagesComposer.WRONG_PASSWORD_USED));
                habbo.getClient().sendResponse(new HotelViewComposer());
                habbo.getHabboInfo().setLoadingRoom(0);
                return;
            }
        }
        boolean z2 = false;
        synchronized (roomLoadRoom.roomUnitLock) {
            for (Habbo habbo2 : roomLoadRoom.getHabbos()) {
                if (roomLoadRoom.hasRights(habbo2) || habbo2.getHabboInfo().getId() == roomLoadRoom.getOwnerId() || (roomLoadRoom.hasGuild() && roomLoadRoom.getGuildRightLevel(habbo2).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS))) {
                    habbo2.getClient().sendResponse(new DoorbellAddUserComposer(habbo.getHabboInfo().getUsername()));
                    z2 = true;
                }
            }
        }
        if (z2) {
            habbo.getHabboInfo().setRoomQueueId(i);
            habbo.getClient().sendResponse(new DoorbellAddUserComposer(Emulator.PREVIEW));
            roomLoadRoom.addToQueue(habbo);
        } else {
            habbo.getClient().sendResponse(new RoomAccessDeniedComposer(Emulator.PREVIEW));
            habbo.getClient().sendResponse(new HotelViewComposer());
            habbo.getHabboInfo().setLoadingRoom(0);
        }
    }

    void openRoom(Habbo habbo, Room room, RoomTile roomTile) {
        Room room2;
        Room room3;
        if (room == null || room.getLayout() == null) {
            return;
        }
        if (Emulator.getConfig().getBoolean("hotel.room.enter.logs")) {
            logEnter(habbo, room);
        }
        if (habbo.getHabboInfo().getRoomQueueId() > 0 && (room3 = Emulator.getGameEnvironment().getRoomManager().getRoom(habbo.getHabboInfo().getRoomQueueId())) != null) {
            room3.removeFromQueue(habbo);
        }
        habbo.getHabboInfo().setRoomQueueId(0);
        habbo.getClient().sendResponse(new HideDoorbellComposer(Emulator.PREVIEW));
        if (habbo.getRoomUnit() != null) {
            RoomUnit roomUnit = habbo.getRoomUnit();
            if (roomUnit.getRoom() != null) {
                if (roomUnit.getCurrentLocation() != null) {
                    roomUnit.getCurrentLocation().removeUnit(roomUnit);
                }
                roomUnit.getRoom().sendComposer(new RoomUserRemoveComposer(roomUnit).compose());
            }
            habbo.getRoomUnit().setRoom(null);
        }
        habbo.setRoomUnit(new RoomUnit());
        habbo.getRoomUnit().clearStatus();
        if (habbo.getRoomUnit().getCurrentLocation() == null) {
            habbo.getRoomUnit().setLocation(roomTile != null ? roomTile : room.getLayout().getDoorTile());
            if (habbo.getRoomUnit().getCurrentLocation() != null) {
                habbo.getRoomUnit().setZ(habbo.getRoomUnit().getCurrentLocation().getStackHeight());
            }
            if (roomTile == null) {
                habbo.getRoomUnit().setBodyRotation(RoomUserRotation.values()[room.getLayout().getDoorDirection()]);
                habbo.getRoomUnit().setHeadRotation(RoomUserRotation.values()[room.getLayout().getDoorDirection()]);
            } else {
                habbo.getRoomUnit().setCanLeaveRoomByDoor(false);
                habbo.getRoomUnit().isTeleporting = true;
                HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y);
                if (topItemAt != null) {
                    habbo.getRoomUnit().setRotation(RoomUserRotation.values()[topItemAt.getRotation()]);
                }
            }
        }
        habbo.getRoomUnit().setRoomUnitType(RoomUnitType.USER);
        if (room.isBanned(habbo)) {
            habbo.getClient().sendResponse(new RoomEnterErrorComposer(4));
            return;
        }
        if (room.getUserCount() >= room.getUsersMax() && !habbo.hasPermission(Permission.ACC_FULLROOMS) && !room.hasRights(habbo)) {
            habbo.getClient().sendResponse(new RoomEnterErrorComposer(1));
            return;
        }
        habbo.getRoomUnit().clearStatus();
        habbo.getRoomUnit().cmdTeleport = false;
        habbo.getClient().sendResponse(new RoomOpenComposer());
        habbo.getRoomUnit().setInRoom(true);
        if (habbo.getHabboInfo().getCurrentRoom() != room && habbo.getHabboInfo().getCurrentRoom() != null) {
            habbo.getHabboInfo().getCurrentRoom().removeHabbo(habbo, true);
        } else if (!habbo.getHabboStats().blockFollowing && habbo.getHabboInfo().getCurrentRoom() == null) {
            habbo.getMessenger().connectionChanged(habbo, true, true);
        }
        if (habbo.getHabboInfo().getLoadingRoom() != 0 && (room2 = Emulator.getGameEnvironment().getRoomManager().getRoom(habbo.getHabboInfo().getLoadingRoom())) != null) {
            room2.removeFromQueue(habbo);
        }
        habbo.getHabboInfo().setLoadingRoom(room.getId());
        habbo.getClient().sendResponse(new RoomModelComposer(room));
        if (!room.getWallPaint().equals("0.0")) {
            habbo.getClient().sendResponse(new RoomPaintComposer("wallpaper", room.getWallPaint()));
        }
        if (!room.getFloorPaint().equals("0.0")) {
            habbo.getClient().sendResponse(new RoomPaintComposer("floor", room.getFloorPaint()));
        }
        habbo.getClient().sendResponse(new RoomPaintComposer("landscape", room.getBackgroundPaint()));
        room.refreshRightsForHabbo(habbo);
        habbo.getClient().sendResponse(new RoomScoreComposer(room.getScore(), !hasVotedForRoom(habbo, room)));
        habbo.getRoomUnit().setFastWalk(habbo.getRoomUnit().isFastWalk() && habbo.hasPermission("cmd_fastwalk", room.hasRights(habbo)));
        if (room.isPromoted()) {
            habbo.getClient().sendResponse(new RoomPromotionMessageComposer(room, room.getPromotion()));
        } else {
            habbo.getClient().sendResponse(new RoomPromotionMessageComposer(null, null));
        }
        if (room.getOwnerId() == habbo.getHabboInfo().getId() || habbo.getHabboStats().visitedRoom(room.getId())) {
            return;
        }
        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomEntry"));
    }

    public void enterRoom(final Habbo habbo, final Room room) {
        Poll poll;
        Guild guild;
        Guild guild2;
        if (habbo.getHabboInfo().getLoadingRoom() != room.getId()) {
            if (habbo.getHabboInfo().getLoadingRoom() != 0) {
                habbo.getClient().sendResponse(new HotelViewComposer());
                return;
            }
            return;
        }
        habbo.getRoomUnit().removeStatus(RoomUnitStatus.FLAT_CONTROL);
        habbo.getHabboInfo().setLoadingRoom(0);
        habbo.getHabboInfo().setCurrentRoom(room);
        habbo.getRoomUnit().setPathFinderRoom(room);
        habbo.getRoomUnit().setHandItem(0);
        habbo.getRoomUnit().setRightsLevel(RoomRightLevels.NONE);
        room.refreshRightsForHabbo(habbo);
        if (habbo.getRoomUnit().isKicked && !habbo.getRoomUnit().canWalk()) {
            habbo.getRoomUnit().setCanWalk(true);
        }
        habbo.getRoomUnit().isKicked = false;
        if (habbo.getRoomUnit().getCurrentLocation() == null && !habbo.getRoomUnit().isTeleporting) {
            RoomTile tile = room.getLayout().getTile(room.getLayout().getDoorX(), room.getLayout().getDoorY());
            if (tile != null) {
                habbo.getRoomUnit().setLocation(tile);
                habbo.getRoomUnit().setZ(tile.getStackHeight());
            }
            habbo.getRoomUnit().setBodyRotation(RoomUserRotation.values()[room.getLayout().getDoorDirection()]);
            habbo.getRoomUnit().setHeadRotation(RoomUserRotation.values()[room.getLayout().getDoorDirection()]);
        }
        habbo.getRoomUnit().setPathFinderRoom(room);
        habbo.getRoomUnit().resetIdleTimer();
        habbo.getRoomUnit().setInvisible(false);
        room.addHabbo(habbo);
        ArrayList<Habbo> arrayList = new ArrayList();
        if (!room.getCurrentHabbos().isEmpty()) {
            Collection<Habbo> collectionValues = room.getCurrentHabbos().values();
            Collection<Habbo> habbos = room.getHabbos();
            if (Emulator.getPluginManager().isRegistered(HabboAddedToRoomEvent.class, false)) {
                HabboAddedToRoomEvent habboAddedToRoomEvent = (HabboAddedToRoomEvent) Emulator.getPluginManager().fireEvent(new HabboAddedToRoomEvent(habbo, room, collectionValues, habbos));
                collectionValues = habboAddedToRoomEvent.habbosToSendEnter;
                habbos = habboAddedToRoomEvent.visibleHabbos;
            }
            Iterator<Habbo> it = collectionValues.iterator();
            while (it.hasNext()) {
                GameClient client = it.next().getClient();
                if (client != null) {
                    client.sendResponse(new RoomUsersComposer(habbo).compose());
                    client.sendResponse(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
                }
            }
            for (Habbo habbo2 : habbos) {
                if (!habbo2.getRoomUnit().isInvisible()) {
                    arrayList.add(habbo2);
                }
            }
            synchronized (room.roomUnitLock) {
                habbo.getClient().sendResponse(new RoomUsersComposer(arrayList));
                habbo.getClient().sendResponse(new RoomUserStatusComposer(arrayList));
            }
            if (habbo.getHabboStats().guild != 0 && (guild2 = Emulator.getGameEnvironment().getGuildManager().getGuild(habbo.getHabboStats().guild)) != null) {
                room.sendComposer(new RoomUsersAddGuildBadgeComposer(guild2).compose());
            }
            room.giveEffect(habbo.getRoomUnit(), habbo.getInventory().getEffectsComponent().activatedEffect, -1);
        }
        habbo.getClient().sendResponse(new RoomUsersComposer(room.getCurrentBots().valueCollection(), true));
        if (!room.getCurrentBots().isEmpty()) {
            TIntObjectIterator it2 = room.getCurrentBots().iterator();
            int size = room.getCurrentBots().size();
            while (true) {
                int i = size;
                size--;
                if (i <= 0) {
                    break;
                }
                try {
                    it2.advance();
                    Bot bot = (Bot) it2.value();
                    if (!bot.getRoomUnit().getDanceType().equals(DanceType.NONE)) {
                        habbo.getClient().sendResponse(new RoomUserDanceComposer(bot.getRoomUnit()));
                    }
                    habbo.getClient().sendResponse(new RoomUserStatusComposer(bot.getRoomUnit(), bot.getRoomUnit().getZ()));
                } catch (NoSuchElementException e) {
                }
            }
        }
        habbo.getClient().sendResponse(new RoomPaneComposer(room, room.isOwner(habbo)));
        habbo.getClient().sendResponse(new RoomThicknessComposer(room));
        habbo.getClient().sendResponse(new RoomDataComposer(room, habbo.getClient().getHabbo(), false, true));
        habbo.getClient().sendResponse(new RoomWallItemsComposer(room));
        final THashSet tHashSet = new THashSet();
        THashSet<HabboItem> tHashSet2 = new THashSet<>(room.getFloorItems());
        if (Emulator.getPluginManager().isRegistered(RoomFloorItemsLoadEvent.class, true)) {
            RoomFloorItemsLoadEvent roomFloorItemsLoadEvent = (RoomFloorItemsLoadEvent) Emulator.getPluginManager().fireEvent(new RoomFloorItemsLoadEvent(habbo, tHashSet2));
            if (roomFloorItemsLoadEvent.hasChangedFloorItems()) {
                tHashSet2 = roomFloorItemsLoadEvent.getFloorItems();
            }
        }
        tHashSet2.forEach(new TObjectProcedure<HabboItem>() { // from class: com.eu.habbo.habbohotel.rooms.RoomManager.1
            public boolean execute(HabboItem habboItem) {
                if (room.isHideWired() && (habboItem instanceof InteractionWired)) {
                    return true;
                }
                tHashSet.add(habboItem);
                if (tHashSet.size() != 250) {
                    return true;
                }
                habbo.getClient().sendResponse(new RoomFloorItemsComposer(room.getFurniOwnerNames(), tHashSet));
                tHashSet.clear();
                return true;
            }
        });
        habbo.getClient().sendResponse(new RoomFloorItemsComposer(room.getFurniOwnerNames(), tHashSet));
        tHashSet.clear();
        if (!room.getCurrentPets().isEmpty()) {
            habbo.getClient().sendResponse(new RoomPetComposer(room.getCurrentPets()));
            Iterator it3 = room.getCurrentPets().valueCollection().iterator();
            while (it3.hasNext()) {
                habbo.getClient().sendResponse(new RoomUserStatusComposer(((Pet) it3.next()).getRoomUnit()));
            }
        }
        if (!habbo.getHabboStats().allowTalk()) {
            habbo.getHabboStats().mutedBubbleTracker = true;
            int iRemainingMuteTime = habbo.getHabboStats().remainingMuteTime();
            habbo.getClient().sendResponse(new FloodCounterComposer(iRemainingMuteTime));
            habbo.getClient().sendResponse(new MutedWhisperComposer(iRemainingMuteTime));
            room.sendComposer(new RoomUserIgnoredComposer(habbo, 2).compose());
        } else if (habbo.getHabboStats().mutedBubbleTracker) {
            habbo.getHabboStats().mutedBubbleTracker = false;
        }
        THashMap tHashMap = new THashMap();
        for (Habbo habbo3 : arrayList) {
            if (habbo3.getRoomUnit().getDanceType().getType() > 0) {
                habbo.getClient().sendResponse(new RoomUserDanceComposer(habbo3.getRoomUnit()));
            }
            if (habbo3.getRoomUnit().getHandItem() > 0) {
                habbo.getClient().sendResponse(new RoomUserHandItemComposer(habbo3.getRoomUnit()));
            }
            if (habbo3.getRoomUnit().getEffectId() > 0) {
                habbo.getClient().sendResponse(new RoomUserEffectComposer(habbo3.getRoomUnit()));
            }
            if (habbo3.getRoomUnit().isIdle()) {
                habbo.getClient().sendResponse(new RoomUnitIdleComposer(habbo3.getRoomUnit()));
            }
            if (habbo3.getHabboStats().userIgnored(habbo.getHabboInfo().getId())) {
                habbo3.getClient().sendResponse(new RoomUserIgnoredComposer(habbo, 1));
            }
            if (!habbo3.getHabboStats().allowTalk()) {
                habbo.getClient().sendResponse(new RoomUserIgnoredComposer(habbo3, 2));
            } else if (habbo.getHabboStats().userIgnored(habbo3.getHabboInfo().getId())) {
                habbo.getClient().sendResponse(new RoomUserIgnoredComposer(habbo3, 1));
            }
            if (habbo3.getHabboStats().guild != 0 && !tHashMap.containsKey(Integer.valueOf(habbo3.getHabboStats().guild)) && (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(habbo3.getHabboStats().guild)) != null) {
                tHashMap.put(Integer.valueOf(habbo3.getHabboStats().guild), guild.getBadge());
            }
            if (habbo3.getRoomUnit().getRoomUnitType().equals(RoomUnitType.PET)) {
                try {
                    habbo.getClient().sendResponse(new RoomUserRemoveComposer(habbo3.getRoomUnit()));
                    habbo.getClient().sendResponse(new RoomUserPetComposer(((PetData) habbo3.getHabboStats().cache.get("pet_type")).getType(), ((Integer) habbo3.getHabboStats().cache.get("pet_race")).intValue(), (String) habbo3.getHabboStats().cache.get("pet_color"), habbo3));
                } catch (Exception e2) {
                }
            }
        }
        habbo.getClient().sendResponse(new RoomUsersGuildBadgesComposer(tHashMap));
        if ((room.hasRights(habbo) || (room.hasGuild() && room.getGuildRightLevel(habbo).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS))) && !room.getHabboQueue().isEmpty()) {
            Iterator it4 = room.getHabboQueue().valueCollection().iterator();
            while (it4.hasNext()) {
                habbo.getClient().sendResponse(new DoorbellAddUserComposer(((Habbo) it4.next()).getHabboInfo().getUsername()));
            }
        }
        if (room.getPollId() > 0 && !PollManager.donePoll(habbo.getClient().getHabbo(), room.getPollId()) && (poll = Emulator.getGameEnvironment().getPollManager().getPoll(room.getPollId())) != null) {
            habbo.getClient().sendResponse(new PollStartComposer(poll));
        }
        if (room.hasActiveWordQuiz()) {
            habbo.getClient().sendResponse(new SimplePollStartComposer((Emulator.getIntUnixTimestamp() - room.wordQuizEnd) * Outgoing.CraftableProductsComposer, room.wordQuiz));
            if (room.hasVotedInWordQuiz(habbo)) {
                habbo.getClient().sendResponse(new SimplePollAnswersComposer(room.noVotes, room.yesVotes));
            }
        }
        WiredHandler.handle(WiredTriggerType.ENTER_ROOM, habbo.getRoomUnit(), room, (Object[]) null);
        room.habboEntered(habbo);
        if (habbo.getHabboStats().nux) {
            return;
        }
        if (room.isOwner(habbo) || room.isPublicRoom()) {
            UserNuxEvent.handle(habbo);
        }
    }

    void logEnter(Habbo habbo, Room room) {
        habbo.getHabboStats().roomEnterTimestamp = Emulator.getIntUnixTimestamp();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_enter_log (room_id, user_id, timestamp) VALUES(?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, room.getId());
                    preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(3, (int) habbo.getHabboStats().roomEnterTimestamp);
                    preparedStatementPrepareStatement.execute();
                    if (!habbo.getHabboStats().visitedRoom(room.getId())) {
                        habbo.getHabboStats().addVisitRoom(room.getId());
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
        }
    }

    public void leaveRoom(Habbo habbo, Room room) {
        leaveRoom(habbo, room, true);
    }

    public void leaveRoom(Habbo habbo, Room room, boolean z) {
        if (habbo.getHabboInfo().getCurrentRoom() == null || habbo.getHabboInfo().getCurrentRoom() != room) {
            return;
        }
        habbo.getRoomUnit().setPathFinderRoom(null);
        logExit(habbo);
        room.removeHabbo(habbo, true);
        if (z) {
            habbo.getClient().sendResponse(new HotelViewComposer());
        }
        habbo.getHabboInfo().setCurrentRoom(null);
        habbo.getRoomUnit().isKicked = false;
        if (room.getOwnerId() != habbo.getHabboInfo().getId()) {
            AchievementManager.progressAchievement(room.getOwnerId(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoHosting"), (int) Math.floor((((long) Emulator.getIntUnixTimestamp()) - habbo.getHabboStats().roomEnterTimestamp) / 60000));
        }
        habbo.getMessenger().connectionChanged(habbo, habbo.isOnline(), false);
    }

    public void logExit(Habbo habbo) {
        Emulator.getPluginManager().fireEvent(new UserExitRoomEvent(habbo, UserExitRoomEvent.UserExitRoomReason.DOOR));
        if (habbo.getRoomUnit().getCacheable().containsKey("control")) {
            ((Habbo) habbo.getRoomUnit().getCacheable().remove("control")).getRoomUnit().getCacheable().remove("controller");
        }
        if (habbo.getHabboInfo().getRiding() != null) {
            if (habbo.getHabboInfo().getRiding().getRoomUnit() != null) {
                habbo.getHabboInfo().getRiding().getRoomUnit().setGoalLocation(habbo.getHabboInfo().getRiding().getRoomUnit().getCurrentLocation());
            }
            habbo.getHabboInfo().getRiding().setTask(PetTasks.FREE);
            habbo.getHabboInfo().getRiding().setRider(null);
            habbo.getHabboInfo().setRiding(null);
        }
        Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE room_enter_log SET exit_timestamp = ? WHERE user_id = ? AND room_id = ? ORDER BY timestamp DESC LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp());
                        preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(3, currentRoom.getId());
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
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
            }
        }
    }

    public Set<String> getTags() {
        HashMap map = new HashMap();
        Iterator<Room> it = this.activeRooms.values().iterator();
        while (it.hasNext()) {
            for (String str : it.next().getTags().split(";")) {
                int i = 0;
                if (map.get(str) != null) {
                    i = 0 + 1;
                }
                int i2 = i;
                int i3 = i + 1;
                map.put(str, Integer.valueOf(i2));
            }
        }
        return new TreeMap(map).keySet();
    }

    public ArrayList<Room> getPublicRooms() {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            if (room.isPublicRoom()) {
                arrayList.add(room);
            }
        }
        arrayList.sort(Room.SORT_ID);
        return arrayList;
    }

    public ArrayList<Room> getPopularRooms(int i) {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            if (room.getUserCount() > 0 && (!room.isPublicRoom() || SHOW_PUBLIC_IN_POPULAR_TAB)) {
                arrayList.add(room);
            }
        }
        if (arrayList.isEmpty()) {
            return arrayList;
        }
        Collections.sort(arrayList);
        return new ArrayList<>(arrayList.subList(0, arrayList.size() < i ? arrayList.size() : i));
    }

    public ArrayList<Room> getPopularRooms(int i, int i2) {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            if (!room.isPublicRoom() && room.getCategory() == i2) {
                arrayList.add(room);
            }
        }
        if (arrayList.isEmpty()) {
            return arrayList;
        }
        Collections.sort(arrayList);
        return new ArrayList<>(arrayList.subList(0, arrayList.size() < i ? arrayList.size() : i));
    }

    public Map<Integer, List<Room>> getPopularRoomsByCategory(int i) {
        HashMap map = new HashMap();
        for (Room room : this.activeRooms.values()) {
            if (!room.isPublicRoom()) {
                if (!map.containsKey(Integer.valueOf(room.getCategory()))) {
                    map.put(Integer.valueOf(room.getCategory()), new ArrayList());
                }
                ((List) map.get(Integer.valueOf(room.getCategory()))).add(room);
            }
        }
        HashMap map2 = new HashMap();
        for (Map.Entry entry : map.entrySet()) {
            if (!((List) entry.getValue()).isEmpty()) {
                Collections.sort((List) entry.getValue());
                map2.put((Integer) entry.getKey(), new ArrayList(((List) entry.getValue()).subList(0, ((List) entry.getValue()).size() < i ? ((List) entry.getValue()).size() : i)));
            }
        }
        return map2;
    }

    public ArrayList<Room> getRoomsWithName(String str) {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            if (room.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(room);
            }
        }
        if (arrayList.size() < 25) {
            arrayList.addAll(getOfflineRoomsWithName(str));
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    private ArrayList<Room> getOfflineRoomsWithName(String str) {
        ArrayList<Room> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username AS owner_name, rooms.* FROM rooms INNER JOIN users ON owner_id = users.id WHERE name LIKE ? ORDER BY id DESC LIMIT 25");
                try {
                    preparedStatementPrepareStatement.setString(1, "%" + str + "%");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (!this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                                Room room = new Room(resultSetExecuteQuery);
                                arrayList.add(room);
                                this.activeRooms.put(Integer.valueOf(room.getId()), room);
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<Room> getRoomsWithTag(String str) {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            String[] strArrSplit = room.getTags().split(";");
            int length = strArrSplit.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (strArrSplit[i].toLowerCase().equals(str.toLowerCase())) {
                    arrayList.add(room);
                    break;
                }
                i++;
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public ArrayList<Room> getGroupRoomsWithName(String str) {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : this.activeRooms.values()) {
            if (room.getGuildId() != 0 && room.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(room);
            }
        }
        if (arrayList.size() < 25) {
            arrayList.addAll(getOfflineGroupRoomsWithName(str));
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    private ArrayList<Room> getOfflineGroupRoomsWithName(String str) {
        ArrayList<Room> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username AS owner_name, rooms.* FROM rooms INNER JOIN users ON rooms.owner_id = users.id WHERE name LIKE ? AND guild_id != 0 ORDER BY id DESC LIMIT 25");
                try {
                    preparedStatementPrepareStatement.setString(1, "%" + str + "%");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (!this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                                Room room = new Room(resultSetExecuteQuery);
                                arrayList.add(room);
                                this.activeRooms.put(Integer.valueOf(room.getId()), room);
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<Room> getRoomsFriendsNow(Habbo habbo) {
        Habbo habbo2;
        ArrayList<Room> arrayList = new ArrayList<>();
        for (MessengerBuddy messengerBuddy : habbo.getMessenger().getFriends().values()) {
            if (messengerBuddy.getOnline() != 0 && (habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(messengerBuddy.getId())) != null && habbo2.getHabboInfo().getCurrentRoom() != null) {
                arrayList.add(habbo2.getHabboInfo().getCurrentRoom());
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public ArrayList<Room> getRoomsFriendsOwn(Habbo habbo) {
        Habbo habbo2;
        ArrayList<Room> arrayList = new ArrayList<>();
        for (MessengerBuddy messengerBuddy : habbo.getMessenger().getFriends().values()) {
            if (messengerBuddy.getOnline() != 0 && (habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(messengerBuddy.getId())) != null) {
                arrayList.addAll(getRoomsForHabbo(habbo2));
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public ArrayList<Room> getRoomsVisited(Habbo habbo, boolean z, int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList<Room> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT rooms.* FROM room_enter_log INNER JOIN rooms ON room_enter_log.room_id = rooms.id WHERE user_id = ? AND timestamp >= ? AND rooms.owner_id != ? GROUP BY rooms.id ORDER BY timestamp DESC LIMIT " + i);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
            preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp() - 259200);
            preparedStatementPrepareStatement.setInt(3, z ? 0 : habbo.getHabboInfo().getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    Room room = this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id")));
                    if (room == null) {
                        room = new Room(resultSetExecuteQuery);
                        this.activeRooms.put(Integer.valueOf(room.getId()), room);
                    }
                    arrayList.add(room);
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
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            Collections.sort(arrayList);
            return arrayList;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public ArrayList<Room> getRoomsFavourite(final Habbo habbo) {
        final ArrayList<Room> arrayList = new ArrayList<>();
        habbo.getHabboStats().getFavoriteRooms().forEach(new TIntProcedure() { // from class: com.eu.habbo.habbohotel.rooms.RoomManager.2
            public boolean execute(int i) {
                Room room = RoomManager.this.getRoom(i);
                if (room == null) {
                    return true;
                }
                if (room.getState() == RoomState.INVISIBLE) {
                    room.loadData();
                    if (!room.hasRights(habbo)) {
                        return true;
                    }
                }
                arrayList.add(room);
                return true;
            }
        });
        return arrayList;
    }

    public List<Room> getGroupRooms(Habbo habbo, int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList arrayList = new ArrayList();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT rooms.* FROM rooms INNER JOIN guilds_members ON guilds_members.guild_id = rooms.guild_id WHERE guilds_members.user_id = ? AND level_id != 3");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    if (this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                        arrayList.add(this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id"))));
                    } else {
                        arrayList.add(new Room(resultSetExecuteQuery));
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
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            Collections.sort(arrayList);
            return arrayList.subList(0, arrayList.size() > i ? i : arrayList.size());
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public ArrayList<Room> getRoomsWithRights(Habbo habbo) {
        ArrayList<Room> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT rooms.* FROM rooms INNER JOIN room_rights ON room_rights.room_id = rooms.id WHERE room_rights.user_id = ? ORDER BY rooms.id DESC LIMIT 30");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                                arrayList.add(this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id"))));
                            } else {
                                arrayList.add(new Room(resultSetExecuteQuery));
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<Room> getRoomsWithFriendsIn(Habbo habbo, int i) {
        ArrayList<Room> arrayList = new ArrayList<>();
        Iterator<MessengerBuddy> it = habbo.getMessenger().getFriends().values().iterator();
        while (it.hasNext()) {
            Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(it.next().getId());
            if (habbo2 != null && habbo2.getHabboInfo() != null) {
                Room currentRoom = habbo2.getHabboInfo().getCurrentRoom();
                if (currentRoom != null && !arrayList.contains(currentRoom) && currentRoom.hasRights(habbo)) {
                    arrayList.add(currentRoom);
                }
                if (arrayList.size() >= i) {
                    break;
                }
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    public List<Room> getTopRatedRooms(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList arrayList = new ArrayList();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms ORDER BY score DESC LIMIT ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    if (this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                        arrayList.add(this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id"))));
                    } else {
                        arrayList.add(new Room(resultSetExecuteQuery));
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
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public ArrayList<Room> getRoomsWithAdminRights(Habbo habbo) {
        ArrayList<Room> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM rooms INNER JOIN guilds_members ON guilds_members.guild_id = rooms.guild_id WHERE guilds_members.user_id = ? AND level_id = 0");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (this.activeRooms.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("id")))) {
                                arrayList.add(this.activeRooms.get(Integer.valueOf(resultSetExecuteQuery.getInt("id"))));
                            } else {
                                arrayList.add(new Room(resultSetExecuteQuery));
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<Room> getRoomsInGroup(Habbo habbo) {
        return new ArrayList<>();
    }

    public ArrayList<Room> getRoomsPromoted() {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : getActiveRooms()) {
            if (room.isPromoted()) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public ArrayList<Room> getRoomsStaffPromoted() {
        ArrayList<Room> arrayList = new ArrayList<>();
        for (Room room : getActiveRooms()) {
            if (room.isStaffPromotedRoom()) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public List<Room> filterRoomsByOwner(List<Room> list, String str) {
        ArrayList arrayList = new ArrayList();
        for (Room room : list) {
            if (room.getOwnerName().equalsIgnoreCase(str)) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public List<Room> filterRoomsByName(List<Room> list, String str) {
        ArrayList arrayList = new ArrayList();
        for (Room room : list) {
            if (room.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public List<Room> filterRoomsByNameAndDescription(List<Room> list, String str) {
        ArrayList arrayList = new ArrayList();
        for (Room room : list) {
            if (room.getName().toLowerCase().contains(str.toLowerCase()) || room.getDescription().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public List<Room> filterRoomsByTag(List<Room> list, String str) {
        ArrayList arrayList = new ArrayList();
        for (Room room : list) {
            if (room.getTags().split(";").length != 0) {
                for (String str2 : room.getTags().split(";")) {
                    if (str2.equalsIgnoreCase(str)) {
                        arrayList.add(room);
                    }
                }
            }
        }
        return arrayList;
    }

    public List<Room> filterRoomsByGroup(List<Room> list, String str) {
        ArrayList arrayList = new ArrayList();
        for (Room room : list) {
            if (room.getGuildId() != 0 && Emulator.getGameEnvironment().getGuildManager().getGuild(room.getGuildId()).getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(room);
            }
        }
        return arrayList;
    }

    public synchronized void dispose() {
        Iterator<Room> it = this.activeRooms.values().iterator();
        while (it.hasNext()) {
            it.next().dispose();
        }
        this.activeRooms.clear();
        LOGGER.info("Room Manager -> Disposed!");
    }

    public CustomRoomLayout insertCustomLayout(Room room, String str, int i, int i2, int i3) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_models_custom (id, name, door_x, door_y, door_dir, heightmap) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE door_x = ?, door_y = ?, door_dir = ?, heightmap = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, room.getId());
                    preparedStatementPrepareStatement.setString(2, "custom_" + room.getId());
                    preparedStatementPrepareStatement.setInt(3, i);
                    preparedStatementPrepareStatement.setInt(4, i2);
                    preparedStatementPrepareStatement.setInt(5, i3);
                    preparedStatementPrepareStatement.setString(6, str);
                    preparedStatementPrepareStatement.setInt(7, i);
                    preparedStatementPrepareStatement.setInt(8, i2);
                    preparedStatementPrepareStatement.setInt(9, i3);
                    preparedStatementPrepareStatement.setString(10, str);
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
        }
        return loadCustomLayout(room);
    }

    public void banUserFromRoom(Habbo habbo, int i, int i2, RoomBanTypes roomBanTypes) {
        Room room = getRoom(i2);
        if (room == null) {
            return;
        }
        if (habbo == null || room.hasRights(habbo)) {
            String username = Emulator.PREVIEW;
            Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
            if (habbo2 == null) {
                HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(i);
                if (offlineHabboInfo != null) {
                    if (offlineHabboInfo.getRank().hasPermission(Permission.ACC_UNKICKABLE, false)) {
                        return;
                    } else {
                        username = offlineHabboInfo.getUsername();
                    }
                }
            } else if (habbo2.hasPermission(Permission.ACC_UNKICKABLE)) {
                return;
            } else {
                username = habbo2.getHabboInfo().getUsername();
            }
            if (username.isEmpty()) {
                return;
            }
            RoomBan roomBan = new RoomBan(i2, i, username, Emulator.getIntUnixTimestamp() + roomBanTypes.duration);
            roomBan.insert();
            room.addRoomBan(roomBan);
            if (habbo2 == null || habbo2.getHabboInfo().getCurrentRoom() != room) {
                return;
            }
            room.removeHabbo(habbo2, true);
            habbo2.getClient().sendResponse(new RoomEnterErrorComposer(4));
        }
    }

    public void registerGameType(Class<? extends Game> cls) {
        this.gameTypes.add(cls);
    }

    public void unregisterGameType(Class<? extends Game> cls) {
        this.gameTypes.remove(cls);
    }

    public ArrayList<Class<? extends Game>> getGameTypes() {
        return this.gameTypes;
    }
}
