package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionBackgroundToner;
import com.eu.habbo.habbohotel.items.interactions.InteractionBlackHole;
import com.eu.habbo.habbohotel.items.interactions.InteractionBuildArea;
import com.eu.habbo.habbohotel.items.interactions.InteractionFireworks;
import com.eu.habbo.habbohotel.items.interactions.InteractionGate;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni;
import com.eu.habbo.habbohotel.items.interactions.InteractionJukeBox;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.items.interactions.InteractionMuteArea;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.items.interactions.InteractionPyramid;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoller;
import com.eu.habbo.habbohotel.items.interactions.InteractionSnowboardSlope;
import com.eu.habbo.habbohotel.items.interactions.InteractionStackHelper;
import com.eu.habbo.habbohotel.items.interactions.InteractionStickyPole;
import com.eu.habbo.habbohotel.items.interactions.InteractionTalkingFurniture;
import com.eu.habbo.habbohotel.items.interactions.InteractionTent;
import com.eu.habbo.habbohotel.items.interactions.InteractionWater;
import com.eu.habbo.habbohotel.items.interactions.InteractionWaterItem;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredHighscore;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameScoreboard;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiTeleporter;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeExitTile;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionNest;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetDrink;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetFood;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredBlob;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericErrorMessagesComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.AddPetComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.polls.infobus.SimplePollAnswerComposer;
import com.eu.habbo.messages.outgoing.polls.infobus.SimplePollStartComposer;
import com.eu.habbo.messages.outgoing.rooms.HideDoorbellComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomAccessDeniedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomAddRightsListComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomOwnerComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomRemoveRightsListComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomRightsComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomRightsListComposer;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddWallItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.items.ItemStateComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveWallItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RoomFloorItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.items.WallItemUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitIdleComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitOnRollerComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDanceComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserUnbannedComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.plugin.events.furniture.FurnitureBuildheightEvent;
import com.eu.habbo.plugin.events.furniture.FurnitureMovedEvent;
import com.eu.habbo.plugin.events.furniture.FurniturePickedUpEvent;
import com.eu.habbo.plugin.events.furniture.FurniturePlacedEvent;
import com.eu.habbo.plugin.events.furniture.FurnitureRolledEvent;
import com.eu.habbo.plugin.events.furniture.FurnitureRotatedEvent;
import com.eu.habbo.plugin.events.furniture.FurnitureStackHeightEvent;
import com.eu.habbo.plugin.events.rooms.RoomLoadedEvent;
import com.eu.habbo.plugin.events.rooms.RoomUnloadedEvent;
import com.eu.habbo.plugin.events.rooms.RoomUnloadingEvent;
import com.eu.habbo.plugin.events.users.UserExitRoomEvent;
import com.eu.habbo.plugin.events.users.UserRightsTakenEvent;
import com.eu.habbo.plugin.events.users.UserRolledEvent;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import io.netty.util.internal.ConcurrentSet;
import java.awt.Color;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/Room.class */
public class Room implements Comparable<Room>, ISerialize, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);
    public static final Comparator SORT_SCORE = (obj, obj2) -> {
        if ((obj instanceof Room) && (obj2 instanceof Room)) {
            return ((Room) obj2).getScore() - ((Room) obj).getScore();
        }
        return 0;
    };
    public static final Comparator SORT_ID = (obj, obj2) -> {
        if ((obj instanceof Room) && (obj2 instanceof Room)) {
            return ((Room) obj2).getId() - ((Room) obj).getId();
        }
        return 0;
    };
    private static final TIntObjectHashMap<RoomMoodlightData> defaultMoodData = new TIntObjectHashMap<>();
    public static boolean HABBO_CHAT_DELAY = false;
    public static int MAXIMUM_BOTS = 10;
    public static int MAXIMUM_PETS = 10;
    public static int MAXIMUM_FURNI = 2500;
    public static int MAXIMUM_POSTITNOTES = RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS;
    public static int HAND_ITEM_TIME = 10;
    public static int IDLE_CYCLES = 240;
    public static int IDLE_CYCLES_KICK = 480;
    public static String PREFIX_FORMAT = "[<font color=\"%color%\">%prefix%</font>] ";
    public static int ROLLERS_MAXIMUM_ROLL_AVATARS = 1;
    public static boolean MUTEAREA_CAN_WHISPER = false;
    public static double MAXIMUM_FURNI_HEIGHT = 40.0d;
    public final List<Integer> userVotes;
    private final THashSet<RoomTrade> activeTrades;
    private final TIntArrayList rights;
    private final TIntIntHashMap mutedHabbos;
    private final ConcurrentSet<Game> games;
    private final TIntObjectMap<String> furniOwnerNames;
    private final TIntIntMap furniOwnerCount;
    private final TIntObjectMap<RoomMoodlightData> moodlightData;
    private final THashSet<String> wordFilterWords;
    private final TIntObjectMap<HabboItem> roomItems;
    public ScheduledFuture roomCycleTask;
    private int id;
    private int ownerId;
    private String ownerName;
    private String name;
    private String description;
    private RoomLayout layout;
    private boolean overrideModel;
    private String layoutName;
    private String password;
    private RoomState state;
    private int usersMax;
    private volatile int score;
    private volatile int category;
    private String floorPaint;
    private String wallPaint;
    private String backgroundPaint;
    private int wallSize;
    private int wallHeight;
    private int floorSize;
    private int guild;
    private String tags;
    private volatile boolean publicRoom;
    private volatile boolean staffPromotedRoom;
    private volatile boolean allowPets;
    private volatile boolean allowPetsEat;
    private volatile boolean allowWalkthrough;
    private volatile boolean allowBotsWalk;
    private volatile boolean allowEffects;
    private volatile boolean hideWall;
    private volatile int chatMode;
    private volatile int chatWeight;
    private volatile int chatSpeed;
    private volatile int chatDistance;
    private volatile int chatProtection;
    private volatile int muteOption;
    private volatile int kickOption;
    private volatile int banOption;
    private volatile int pollId;
    private volatile boolean promoted;
    private volatile int tradeMode;
    private volatile boolean moveDiagonally;
    private volatile boolean jukeboxActive;
    private volatile boolean hideWired;
    private RoomPromotion promotion;
    private volatile boolean needsUpdate;
    private volatile boolean loaded;
    private volatile boolean preLoaded;
    private int idleCycles;
    private volatile int unitCounter;
    private volatile int rollerSpeed;
    private volatile boolean muted;
    private RoomSpecialTypes roomSpecialTypes;
    private TraxManager traxManager;
    private boolean cycleOdd;
    private long cycleTimestamp;
    public final Object roomUnitLock = new Object();
    public final ConcurrentHashMap<RoomTile, THashSet<HabboItem>> tileCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Habbo> currentHabbos = new ConcurrentHashMap<>(3);
    private final TIntObjectMap<Habbo> habboQueue = TCollections.synchronizedMap(new TIntObjectHashMap(0));
    private final TIntObjectMap<Bot> currentBots = TCollections.synchronizedMap(new TIntObjectHashMap(0));
    private final TIntObjectMap<Pet> currentPets = TCollections.synchronizedMap(new TIntObjectHashMap(0));
    private final Object loadLock = new Object();
    public volatile boolean preventUnloading = false;
    public volatile boolean preventUncaching = false;
    public ConcurrentSet<ServerMessage> scheduledComposers = new ConcurrentSet<>();
    public ConcurrentSet<Runnable> scheduledTasks = new ConcurrentSet<>();
    public String wordQuiz = Emulator.PREVIEW;
    public int noVotes = 0;
    public int yesVotes = 0;
    public int wordQuizEnd = 0;
    private final int muteTime = Emulator.getConfig().getInt("hotel.flood.mute.time", 30);
    private long rollerCycle = System.currentTimeMillis();
    private volatile int lastTimerReset = Emulator.getIntUnixTimestamp();
    private final TIntObjectHashMap<RoomBan> bannedHabbos = new TIntObjectHashMap<>();

    public Room(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.ownerId = resultSet.getInt("owner_id");
        this.ownerName = resultSet.getString("owner_name");
        this.name = resultSet.getString("name");
        this.description = resultSet.getString("description");
        this.password = resultSet.getString("password");
        this.state = RoomState.valueOf(resultSet.getString("state").toUpperCase());
        this.usersMax = resultSet.getInt("users_max");
        this.score = resultSet.getInt("score");
        this.category = resultSet.getInt("category");
        this.floorPaint = resultSet.getString("paper_floor");
        this.wallPaint = resultSet.getString("paper_wall");
        this.backgroundPaint = resultSet.getString("paper_landscape");
        this.wallSize = resultSet.getInt("thickness_wall");
        this.wallHeight = resultSet.getInt("wall_height");
        this.floorSize = resultSet.getInt("thickness_floor");
        this.tags = resultSet.getString("tags");
        this.publicRoom = resultSet.getBoolean("is_public");
        this.staffPromotedRoom = resultSet.getBoolean("is_staff_picked");
        this.allowPets = resultSet.getBoolean("allow_other_pets");
        this.allowPetsEat = resultSet.getBoolean("allow_other_pets_eat");
        this.allowWalkthrough = resultSet.getBoolean("allow_walkthrough");
        this.hideWall = resultSet.getBoolean("allow_hidewall");
        this.chatMode = resultSet.getInt("chat_mode");
        this.chatWeight = resultSet.getInt("chat_weight");
        this.chatSpeed = resultSet.getInt("chat_speed");
        this.chatDistance = resultSet.getInt("chat_hearing_distance");
        this.chatProtection = resultSet.getInt("chat_protection");
        this.muteOption = resultSet.getInt("who_can_mute");
        this.kickOption = resultSet.getInt("who_can_kick");
        this.banOption = resultSet.getInt("who_can_ban");
        this.pollId = resultSet.getInt("poll_id");
        this.guild = resultSet.getInt("guild_id");
        this.rollerSpeed = resultSet.getInt("roller_speed");
        this.overrideModel = resultSet.getString("override_model").equals("1");
        this.layoutName = resultSet.getString("model");
        this.promoted = resultSet.getString("promoted").equals("1");
        this.jukeboxActive = resultSet.getString("jukebox_active").equals("1");
        this.hideWired = resultSet.getString("hidewired").equals("1");
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM room_promotions WHERE room_id = ? AND end_timestamp > ? LIMIT 1");
                try {
                    if (this.promoted) {
                        preparedStatementPrepareStatement.setInt(1, this.id);
                        preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp());
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            this.promoted = false;
                            if (resultSetExecuteQuery.next()) {
                                this.promoted = true;
                                this.promotion = new RoomPromotion(this, resultSetExecuteQuery);
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
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
                    loadBans(connection);
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
        this.tradeMode = resultSet.getInt("trade_mode");
        this.moveDiagonally = resultSet.getString("move_diagonally").equals("1");
        this.preLoaded = true;
        this.allowBotsWalk = true;
        this.allowEffects = true;
        this.furniOwnerNames = TCollections.synchronizedMap(new TIntObjectHashMap(0));
        this.furniOwnerCount = TCollections.synchronizedMap(new TIntIntHashMap(0));
        this.roomItems = TCollections.synchronizedMap(new TIntObjectHashMap(0));
        this.wordFilterWords = new THashSet<>(0);
        this.moodlightData = new TIntObjectHashMap(defaultMoodData);
        for (String str : resultSet.getString("moodlight_data").split(";")) {
            RoomMoodlightData roomMoodlightDataFromString = RoomMoodlightData.fromString(str);
            this.moodlightData.put(roomMoodlightDataFromString.getId(), roomMoodlightDataFromString);
        }
        this.mutedHabbos = new TIntIntHashMap();
        this.games = new ConcurrentSet<>();
        this.activeTrades = new THashSet<>(0);
        this.rights = new TIntArrayList();
        this.userVotes = new ArrayList();
    }

    public synchronized void loadData() {
        Connection connection;
        synchronized (this.loadLock) {
            if (!this.preLoaded || this.loaded) {
                return;
            }
            this.preLoaded = false;
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
            try {
                synchronized (this.roomUnitLock) {
                    this.unitCounter = 0;
                    this.currentHabbos.clear();
                    this.currentPets.clear();
                    this.currentBots.clear();
                }
                this.roomSpecialTypes = new RoomSpecialTypes();
                try {
                    loadLayout();
                } catch (Exception e2) {
                    LOGGER.error("Caught exception", e2);
                }
                try {
                    loadRights(connection);
                } catch (Exception e3) {
                    LOGGER.error("Caught exception", e3);
                }
                try {
                    loadItems(connection);
                } catch (Exception e4) {
                    LOGGER.error("Caught exception", e4);
                }
                try {
                    loadHeightmap();
                } catch (Exception e5) {
                    LOGGER.error("Caught exception", e5);
                }
                try {
                    loadBots(connection);
                } catch (Exception e6) {
                    LOGGER.error("Caught exception", e6);
                }
                try {
                    loadPets(connection);
                } catch (Exception e7) {
                    LOGGER.error("Caught exception", e7);
                }
                try {
                    loadWordFilter(connection);
                } catch (Exception e8) {
                    LOGGER.error("Caught exception", e8);
                }
                try {
                    loadWiredData(connection);
                } catch (Exception e9) {
                    LOGGER.error("Caught exception", e9);
                }
                this.idleCycles = 0;
                this.loaded = true;
                this.roomCycleTask = Emulator.getThreading().getService().scheduleAtFixedRate(this, 500L, 500L, TimeUnit.MILLISECONDS);
                if (connection != null) {
                    connection.close();
                }
                this.traxManager = new TraxManager(this);
                if (this.jukeboxActive) {
                    this.traxManager.play(0);
                    TObjectHashIterator it = this.roomSpecialTypes.getItemsOfType(InteractionJukeBox.class).iterator();
                    while (it.hasNext()) {
                        HabboItem habboItem = (HabboItem) it.next();
                        habboItem.setExtradata("1");
                        updateItem(habboItem);
                    }
                }
                TObjectHashIterator it2 = this.roomSpecialTypes.getItemsOfType(InteractionFireworks.class).iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem2 = (HabboItem) it2.next();
                    habboItem2.setExtradata("1");
                    updateItem(habboItem2);
                }
                Emulator.getPluginManager().fireEvent(new RoomLoadedEvent(this));
            } catch (Throwable th) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
    }

    private synchronized void loadLayout() {
        if (this.layout == null) {
            if (this.overrideModel) {
                this.layout = Emulator.getGameEnvironment().getRoomManager().loadCustomLayout(this);
            } else {
                this.layout = Emulator.getGameEnvironment().getRoomManager().loadLayout(this.layoutName, this);
            }
        }
    }

    private synchronized void loadHeightmap() {
        if (this.layout == null) {
            LOGGER.error("Unknown Room Layout for Room (ID: {})", Integer.valueOf(this.id));
            return;
        }
        short s = 0;
        while (true) {
            short s2 = s;
            if (s2 >= this.layout.getMapSizeX()) {
                return;
            }
            short s3 = 0;
            while (true) {
                short s4 = s3;
                if (s4 < this.layout.getMapSizeY()) {
                    RoomTile tile = this.layout.getTile(s2, s4);
                    if (tile != null) {
                        updateTile(tile);
                    }
                    s3 = (short) (s4 + 1);
                }
            }
            s = (short) (s2 + 1);
        }
    }

    private synchronized void loadItems(Connection connection) {
        this.roomItems.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM items WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        addHabboItem(Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        if (itemCount() > MAXIMUM_FURNI) {
            LOGGER.error("Room ID: {} has exceeded the furniture limit ({} > {}).", new Object[]{Integer.valueOf(getId()), Integer.valueOf(itemCount()), Integer.valueOf(MAXIMUM_FURNI)});
        }
    }

    private synchronized void loadWiredData(Connection connection) {
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT id, wired_data FROM items WHERE room_id = ? AND wired_data<>''");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        try {
                            HabboItem habboItem = getHabboItem(resultSetExecuteQuery.getInt("id"));
                            if (habboItem instanceof InteractionWired) {
                                ((InteractionWired) habboItem).loadWiredData(resultSetExecuteQuery, this);
                            }
                        } catch (SQLException e) {
                            LOGGER.error("Caught SQL exception", e);
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
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        } catch (Exception e3) {
            LOGGER.error("Caught exception", e3);
        }
    }

    private synchronized void loadBots(Connection connection) {
        this.currentBots.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username AS owner_name, bots.* FROM bots INNER JOIN users ON bots.user_id = users.id WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        Bot botLoadBot = Emulator.getGameEnvironment().getBotManager().loadBot(resultSetExecuteQuery);
                        if (botLoadBot != null) {
                            botLoadBot.setRoom(this);
                            botLoadBot.setRoomUnit(new RoomUnit());
                            botLoadBot.getRoomUnit().setPathFinderRoom(this);
                            botLoadBot.getRoomUnit().setLocation(this.layout.getTile((short) resultSetExecuteQuery.getInt("x"), (short) resultSetExecuteQuery.getInt("y")));
                            if (botLoadBot.getRoomUnit().getCurrentLocation() == null) {
                                botLoadBot.getRoomUnit().setLocation(getLayout().getDoorTile());
                                botLoadBot.getRoomUnit().setRotation(RoomUserRotation.fromValue(getLayout().getDoorDirection()));
                            } else {
                                botLoadBot.getRoomUnit().setZ(resultSetExecuteQuery.getDouble("z"));
                                botLoadBot.getRoomUnit().setPreviousLocationZ(resultSetExecuteQuery.getDouble("z"));
                                botLoadBot.getRoomUnit().setRotation(RoomUserRotation.values()[resultSetExecuteQuery.getInt("rot")]);
                            }
                            botLoadBot.getRoomUnit().setRoomUnitType(RoomUnitType.BOT);
                            botLoadBot.getRoomUnit().setDanceType(DanceType.values()[resultSetExecuteQuery.getInt("dance")]);
                            botLoadBot.getRoomUnit().setInRoom(true);
                            giveEffect(botLoadBot.getRoomUnit(), resultSetExecuteQuery.getInt("effect"), Integer.MAX_VALUE);
                            addBot(botLoadBot);
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private synchronized void loadPets(Connection connection) {
        this.currentPets.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username as pet_owner_name, users_pets.* FROM users_pets INNER JOIN users ON users_pets.user_id = users.id WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        try {
                            Pet petLoadPet = PetManager.loadPet(resultSetExecuteQuery);
                            petLoadPet.setRoom(this);
                            petLoadPet.setRoomUnit(new RoomUnit());
                            petLoadPet.getRoomUnit().setPathFinderRoom(this);
                            petLoadPet.getRoomUnit().setLocation(this.layout.getTile((short) resultSetExecuteQuery.getInt("x"), (short) resultSetExecuteQuery.getInt("y")));
                            if (petLoadPet.getRoomUnit().getCurrentLocation() == null) {
                                petLoadPet.getRoomUnit().setLocation(getLayout().getDoorTile());
                                petLoadPet.getRoomUnit().setRotation(RoomUserRotation.fromValue(getLayout().getDoorDirection()));
                            } else {
                                petLoadPet.getRoomUnit().setZ(resultSetExecuteQuery.getDouble("z"));
                                petLoadPet.getRoomUnit().setRotation(RoomUserRotation.values()[resultSetExecuteQuery.getInt("rot")]);
                            }
                            petLoadPet.getRoomUnit().setRoomUnitType(RoomUnitType.PET);
                            petLoadPet.getRoomUnit().setCanWalk(true);
                            addPet(petLoadPet);
                            getFurniOwnerNames().put(petLoadPet.getUserId(), resultSetExecuteQuery.getString("pet_owner_name"));
                        } catch (SQLException e) {
                            LOGGER.error("Caught SQL exception", e);
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
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
    }

    private synchronized void loadWordFilter(Connection connection) {
        this.wordFilterWords.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM room_wordfilter WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        this.wordFilterWords.add(resultSetExecuteQuery.getString("word"));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void updateTile(RoomTile roomTile) {
        if (roomTile != null) {
            roomTile.setStackHeight(getStackHeight(roomTile.x, roomTile.y, false));
            roomTile.setState(calculateTileState(roomTile));
        }
    }

    public void updateTiles(THashSet<RoomTile> tHashSet) {
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            this.tileCache.remove(roomTile);
            roomTile.setStackHeight(getStackHeight(roomTile.x, roomTile.y, false));
            roomTile.setState(calculateTileState(roomTile));
        }
        sendComposer(new UpdateStackHeightComposer(this, tHashSet).compose());
    }

    private RoomTileState calculateTileState(RoomTile roomTile) {
        return calculateTileState(roomTile, null);
    }

    private RoomTileState calculateTileState(RoomTile roomTile, HabboItem habboItem) {
        if (roomTile == null || roomTile.state == RoomTileState.INVALID) {
            return RoomTileState.INVALID;
        }
        RoomTileState roomTileStateCheckStateForItem = RoomTileState.OPEN;
        THashSet<HabboItem> itemsAt = getItemsAt(roomTile);
        if (itemsAt == null) {
            return RoomTileState.INVALID;
        }
        HabboItem habboItem2 = null;
        TObjectHashIterator it = itemsAt.iterator();
        while (it.hasNext()) {
            HabboItem habboItem3 = (HabboItem) it.next();
            if (habboItem == null || habboItem3 != habboItem) {
                if (habboItem3.getBaseItem().allowLay()) {
                    return RoomTileState.LAY;
                }
                if (habboItem2 == null || habboItem2.getZ() + Item.getCurrentHeight(habboItem2) <= habboItem3.getZ() + Item.getCurrentHeight(habboItem3)) {
                    roomTileStateCheckStateForItem = checkStateForItem(habboItem3, roomTile);
                    habboItem2 = habboItem3;
                }
            }
        }
        return roomTileStateCheckStateForItem;
    }

    private RoomTileState checkStateForItem(HabboItem habboItem, RoomTile roomTile) {
        RoomTileState roomTileState = RoomTileState.BLOCKED;
        if (habboItem.isWalkable()) {
            roomTileState = RoomTileState.OPEN;
        }
        if (habboItem.getBaseItem().allowSit()) {
            roomTileState = RoomTileState.SIT;
        }
        if (habboItem.getBaseItem().allowLay()) {
            roomTileState = RoomTileState.LAY;
        }
        RoomTileState overrideTileState = habboItem.getOverrideTileState(roomTile, this);
        if (overrideTileState != null) {
            roomTileState = overrideTileState;
        }
        return roomTileState;
    }

    public boolean tileWalkable(RoomTile roomTile) {
        return tileWalkable(roomTile.x, roomTile.y);
    }

    public boolean tileWalkable(short s, short s2) {
        boolean zTileWalkable = this.layout.tileWalkable(s, s2);
        RoomTile tile = getLayout().getTile(s, s2);
        if (zTileWalkable && tile != null && tile.hasUnits() && !this.allowWalkthrough) {
            zTileWalkable = false;
        }
        return zTileWalkable;
    }

    public void pickUpItem(HabboItem habboItem, Habbo habbo) {
        if (habboItem == null) {
            return;
        }
        if (Emulator.getPluginManager().isRegistered(FurniturePickedUpEvent.class, true)) {
            FurniturePickedUpEvent furniturePickedUpEvent = new FurniturePickedUpEvent(habboItem, habbo);
            Emulator.getPluginManager().fireEvent(furniturePickedUpEvent);
            if (furniturePickedUpEvent.isCancelled()) {
                return;
            }
        }
        removeHabboItem(habboItem.getId());
        habboItem.onPickUp(this);
        habboItem.setRoomId(0);
        habboItem.needsUpdate(true);
        if (habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
            sendComposer(new RemoveFloorItemComposer(habboItem).compose());
            THashSet<RoomTile> tHashSet = new THashSet<>();
            Rectangle rectangle = RoomLayout.getRectangle(habboItem.getX(), habboItem.getY(), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation());
            int i = rectangle.x;
            while (true) {
                short s = (short) i;
                if (s >= ((double) rectangle.x) + rectangle.getWidth()) {
                    break;
                }
                int i2 = rectangle.y;
                while (true) {
                    short s2 = (short) i2;
                    if (s2 < ((double) rectangle.y) + rectangle.getHeight()) {
                        double stackHeight = getStackHeight(s, s2, false);
                        RoomTile tile = this.layout.getTile(s, s2);
                        if (tile != null) {
                            tile.setStackHeight(stackHeight);
                            tHashSet.add(tile);
                        }
                        i2 = s2 + 1;
                    }
                }
                i = s + 1;
            }
            sendComposer(new UpdateStackHeightComposer(this, tHashSet).compose());
            updateTiles(tHashSet);
            TObjectHashIterator it = tHashSet.iterator();
            while (it.hasNext()) {
                RoomTile roomTile = (RoomTile) it.next();
                updateHabbosAt(roomTile.x, roomTile.y);
                updateBotsAt(roomTile.x, roomTile.y);
            }
        } else if (habboItem.getBaseItem().getType() == FurnitureType.WALL) {
            sendComposer(new RemoveWallItemComposer(habboItem).compose());
        }
        Habbo habbo2 = (habbo == null || habbo.getHabboInfo().getId() != habboItem.getId()) ? Emulator.getGameServer().getGameClientManager().getHabbo(habboItem.getUserId()) : habbo;
        if (habbo2 != null) {
            habbo2.getInventory().getItemsComponent().addItem(habboItem);
            habbo2.getClient().sendResponse(new AddHabboItemComposer(habboItem));
            habbo2.getClient().sendResponse(new InventoryRefreshComposer());
        }
        Emulator.getThreading().run(habboItem);
    }

    public void updateHabbosAt(Rectangle rectangle) {
        int i = rectangle.x;
        while (true) {
            short s = (short) i;
            if (s >= rectangle.x + rectangle.width) {
                return;
            }
            int i2 = rectangle.y;
            while (true) {
                short s2 = (short) i2;
                if (s2 < rectangle.y + rectangle.height) {
                    updateHabbosAt(s, s2);
                    i2 = s2 + 1;
                }
            }
            i = s + 1;
        }
    }

    public void updateHabbo(Habbo habbo) {
        updateRoomUnit(habbo.getRoomUnit());
    }

    public void updateRoomUnit(RoomUnit roomUnit) {
        HabboItem topItemAt = getTopItemAt(roomUnit.getX(), roomUnit.getY());
        if ((topItemAt == null && !roomUnit.cmdSit) || (topItemAt != null && !topItemAt.getBaseItem().allowSit())) {
            roomUnit.removeStatus(RoomUnitStatus.SIT);
        }
        double z = roomUnit.getZ();
        if (topItemAt != null) {
            if (topItemAt.getBaseItem().allowSit()) {
                roomUnit.setZ(topItemAt.getZ());
            } else {
                roomUnit.setZ(topItemAt.getZ() + Item.getCurrentHeight(topItemAt));
            }
            if (z != roomUnit.getZ()) {
                this.scheduledTasks.add(() -> {
                    try {
                        topItemAt.onWalkOn(roomUnit, this, null);
                    } catch (Exception e) {
                    }
                });
            }
        }
        sendComposer(new RoomUserStatusComposer(roomUnit).compose());
    }

    public void updateHabbosAt(short s, short s2) {
        updateHabbosAt(s, s2, getHabbosAt(s, s2));
    }

    public void updateHabbosAt(short s, short s2, THashSet<Habbo> tHashSet) {
        HabboItem topItemAt = getTopItemAt(s, s2);
        new THashSet();
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            Habbo habbo = (Habbo) it.next();
            double z = habbo.getRoomUnit().getZ();
            RoomUserRotation bodyRotation = habbo.getRoomUnit().getBodyRotation();
            double stackHeight = habbo.getRoomUnit().getCurrentLocation().getStackHeight();
            boolean z2 = false;
            if (habbo.getRoomUnit().hasStatus(RoomUnitStatus.SIT) && ((topItemAt == null && !habbo.getRoomUnit().cmdSit) || (topItemAt != null && !topItemAt.getBaseItem().allowSit()))) {
                habbo.getRoomUnit().removeStatus(RoomUnitStatus.SIT);
                z2 = true;
            }
            if (habbo.getRoomUnit().hasStatus(RoomUnitStatus.LAY) && ((topItemAt == null && !habbo.getRoomUnit().cmdLay) || (topItemAt != null && !topItemAt.getBaseItem().allowLay()))) {
                habbo.getRoomUnit().removeStatus(RoomUnitStatus.LAY);
                z2 = true;
            }
            if (topItemAt == null || !(topItemAt.getBaseItem().allowSit() || topItemAt.getBaseItem().allowLay())) {
                habbo.getRoomUnit().setZ(stackHeight);
                habbo.getRoomUnit().setPreviousLocationZ(stackHeight);
            } else {
                habbo.getRoomUnit().setZ(topItemAt.getZ());
                habbo.getRoomUnit().setPreviousLocationZ(topItemAt.getZ());
                habbo.getRoomUnit().setRotation(RoomUserRotation.fromValue(topItemAt.getRotation()));
            }
            if (habbo.getRoomUnit().getCurrentLocation().is(s, s2) && (z != stackHeight || z2 || bodyRotation != habbo.getRoomUnit().getBodyRotation())) {
                habbo.getRoomUnit().statusUpdate(true);
            }
        }
    }

    public void updateBotsAt(short s, short s2) {
        HabboItem topItemAt = getTopItemAt(s, s2);
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = getBotsAt(this.layout.getTile(s, s2)).iterator();
        while (it.hasNext()) {
            Bot bot = (Bot) it.next();
            if (topItemAt == null) {
                bot.getRoomUnit().setZ(bot.getRoomUnit().getCurrentLocation().getStackHeight());
                bot.getRoomUnit().setPreviousLocationZ(bot.getRoomUnit().getCurrentLocation().getStackHeight());
            } else if (topItemAt.getBaseItem().allowSit()) {
                bot.getRoomUnit().setZ(topItemAt.getZ());
                bot.getRoomUnit().setPreviousLocationZ(topItemAt.getZ());
                bot.getRoomUnit().setRotation(RoomUserRotation.fromValue(topItemAt.getRotation()));
            } else {
                bot.getRoomUnit().setZ(topItemAt.getZ() + Item.getCurrentHeight(topItemAt));
                if (topItemAt.getBaseItem().allowLay()) {
                    bot.getRoomUnit().setStatus(RoomUnitStatus.LAY, (topItemAt.getZ() + topItemAt.getBaseItem().getHeight()) + Emulator.PREVIEW);
                }
            }
            tHashSet.add(bot.getRoomUnit());
        }
        if (tHashSet.isEmpty()) {
            return;
        }
        sendComposer(new RoomUserStatusComposer((THashSet<RoomUnit>) tHashSet, true).compose());
    }

    public void pickupPetsForHabbo(Habbo habbo) {
        THashSet tHashSet = new THashSet();
        synchronized (this.currentPets) {
            for (Pet pet : this.currentPets.valueCollection()) {
                if (pet.getUserId() == habbo.getHabboInfo().getId()) {
                    tHashSet.add(pet);
                }
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            Pet pet2 = (Pet) it.next();
            pet2.removeFromRoom();
            Emulator.getThreading().run(pet2);
            habbo.getInventory().getPetsComponent().addPet(pet2);
            habbo.getClient().sendResponse(new AddPetComposer(pet2));
            this.currentPets.remove(pet2.getId());
        }
    }

    public void startTrade(Habbo habbo, Habbo habbo2) {
        RoomTrade roomTrade = new RoomTrade(habbo, habbo2, this);
        synchronized (this.activeTrades) {
            this.activeTrades.add(roomTrade);
        }
        roomTrade.start();
    }

    public void stopTrade(RoomTrade roomTrade) {
        synchronized (this.activeTrades) {
            this.activeTrades.remove(roomTrade);
        }
    }

    public RoomTrade getActiveTradeForHabbo(Habbo habbo) {
        synchronized (this.activeTrades) {
            TObjectHashIterator it = this.activeTrades.iterator();
            while (it.hasNext()) {
                RoomTrade roomTrade = (RoomTrade) it.next();
                Iterator<RoomTradeUser> it2 = roomTrade.getRoomTradeUsers().iterator();
                while (it2.hasNext()) {
                    if (it2.next().getHabbo() == habbo) {
                        return roomTrade;
                    }
                }
            }
            return null;
        }
    }

    public synchronized void dispose() {
        synchronized (this.loadLock) {
            if (this.preventUnloading) {
                return;
            }
            if (((RoomUnloadingEvent) Emulator.getPluginManager().fireEvent(new RoomUnloadingEvent(this))).isCancelled()) {
                return;
            }
            if (!this.loaded) {
                this.wordQuiz = Emulator.PREVIEW;
                this.yesVotes = 0;
                this.noVotes = 0;
                updateDatabaseUserCount();
                this.preLoaded = true;
                this.layout = null;
                Emulator.getPluginManager().fireEvent(new RoomUnloadedEvent(this));
                return;
            }
            try {
                if (this.traxManager != null && !this.traxManager.disposed()) {
                    this.traxManager.dispose();
                }
                this.roomCycleTask.cancel(false);
                this.scheduledTasks.clear();
                this.scheduledComposers.clear();
                this.loaded = false;
                this.tileCache.clear();
                synchronized (this.mutedHabbos) {
                    this.mutedHabbos.clear();
                }
                Iterator it = getRoomSpecialTypes().getGameTimers().values().iterator();
                while (it.hasNext()) {
                    ((InteractionGameTimer) it.next()).setRunning(false);
                }
                Iterator it2 = this.games.iterator();
                while (it2.hasNext()) {
                    ((Game) it2.next()).dispose();
                }
                this.games.clear();
                removeAllPets(this.ownerId);
                synchronized (this.roomItems) {
                    TIntObjectIterator it3 = this.roomItems.iterator();
                    int size = this.roomItems.size();
                    while (true) {
                        int i = size;
                        size--;
                        if (i <= 0) {
                            break;
                        }
                        try {
                            it3.advance();
                            if (((HabboItem) it3.value()).needsUpdate()) {
                                ((HabboItem) it3.value()).run();
                            }
                        } catch (NoSuchElementException e) {
                        }
                    }
                }
                if (this.roomSpecialTypes != null) {
                    this.roomSpecialTypes.dispose();
                }
                synchronized (this.roomItems) {
                    this.roomItems.clear();
                }
                synchronized (this.habboQueue) {
                    this.habboQueue.clear();
                }
                Iterator<Habbo> it4 = this.currentHabbos.values().iterator();
                while (it4.hasNext()) {
                    Emulator.getGameEnvironment().getRoomManager().leaveRoom(it4.next(), this);
                }
                sendComposer(new HotelViewComposer().compose());
                this.currentHabbos.clear();
                TIntObjectIterator it5 = this.currentBots.iterator();
                int size2 = this.currentBots.size();
                while (true) {
                    int i2 = size2;
                    size2--;
                    if (i2 <= 0) {
                        break;
                    }
                    try {
                        it5.advance();
                        ((Bot) it5.value()).needsUpdate(true);
                        Emulator.getThreading().run((Runnable) it5.value());
                    } catch (NoSuchElementException e2) {
                        LOGGER.error("Caught exception", e2);
                    }
                }
                this.currentBots.clear();
                this.currentPets.clear();
            } catch (Exception e3) {
                LOGGER.error("Caught exception", e3);
            }
            try {
                this.wordQuiz = Emulator.PREVIEW;
                this.yesVotes = 0;
                this.noVotes = 0;
                updateDatabaseUserCount();
                this.preLoaded = true;
                this.layout = null;
            } catch (Exception e4) {
                LOGGER.error("Caught exception", e4);
            }
            Emulator.getPluginManager().fireEvent(new RoomUnloadedEvent(this));
            return;
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(Room room) {
        return room.getUserCount() != getUserCount() ? room.getCurrentHabbos().size() - getCurrentHabbos().size() : this.id - room.id;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.name);
        if (isPublicRoom()) {
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendString(Emulator.PREVIEW);
        } else {
            serverMessage.appendInt(Integer.valueOf(this.ownerId));
            serverMessage.appendString(this.ownerName);
        }
        serverMessage.appendInt(Integer.valueOf(this.state.getState()));
        serverMessage.appendInt(Integer.valueOf(getUserCount()));
        serverMessage.appendInt(Integer.valueOf(this.usersMax));
        serverMessage.appendString(this.description);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(this.score));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(this.category));
        String[] strArr = (String[]) Arrays.stream(this.tags.split(";")).filter(str -> {
            return !str.isEmpty();
        }).toArray(i -> {
            return new String[i];
        });
        serverMessage.appendInt(Integer.valueOf(strArr.length));
        for (String str2 : strArr) {
            serverMessage.appendString(str2);
        }
        int i2 = getGuildId() > 0 ? 0 | 2 : 0;
        if (isPromoted()) {
            i2 |= 4;
        }
        if (!isPublicRoom()) {
            i2 |= 8;
        }
        serverMessage.appendInt(Integer.valueOf(i2));
        if (getGuildId() > 0) {
            Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(getGuildId());
            if (guild != null) {
                serverMessage.appendInt(Integer.valueOf(guild.getId()));
                serverMessage.appendString(guild.getName());
                serverMessage.appendString(guild.getBadge());
            } else {
                serverMessage.appendInt((Integer) 0);
                serverMessage.appendString(Emulator.PREVIEW);
                serverMessage.appendString(Emulator.PREVIEW);
            }
        }
        if (this.promoted) {
            serverMessage.appendString(this.promotion.getTitle());
            serverMessage.appendString(this.promotion.getDescription());
            serverMessage.appendInt(Integer.valueOf((this.promotion.getEndTimestamp() - Emulator.getIntUnixTimestamp()) / 60));
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        System.currentTimeMillis();
        synchronized (this.loadLock) {
            if (this.loaded) {
                try {
                    Emulator.getThreading().run(this::cycle);
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
        save();
    }

    public void save() {
        if (this.needsUpdate) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE rooms SET name = ?, description = ?, password = ?, state = ?, users_max = ?, category = ?, score = ?, paper_floor = ?, paper_wall = ?, paper_landscape = ?, thickness_wall = ?, wall_height = ?, thickness_floor = ?, moodlight_data = ?, tags = ?, allow_other_pets = ?, allow_other_pets_eat = ?, allow_walkthrough = ?, allow_hidewall = ?, chat_mode = ?, chat_weight = ?, chat_speed = ?, chat_hearing_distance = ?, chat_protection =?, who_can_mute = ?, who_can_kick = ?, who_can_ban = ?, poll_id = ?, guild_id = ?, roller_speed = ?, override_model = ?, is_staff_picked = ?, promoted = ?, trade_mode = ?, move_diagonally = ?, owner_id = ?, owner_name = ?, jukebox_active = ?, hidewired = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setString(1, this.name);
                        preparedStatementPrepareStatement.setString(2, this.description);
                        preparedStatementPrepareStatement.setString(3, this.password);
                        preparedStatementPrepareStatement.setString(4, this.state.name().toLowerCase());
                        preparedStatementPrepareStatement.setInt(5, this.usersMax);
                        preparedStatementPrepareStatement.setInt(6, this.category);
                        preparedStatementPrepareStatement.setInt(7, this.score);
                        preparedStatementPrepareStatement.setString(8, this.floorPaint);
                        preparedStatementPrepareStatement.setString(9, this.wallPaint);
                        preparedStatementPrepareStatement.setString(10, this.backgroundPaint);
                        preparedStatementPrepareStatement.setInt(11, this.wallSize);
                        preparedStatementPrepareStatement.setInt(12, this.wallHeight);
                        preparedStatementPrepareStatement.setInt(13, this.floorSize);
                        StringBuilder sb = new StringBuilder();
                        int i = 1;
                        for (RoomMoodlightData roomMoodlightData : this.moodlightData.valueCollection()) {
                            roomMoodlightData.setId(i);
                            sb.append(roomMoodlightData.toString()).append(";");
                            i++;
                        }
                        preparedStatementPrepareStatement.setString(14, sb.toString());
                        preparedStatementPrepareStatement.setString(15, this.tags);
                        preparedStatementPrepareStatement.setString(16, this.allowPets ? "1" : "0");
                        preparedStatementPrepareStatement.setString(17, this.allowPetsEat ? "1" : "0");
                        preparedStatementPrepareStatement.setString(18, this.allowWalkthrough ? "1" : "0");
                        preparedStatementPrepareStatement.setString(19, this.hideWall ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(20, this.chatMode);
                        preparedStatementPrepareStatement.setInt(21, this.chatWeight);
                        preparedStatementPrepareStatement.setInt(22, this.chatSpeed);
                        preparedStatementPrepareStatement.setInt(23, this.chatDistance);
                        preparedStatementPrepareStatement.setInt(24, this.chatProtection);
                        preparedStatementPrepareStatement.setInt(25, this.muteOption);
                        preparedStatementPrepareStatement.setInt(26, this.kickOption);
                        preparedStatementPrepareStatement.setInt(27, this.banOption);
                        preparedStatementPrepareStatement.setInt(28, this.pollId);
                        preparedStatementPrepareStatement.setInt(29, this.guild);
                        preparedStatementPrepareStatement.setInt(30, this.rollerSpeed);
                        preparedStatementPrepareStatement.setString(31, this.overrideModel ? "1" : "0");
                        preparedStatementPrepareStatement.setString(32, this.staffPromotedRoom ? "1" : "0");
                        preparedStatementPrepareStatement.setString(33, this.promoted ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(34, this.tradeMode);
                        preparedStatementPrepareStatement.setString(35, this.moveDiagonally ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(36, this.ownerId);
                        preparedStatementPrepareStatement.setString(37, this.ownerName);
                        preparedStatementPrepareStatement.setString(38, this.jukeboxActive ? "1" : "0");
                        preparedStatementPrepareStatement.setString(39, this.hideWired ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(40, this.id);
                        preparedStatementPrepareStatement.executeUpdate();
                        this.needsUpdate = false;
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

    private void updateDatabaseUserCount() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE rooms SET users = ? WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.currentHabbos.size());
                    preparedStatementPrepareStatement.setInt(2, this.id);
                    preparedStatementPrepareStatement.executeUpdate();
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

    private void cycle() {
        boolean z;
        this.cycleOdd = !this.cycleOdd;
        this.cycleTimestamp = System.currentTimeMillis();
        boolean[] zArr = {false};
        synchronized (this.loadLock) {
            z = this.loaded;
        }
        this.tileCache.clear();
        if (z) {
            if (!this.scheduledTasks.isEmpty()) {
                ConcurrentSet<Runnable> concurrentSet = this.scheduledTasks;
                this.scheduledTasks = new ConcurrentSet<>();
                Iterator it = concurrentSet.iterator();
                while (it.hasNext()) {
                    Emulator.getThreading().run((Runnable) it.next());
                }
            }
            TObjectHashIterator it2 = this.roomSpecialTypes.getCycleTasks().iterator();
            while (it2.hasNext()) {
                ((ICycleable) it2.next()).cycle(this);
            }
            if (!this.currentHabbos.isEmpty()) {
                this.idleCycles = 0;
                THashSet tHashSet = new THashSet();
                ArrayList arrayList = new ArrayList();
                long jCurrentTimeMillis = System.currentTimeMillis();
                for (Habbo habbo : this.currentHabbos.values()) {
                    if (!zArr[0]) {
                        zArr[0] = habbo.getRoomUnit().getRightsLevel() != RoomRightLevels.NONE;
                    }
                    if (habbo.getRoomUnit().getHandItem() > 0 && jCurrentTimeMillis - habbo.getRoomUnit().getHandItemTimestamp() > HAND_ITEM_TIME * Outgoing.CraftableProductsComposer) {
                        giveHandItem(habbo, 0);
                    }
                    if (habbo.getRoomUnit().getEffectId() > 0 && jCurrentTimeMillis / 1000 > habbo.getRoomUnit().getEffectEndTimestamp()) {
                        giveEffect(habbo, 0, -1);
                    }
                    if (habbo.getRoomUnit().isKicked) {
                        habbo.getRoomUnit().kickCount++;
                        if (habbo.getRoomUnit().kickCount >= 5) {
                            this.scheduledTasks.add(() -> {
                                Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo, this);
                            });
                        }
                    }
                    if (Emulator.getConfig().getBoolean("hotel.rooms.auto.idle")) {
                        if (habbo.getRoomUnit().isIdle()) {
                            habbo.getRoomUnit().increaseIdleTimer();
                            if (!isOwner(habbo) && habbo.getRoomUnit().getIdleTimer() >= IDLE_CYCLES_KICK) {
                                UserExitRoomEvent userExitRoomEvent = new UserExitRoomEvent(habbo, UserExitRoomEvent.UserExitRoomReason.KICKED_IDLE);
                                Emulator.getPluginManager().fireEvent(userExitRoomEvent);
                                if (!userExitRoomEvent.isCancelled()) {
                                    arrayList.add(habbo);
                                }
                            }
                        } else {
                            habbo.getRoomUnit().increaseIdleTimer();
                            if (habbo.getRoomUnit().isIdle()) {
                                boolean z2 = habbo.getRoomUnit().getDanceType() == DanceType.NONE;
                                if (z2) {
                                    sendComposer(new RoomUnitIdleComposer(habbo.getRoomUnit()).compose());
                                }
                                if (z2 && !Emulator.getConfig().getBoolean("hotel.roomuser.idle.not_dancing.ignore.wired_idle")) {
                                    WiredHandler.handle(WiredTriggerType.IDLES, habbo.getRoomUnit(), this, new Object[]{habbo});
                                }
                            }
                        }
                    }
                    if (Emulator.getConfig().getBoolean("hotel.rooms.deco_hosting") && this.ownerId != habbo.getHabboInfo().getId()) {
                        if (habbo.getRoomUnit().getTimeInRoom() >= 120) {
                            AchievementManager.progressAchievement(this.ownerId, Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoHosting"));
                            habbo.getRoomUnit().resetTimeInRoom();
                        } else {
                            habbo.getRoomUnit().increaseTimeInRoom();
                        }
                    }
                    if (habbo.getHabboStats().mutedBubbleTracker && habbo.getHabboStats().allowTalk()) {
                        habbo.getHabboStats().mutedBubbleTracker = false;
                        sendComposer(new RoomUserIgnoredComposer(habbo, 3).compose());
                    }
                    if (this.cycleOdd && habbo.getHabboStats().chatCounter.get() > 0) {
                        habbo.getHabboStats().chatCounter.decrementAndGet();
                    }
                    if (cycleRoomUnit(habbo.getRoomUnit(), RoomUnitType.USER)) {
                        tHashSet.add(habbo.getRoomUnit());
                    }
                }
                if (!arrayList.isEmpty()) {
                    Iterator it3 = arrayList.iterator();
                    while (it3.hasNext()) {
                        Emulator.getGameEnvironment().getRoomManager().leaveRoom((Habbo) it3.next(), this);
                    }
                }
                if (!this.currentBots.isEmpty()) {
                    TIntObjectIterator it4 = this.currentBots.iterator();
                    int size = this.currentBots.size();
                    while (true) {
                        int i = size;
                        size--;
                        if (i <= 0) {
                            break;
                        }
                        try {
                            try {
                                it4.advance();
                                Bot bot = (Bot) it4.value();
                                if (this.allowBotsWalk || !bot.getRoomUnit().isWalking()) {
                                    ((Bot) it4.value()).cycle(this.allowBotsWalk);
                                    if (cycleRoomUnit(bot.getRoomUnit(), RoomUnitType.BOT)) {
                                        tHashSet.add(bot.getRoomUnit());
                                    }
                                } else {
                                    bot.getRoomUnit().stopWalking();
                                    tHashSet.add(bot.getRoomUnit());
                                }
                            } catch (NoSuchElementException e) {
                                LOGGER.error("Caught exception", e);
                            }
                        } catch (Exception e2) {
                        }
                    }
                }
                if (!this.currentPets.isEmpty() && this.allowBotsWalk) {
                    TIntObjectIterator it5 = this.currentPets.iterator();
                    int size2 = this.currentPets.size();
                    while (true) {
                        int i2 = size2;
                        size2--;
                        if (i2 <= 0) {
                            break;
                        }
                        try {
                            it5.advance();
                            Pet pet = (Pet) it5.value();
                            if (cycleRoomUnit(pet.getRoomUnit(), RoomUnitType.PET)) {
                                tHashSet.add(pet.getRoomUnit());
                            }
                            pet.cycle();
                            if (pet.packetUpdate) {
                                tHashSet.add(pet.getRoomUnit());
                                pet.packetUpdate = false;
                            }
                            if (pet.getRoomUnit().isWalking() && pet.getRoomUnit().getPath().size() == 1 && pet.getRoomUnit().hasStatus(RoomUnitStatus.GESTURE)) {
                                pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                                tHashSet.add(pet.getRoomUnit());
                            }
                        } catch (NoSuchElementException e3) {
                            LOGGER.error("Caught exception", e3);
                        }
                    }
                }
                if (this.rollerSpeed == -1 || this.rollerCycle < this.rollerSpeed) {
                    this.rollerCycle++;
                } else {
                    this.rollerCycle = 0L;
                    THashSet tHashSet2 = new THashSet();
                    ArrayList arrayList2 = new ArrayList();
                    ArrayList arrayList3 = new ArrayList();
                    this.roomSpecialTypes.getRollers().forEachValue(interactionRoller -> {
                        RoomTile tileInFront;
                        HabboItem topItemAt;
                        Habbo habbo2;
                        RideablePet riding;
                        HabboItem habboItem = null;
                        RoomTile tile = getLayout().getTile(interactionRoller.getX(), interactionRoller.getY());
                        if (tile == null) {
                            return true;
                        }
                        THashSet tHashSet3 = new THashSet();
                        TObjectHashIterator it6 = getItemsAt(tile).iterator();
                        while (it6.hasNext()) {
                            HabboItem habboItem2 = (HabboItem) it6.next();
                            if (habboItem2.getZ() >= interactionRoller.getZ() + Item.getCurrentHeight(interactionRoller)) {
                                tHashSet3.add(habboItem2);
                            }
                        }
                        tHashSet3.remove(interactionRoller);
                        if ((!tile.hasUnits() && tHashSet3.isEmpty()) || (tileInFront = this.layout.getTileInFront(this.layout.getTile(interactionRoller.getX(), interactionRoller.getY()), interactionRoller.getRotation())) == null || !this.layout.tileExists(tileInFront.x, tileInFront.y) || tileInFront.state == RoomTileState.INVALID) {
                            return true;
                        }
                        if ((!tileInFront.getAllowStack() && !tileInFront.isWalkable() && tileInFront.state != RoomTileState.SIT && tileInFront.state != RoomTileState.LAY) || tileInFront.hasUnits()) {
                            return true;
                        }
                        THashSet tHashSet4 = new THashSet();
                        tHashSet4.addAll(getItemsAt(tileInFront));
                        tHashSet4.removeAll(tHashSet3);
                        ArrayList arrayList4 = new ArrayList();
                        TObjectHashIterator it7 = tHashSet3.iterator();
                        while (it7.hasNext()) {
                            HabboItem habboItem3 = (HabboItem) it7.next();
                            if (habboItem3.getX() != interactionRoller.getX() || habboItem3.getY() != interactionRoller.getY() || arrayList2.contains(Integer.valueOf(habboItem3.getId()))) {
                                arrayList4.add(habboItem3);
                            }
                        }
                        tHashSet3.removeAll(arrayList4);
                        HabboItem topItemAt2 = getTopItemAt(tileInFront.x, tileInFront.y);
                        boolean z3 = true;
                        boolean allowStack = true;
                        boolean z4 = false;
                        TObjectHashIterator it8 = tHashSet4.iterator();
                        while (it8.hasNext()) {
                            HabboItem habboItem4 = (HabboItem) it8.next();
                            if (!habboItem4.getBaseItem().allowWalk() && !habboItem4.getBaseItem().allowSit() && (!(habboItem4 instanceof InteractionGate) || !habboItem4.getExtradata().equals("1"))) {
                                z3 = false;
                            }
                            if (habboItem4 instanceof InteractionRoller) {
                                habboItem = habboItem4;
                                z4 = true;
                                if ((habboItem4.getZ() == interactionRoller.getZ() && (tHashSet4.size() <= 1 || habboItem4 == topItemAt2)) || InteractionRoller.NO_RULES) {
                                    break;
                                }
                                z3 = false;
                                allowStack = false;
                            } else {
                                allowStack = false;
                            }
                        }
                        if (allowStack) {
                            allowStack = tileInFront.getAllowStack();
                        }
                        double stackHeight = 0.0d;
                        if (habboItem == null) {
                            stackHeight = ((-Item.getCurrentHeight(interactionRoller)) + tileInFront.getStackHeight()) - ((double) tile.z);
                        } else if (!tHashSet4.isEmpty() && tHashSet4.size() > 1 && !InteractionRoller.NO_RULES) {
                            return true;
                        }
                        if (z3) {
                            UserRolledEvent userRolledEvent = Emulator.getPluginManager().isRegistered(UserRolledEvent.class, true) ? new UserRolledEvent(null, null, null) : null;
                            ArrayList<RoomUnit> arrayList5 = new ArrayList(tile.getUnits());
                            for (RoomUnit roomUnit : tile.getUnits()) {
                                if (roomUnit.getRoomUnitType() == RoomUnitType.PET) {
                                    Pet pet2 = getPet(roomUnit);
                                    if ((pet2 instanceof RideablePet) && ((RideablePet) pet2).getRider() != null) {
                                        arrayList5.remove(roomUnit);
                                    }
                                }
                            }
                            getTallestChair(tileInFront);
                            THashSet tHashSet5 = new THashSet();
                            for (RoomUnit roomUnit2 : arrayList5) {
                                if (!arrayList3.contains(Integer.valueOf(roomUnit2.getId()))) {
                                    if (tHashSet5.size() >= ROLLERS_MAXIMUM_ROLL_AVATARS) {
                                        break;
                                    }
                                    if (!z4 || allowStack || (topItemAt2 != null && topItemAt2.isWalkable())) {
                                        if (!roomUnit2.hasStatus(RoomUnitStatus.MOVE)) {
                                            RoomTile roomTileCopy = tileInFront.copy();
                                            roomTileCopy.setStackHeight(roomUnit2.getZ() + stackHeight);
                                            if (userRolledEvent != null && roomUnit2.getRoomUnitType() == RoomUnitType.USER) {
                                                userRolledEvent = new UserRolledEvent(getHabbo(roomUnit2), interactionRoller, roomTileCopy);
                                                Emulator.getPluginManager().fireEvent(userRolledEvent);
                                                if (userRolledEvent.isCancelled()) {
                                                }
                                            }
                                            boolean z5 = false;
                                            if (roomUnit2.getRoomUnitType() == RoomUnitType.USER && (habbo2 = getHabbo(roomUnit2)) != null && habbo2.getHabboInfo() != null && (riding = habbo2.getHabboInfo().getRiding()) != null) {
                                                RoomUnit roomUnit3 = riding.getRoomUnit();
                                                roomTileCopy.setStackHeight(roomUnit3.getZ() + stackHeight);
                                                arrayList3.add(Integer.valueOf(roomUnit3.getId()));
                                                tHashSet.remove(roomUnit3);
                                                tHashSet2.add(new RoomUnitOnRollerComposer(roomUnit3, interactionRoller, roomUnit3.getCurrentLocation(), roomUnit3.getZ(), roomTileCopy, roomTileCopy.getStackHeight(), this));
                                                z5 = true;
                                            }
                                            tHashSet5.add(Integer.valueOf(roomUnit2.getId()));
                                            arrayList3.add(Integer.valueOf(roomUnit2.getId()));
                                            tHashSet.remove(roomUnit2);
                                            tHashSet2.add(new RoomUnitOnRollerComposer(roomUnit2, interactionRoller, roomUnit2.getCurrentLocation(), roomUnit2.getZ() + ((double) (z5 ? 1 : 0)), roomTileCopy, roomTileCopy.getStackHeight() + ((double) (z5 ? 1 : 0)), this));
                                            if (tHashSet3.isEmpty() && (topItemAt = this.getTopItemAt(tileInFront.x, tileInFront.y)) != null && tHashSet4.contains(topItemAt) && !tHashSet3.contains(topItemAt)) {
                                                Emulator.getThreading().run(() -> {
                                                    if (roomUnit2.getGoal() == tile) {
                                                        try {
                                                            topItemAt.onWalkOn(roomUnit2, this, new Object[]{tile, tileInFront});
                                                        } catch (Exception e4) {
                                                            LOGGER.error("Caught exception", e4);
                                                        }
                                                    }
                                                }, getRollerSpeed() == 0 ? 250L : InteractionRoller.DELAY);
                                            }
                                            if (roomUnit2.hasStatus(RoomUnitStatus.SIT)) {
                                                roomUnit2.sitUpdate = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!tHashSet2.isEmpty()) {
                            TObjectHashIterator it9 = tHashSet2.iterator();
                            while (it9.hasNext()) {
                                this.sendComposer(((MessageComposer) it9.next()).compose());
                            }
                            tHashSet2.clear();
                        }
                        if (allowStack || !z4 || InteractionRoller.NO_RULES) {
                            FurnitureRolledEvent furnitureRolledEvent = Emulator.getPluginManager().isRegistered(FurnitureRolledEvent.class, true) ? new FurnitureRolledEvent(null, null, null) : null;
                            if (habboItem == null || topItemAt2 == habboItem) {
                                ArrayList<HabboItem> arrayList6 = new ArrayList((Collection) tHashSet3);
                                arrayList6.sort((habboItem5, habboItem6) -> {
                                    return habboItem5.getZ() > habboItem6.getZ() ? -1 : 1;
                                });
                                for (HabboItem habboItem7 : arrayList6) {
                                    if (habboItem7.getX() == interactionRoller.getX() && habboItem7.getY() == interactionRoller.getY() && stackHeight <= 0.0d && habboItem7 != interactionRoller) {
                                        if (furnitureRolledEvent != null) {
                                            furnitureRolledEvent = new FurnitureRolledEvent(habboItem7, interactionRoller, tileInFront);
                                            Emulator.getPluginManager().fireEvent(furnitureRolledEvent);
                                            if (furnitureRolledEvent.isCancelled()) {
                                            }
                                        }
                                        tHashSet2.add(new FloorItemOnRollerComposer(habboItem7, interactionRoller, tileInFront, stackHeight, this));
                                        arrayList2.add(Integer.valueOf(habboItem7.getId()));
                                    }
                                }
                            }
                        }
                        if (tHashSet2.isEmpty()) {
                            return true;
                        }
                        TObjectHashIterator it10 = tHashSet2.iterator();
                        while (it10.hasNext()) {
                            this.sendComposer(((MessageComposer) it10.next()).compose());
                        }
                        tHashSet2.clear();
                        return true;
                    });
                    int i3 = (int) (this.cycleTimestamp / 1000);
                    TObjectHashIterator it6 = this.roomSpecialTypes.getItemsOfType(InteractionPyramid.class).iterator();
                    while (it6.hasNext()) {
                        HabboItem habboItem = (HabboItem) it6.next();
                        if ((habboItem instanceof InteractionPyramid) && ((InteractionPyramid) habboItem).getNextChange() < i3) {
                            ((InteractionPyramid) habboItem).change(this);
                        }
                    }
                }
                if (!tHashSet.isEmpty()) {
                    sendComposer(new RoomUserStatusComposer((THashSet<RoomUnit>) tHashSet, true).compose());
                }
                this.traxManager.cycle();
            } else if (this.idleCycles < 60) {
                this.idleCycles++;
            } else {
                dispose();
            }
        }
        synchronized (this.habboQueue) {
            if (!this.habboQueue.isEmpty() && !zArr[0]) {
                this.habboQueue.forEachEntry(new TIntObjectProcedure<Habbo>() { // from class: com.eu.habbo.habbohotel.rooms.Room.1
                    public boolean execute(int i4, Habbo habbo2) {
                        if (!habbo2.isOnline() || habbo2.getHabboInfo().getRoomQueueId() != Room.this.getId()) {
                            return true;
                        }
                        habbo2.getClient().sendResponse(new RoomAccessDeniedComposer(Emulator.PREVIEW));
                        return true;
                    }
                });
                this.habboQueue.clear();
            }
        }
        if (this.scheduledComposers.isEmpty()) {
            return;
        }
        Iterator it7 = this.scheduledComposers.iterator();
        while (it7.hasNext()) {
            sendComposer((ServerMessage) it7.next());
        }
        this.scheduledComposers.clear();
    }

    private boolean cycleRoomUnit(RoomUnit roomUnit, RoomUnitType roomUnitType) {
        boolean zNeedsStatusUpdate = roomUnit.needsStatusUpdate();
        if (roomUnit.hasStatus(RoomUnitStatus.SIGN)) {
            sendComposer(new RoomUserStatusComposer(roomUnit).compose());
            roomUnit.removeStatus(RoomUnitStatus.SIGN);
        }
        if (!roomUnit.isWalking() || roomUnit.getPath() == null || roomUnit.getPath().isEmpty()) {
            if (roomUnit.hasStatus(RoomUnitStatus.MOVE) && !roomUnit.animateWalk) {
                roomUnit.removeStatus(RoomUnitStatus.MOVE);
                zNeedsStatusUpdate = true;
            }
            if (!roomUnit.isWalking() && !roomUnit.cmdSit) {
                RoomTile tile = getLayout().getTile(roomUnit.getX(), roomUnit.getY());
                HabboItem tallestChair = getTallestChair(tile);
                if (tallestChair == null || !tallestChair.getBaseItem().allowSit()) {
                    if (roomUnit.hasStatus(RoomUnitStatus.SIT)) {
                        roomUnit.removeStatus(RoomUnitStatus.SIT);
                        zNeedsStatusUpdate = true;
                    }
                } else if (tile.state == RoomTileState.SIT && (!roomUnit.hasStatus(RoomUnitStatus.SIT) || roomUnit.sitUpdate)) {
                    dance(roomUnit, DanceType.NONE);
                    roomUnit.setStatus(RoomUnitStatus.SIT, (Item.getCurrentHeight(tallestChair) * 1.0d) + Emulator.PREVIEW);
                    roomUnit.setZ(tallestChair.getZ());
                    roomUnit.setRotation(RoomUserRotation.values()[tallestChair.getRotation()]);
                    roomUnit.sitUpdate = false;
                    return true;
                }
            }
        } else if (!roomUnit.cycle(this)) {
            return true;
        }
        if (!roomUnit.isWalking() && !roomUnit.cmdLay) {
            HabboItem topItemAt = getTopItemAt(roomUnit.getX(), roomUnit.getY());
            if (topItemAt == null || !topItemAt.getBaseItem().allowLay()) {
                if (roomUnit.hasStatus(RoomUnitStatus.LAY)) {
                    roomUnit.removeStatus(RoomUnitStatus.LAY);
                    zNeedsStatusUpdate = true;
                }
            } else if (!roomUnit.hasStatus(RoomUnitStatus.LAY)) {
                roomUnit.setStatus(RoomUnitStatus.LAY, (Item.getCurrentHeight(topItemAt) * 1.0d) + Emulator.PREVIEW);
                roomUnit.setRotation(RoomUserRotation.values()[topItemAt.getRotation() % 4]);
                if (topItemAt.getRotation() == 0 || topItemAt.getRotation() == 4) {
                    roomUnit.setLocation(this.layout.getTile(roomUnit.getX(), topItemAt.getY()));
                } else {
                    roomUnit.setLocation(this.layout.getTile(topItemAt.getX(), roomUnit.getY()));
                }
                zNeedsStatusUpdate = true;
            }
        }
        if (zNeedsStatusUpdate) {
            roomUnit.statusUpdate(false);
        }
        return zNeedsStatusUpdate;
    }

    public int getId() {
        return this.id;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(int i) {
        this.ownerId = i;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String str) {
        this.ownerName = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        Guild guild;
        this.name = str;
        if (this.name.length() > 50) {
            this.name = this.name.substring(0, 50);
        }
        if (!hasGuild() || (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.guild)) == null) {
            return;
        }
        guild.setRoomName(str);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
        if (this.description.length() > 250) {
            this.description = this.description.substring(0, 250);
        }
    }

    public RoomLayout getLayout() {
        return this.layout;
    }

    public void setLayout(RoomLayout roomLayout) {
        this.layout = roomLayout;
    }

    public boolean hasCustomLayout() {
        return this.overrideModel;
    }

    public void setHasCustomLayout(boolean z) {
        this.overrideModel = z;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String str) {
        this.password = str;
        if (this.password.length() > 20) {
            this.password = this.password.substring(0, 20);
        }
    }

    public RoomState getState() {
        return this.state;
    }

    public void setState(RoomState roomState) {
        this.state = roomState;
    }

    public int getUsersMax() {
        return this.usersMax;
    }

    public void setUsersMax(int i) {
        this.usersMax = i;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int i) {
        this.score = i;
    }

    public int getCategory() {
        return this.category;
    }

    public void setCategory(int i) {
        this.category = i;
    }

    public String getFloorPaint() {
        return this.floorPaint;
    }

    public void setFloorPaint(String str) {
        this.floorPaint = str;
    }

    public String getWallPaint() {
        return this.wallPaint;
    }

    public void setWallPaint(String str) {
        this.wallPaint = str;
    }

    public String getBackgroundPaint() {
        return this.backgroundPaint;
    }

    public void setBackgroundPaint(String str) {
        this.backgroundPaint = str;
    }

    public int getWallSize() {
        return this.wallSize;
    }

    public void setWallSize(int i) {
        this.wallSize = i;
    }

    public int getWallHeight() {
        return this.wallHeight;
    }

    public void setWallHeight(int i) {
        this.wallHeight = i;
    }

    public int getFloorSize() {
        return this.floorSize;
    }

    public void setFloorSize(int i) {
        this.floorSize = i;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String str) {
        this.tags = str;
    }

    public int getTradeMode() {
        return this.tradeMode;
    }

    public void setTradeMode(int i) {
        this.tradeMode = i;
    }

    public boolean moveDiagonally() {
        return this.moveDiagonally;
    }

    public void moveDiagonally(boolean z) {
        this.moveDiagonally = z;
        this.layout.moveDiagonally(this.moveDiagonally);
        this.needsUpdate = true;
    }

    public int getGuildId() {
        return this.guild;
    }

    public boolean hasGuild() {
        return this.guild != 0;
    }

    public void setGuild(int i) {
        this.guild = i;
    }

    public String getGuildName() {
        Guild guild;
        return (!hasGuild() || (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.guild)) == null) ? Emulator.PREVIEW : guild.getName();
    }

    public boolean isPublicRoom() {
        return this.publicRoom;
    }

    public void setPublicRoom(boolean z) {
        this.publicRoom = z;
    }

    public boolean isStaffPromotedRoom() {
        return this.staffPromotedRoom;
    }

    public void setStaffPromotedRoom(boolean z) {
        this.staffPromotedRoom = z;
    }

    public boolean isAllowPets() {
        return this.allowPets;
    }

    public void setAllowPets(boolean z) {
        this.allowPets = z;
        if (z) {
            return;
        }
        removeAllPets(this.ownerId);
    }

    public boolean isAllowPetsEat() {
        return this.allowPetsEat;
    }

    public void setAllowPetsEat(boolean z) {
        this.allowPetsEat = z;
    }

    public boolean isAllowWalkthrough() {
        return this.allowWalkthrough;
    }

    public void setAllowWalkthrough(boolean z) {
        this.allowWalkthrough = z;
    }

    public boolean isAllowBotsWalk() {
        return this.allowBotsWalk;
    }

    public void setAllowBotsWalk(boolean z) {
        this.allowBotsWalk = z;
    }

    public boolean isAllowEffects() {
        return this.allowEffects;
    }

    public void setAllowEffects(boolean z) {
        this.allowEffects = z;
    }

    public boolean isHideWall() {
        return this.hideWall;
    }

    public void setHideWall(boolean z) {
        this.hideWall = z;
    }

    public Color getBackgroundTonerColor() {
        Color color = new Color(0, 0, 0);
        TIntObjectIterator it = this.roomItems.iterator();
        for (int size = this.roomItems.size(); size > 0; size--) {
            try {
                it.advance();
                HabboItem habboItem = (HabboItem) it.value();
                if (habboItem instanceof InteractionBackgroundToner) {
                    String[] strArrSplit = habboItem.getExtradata().split(":");
                    if (strArrSplit.length == 4 && strArrSplit[0].equalsIgnoreCase("1")) {
                        return Color.getHSBColor(Integer.parseInt(strArrSplit[1]), Integer.parseInt(strArrSplit[2]), Integer.parseInt(strArrSplit[3]));
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
            }
        }
        return color;
    }

    public int getChatMode() {
        return this.chatMode;
    }

    public void setChatMode(int i) {
        this.chatMode = i;
    }

    public int getChatWeight() {
        return this.chatWeight;
    }

    public void setChatWeight(int i) {
        this.chatWeight = i;
    }

    public int getChatSpeed() {
        return this.chatSpeed;
    }

    public void setChatSpeed(int i) {
        this.chatSpeed = i;
    }

    public int getChatDistance() {
        return this.chatDistance;
    }

    public void setChatDistance(int i) {
        this.chatDistance = i;
    }

    public void removeAllPets() {
        removeAllPets(-1);
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x0084  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00ed A[LOOP:2: B:31:0x00e3->B:33:0x00ed, LOOP_END] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void removeAllPets(int r6) {
        /*
            Method dump skipped, instruction units count: 268
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eu.habbo.habbohotel.rooms.Room.removeAllPets(int):void");
    }

    public int getChatProtection() {
        return this.chatProtection;
    }

    public void setChatProtection(int i) {
        this.chatProtection = i;
    }

    public int getMuteOption() {
        return this.muteOption;
    }

    public void setMuteOption(int i) {
        this.muteOption = i;
    }

    public int getKickOption() {
        return this.kickOption;
    }

    public void setKickOption(int i) {
        this.kickOption = i;
    }

    public int getBanOption() {
        return this.banOption;
    }

    public void setBanOption(int i) {
        this.banOption = i;
    }

    public int getPollId() {
        return this.pollId;
    }

    public void setPollId(int i) {
        this.pollId = i;
    }

    public int getRollerSpeed() {
        return this.rollerSpeed;
    }

    public void setRollerSpeed(int i) {
        this.rollerSpeed = i;
        this.rollerCycle = 0L;
        this.needsUpdate = true;
    }

    public String[] filterAnything() {
        return new String[]{getOwnerName(), getGuildName(), getDescription(), getPromotionDesc()};
    }

    public long getCycleTimestamp() {
        return this.cycleTimestamp;
    }

    public boolean isPromoted() {
        this.promoted = this.promotion != null && this.promotion.getEndTimestamp() > Emulator.getIntUnixTimestamp();
        this.needsUpdate = true;
        return this.promoted;
    }

    public RoomPromotion getPromotion() {
        return this.promotion;
    }

    public String getPromotionDesc() {
        return this.promotion != null ? this.promotion.getDescription() : Emulator.PREVIEW;
    }

    public void createPromotion(String str, String str2, int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        this.promoted = true;
        if (this.promotion == null) {
            this.promotion = new RoomPromotion(this, str, str2, Emulator.getIntUnixTimestamp() + 7200, Emulator.getIntUnixTimestamp(), i);
        } else {
            this.promotion.setTitle(str);
            this.promotion.setDescription(str2);
            this.promotion.setEndTimestamp(Emulator.getIntUnixTimestamp() + 7200);
            this.promotion.setCategory(i);
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_promotions (room_id, title, description, end_timestamp, start_timestamp, category) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title = ?, description = ?, end_timestamp = ?, category = ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, this.id);
            preparedStatementPrepareStatement.setString(2, str);
            preparedStatementPrepareStatement.setString(3, str2);
            preparedStatementPrepareStatement.setInt(4, this.promotion.getEndTimestamp());
            preparedStatementPrepareStatement.setInt(5, this.promotion.getStartTimestamp());
            preparedStatementPrepareStatement.setInt(6, i);
            preparedStatementPrepareStatement.setString(7, this.promotion.getTitle());
            preparedStatementPrepareStatement.setString(8, this.promotion.getDescription());
            preparedStatementPrepareStatement.setInt(9, this.promotion.getEndTimestamp());
            preparedStatementPrepareStatement.setInt(10, this.promotion.getCategory());
            preparedStatementPrepareStatement.execute();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            this.needsUpdate = true;
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
    }

    public boolean addGame(Game game) {
        boolean zAdd;
        synchronized (this.games) {
            zAdd = this.games.add(game);
        }
        return zAdd;
    }

    public boolean deleteGame(Game game) {
        boolean zRemove;
        game.stop();
        game.dispose();
        synchronized (this.games) {
            zRemove = this.games.remove(game);
        }
        return zRemove;
    }

    public Game getGame(Class<? extends Game> cls) {
        if (cls == null) {
            return null;
        }
        synchronized (this.games) {
            for (Game game : this.games) {
                if (cls.isInstance(game)) {
                    return game;
                }
            }
            return null;
        }
    }

    public Game getGameOrCreate(Class<? extends Game> cls) {
        Game game = getGame(cls);
        if (game == null) {
            try {
                game = cls.getDeclaredConstructor(Room.class).newInstance(this);
                addGame(game);
            } catch (Exception e) {
                LOGGER.error("Error getting game " + cls.getName(), e);
            }
        }
        return game;
    }

    public ConcurrentSet<Game> getGames() {
        return this.games;
    }

    public int getUserCount() {
        return this.currentHabbos.size();
    }

    public ConcurrentHashMap<Integer, Habbo> getCurrentHabbos() {
        return this.currentHabbos;
    }

    public Collection<Habbo> getHabbos() {
        return this.currentHabbos.values();
    }

    public TIntObjectMap<Habbo> getHabboQueue() {
        return this.habboQueue;
    }

    public TIntObjectMap<String> getFurniOwnerNames() {
        return this.furniOwnerNames;
    }

    public String getFurniOwnerName(int i) {
        return (String) this.furniOwnerNames.get(i);
    }

    public TIntIntMap getFurniOwnerCount() {
        return this.furniOwnerCount;
    }

    public TIntObjectMap<RoomMoodlightData> getMoodlightData() {
        return this.moodlightData;
    }

    public int getLastTimerReset() {
        return this.lastTimerReset;
    }

    public void setLastTimerReset(int i) {
        this.lastTimerReset = i;
    }

    public void addToQueue(Habbo habbo) {
        synchronized (this.habboQueue) {
            this.habboQueue.put(habbo.getHabboInfo().getId(), habbo);
        }
    }

    public boolean removeFromQueue(Habbo habbo) {
        boolean z;
        try {
            sendComposer(new HideDoorbellComposer(habbo.getHabboInfo().getUsername()).compose());
            synchronized (this.habboQueue) {
                z = this.habboQueue.remove(habbo.getHabboInfo().getId()) != null;
            }
            return z;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return true;
        }
    }

    public TIntObjectMap<Bot> getCurrentBots() {
        return this.currentBots;
    }

    public TIntObjectMap<Pet> getCurrentPets() {
        return this.currentPets;
    }

    public THashSet<String> getWordFilterWords() {
        return this.wordFilterWords;
    }

    public RoomSpecialTypes getRoomSpecialTypes() {
        return this.roomSpecialTypes;
    }

    public boolean isPreLoaded() {
        return this.preLoaded;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setNeedsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public TIntArrayList getRights() {
        return this.rights;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean z) {
        this.muted = z;
    }

    public TraxManager getTraxManager() {
        return this.traxManager;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void addHabboItem(HabboItem habboItem) {
        if (habboItem == 0) {
            return;
        }
        synchronized (this.roomItems) {
            try {
                this.roomItems.put(habboItem.getId(), habboItem);
            } catch (Exception e) {
            }
        }
        synchronized (this.furniOwnerCount) {
            this.furniOwnerCount.put(habboItem.getUserId(), this.furniOwnerCount.get(habboItem.getUserId()) + 1);
        }
        synchronized (this.furniOwnerNames) {
            if (!this.furniOwnerNames.containsKey(habboItem.getUserId())) {
                HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(habboItem.getUserId());
                if (offlineHabboInfo != null) {
                    this.furniOwnerNames.put(habboItem.getUserId(), offlineHabboInfo.getUsername());
                } else {
                    LOGGER.error("Failed to find username for item (ID: {}, UserID: {})", Integer.valueOf(habboItem.getId()), Integer.valueOf(habboItem.getUserId()));
                }
            }
        }
        synchronized (this.roomSpecialTypes) {
            if (habboItem instanceof ICycleable) {
                this.roomSpecialTypes.addCycleTask((ICycleable) habboItem);
            }
            if (habboItem instanceof InteractionWiredTrigger) {
                this.roomSpecialTypes.addTrigger((InteractionWiredTrigger) habboItem);
            } else if (habboItem instanceof InteractionWiredEffect) {
                this.roomSpecialTypes.addEffect((InteractionWiredEffect) habboItem);
            } else if (habboItem instanceof InteractionWiredCondition) {
                this.roomSpecialTypes.addCondition((InteractionWiredCondition) habboItem);
            } else if (habboItem instanceof InteractionWiredExtra) {
                this.roomSpecialTypes.addExtra((InteractionWiredExtra) habboItem);
            } else if (habboItem instanceof InteractionBattleBanzaiTeleporter) {
                this.roomSpecialTypes.addBanzaiTeleporter((InteractionBattleBanzaiTeleporter) habboItem);
            } else if (habboItem instanceof InteractionRoller) {
                this.roomSpecialTypes.addRoller((InteractionRoller) habboItem);
            } else if (habboItem instanceof InteractionGameScoreboard) {
                this.roomSpecialTypes.addGameScoreboard((InteractionGameScoreboard) habboItem);
            } else if (habboItem instanceof InteractionGameGate) {
                this.roomSpecialTypes.addGameGate((InteractionGameGate) habboItem);
            } else if (habboItem instanceof InteractionGameTimer) {
                this.roomSpecialTypes.addGameTimer((InteractionGameTimer) habboItem);
            } else if (habboItem instanceof InteractionFreezeExitTile) {
                this.roomSpecialTypes.addFreezeExitTile((InteractionFreezeExitTile) habboItem);
            } else if (habboItem instanceof InteractionNest) {
                this.roomSpecialTypes.addNest((InteractionNest) habboItem);
            } else if (habboItem instanceof InteractionPetDrink) {
                this.roomSpecialTypes.addPetDrink((InteractionPetDrink) habboItem);
            } else if (habboItem instanceof InteractionPetFood) {
                this.roomSpecialTypes.addPetFood((InteractionPetFood) habboItem);
            } else if ((habboItem instanceof InteractionMoodLight) || (habboItem instanceof InteractionPyramid) || (habboItem instanceof InteractionMusicDisc) || (habboItem instanceof InteractionBattleBanzaiSphere) || (habboItem instanceof InteractionTalkingFurniture) || (habboItem instanceof InteractionWater) || (habboItem instanceof InteractionWaterItem) || (habboItem instanceof InteractionMuteArea) || (habboItem instanceof InteractionBuildArea) || (habboItem instanceof InteractionTagPole) || (habboItem instanceof InteractionTagField) || (habboItem instanceof InteractionJukeBox) || (habboItem instanceof InteractionPetBreedingNest) || (habboItem instanceof InteractionBlackHole) || (habboItem instanceof InteractionWiredHighscore) || (habboItem instanceof InteractionStickyPole) || (habboItem instanceof WiredBlob) || (habboItem instanceof InteractionTent) || (habboItem instanceof InteractionSnowboardSlope) || (habboItem instanceof InteractionFireworks)) {
                this.roomSpecialTypes.addUndefined(habboItem);
            }
        }
    }

    public HabboItem getHabboItem(int i) {
        HabboItem petFood;
        if (this.roomItems == null || this.roomSpecialTypes == null) {
            return null;
        }
        synchronized (this.roomItems) {
            petFood = (HabboItem) this.roomItems.get(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getBanzaiTeleporter(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getTrigger(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getEffect(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getCondition(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getGameGate(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getGameScorebord(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getGameTimer(i);
        }
        if (petFood == null) {
            petFood = (HabboItem) this.roomSpecialTypes.getFreezeExitTiles().get(Integer.valueOf(i));
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getRoller(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getNest(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getPetDrink(i);
        }
        if (petFood == null) {
            petFood = this.roomSpecialTypes.getPetFood(i);
        }
        return petFood;
    }

    void removeHabboItem(int i) {
        removeHabboItem(getHabboItem(i));
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void removeHabboItem(HabboItem habboItem) {
        HabboItem habboItem2;
        if (habboItem != 0) {
            synchronized (this.roomItems) {
                habboItem2 = (HabboItem) this.roomItems.remove(habboItem.getId());
            }
            if (habboItem2 != null) {
                synchronized (this.furniOwnerCount) {
                    synchronized (this.furniOwnerNames) {
                        int i = this.furniOwnerCount.get(habboItem2.getUserId());
                        if (i > 1) {
                            this.furniOwnerCount.put(habboItem2.getUserId(), i - 1);
                        } else {
                            this.furniOwnerCount.remove(habboItem2.getUserId());
                            this.furniOwnerNames.remove(habboItem2.getUserId());
                        }
                    }
                }
                if (habboItem instanceof ICycleable) {
                    this.roomSpecialTypes.removeCycleTask((ICycleable) habboItem);
                }
                if (habboItem instanceof InteractionBattleBanzaiTeleporter) {
                    this.roomSpecialTypes.removeBanzaiTeleporter((InteractionBattleBanzaiTeleporter) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWiredTrigger) {
                    this.roomSpecialTypes.removeTrigger((InteractionWiredTrigger) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWiredEffect) {
                    this.roomSpecialTypes.removeEffect((InteractionWiredEffect) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWiredCondition) {
                    this.roomSpecialTypes.removeCondition((InteractionWiredCondition) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWiredExtra) {
                    this.roomSpecialTypes.removeExtra((InteractionWiredExtra) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionRoller) {
                    this.roomSpecialTypes.removeRoller((InteractionRoller) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionGameScoreboard) {
                    this.roomSpecialTypes.removeScoreboard((InteractionGameScoreboard) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionGameGate) {
                    this.roomSpecialTypes.removeGameGate((InteractionGameGate) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionGameTimer) {
                    this.roomSpecialTypes.removeGameTimer((InteractionGameTimer) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionFreezeExitTile) {
                    this.roomSpecialTypes.removeFreezeExitTile((InteractionFreezeExitTile) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionNest) {
                    this.roomSpecialTypes.removeNest((InteractionNest) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionPetDrink) {
                    this.roomSpecialTypes.removePetDrink((InteractionPetDrink) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionPetFood) {
                    this.roomSpecialTypes.removePetFood((InteractionPetFood) habboItem);
                    return;
                }
                if (habboItem instanceof InteractionMoodLight) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionPyramid) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionMusicDisc) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionBattleBanzaiSphere) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionTalkingFurniture) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWaterItem) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWater) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionMuteArea) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionTagPole) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionTagField) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionJukeBox) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionPetBreedingNest) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionBlackHole) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionWiredHighscore) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof InteractionStickyPole) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                    return;
                }
                if (habboItem instanceof WiredBlob) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                } else if (habboItem instanceof InteractionTent) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                } else if (habboItem instanceof InteractionSnowboardSlope) {
                    this.roomSpecialTypes.removeUndefined(habboItem);
                }
            }
        }
    }

    public THashSet<HabboItem> getFloorItems() {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                if (((HabboItem) it.value()).getBaseItem().getType() == FurnitureType.FLOOR) {
                    tHashSet.add((HabboItem) it.value());
                }
            } catch (Exception e) {
            }
        }
        return tHashSet;
    }

    public THashSet<HabboItem> getWallItems() {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                if (((HabboItem) it.value()).getBaseItem().getType() == FurnitureType.WALL) {
                    tHashSet.add((HabboItem) it.value());
                }
            } catch (Exception e) {
            }
        }
        return tHashSet;
    }

    public THashSet<HabboItem> getPostItNotes() {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                if (((HabboItem) it.value()).getBaseItem().getInteractionType().getType() == InteractionPostIt.class) {
                    tHashSet.add((HabboItem) it.value());
                }
            } catch (Exception e) {
            }
        }
        return tHashSet;
    }

    public void addHabbo(Habbo habbo) {
        synchronized (this.roomUnitLock) {
            habbo.getRoomUnit().setId(this.unitCounter);
            this.currentHabbos.put(Integer.valueOf(habbo.getHabboInfo().getId()), habbo);
            this.unitCounter++;
            updateDatabaseUserCount();
        }
    }

    public void kickHabbo(Habbo habbo, boolean z) {
        if (z) {
            habbo.getClient().sendResponse(new GenericErrorMessagesComposer(GenericErrorMessagesComposer.KICKED_OUT_OF_THE_ROOM));
        }
        habbo.getRoomUnit().isKicked = true;
        habbo.getRoomUnit().setGoalLocation(this.layout.getDoorTile());
        if (habbo.getRoomUnit().getPath() == null || habbo.getRoomUnit().getPath().size() <= 1 || isPublicRoom()) {
            habbo.getRoomUnit().setCanWalk(true);
            Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo, this);
        }
    }

    public void removeHabbo(Habbo habbo) {
        removeHabbo(habbo, false);
    }

    public void removeHabbo(Habbo habbo, boolean z) {
        HabboItem topItemAt;
        if (habbo.getRoomUnit() != null && habbo.getRoomUnit().getCurrentLocation() != null) {
            habbo.getRoomUnit().getCurrentLocation().removeUnit(habbo.getRoomUnit());
        }
        synchronized (this.roomUnitLock) {
            this.currentHabbos.remove(Integer.valueOf(habbo.getHabboInfo().getId()));
        }
        if (z && habbo.getRoomUnit() != null && !habbo.getRoomUnit().isTeleporting) {
            sendComposer(new RoomUserRemoveComposer(habbo.getRoomUnit()).compose());
        }
        if (habbo.getRoomUnit().getCurrentLocation() != null && (topItemAt = getTopItemAt(habbo.getRoomUnit().getX(), habbo.getRoomUnit().getY())) != null) {
            try {
                topItemAt.onWalkOff(habbo.getRoomUnit(), this, new Object[0]);
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
        if (habbo.getHabboInfo().getCurrentGame() != null && getGame(habbo.getHabboInfo().getCurrentGame()) != null) {
            getGame(habbo.getHabboInfo().getCurrentGame()).removeHabbo(habbo);
        }
        RoomTrade activeTradeForHabbo = getActiveTradeForHabbo(habbo);
        if (activeTradeForHabbo != null) {
            activeTradeForHabbo.stopTrade(habbo);
        }
        if (habbo.getHabboInfo().getId() != this.ownerId) {
            pickupPetsForHabbo(habbo);
        }
        updateDatabaseUserCount();
    }

    public void addBot(Bot bot) {
        synchronized (this.roomUnitLock) {
            bot.getRoomUnit().setId(this.unitCounter);
            this.currentBots.put(bot.getId(), bot);
            this.unitCounter++;
        }
    }

    public void addPet(Pet pet) {
        synchronized (this.roomUnitLock) {
            pet.getRoomUnit().setId(this.unitCounter);
            this.currentPets.put(pet.getId(), pet);
            this.unitCounter++;
            if (getHabbo(pet.getUserId()) != null) {
                this.furniOwnerNames.put(pet.getUserId(), getHabbo(pet.getUserId()).getHabboInfo().getUsername());
            }
        }
    }

    public Bot getBot(int i) {
        return (Bot) this.currentBots.get(i);
    }

    public Bot getBot(RoomUnit roomUnit) {
        synchronized (this.currentBots) {
            TIntObjectIterator it = this.currentBots.iterator();
            int size = this.currentBots.size();
            do {
                int i = size;
                size--;
                if (i > 0) {
                    try {
                        it.advance();
                    } catch (NoSuchElementException e) {
                        LOGGER.error("Caught exception", e);
                    }
                }
                return null;
            } while (((Bot) it.value()).getRoomUnit() != roomUnit);
            return (Bot) it.value();
        }
    }

    public Bot getBotByRoomUnitId(int i) {
        synchronized (this.currentBots) {
            TIntObjectIterator it = this.currentBots.iterator();
            int size = this.currentBots.size();
            do {
                int i2 = size;
                size--;
                if (i2 > 0) {
                    try {
                        it.advance();
                    } catch (NoSuchElementException e) {
                        LOGGER.error("Caught exception", e);
                    }
                }
                return null;
            } while (((Bot) it.value()).getRoomUnit().getId() != i);
            return (Bot) it.value();
        }
    }

    public List<Bot> getBots(String str) {
        ArrayList arrayList = new ArrayList();
        synchronized (this.currentBots) {
            TIntObjectIterator it = this.currentBots.iterator();
            int size = this.currentBots.size();
            while (true) {
                int i = size;
                size--;
                if (i <= 0) {
                    break;
                }
                try {
                    it.advance();
                    if (((Bot) it.value()).getName().equalsIgnoreCase(str)) {
                        arrayList.add((Bot) it.value());
                    }
                } catch (NoSuchElementException e) {
                    LOGGER.error("Caught exception", e);
                    return arrayList;
                }
            }
        }
        return arrayList;
    }

    public boolean hasBotsAt(final int i, final int i2) {
        final boolean[] zArr = {false};
        synchronized (this.currentBots) {
            this.currentBots.forEachValue(new TObjectProcedure<Bot>() { // from class: com.eu.habbo.habbohotel.rooms.Room.2
                public boolean execute(Bot bot) {
                    if (bot.getRoomUnit().getX() != i || bot.getRoomUnit().getY() != i2) {
                        return true;
                    }
                    zArr[0] = true;
                    return false;
                }
            });
        }
        return zArr[0];
    }

    public Pet getPet(int i) {
        return (Pet) this.currentPets.get(i);
    }

    public Pet getPet(RoomUnit roomUnit) {
        TIntObjectIterator it = this.currentPets.iterator();
        int size = this.currentPets.size();
        do {
            int i = size;
            size--;
            if (i <= 0) {
                return null;
            }
            try {
                it.advance();
            } catch (NoSuchElementException e) {
                LOGGER.error("Caught exception", e);
                return null;
            }
        } while (((Pet) it.value()).getRoomUnit() != roomUnit);
        return (Pet) it.value();
    }

    public boolean removeBot(Bot bot) {
        synchronized (this.currentBots) {
            if (!this.currentBots.containsKey(bot.getId())) {
                return false;
            }
            if (bot.getRoomUnit() != null && bot.getRoomUnit().getCurrentLocation() != null) {
                bot.getRoomUnit().getCurrentLocation().removeUnit(bot.getRoomUnit());
            }
            this.currentBots.remove(bot.getId());
            bot.getRoomUnit().setInRoom(false);
            bot.setRoom(null);
            sendComposer(new RoomUserRemoveComposer(bot.getRoomUnit()).compose());
            bot.setRoomUnit(null);
            return true;
        }
    }

    public void placePet(Pet pet, short s, short s2, double d, int i) {
        synchronized (this.currentPets) {
            RoomTile tile = this.layout.getTile(s, s2);
            if (tile == null) {
                tile = this.layout.getDoorTile();
            }
            pet.setRoomUnit(new RoomUnit());
            pet.setRoom(this);
            pet.getRoomUnit().setGoalLocation(tile);
            pet.getRoomUnit().setLocation(tile);
            pet.getRoomUnit().setRoomUnitType(RoomUnitType.PET);
            pet.getRoomUnit().setCanWalk(true);
            pet.getRoomUnit().setPathFinderRoom(this);
            pet.getRoomUnit().setPreviousLocationZ(d);
            pet.getRoomUnit().setZ(d);
            if (pet.getRoomUnit().getCurrentLocation() == null) {
                pet.getRoomUnit().setLocation(getLayout().getDoorTile());
                pet.getRoomUnit().setRotation(RoomUserRotation.fromValue(getLayout().getDoorDirection()));
            }
            pet.needsUpdate = true;
            this.furniOwnerNames.put(pet.getUserId(), getHabbo(pet.getUserId()).getHabboInfo().getUsername());
            addPet(pet);
            sendComposer(new RoomPetComposer(pet).compose());
        }
    }

    public Pet removePet(int i) {
        return (Pet) this.currentPets.remove(i);
    }

    public boolean hasHabbosAt(int i, int i2) {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getRoomUnit().getX() == i && habbo.getRoomUnit().getY() == i2) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPetsAt(int i, int i2) {
        synchronized (this.currentPets) {
            TIntObjectIterator it = this.currentPets.iterator();
            int size = this.currentPets.size();
            while (true) {
                int i3 = size;
                size--;
                if (i3 <= 0) {
                    break;
                }
                try {
                    it.advance();
                    if (((Pet) it.value()).getRoomUnit().getX() == i && ((Pet) it.value()).getRoomUnit().getY() == i2) {
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    LOGGER.error("Caught exception", e);
                    return false;
                }
            }
        }
    }

    public THashSet<Bot> getBotsAt(RoomTile roomTile) {
        THashSet<Bot> tHashSet = new THashSet<>();
        synchronized (this.currentBots) {
            TIntObjectIterator it = this.currentBots.iterator();
            int size = this.currentBots.size();
            while (true) {
                int i = size;
                size--;
                if (i <= 0) {
                    break;
                }
                try {
                    it.advance();
                    if (((Bot) it.value()).getRoomUnit().getCurrentLocation().equals(roomTile)) {
                        tHashSet.add((Bot) it.value());
                    }
                } catch (Exception e) {
                }
            }
        }
        return tHashSet;
    }

    public THashSet<Habbo> getHabbosAt(short s, short s2) {
        return getHabbosAt(this.layout.getTile(s, s2));
    }

    public THashSet<Habbo> getHabbosAt(RoomTile roomTile) {
        THashSet<Habbo> tHashSet = new THashSet<>();
        for (Habbo habbo : getHabbos()) {
            if (habbo.getRoomUnit().getCurrentLocation().equals(roomTile)) {
                tHashSet.add(habbo);
            }
        }
        return tHashSet;
    }

    public THashSet<RoomUnit> getHabbosAndBotsAt(short s, short s2) {
        return getHabbosAndBotsAt(this.layout.getTile(s, s2));
    }

    public THashSet<RoomUnit> getHabbosAndBotsAt(RoomTile roomTile) {
        THashSet<RoomUnit> tHashSet = new THashSet<>();
        TObjectHashIterator it = getBotsAt(roomTile).iterator();
        while (it.hasNext()) {
            tHashSet.add(((Bot) it.next()).getRoomUnit());
        }
        TObjectHashIterator it2 = getHabbosAt(roomTile).iterator();
        while (it2.hasNext()) {
            tHashSet.add(((Habbo) it2.next()).getRoomUnit());
        }
        return tHashSet;
    }

    public THashSet<Habbo> getHabbosOnItem(HabboItem habboItem) {
        THashSet<Habbo> tHashSet = new THashSet<>();
        short x = habboItem.getX();
        while (true) {
            short s = x;
            if (s >= habboItem.getX() + habboItem.getBaseItem().getLength()) {
                return tHashSet;
            }
            short y = habboItem.getY();
            while (true) {
                short s2 = y;
                if (s2 < habboItem.getY() + habboItem.getBaseItem().getWidth()) {
                    tHashSet.addAll(getHabbosAt(s, s2));
                    y = (short) (s2 + 1);
                }
            }
            x = (short) (s + 1);
        }
    }

    public THashSet<Bot> getBotsOnItem(HabboItem habboItem) {
        THashSet<Bot> tHashSet = new THashSet<>();
        short x = habboItem.getX();
        while (true) {
            short s = x;
            if (s >= habboItem.getX() + habboItem.getBaseItem().getLength()) {
                return tHashSet;
            }
            short y = habboItem.getY();
            while (true) {
                short s2 = y;
                if (s2 < habboItem.getY() + habboItem.getBaseItem().getWidth()) {
                    tHashSet.addAll(getBotsAt(getLayout().getTile(s, s2)));
                    y = (short) (s2 + 1);
                }
            }
            x = (short) (s + 1);
        }
    }

    public void teleportHabboToItem(Habbo habbo, HabboItem habboItem) {
        teleportRoomUnitToLocation(habbo.getRoomUnit(), habboItem.getX(), habboItem.getY(), habboItem.getZ() + Item.getCurrentHeight(habboItem));
    }

    public void teleportHabboToLocation(Habbo habbo, short s, short s2) {
        teleportRoomUnitToLocation(habbo.getRoomUnit(), s, s2, 0.0d);
    }

    public void teleportRoomUnitToItem(RoomUnit roomUnit, HabboItem habboItem) {
        teleportRoomUnitToLocation(roomUnit, habboItem.getX(), habboItem.getY(), habboItem.getZ() + Item.getCurrentHeight(habboItem));
    }

    public void teleportRoomUnitToLocation(RoomUnit roomUnit, short s, short s2) {
        teleportRoomUnitToLocation(roomUnit, s, s2, 0.0d);
    }

    public void teleportRoomUnitToLocation(RoomUnit roomUnit, short s, short s2, double d) {
        if (this.loaded) {
            RoomTile tile = this.layout.getTile(s, s2);
            if (d < tile.z) {
                d = tile.z;
            }
            roomUnit.setLocation(tile);
            roomUnit.setGoalLocation(tile);
            roomUnit.setZ(d);
            roomUnit.setPreviousLocationZ(d);
            updateRoomUnit(roomUnit);
        }
    }

    public void muteHabbo(Habbo habbo, int i) {
        synchronized (this.mutedHabbos) {
            this.mutedHabbos.put(habbo.getHabboInfo().getId(), Emulator.getIntUnixTimestamp() + (i * 60));
        }
    }

    public boolean isMuted(Habbo habbo) {
        if (isOwner(habbo) || hasRights(habbo) || !this.mutedHabbos.containsKey(habbo.getHabboInfo().getId())) {
            return false;
        }
        boolean z = this.mutedHabbos.get(habbo.getHabboInfo().getId()) > Emulator.getIntUnixTimestamp();
        if (!z) {
            this.mutedHabbos.remove(habbo.getHabboInfo().getId());
        }
        return z;
    }

    /* JADX WARN: Code restructure failed: missing block: B:21:0x0093, code lost:
    
        ((com.eu.habbo.habbohotel.bots.VisitorBot) r0.value()).onUserEnter(r7);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void habboEntered(com.eu.habbo.habbohotel.users.Habbo r7) {
        /*
            Method dump skipped, instruction units count: 243
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eu.habbo.habbohotel.rooms.Room.habboEntered(com.eu.habbo.habbohotel.users.Habbo):void");
    }

    public void floodMuteHabbo(Habbo habbo, int i) {
        habbo.getHabboStats().mutedCount++;
        int iCeil = i + (i * ((int) Math.ceil(Math.pow(habbo.getHabboStats().mutedCount, 2.0d))));
        habbo.getHabboStats().chatCounter.set(0);
        habbo.mute(iCeil, true);
    }

    public void talk(Habbo habbo, RoomChatMessage roomChatMessage, RoomChatType roomChatType) {
        talk(habbo, roomChatMessage, roomChatType, false);
    }

    /* JADX WARN: Removed duplicated region for block: B:237:0x06a9  */
    /* JADX WARN: Removed duplicated region for block: B:312:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void talk(com.eu.habbo.habbohotel.users.Habbo r11, com.eu.habbo.habbohotel.rooms.RoomChatMessage r12, com.eu.habbo.habbohotel.rooms.RoomChatType r13, boolean r14) {
        /*
            Method dump skipped, instruction units count: 2058
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eu.habbo.habbohotel.rooms.Room.talk(com.eu.habbo.habbohotel.users.Habbo, com.eu.habbo.habbohotel.rooms.RoomChatMessage, com.eu.habbo.habbohotel.rooms.RoomChatType, boolean):void");
    }

    private void showTentChatMessageOutsideTentIfPermitted(Habbo habbo, RoomChatMessage roomChatMessage, Rectangle rectangle) {
        if (habbo == null || !habbo.hasPermission(Permission.ACC_SEE_TENTCHAT) || rectangle == null || RoomLayout.tileInSquare(rectangle, habbo.getRoomUnit().getCurrentLocation())) {
            return;
        }
        RoomChatMessage roomChatMessage2 = new RoomChatMessage(roomChatMessage);
        roomChatMessage2.setMessage("[" + Emulator.getTexts().getValue("hotel.room.tent.prefix") + "] " + roomChatMessage2.getMessage());
        habbo.getClient().sendResponse(new RoomUserWhisperComposer(roomChatMessage2).compose());
    }

    public THashSet<RoomTile> getLockedTiles() {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                HabboItem habboItem = (HabboItem) it.value();
                if (habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
                    boolean z = false;
                    TObjectHashIterator it2 = tHashSet.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        RoomTile roomTile = (RoomTile) it2.next();
                        if (roomTile.x == habboItem.getX() && roomTile.y == habboItem.getY()) {
                            z = true;
                            break;
                        }
                    }
                    if (!z) {
                        if (habboItem.getRotation() == 0 || habboItem.getRotation() == 4) {
                            short s = 0;
                            while (true) {
                                short s2 = s;
                                if (s2 < habboItem.getBaseItem().getLength()) {
                                    short s3 = 0;
                                    while (true) {
                                        short s4 = s3;
                                        if (s4 < habboItem.getBaseItem().getWidth()) {
                                            RoomTile tile = this.layout.getTile((short) (habboItem.getX() + s4), (short) (habboItem.getY() + s2));
                                            if (tile != null) {
                                                tHashSet.add(tile);
                                            }
                                            s3 = (short) (s4 + 1);
                                        }
                                    }
                                    s = (short) (s2 + 1);
                                }
                            }
                        } else {
                            short s5 = 0;
                            while (true) {
                                short s6 = s5;
                                if (s6 < habboItem.getBaseItem().getWidth()) {
                                    short s7 = 0;
                                    while (true) {
                                        short s8 = s7;
                                        if (s8 < habboItem.getBaseItem().getLength()) {
                                            RoomTile tile2 = this.layout.getTile((short) (habboItem.getX() + s8), (short) (habboItem.getY() + s6));
                                            if (tile2 != null) {
                                                tHashSet.add(tile2);
                                            }
                                            s7 = (short) (s8 + 1);
                                        }
                                    }
                                    s5 = (short) (s6 + 1);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return tHashSet;
    }

    @Deprecated
    public THashSet<HabboItem> getItemsAt(int i, int i2) {
        RoomTile tile = getLayout().getTile((short) i, (short) i2);
        return tile != null ? getItemsAt(tile) : new THashSet<>(0);
    }

    public THashSet<HabboItem> getItemsAt(RoomTile roomTile) {
        return getItemsAt(roomTile, false);
    }

    public THashSet<HabboItem> getItemsAt(RoomTile roomTile, boolean z) {
        int length;
        int width;
        THashSet<HabboItem> tHashSet;
        THashSet<HabboItem> tHashSet2 = new THashSet<>(0);
        if (roomTile == null) {
            return tHashSet2;
        }
        if (this.loaded && (tHashSet = this.tileCache.get(roomTile)) != null) {
            return tHashSet;
        }
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                HabboItem habboItem = (HabboItem) it.value();
                if (habboItem != null && habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
                    if (habboItem.getRotation() == 2 || habboItem.getRotation() == 6) {
                        length = habboItem.getBaseItem().getLength() > 0 ? habboItem.getBaseItem().getLength() : 1;
                        width = habboItem.getBaseItem().getWidth() > 0 ? habboItem.getBaseItem().getWidth() : 1;
                    } else {
                        length = habboItem.getBaseItem().getWidth() > 0 ? habboItem.getBaseItem().getWidth() : 1;
                        width = habboItem.getBaseItem().getLength() > 0 ? habboItem.getBaseItem().getLength() : 1;
                    }
                    if (roomTile.x >= habboItem.getX() && roomTile.x <= (habboItem.getX() + length) - 1 && roomTile.y >= habboItem.getY() && roomTile.y <= (habboItem.getY() + width) - 1) {
                        tHashSet2.add(habboItem);
                        if (z) {
                            return tHashSet2;
                        }
                    }
                }
            } catch (Exception e) {
                if (this.loaded) {
                    this.tileCache.put(roomTile, tHashSet2);
                }
                return tHashSet2;
            }
        }
    }

    public THashSet<HabboItem> getItemsAt(int i, int i2, double d) {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TObjectHashIterator it = getItemsAt(i, i2).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getZ() >= d) {
                tHashSet.add(habboItem);
            }
        }
        return tHashSet;
    }

    public THashSet<HabboItem> getItemsAt(Class<? extends HabboItem> cls, int i, int i2) {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TObjectHashIterator it = getItemsAt(i, i2).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getClass().equals(cls)) {
                tHashSet.add(habboItem);
            }
        }
        return tHashSet;
    }

    public boolean hasItemsAt(int i, int i2) {
        RoomTile tile = getLayout().getTile((short) i, (short) i2);
        return tile != null && getItemsAt(tile, true).size() > 0;
    }

    public HabboItem getTopItemAt(int i, int i2) {
        return getTopItemAt(i, i2, null);
    }

    public HabboItem getTopItemAt(int i, int i2, HabboItem habboItem) {
        if (getLayout().getTile((short) i, (short) i2) == null) {
            return null;
        }
        HabboItem habboItem2 = null;
        TObjectHashIterator it = getItemsAt(i, i2).iterator();
        while (it.hasNext()) {
            HabboItem habboItem3 = (HabboItem) it.next();
            if (habboItem == null || habboItem != habboItem3) {
                if (habboItem2 == null || habboItem2.getZ() + Item.getCurrentHeight(habboItem2) <= habboItem3.getZ() + Item.getCurrentHeight(habboItem3)) {
                    habboItem2 = habboItem3;
                }
            }
        }
        return habboItem2;
    }

    public HabboItem getTopItemAt(THashSet<RoomTile> tHashSet, HabboItem habboItem) {
        HabboItem habboItem2 = null;
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            if (roomTile != null) {
                TObjectHashIterator it2 = getItemsAt(roomTile.x, roomTile.y).iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem3 = (HabboItem) it2.next();
                    if (habboItem == null || habboItem != habboItem3) {
                        if (habboItem2 == null || habboItem2.getZ() + Item.getCurrentHeight(habboItem2) <= habboItem3.getZ() + Item.getCurrentHeight(habboItem3)) {
                            habboItem2 = habboItem3;
                        }
                    }
                }
            }
        }
        return habboItem2;
    }

    public double getTopHeightAt(int i, int i2) {
        HabboItem topItemAt = getTopItemAt(i, i2);
        return topItemAt != null ? topItemAt.getZ() + Item.getCurrentHeight(topItemAt) : this.layout.getHeightAtSquare(i, i2);
    }

    @Deprecated
    public HabboItem getLowestChair(int i, int i2) {
        RoomTile tile;
        if (this.layout == null || (tile = this.layout.getTile((short) i, (short) i2)) == null) {
            return null;
        }
        return getLowestChair(tile);
    }

    public HabboItem getLowestChair(RoomTile roomTile) {
        HabboItem habboItem = null;
        THashSet<HabboItem> itemsAt = getItemsAt(roomTile);
        if (itemsAt != null && !itemsAt.isEmpty()) {
            TObjectHashIterator it = itemsAt.iterator();
            while (it.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it.next();
                if (habboItem2.getBaseItem().allowSit() && (habboItem == null || habboItem.getZ() >= habboItem2.getZ())) {
                    habboItem = habboItem2;
                }
            }
        }
        return habboItem;
    }

    public HabboItem getTallestChair(RoomTile roomTile) {
        HabboItem habboItem = null;
        THashSet<HabboItem> itemsAt = getItemsAt(roomTile);
        if (itemsAt != null && !itemsAt.isEmpty()) {
            TObjectHashIterator it = itemsAt.iterator();
            while (it.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it.next();
                if (habboItem2.getBaseItem().allowSit() && (habboItem == null || habboItem.getZ() + Item.getCurrentHeight(habboItem) <= habboItem2.getZ() + Item.getCurrentHeight(habboItem2))) {
                    habboItem = habboItem2;
                }
            }
        }
        return habboItem;
    }

    public double getStackHeight(short s, short s2, boolean z, HabboItem habboItem) {
        if (s < 0 || s2 < 0 || this.layout == null) {
            return z ? 32767.0d : 0.0d;
        }
        if (Emulator.getPluginManager().isRegistered(FurnitureStackHeightEvent.class, true)) {
            FurnitureStackHeightEvent furnitureStackHeightEvent = (FurnitureStackHeightEvent) Emulator.getPluginManager().fireEvent(new FurnitureStackHeightEvent(s, s2, this));
            if (furnitureStackHeightEvent.hasPluginHelper()) {
                return z ? furnitureStackHeightEvent.getHeight().doubleValue() * 256.0d : furnitureStackHeightEvent.getHeight().doubleValue();
            }
        }
        double heightAtSquare = this.layout.getHeightAtSquare(s, s2);
        boolean zAllowStack = true;
        THashSet<HabboItem> itemsAt = getItemsAt(InteractionStackHelper.class, s, s2);
        if (itemsAt.size() > 0) {
            TObjectHashIterator it = itemsAt.iterator();
            while (it.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it.next();
                if (habboItem2 != habboItem) {
                    return z ? habboItem2.getZ() * 256.0d : habboItem2.getZ();
                }
            }
        }
        HabboItem topItemAt = getTopItemAt(s, s2, habboItem);
        if (topItemAt != null) {
            zAllowStack = topItemAt.getBaseItem().allowStack();
            heightAtSquare = topItemAt.getZ() + (topItemAt.getBaseItem().allowSit() ? 0.0d : Item.getCurrentHeight(topItemAt));
        }
        if (z) {
            if (zAllowStack) {
                return heightAtSquare * 256.0d;
            }
            return 32767.0d;
        }
        if (zAllowStack) {
            return heightAtSquare;
        }
        return -1.0d;
    }

    public double getStackHeight(short s, short s2, boolean z) {
        return getStackHeight(s, s2, z, null);
    }

    public boolean hasObjectTypeAt(Class<?> cls, int i, int i2) {
        TObjectHashIterator it = getItemsAt(i, i2).iterator();
        while (it.hasNext()) {
            if (((HabboItem) it.next()).getClass() == cls) {
                return true;
            }
        }
        return false;
    }

    public boolean canSitOrLayAt(int i, int i2) {
        if (hasHabbosAt(i, i2)) {
            return false;
        }
        THashSet<HabboItem> itemsAt = getItemsAt(i, i2);
        return canSitAt(itemsAt) || canLayAt(itemsAt);
    }

    public boolean canSitAt(int i, int i2) {
        if (hasHabbosAt(i, i2)) {
            return false;
        }
        return canSitAt(getItemsAt(i, i2));
    }

    boolean canWalkAt(RoomTile roomTile) {
        if (roomTile == null || roomTile.state == RoomTileState.INVALID) {
            return false;
        }
        HabboItem habboItem = null;
        boolean z = true;
        THashSet<HabboItem> itemsAt = getItemsAt(roomTile);
        if (itemsAt != null) {
            TObjectHashIterator it = itemsAt.iterator();
            while (it.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it.next();
                if (habboItem == null) {
                    habboItem = habboItem2;
                }
                if (habboItem2.getZ() > habboItem.getZ()) {
                    habboItem = habboItem2;
                    z = habboItem.isWalkable() || habboItem.getBaseItem().allowWalk();
                } else if (habboItem2.getZ() == habboItem.getZ() && z && ((!habboItem.isWalkable() && !habboItem.getBaseItem().allowWalk()) || (!habboItem2.getBaseItem().allowWalk() && !habboItem2.isWalkable()))) {
                    z = false;
                }
            }
        }
        return z;
    }

    boolean canSitAt(THashSet<HabboItem> tHashSet) {
        if (tHashSet == null) {
            return false;
        }
        HabboItem habboItem = null;
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            if (habboItem == null || habboItem.getZ() + Item.getCurrentHeight(habboItem) <= habboItem2.getZ() + Item.getCurrentHeight(habboItem2)) {
                habboItem = habboItem2;
            }
        }
        if (habboItem == null) {
            return false;
        }
        return habboItem.getBaseItem().allowSit();
    }

    public boolean canLayAt(int i, int i2) {
        return canLayAt(getItemsAt(i, i2));
    }

    boolean canLayAt(THashSet<HabboItem> tHashSet) {
        if (tHashSet == null || tHashSet.isEmpty()) {
            return true;
        }
        HabboItem habboItem = null;
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            if (habboItem == null || habboItem2.getZ() > habboItem.getZ()) {
                habboItem = habboItem2;
            }
        }
        return habboItem == null || habboItem.getBaseItem().allowLay();
    }

    public RoomTile getRandomWalkableTile() {
        for (int i = 0; i < 10; i++) {
            RoomTile tile = this.layout.getTile((short) (Math.random() * ((double) this.layout.getMapSizeX())), (short) (Math.random() * ((double) this.layout.getMapSizeY())));
            if (tile != null && tile.getState() != RoomTileState.BLOCKED && tile.getState() != RoomTileState.INVALID) {
                return tile;
            }
        }
        return null;
    }

    public Habbo getHabbo(String str) {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getHabboInfo().getUsername().equalsIgnoreCase(str)) {
                return habbo;
            }
        }
        return null;
    }

    public Habbo getHabbo(RoomUnit roomUnit) {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getRoomUnit() == roomUnit) {
                return habbo;
            }
        }
        return null;
    }

    public Habbo getHabbo(int i) {
        return this.currentHabbos.get(Integer.valueOf(i));
    }

    public Habbo getHabboByRoomUnitId(int i) {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getRoomUnit().getId() == i) {
                return habbo;
            }
        }
        return null;
    }

    public void sendComposer(ServerMessage serverMessage) {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getClient() != null) {
                habbo.getClient().sendResponse(serverMessage);
            }
        }
    }

    public void sendWhisper(String str, RoomChatMessageBubbles roomChatMessageBubbles) {
        Iterator<Habbo> it = getHabbos().iterator();
        while (it.hasNext()) {
            it.next().whisper(str, RoomChatMessageBubbles.ALERT);
        }
    }

    public void sendComposerToHabbosWithRights(ServerMessage serverMessage) {
        for (Habbo habbo : getHabbos()) {
            if (hasRights(habbo)) {
                habbo.getClient().sendResponse(serverMessage);
            }
        }
    }

    public void petChat(ServerMessage serverMessage) {
        for (Habbo habbo : getHabbos()) {
            if (!habbo.getHabboStats().ignorePets) {
                habbo.getClient().sendResponse(serverMessage);
            }
        }
    }

    public void botChat(ServerMessage serverMessage) {
        if (serverMessage == null) {
            return;
        }
        for (Habbo habbo : getHabbos()) {
            if (!habbo.getHabboStats().ignoreBots) {
                habbo.getClient().sendResponse(serverMessage);
            }
        }
    }

    private void loadRights(Connection connection) {
        this.rights.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT user_id FROM room_rights WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        this.rights.add(resultSetExecuteQuery.getInt("user_id"));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadBans(Connection connection) {
        this.bannedHabbos.clear();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.id, room_bans.* FROM room_bans INNER JOIN users ON room_bans.user_id = users.id WHERE ends > ? AND room_bans.room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp());
                preparedStatementPrepareStatement.setInt(2, this.id);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        if (!this.bannedHabbos.containsKey(resultSetExecuteQuery.getInt("user_id"))) {
                            this.bannedHabbos.put(resultSetExecuteQuery.getInt("user_id"), new RoomBan(resultSetExecuteQuery));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public RoomRightLevels getGuildRightLevel(Habbo habbo) {
        if (this.guild > 0 && habbo.getHabboStats().hasGuild(this.guild)) {
            Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.guild);
            if (Emulator.getGameEnvironment().getGuildManager().getOnlyAdmins(guild).get(Integer.valueOf(habbo.getHabboInfo().getId())) != null) {
                return RoomRightLevels.GUILD_ADMIN;
            }
            if (guild.getRights()) {
                return RoomRightLevels.GUILD_RIGHTS;
            }
        }
        return RoomRightLevels.NONE;
    }

    @Deprecated
    public int guildRightLevel(Habbo habbo) {
        return getGuildRightLevel(habbo).level;
    }

    public boolean isOwner(Habbo habbo) {
        return habbo.getHabboInfo().getId() == this.ownerId || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
    }

    public boolean hasRights(Habbo habbo) {
        return isOwner(habbo) || this.rights.contains(habbo.getHabboInfo().getId()) || (habbo.getRoomUnit().getRightsLevel() != RoomRightLevels.NONE && this.currentHabbos.containsKey(Integer.valueOf(habbo.getHabboInfo().getId())));
    }

    public void giveRights(Habbo habbo) {
        if (habbo != null) {
            giveRights(habbo.getHabboInfo().getId());
        }
    }

    public void giveRights(int i) {
        MessengerBuddy friend;
        if (this.rights.contains(i)) {
            return;
        }
        if (this.rights.add(i)) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_rights VALUES (?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.id);
                        preparedStatementPrepareStatement.setInt(2, i);
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
        Habbo habbo = getHabbo(i);
        if (habbo != null) {
            refreshRightsForHabbo(habbo);
            sendComposer(new RoomAddRightsListComposer(this, habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername()).compose());
            return;
        }
        Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.ownerId);
        if (habbo2 == null || (friend = habbo2.getMessenger().getFriend(i)) == null) {
            return;
        }
        sendComposer(new RoomAddRightsListComposer(this, i, friend.getUsername()).compose());
    }

    public void removeRights(int i) {
        Habbo habbo = getHabbo(i);
        if (((UserRightsTakenEvent) Emulator.getPluginManager().fireEvent(new UserRightsTakenEvent(getHabbo(getOwnerId()), i, habbo))).isCancelled()) {
            return;
        }
        sendComposer(new RoomRemoveRightsListComposer(this, i).compose());
        if (this.rights.remove(i)) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_rights WHERE room_id = ? AND user_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.id);
                        preparedStatementPrepareStatement.setInt(2, i);
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
        if (habbo != null) {
            ejectUserFurni(habbo.getHabboInfo().getId());
            habbo.getRoomUnit().setRightsLevel(RoomRightLevels.NONE);
            habbo.getRoomUnit().removeStatus(RoomUnitStatus.FLAT_CONTROL);
            refreshRightsForHabbo(habbo);
        }
    }

    public void removeAllRights() {
        Connection connection;
        for (int i : this.rights.toArray()) {
            ejectUserFurni(i);
        }
        this.rights.clear();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_rights WHERE room_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.id);
                preparedStatementPrepareStatement.execute();
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                refreshRightsInRoom();
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
    }

    void refreshRightsInRoom() {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getHabboInfo().getCurrentRoom() == this) {
                refreshRightsForHabbo(habbo);
            }
        }
    }

    public void refreshRightsForHabbo(Habbo habbo) {
        RoomRightLevels guildRightLevel = RoomRightLevels.NONE;
        if (!habbo.getHabboStats().isRentingSpace() || getHabboItem(habbo.getHabboStats().getRentedItemId()) == null) {
            if (habbo.hasPermission(Permission.ACC_ANYROOMOWNER) || isOwner(habbo)) {
                habbo.getClient().sendResponse(new RoomOwnerComposer());
                guildRightLevel = RoomRightLevels.MODERATOR;
            } else if (hasRights(habbo) && !hasGuild()) {
                guildRightLevel = RoomRightLevels.RIGHTS;
            } else if (hasGuild()) {
                guildRightLevel = getGuildRightLevel(habbo);
            }
            habbo.getClient().sendResponse(new RoomRightsComposer(guildRightLevel));
            habbo.getRoomUnit().setStatus(RoomUnitStatus.FLAT_CONTROL, guildRightLevel.level + Emulator.PREVIEW);
            habbo.getRoomUnit().setRightsLevel(guildRightLevel);
            habbo.getRoomUnit().statusUpdate(true);
            if (guildRightLevel.equals(RoomRightLevels.MODERATOR)) {
                habbo.getClient().sendResponse(new RoomRightsListComposer(this));
            }
        }
    }

    public THashMap<Integer, String> getUsersWithRights() {
        THashMap<Integer, String> tHashMap = new THashMap<>();
        if (!this.rights.isEmpty()) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username AS username, users.id as user_id FROM room_rights INNER JOIN users ON room_rights.user_id = users.id WHERE room_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.id);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            try {
                                tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("user_id")), resultSetExecuteQuery.getString("username"));
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
        return tHashMap;
    }

    public void unbanHabbo(int i) {
        RoomBan roomBan = (RoomBan) this.bannedHabbos.remove(i);
        if (roomBan != null) {
            roomBan.delete();
        }
        sendComposer(new RoomUserUnbannedComposer(this, i).compose());
    }

    public boolean isBanned(Habbo habbo) {
        RoomBan roomBan = (RoomBan) this.bannedHabbos.get(habbo.getHabboInfo().getId());
        boolean z = (roomBan == null || roomBan.endTimestamp <= Emulator.getIntUnixTimestamp() || habbo.hasPermission(Permission.ACC_ANYROOMOWNER) || habbo.hasPermission("acc_enteranyroom")) ? false : true;
        if (!z && roomBan != null) {
            unbanHabbo(habbo.getHabboInfo().getId());
        }
        return z;
    }

    public TIntObjectHashMap<RoomBan> getBannedHabbos() {
        return this.bannedHabbos;
    }

    public void addRoomBan(RoomBan roomBan) {
        this.bannedHabbos.put(roomBan.userId, roomBan);
    }

    public void makeSit(Habbo habbo) {
        if (habbo.getRoomUnit() == null || habbo.getRoomUnit().hasStatus(RoomUnitStatus.SIT) || !habbo.getRoomUnit().canForcePosture()) {
            return;
        }
        dance(habbo, DanceType.NONE);
        habbo.getRoomUnit().cmdSit = true;
        habbo.getRoomUnit().setBodyRotation(RoomUserRotation.values()[habbo.getRoomUnit().getBodyRotation().getValue() - (habbo.getRoomUnit().getBodyRotation().getValue() % 2)]);
        habbo.getRoomUnit().setStatus(RoomUnitStatus.SIT, "0.5");
        sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
    }

    public void makeStand(Habbo habbo) {
        if (habbo.getRoomUnit() == null) {
            return;
        }
        HabboItem topItemAt = getTopItemAt(habbo.getRoomUnit().getX(), habbo.getRoomUnit().getY());
        if (topItemAt != null && topItemAt.getBaseItem().allowSit() && topItemAt.getBaseItem().allowLay()) {
            return;
        }
        habbo.getRoomUnit().cmdStand = true;
        habbo.getRoomUnit().setBodyRotation(RoomUserRotation.values()[habbo.getRoomUnit().getBodyRotation().getValue() - (habbo.getRoomUnit().getBodyRotation().getValue() % 2)]);
        habbo.getRoomUnit().removeStatus(RoomUnitStatus.SIT);
        sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
    }

    public void giveEffect(Habbo habbo, int i, int i2) {
        if (this.currentHabbos.containsKey(Integer.valueOf(habbo.getHabboInfo().getId()))) {
            giveEffect(habbo.getRoomUnit(), i, i2);
        }
    }

    public void giveEffect(RoomUnit roomUnit, int i, int i2) {
        int intUnixTimestamp = (i2 == -1 || i2 == Integer.MAX_VALUE) ? Integer.MAX_VALUE : i2 + Emulator.getIntUnixTimestamp();
        if (!this.allowEffects || roomUnit == null) {
            return;
        }
        roomUnit.setEffectId(i, intUnixTimestamp);
        sendComposer(new RoomUserEffectComposer(roomUnit).compose());
    }

    public void giveHandItem(Habbo habbo, int i) {
        giveHandItem(habbo.getRoomUnit(), i);
    }

    public void giveHandItem(RoomUnit roomUnit, int i) {
        roomUnit.setHandItem(i);
        sendComposer(new RoomUserHandItemComposer(roomUnit).compose());
    }

    public void updateItem(HabboItem habboItem) {
        if (!isLoaded() || habboItem == null || habboItem.getRoomId() != this.id || habboItem.getBaseItem() == null) {
            return;
        }
        if (habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
            sendComposer(new FloorItemUpdateComposer(habboItem).compose());
            updateTiles(getLayout().getTilesAt(this.layout.getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()));
        } else if (habboItem.getBaseItem().getType() == FurnitureType.WALL) {
            sendComposer(new WallItemUpdateComposer(habboItem).compose());
        }
    }

    public void updateItemState(HabboItem habboItem) {
        if (habboItem.isLimited()) {
            sendComposer(new FloorItemUpdateComposer(habboItem).compose());
        } else {
            sendComposer(new ItemStateComposer(habboItem).compose());
        }
        if (habboItem.getBaseItem().getType() != FurnitureType.FLOOR || this.layout == null) {
            return;
        }
        updateTiles(getLayout().getTilesAt(this.layout.getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()));
        if (habboItem instanceof InteractionMultiHeight) {
            ((InteractionMultiHeight) habboItem).updateUnitsOnItem(this);
        }
    }

    public int getUserFurniCount(int i) {
        return this.furniOwnerCount.get(i);
    }

    public int getUserUniqueFurniCount(int i) {
        THashSet tHashSet = new THashSet();
        for (HabboItem habboItem : this.roomItems.valueCollection()) {
            if (!tHashSet.contains(habboItem.getBaseItem()) && habboItem.getUserId() == i) {
                tHashSet.add(habboItem.getBaseItem());
            }
        }
        return tHashSet.size();
    }

    public void ejectUserFurni(int i) {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i2 = size;
            size--;
            if (i2 <= 0) {
                break;
            }
            try {
                it.advance();
                if (((HabboItem) it.value()).getUserId() == i) {
                    tHashSet.add((HabboItem) it.value());
                    ((HabboItem) it.value()).setRoomId(0);
                }
            } catch (Exception e) {
            }
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
        if (habbo != null) {
            habbo.getInventory().getItemsComponent().addItems(tHashSet);
            habbo.getClient().sendResponse(new AddHabboItemComposer(tHashSet));
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            pickUpItem((HabboItem) it2.next(), null);
        }
    }

    public void ejectUserItem(HabboItem habboItem) {
        pickUpItem(habboItem, null);
    }

    public void ejectAll() {
        ejectAll(null);
    }

    public void ejectAll(Habbo habbo) {
        THashMap tHashMap = new THashMap();
        synchronized (this.roomItems) {
            TIntObjectIterator it = this.roomItems.iterator();
            int size = this.roomItems.size();
            while (true) {
                int i = size;
                size--;
                if (i <= 0) {
                    break;
                }
                try {
                    it.advance();
                    if (habbo == null || ((HabboItem) it.value()).getUserId() != habbo.getHabboInfo().getId()) {
                        if (!(it.value() instanceof InteractionPostIt)) {
                            ((THashSet) tHashMap.computeIfAbsent(Integer.valueOf(((HabboItem) it.value()).getUserId()), num -> {
                                return new THashSet();
                            })).add((HabboItem) it.value());
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        for (Map.Entry entry : tHashMap.entrySet()) {
            TObjectHashIterator it2 = ((THashSet) entry.getValue()).iterator();
            while (it2.hasNext()) {
                pickUpItem((HabboItem) it2.next(), null);
            }
            Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(((Integer) entry.getKey()).intValue());
            if (habbo2 != null) {
                habbo2.getInventory().getItemsComponent().addItems((THashSet) entry.getValue());
                habbo2.getClient().sendResponse(new AddHabboItemComposer((THashSet<HabboItem>) entry.getValue()));
            }
        }
    }

    public void refreshGuild(Guild guild) {
        if (guild.getRoomId() == this.id) {
            THashSet<GuildMember> guildMembers = Emulator.getGameEnvironment().getGuildManager().getGuildMembers(guild.getId());
            for (Habbo habbo : getHabbos()) {
                Optional optionalFindAny = guildMembers.stream().filter(guildMember -> {
                    return guildMember.getUserId() == habbo.getHabboInfo().getId();
                }).findAny();
                if (optionalFindAny.isPresent()) {
                    habbo.getClient().sendResponse(new GuildInfoComposer(guild, habbo.getClient(), false, (GuildMember) optionalFindAny.get()));
                }
            }
        }
        refreshGuildRightsInRoom();
    }

    public void refreshGuildColors(Guild guild) {
        if (guild.getRoomId() != this.id) {
            return;
        }
        TIntObjectIterator it = this.roomItems.iterator();
        int size = this.roomItems.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                return;
            }
            try {
                it.advance();
                HabboItem habboItem = (HabboItem) it.value();
                if ((habboItem instanceof InteractionGuildFurni) && ((InteractionGuildFurni) habboItem).getGuildId() == guild.getId()) {
                    updateItem(habboItem);
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    public void refreshGuildRightsInRoom() {
        for (Habbo habbo : getHabbos()) {
            if (habbo.getHabboInfo().getCurrentRoom() == this && habbo.getHabboInfo().getId() != this.ownerId && !habbo.hasPermission(Permission.ACC_ANYROOMOWNER) && !habbo.hasPermission(Permission.ACC_MOVEROTATE)) {
                refreshRightsForHabbo(habbo);
            }
        }
    }

    public void idle(Habbo habbo) {
        habbo.getRoomUnit().setIdle();
        if (habbo.getRoomUnit().getDanceType() != DanceType.NONE) {
            dance(habbo, DanceType.NONE);
        }
        sendComposer(new RoomUnitIdleComposer(habbo.getRoomUnit()).compose());
        WiredHandler.handle(WiredTriggerType.IDLES, habbo.getRoomUnit(), this, new Object[]{habbo});
    }

    public void unIdle(Habbo habbo) {
        if (habbo == null || habbo.getRoomUnit() == null) {
            return;
        }
        habbo.getRoomUnit().resetIdleTimer();
        sendComposer(new RoomUnitIdleComposer(habbo.getRoomUnit()).compose());
        WiredHandler.handle(WiredTriggerType.UNIDLES, habbo.getRoomUnit(), this, new Object[]{habbo});
    }

    public void dance(Habbo habbo, DanceType danceType) {
        dance(habbo.getRoomUnit(), danceType);
    }

    public void dance(RoomUnit roomUnit, DanceType danceType) {
        if (roomUnit.getDanceType() != danceType) {
            boolean z = !roomUnit.getDanceType().equals(DanceType.NONE);
            roomUnit.setDanceType(danceType);
            sendComposer(new RoomUserDanceComposer(roomUnit).compose());
            if (danceType.equals(DanceType.NONE) && z) {
                WiredHandler.handle(WiredTriggerType.STOPS_DANCING, roomUnit, this, new Object[]{roomUnit});
            } else {
                if (danceType.equals(DanceType.NONE) || z) {
                    return;
                }
                WiredHandler.handle(WiredTriggerType.STARTS_DANCING, roomUnit, this, new Object[]{roomUnit});
            }
        }
    }

    public void addToWordFilter(String str) {
        synchronized (this.wordFilterWords) {
            if (this.wordFilterWords.contains(str)) {
                return;
            }
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT IGNORE INTO room_wordfilter VALUES (?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, getId());
                        preparedStatementPrepareStatement.setString(2, str);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        this.wordFilterWords.add(str);
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
                } catch (Throwable th3) {
                    if (connection != null) {
                        try {
                            connection.close();
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
    }

    public void removeFromWordFilter(String str) {
        synchronized (this.wordFilterWords) {
            this.wordFilterWords.remove(str);
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_wordfilter WHERE room_id = ? AND word = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, getId());
                        preparedStatementPrepareStatement.setString(2, str);
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
                } catch (Throwable th3) {
                    if (connection != null) {
                        try {
                            connection.close();
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
    }

    public void handleWordQuiz(Habbo habbo, String str) {
        synchronized (this.userVotes) {
            if (!this.wordQuiz.isEmpty() && !hasVotedInWordQuiz(habbo)) {
                String strReplace = str.replace(":", Emulator.PREVIEW);
                if (strReplace.equals("0")) {
                    this.noVotes++;
                } else if (strReplace.equals("1")) {
                    this.yesVotes++;
                }
                sendComposer(new SimplePollAnswerComposer(habbo.getHabboInfo().getId(), strReplace, this.noVotes, this.yesVotes).compose());
                this.userVotes.add(Integer.valueOf(habbo.getHabboInfo().getId()));
            }
        }
    }

    public void startWordQuiz(String str, int i) {
        if (hasActiveWordQuiz()) {
            return;
        }
        this.wordQuiz = str;
        this.noVotes = 0;
        this.yesVotes = 0;
        this.userVotes.clear();
        this.wordQuizEnd = Emulator.getIntUnixTimestamp() + (i / Outgoing.CraftableProductsComposer);
        sendComposer(new SimplePollStartComposer(i, str).compose());
    }

    public boolean hasActiveWordQuiz() {
        return Emulator.getIntUnixTimestamp() < this.wordQuizEnd;
    }

    public boolean hasVotedInWordQuiz(Habbo habbo) {
        return this.userVotes.contains(Integer.valueOf(habbo.getHabboInfo().getId()));
    }

    public void alert(String str) {
        sendComposer(new GenericAlertComposer(str).compose());
    }

    public int itemCount() {
        return this.roomItems.size();
    }

    public void setJukeBoxActive(boolean z) {
        this.jukeboxActive = z;
        this.needsUpdate = true;
    }

    public boolean isHideWired() {
        return this.hideWired;
    }

    public void setHideWired(boolean z) {
        this.hideWired = z;
        if (!this.hideWired) {
            sendComposer(new RoomFloorItemsComposer(this.furniOwnerNames, this.roomSpecialTypes.getTriggers()).compose());
            sendComposer(new RoomFloorItemsComposer(this.furniOwnerNames, this.roomSpecialTypes.getEffects()).compose());
            sendComposer(new RoomFloorItemsComposer(this.furniOwnerNames, this.roomSpecialTypes.getConditions()).compose());
            sendComposer(new RoomFloorItemsComposer(this.furniOwnerNames, this.roomSpecialTypes.getExtras()).compose());
            return;
        }
        TObjectHashIterator it = this.roomSpecialTypes.getTriggers().iterator();
        while (it.hasNext()) {
            sendComposer(new RemoveFloorItemComposer((HabboItem) it.next()).compose());
        }
        TObjectHashIterator it2 = this.roomSpecialTypes.getEffects().iterator();
        while (it2.hasNext()) {
            sendComposer(new RemoveFloorItemComposer((HabboItem) it2.next()).compose());
        }
        TObjectHashIterator it3 = this.roomSpecialTypes.getConditions().iterator();
        while (it3.hasNext()) {
            sendComposer(new RemoveFloorItemComposer((HabboItem) it3.next()).compose());
        }
        TObjectHashIterator it4 = this.roomSpecialTypes.getExtras().iterator();
        while (it4.hasNext()) {
            sendComposer(new RemoveFloorItemComposer((HabboItem) it4.next()).compose());
        }
    }

    public FurnitureMovementError canPlaceFurnitureAt(HabboItem habboItem, Habbo habbo, RoomTile roomTile, int i) {
        HabboItem habboItem2;
        if (itemCount() >= MAXIMUM_FURNI) {
            return FurnitureMovementError.MAX_ITEMS;
        }
        if (roomTile == null || roomTile.state == RoomTileState.INVALID) {
            return FurnitureMovementError.INVALID_MOVE;
        }
        int i2 = i % 8;
        if (hasRights(habbo) || getGuildRightLevel(habbo).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS) || habbo.hasPermission(Permission.ACC_MOVEROTATE)) {
            return FurnitureMovementError.NONE;
        }
        if (habbo.getHabboStats().isRentingSpace() && (habboItem2 = getHabboItem(habbo.getHabboStats().rentedItemId)) != null) {
            return !RoomLayout.squareInSquare(RoomLayout.getRectangle(habboItem2.getX(), habboItem2.getY(), habboItem2.getBaseItem().getWidth(), habboItem2.getBaseItem().getLength(), habboItem2.getRotation()), RoomLayout.getRectangle(roomTile.x, roomTile.y, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i2)) ? FurnitureMovementError.NO_RIGHTS : FurnitureMovementError.NONE;
        }
        TObjectHashIterator it = getRoomSpecialTypes().getItemsOfType(InteractionBuildArea.class).iterator();
        while (it.hasNext()) {
            HabboItem habboItem3 = (HabboItem) it.next();
            if (((InteractionBuildArea) habboItem3).inSquare(roomTile) && ((InteractionBuildArea) habboItem3).isBuilder(habbo.getHabboInfo().getUsername())) {
                return FurnitureMovementError.NONE;
            }
        }
        return FurnitureMovementError.NO_RIGHTS;
    }

    public FurnitureMovementError furnitureFitsAt(RoomTile roomTile, HabboItem habboItem, int i) {
        return furnitureFitsAt(roomTile, habboItem, i, true);
    }

    public FurnitureMovementError furnitureFitsAt(RoomTile roomTile, HabboItem habboItem, int i, boolean z) {
        if (!this.layout.fitsOnMap(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i)) {
            return FurnitureMovementError.INVALID_MOVE;
        }
        if (habboItem instanceof InteractionStackHelper) {
            return FurnitureMovementError.NONE;
        }
        THashSet<RoomTile> tilesAt = this.layout.getTilesAt(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i);
        TObjectHashIterator it = tilesAt.iterator();
        while (it.hasNext()) {
            RoomTile roomTile2 = (RoomTile) it.next();
            if (roomTile2.state == RoomTileState.INVALID) {
                return FurnitureMovementError.INVALID_MOVE;
            }
            if (!Emulator.getConfig().getBoolean("wired.place.under", false) || (Emulator.getConfig().getBoolean("wired.place.under", false) && !habboItem.isWalkable() && !habboItem.getBaseItem().allowSit() && !habboItem.getBaseItem().allowLay())) {
                if (z && hasHabbosAt(roomTile2.x, roomTile2.y)) {
                    return FurnitureMovementError.TILE_HAS_HABBOS;
                }
                if (z && hasBotsAt(roomTile2.x, roomTile2.y)) {
                    return FurnitureMovementError.TILE_HAS_BOTS;
                }
                if (z && hasPetsAt(roomTile2.x, roomTile2.y)) {
                    return FurnitureMovementError.TILE_HAS_PETS;
                }
            }
        }
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it2 = tilesAt.iterator();
        while (it2.hasNext()) {
            RoomTile roomTile3 = (RoomTile) it2.next();
            arrayList.add(Pair.create(roomTile3, getItemsAt(roomTile3)));
            HabboItem topItemAt = getTopItemAt(roomTile3.x, roomTile3.y, habboItem);
            if (topItemAt != null && !topItemAt.getBaseItem().allowStack() && !roomTile3.getAllowStack()) {
                return FurnitureMovementError.CANT_STACK;
            }
        }
        return !habboItem.canStackAt(this, arrayList) ? FurnitureMovementError.CANT_STACK : FurnitureMovementError.NONE;
    }

    public FurnitureMovementError placeFloorFurniAt(HabboItem habboItem, RoomTile roomTile, int i, Habbo habbo) {
        boolean zHasPluginHelper = false;
        if (Emulator.getPluginManager().isRegistered(FurniturePlacedEvent.class, true)) {
            FurniturePlacedEvent furniturePlacedEvent = (FurniturePlacedEvent) Emulator.getPluginManager().fireEvent(new FurniturePlacedEvent(habboItem, habbo, roomTile));
            if (furniturePlacedEvent.isCancelled()) {
                return FurnitureMovementError.CANCEL_PLUGIN_PLACE;
            }
            zHasPluginHelper = furniturePlacedEvent.hasPluginHelper();
        }
        THashSet<RoomTile> tilesAt = this.layout.getTilesAt(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i);
        FurnitureMovementError furnitureMovementErrorFurnitureFitsAt = furnitureFitsAt(roomTile, habboItem, i);
        if (!furnitureMovementErrorFurnitureFitsAt.equals(FurnitureMovementError.NONE) && !zHasPluginHelper) {
            return furnitureMovementErrorFurnitureFitsAt;
        }
        double stackHeight = roomTile.getStackHeight();
        TObjectHashIterator it = tilesAt.iterator();
        while (it.hasNext()) {
            double stackHeight2 = ((RoomTile) it.next()).getStackHeight();
            if (stackHeight2 > stackHeight) {
                stackHeight = stackHeight2;
            }
        }
        if (Emulator.getPluginManager().isRegistered(FurnitureBuildheightEvent.class, true)) {
            FurnitureBuildheightEvent furnitureBuildheightEvent = (FurnitureBuildheightEvent) Emulator.getPluginManager().fireEvent(new FurnitureBuildheightEvent(habboItem, habbo, 0.0d, stackHeight));
            if (furnitureBuildheightEvent.hasChangedHeight()) {
                stackHeight = furnitureBuildheightEvent.getUpdatedHeight();
            }
        }
        habboItem.setZ(stackHeight);
        habboItem.setX(roomTile.x);
        habboItem.setY(roomTile.y);
        habboItem.setRotation(i);
        if (!this.furniOwnerNames.containsKey(habboItem.getUserId()) && habbo != null) {
            this.furniOwnerNames.put(habboItem.getUserId(), habbo.getHabboInfo().getUsername());
        }
        habboItem.needsUpdate(true);
        addHabboItem(habboItem);
        habboItem.setRoomId(this.id);
        habboItem.onPlace(this);
        updateTiles(tilesAt);
        sendComposer(new AddFloorItemComposer(habboItem, getFurniOwnerName(habboItem.getUserId())).compose());
        TObjectHashIterator it2 = tilesAt.iterator();
        while (it2.hasNext()) {
            RoomTile roomTile2 = (RoomTile) it2.next();
            updateHabbosAt(roomTile2.x, roomTile2.y);
            updateBotsAt(roomTile2.x, roomTile2.y);
        }
        Emulator.getThreading().run(habboItem);
        return FurnitureMovementError.NONE;
    }

    public FurnitureMovementError placeWallFurniAt(HabboItem habboItem, String str, Habbo habbo) {
        if (!hasRights(habbo) && !getGuildRightLevel(habbo).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS)) {
            return FurnitureMovementError.NO_RIGHTS;
        }
        if (Emulator.getPluginManager().isRegistered(FurniturePlacedEvent.class, true)) {
            FurniturePlacedEvent furniturePlacedEvent = new FurniturePlacedEvent(habboItem, habbo, null);
            Emulator.getPluginManager().fireEvent(furniturePlacedEvent);
            if (furniturePlacedEvent.isCancelled()) {
                return FurnitureMovementError.CANCEL_PLUGIN_PLACE;
            }
        }
        habboItem.setWallPosition(str);
        if (!this.furniOwnerNames.containsKey(habboItem.getUserId()) && habbo != null) {
            this.furniOwnerNames.put(habboItem.getUserId(), habbo.getHabboInfo().getUsername());
        }
        sendComposer(new AddWallItemComposer(habboItem, getFurniOwnerName(habboItem.getUserId())).compose());
        habboItem.needsUpdate(true);
        addHabboItem(habboItem);
        habboItem.setRoomId(this.id);
        habboItem.onPlace(this);
        Emulator.getThreading().run(habboItem);
        return FurnitureMovementError.NONE;
    }

    public FurnitureMovementError moveFurniTo(HabboItem habboItem, RoomTile roomTile, int i, Habbo habbo) {
        return moveFurniTo(habboItem, roomTile, i, habbo, true, true);
    }

    public FurnitureMovementError moveFurniTo(HabboItem habboItem, RoomTile roomTile, int i, Habbo habbo, boolean z) {
        return moveFurniTo(habboItem, roomTile, i, habbo, z, true);
    }

    public FurnitureMovementError moveFurniTo(HabboItem habboItem, RoomTile roomTile, int i, Habbo habbo, boolean z, boolean z2) {
        double stackHeight;
        RoomTile tile = this.layout.getTile(habboItem.getX(), habboItem.getY());
        boolean zHasPluginHelper = false;
        if (Emulator.getPluginManager().isRegistered(FurnitureMovedEvent.class, true)) {
            FurnitureMovedEvent furnitureMovedEvent = (FurnitureMovedEvent) Emulator.getPluginManager().fireEvent(new FurnitureMovedEvent(habboItem, habbo, tile, roomTile));
            if (furnitureMovedEvent.isCancelled()) {
                return FurnitureMovementError.CANCEL_PLUGIN_MOVE;
            }
            zHasPluginHelper = furnitureMovedEvent.hasPluginHelper();
        }
        boolean z3 = habboItem instanceof InteractionStackHelper;
        Optional optionalFindAny = getItemsAt(roomTile).stream().filter(habboItem2 -> {
            return habboItem2 instanceof InteractionStackHelper;
        }).findAny();
        THashSet<RoomTile> tilesAt = this.layout.getTilesAt(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i);
        THashSet<RoomTile> tilesAt2 = this.layout.getTilesAt(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i);
        HabboItem topItemAt = getTopItemAt(tilesAt, (HabboItem) null);
        if (!optionalFindAny.isPresent() && !zHasPluginHelper) {
            if (tile != roomTile) {
                TObjectHashIterator it = tilesAt.iterator();
                while (it.hasNext()) {
                    RoomTile roomTile2 = (RoomTile) it.next();
                    HabboItem topItemAt2 = getTopItemAt(roomTile2.x, roomTile2.y);
                    if (!z3) {
                        if (topItemAt2 == null || topItemAt2 == habboItem) {
                            if (calculateTileState(roomTile2, habboItem).equals(RoomTileState.INVALID)) {
                                return FurnitureMovementError.CANT_STACK;
                            }
                        } else if (roomTile2.state.equals(RoomTileState.INVALID) || !roomTile2.getAllowStack() || !topItemAt2.getBaseItem().allowStack()) {
                            return FurnitureMovementError.CANT_STACK;
                        }
                    }
                    if (!Emulator.getConfig().getBoolean("wired.place.under", false) || (Emulator.getConfig().getBoolean("wired.place.under", false) && !habboItem.isWalkable() && !habboItem.getBaseItem().allowSit() && !habboItem.getBaseItem().allowLay())) {
                        if (!z2) {
                            continue;
                        } else {
                            if (!z3 && hasHabbosAt(roomTile2.x, roomTile2.y)) {
                                return FurnitureMovementError.TILE_HAS_HABBOS;
                            }
                            if (!z3 && hasBotsAt(roomTile2.x, roomTile2.y)) {
                                return FurnitureMovementError.TILE_HAS_BOTS;
                            }
                            if (!z3 && hasPetsAt(roomTile2.x, roomTile2.y)) {
                                return FurnitureMovementError.TILE_HAS_PETS;
                            }
                        }
                    }
                }
            }
            ArrayList arrayList = new ArrayList();
            TObjectHashIterator it2 = tilesAt.iterator();
            while (it2.hasNext()) {
                RoomTile roomTile3 = (RoomTile) it2.next();
                arrayList.add(Pair.create(roomTile3, getItemsAt(roomTile3)));
            }
            if (!z3 && !habboItem.canStackAt(this, arrayList)) {
                return FurnitureMovementError.CANT_STACK;
            }
        }
        THashSet<RoomTile> tilesAt3 = this.layout.getTilesAt(this.layout.getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation());
        int rotation = habboItem.getRotation();
        if (rotation != i) {
            habboItem.setRotation(i);
            if (Emulator.getPluginManager().isRegistered(FurnitureRotatedEvent.class, true)) {
                FurnitureRotatedEvent furnitureRotatedEvent = new FurnitureRotatedEvent(habboItem, habbo, rotation);
                Emulator.getPluginManager().fireEvent(furnitureRotatedEvent);
                if (furnitureRotatedEvent.isCancelled()) {
                    habboItem.setRotation(rotation);
                    return FurnitureMovementError.CANCEL_PLUGIN_ROTATE;
                }
            }
            if ((!optionalFindAny.isPresent() && topItemAt != null && topItemAt != habboItem && !topItemAt.getBaseItem().allowStack()) || (topItemAt != null && topItemAt != habboItem && topItemAt.getZ() + Item.getCurrentHeight(topItemAt) + Item.getCurrentHeight(habboItem) > MAXIMUM_FURNI_HEIGHT)) {
                habboItem.setRotation(rotation);
                return FurnitureMovementError.CANT_STACK;
            }
        }
        if (optionalFindAny.isPresent()) {
            stackHeight = ((HabboItem) optionalFindAny.get()).getExtradata().isEmpty() ? Double.parseDouble("0.0") : Double.parseDouble(((HabboItem) optionalFindAny.get()).getExtradata()) / 100.0d;
        } else if (habboItem == topItemAt) {
            stackHeight = habboItem.getZ();
        } else {
            stackHeight = getStackHeight(roomTile.x, roomTile.y, false, habboItem);
            TObjectHashIterator it3 = tilesAt.iterator();
            while (it3.hasNext()) {
                RoomTile roomTile4 = (RoomTile) it3.next();
                double stackHeight2 = getStackHeight(roomTile4.x, roomTile4.y, false, habboItem);
                if (stackHeight2 > stackHeight) {
                    stackHeight = stackHeight2;
                }
            }
        }
        if (stackHeight <= MAXIMUM_FURNI_HEIGHT && stackHeight >= getLayout().getHeightAtSquare(roomTile.x, roomTile.y)) {
            if (Emulator.getPluginManager().isRegistered(FurnitureBuildheightEvent.class, true)) {
                FurnitureBuildheightEvent furnitureBuildheightEvent = (FurnitureBuildheightEvent) Emulator.getPluginManager().fireEvent(new FurnitureBuildheightEvent(habboItem, habbo, 0.0d, stackHeight));
                if (furnitureBuildheightEvent.hasChangedHeight()) {
                    stackHeight = furnitureBuildheightEvent.getUpdatedHeight();
                }
            }
            habboItem.setX(roomTile.x);
            habboItem.setY(roomTile.y);
            habboItem.setZ(stackHeight);
            if (z3) {
                habboItem.setZ(roomTile.z);
                habboItem.setExtradata(Emulator.PREVIEW + (habboItem.getZ() * 100.0d));
            }
            if (habboItem.getZ() > MAXIMUM_FURNI_HEIGHT) {
                habboItem.setZ(MAXIMUM_FURNI_HEIGHT);
            }
            habboItem.onMove(this, tile, roomTile);
            habboItem.needsUpdate(true);
            Emulator.getThreading().run(habboItem);
            if (z) {
                sendComposer(new FloorItemUpdateComposer(habboItem).compose());
            }
            tilesAt.removeAll(tilesAt3);
            tilesAt.addAll(tilesAt3);
            updateTiles(tilesAt);
            TObjectHashIterator it4 = tilesAt.iterator();
            while (it4.hasNext()) {
                RoomTile roomTile5 = (RoomTile) it4.next();
                updateHabbosAt(roomTile5.x, roomTile5.y, getHabbosAt(roomTile5.x, roomTile5.y));
                updateBotsAt(roomTile5.x, roomTile5.y);
            }
            if (Emulator.getConfig().getBoolean("wired.place.under", false)) {
                TObjectHashIterator it5 = tilesAt2.iterator();
                while (it5.hasNext()) {
                    RoomTile roomTile6 = (RoomTile) it5.next();
                    TObjectHashIterator it6 = getHabbosAt(roomTile6.x, roomTile6.y).iterator();
                    while (it6.hasNext()) {
                        try {
                            habboItem.onWalkOn(((Habbo) it6.next()).getRoomUnit(), this, null);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return FurnitureMovementError.NONE;
        }
        return FurnitureMovementError.CANT_STACK;
    }

    public FurnitureMovementError slideFurniTo(HabboItem habboItem, RoomTile roomTile, int i) {
        this.layout.getTile(habboItem.getX(), habboItem.getY());
        getTopItemAt(roomTile.x, roomTile.y);
        boolean z = habboItem instanceof InteractionStackHelper;
        THashSet<RoomTile> tilesAt = this.layout.getTilesAt(roomTile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), i);
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = tilesAt.iterator();
        while (it.hasNext()) {
            RoomTile roomTile2 = (RoomTile) it.next();
            arrayList.add(Pair.create(roomTile2, getItemsAt(roomTile2)));
        }
        if (!z && !habboItem.canStackAt(this, arrayList)) {
            return FurnitureMovementError.CANT_STACK;
        }
        this.layout.getTilesAt(this.layout.getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation());
        habboItem.getRotation();
        habboItem.setRotation(i);
        if (z) {
            habboItem.setZ(roomTile.z);
            habboItem.setExtradata(Emulator.PREVIEW + (habboItem.getZ() * 100.0d));
        }
        if (habboItem.getZ() > MAXIMUM_FURNI_HEIGHT) {
            habboItem.setZ(MAXIMUM_FURNI_HEIGHT);
        }
        sendComposer(new FloorItemOnRollerComposer(habboItem, null, roomTile, getStackHeight(roomTile.x, roomTile.y, false, habboItem) - habboItem.getZ(), this).compose());
        TObjectHashIterator it2 = tilesAt.iterator();
        while (it2.hasNext()) {
            RoomTile roomTile3 = (RoomTile) it2.next();
            updateHabbosAt(roomTile3.x, roomTile3.y);
            updateBotsAt(roomTile3.x, roomTile3.y);
        }
        return FurnitureMovementError.NONE;
    }

    public THashSet<RoomUnit> getRoomUnits() {
        return getRoomUnits(null);
    }

    public THashSet<RoomUnit> getRoomUnits(RoomTile roomTile) {
        THashSet<RoomUnit> tHashSet = new THashSet<>();
        for (Habbo habbo : this.currentHabbos.values()) {
            if (habbo != null && habbo.getRoomUnit() != null && habbo.getRoomUnit().getRoom() != null && habbo.getRoomUnit().getRoom().getId() == getId() && (roomTile == null || habbo.getRoomUnit().getCurrentLocation() == roomTile)) {
                tHashSet.add(habbo.getRoomUnit());
            }
        }
        for (Pet pet : this.currentPets.valueCollection()) {
            if (pet != null && pet.getRoomUnit() != null && pet.getRoomUnit().getRoom() != null && pet.getRoomUnit().getRoom().getId() == getId() && (roomTile == null || pet.getRoomUnit().getCurrentLocation() == roomTile)) {
                tHashSet.add(pet.getRoomUnit());
            }
        }
        for (Bot bot : this.currentBots.valueCollection()) {
            if (bot != null && bot.getRoomUnit() != null && bot.getRoomUnit().getRoom() != null && bot.getRoomUnit().getRoom().getId() == getId() && (roomTile == null || bot.getRoomUnit().getCurrentLocation() == roomTile)) {
                tHashSet.add(bot.getRoomUnit());
            }
        }
        return tHashSet;
    }

    public Collection<RoomUnit> getRoomUnitsAt(RoomTile roomTile) {
        return (Collection) getRoomUnits().stream().filter(roomUnit -> {
            return roomUnit.getCurrentLocation() == roomTile;
        }).collect(Collectors.toSet());
    }

    static {
        for (int i = 1; i <= 3; i++) {
            RoomMoodlightData roomMoodlightDataFromString = RoomMoodlightData.fromString(Emulator.PREVIEW);
            roomMoodlightDataFromString.setId(i);
            defaultMoodData.put(i, roomMoodlightDataFromString);
        }
    }
}
