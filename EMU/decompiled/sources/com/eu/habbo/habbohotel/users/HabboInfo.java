package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.messenger.MessengerCategory;
import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboInfo.class */
public class HabboInfo implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboInfo.class);
    private String username;
    private String motto;
    private String look;
    private HabboGender gender;
    private String mail;
    private String sso;
    private String ipRegister;
    private String ipLogin;
    private int id;
    private int accountCreated;
    private Rank rank;
    private int credits;
    private int lastOnline;
    private int homeRoom;
    private boolean online;
    private int loadingRoom;
    private Room currentRoom;
    private int roomQueueId;
    private RideablePet riding;
    private Class<? extends Game> currentGame;
    private TIntIntHashMap currencies;
    private GamePlayer gamePlayer;
    private int photoRoomId;
    private int photoTimestamp;
    private String photoURL;
    private String photoJSON;
    private int webPublishTimestamp;
    private String machineID;
    public boolean firstVisit = false;
    private List<NavigatorSavedSearch> savedSearches = new ArrayList();
    private List<MessengerCategory> messengerCategories = new ArrayList();

    public HabboInfo(ResultSet resultSet) {
        try {
            this.id = resultSet.getInt("id");
            this.username = resultSet.getString("username");
            this.motto = resultSet.getString("motto");
            this.look = resultSet.getString("look");
            this.gender = HabboGender.valueOf(resultSet.getString("gender"));
            this.mail = resultSet.getString("mail");
            this.sso = resultSet.getString("auth_ticket");
            this.ipRegister = resultSet.getString("ip_register");
            this.ipLogin = resultSet.getString("ip_current");
            this.rank = Emulator.getGameEnvironment().getPermissionsManager().getRank(resultSet.getInt("rank"));
            if (this.rank == null) {
                LOGGER.error("No existing rank found with id " + resultSet.getInt("rank") + ". Make sure an entry in the permissions table exists.");
                LOGGER.warn(this.username + " has an invalid rank with id " + resultSet.getInt("rank") + ". Make sure an entry in the permissions table exists.");
                this.rank = Emulator.getGameEnvironment().getPermissionsManager().getRank(1);
            }
            this.accountCreated = resultSet.getInt("account_created");
            this.credits = resultSet.getInt("credits");
            this.homeRoom = resultSet.getInt("home_room");
            this.lastOnline = resultSet.getInt("last_online");
            this.machineID = resultSet.getString("machine_id");
            this.online = false;
            this.currentRoom = null;
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        loadCurrencies();
        loadSavedSearches();
        loadMessengerCategories();
    }

    private void loadCurrencies() {
        this.currencies = new TIntIntHashMap();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_currency WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.currencies.put(resultSetExecuteQuery.getInt("type"), resultSetExecuteQuery.getInt("amount"));
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

    private void saveCurrencies() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                final PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_currency (user_id, type, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = ?");
                try {
                    this.currencies.forEachEntry(new TIntIntProcedure() { // from class: com.eu.habbo.habbohotel.users.HabboInfo.1
                        public boolean execute(int i, int i2) {
                            try {
                                preparedStatementPrepareStatement.setInt(1, HabboInfo.this.getId());
                                preparedStatementPrepareStatement.setInt(2, i);
                                preparedStatementPrepareStatement.setInt(3, i2);
                                preparedStatementPrepareStatement.setInt(4, i2);
                                preparedStatementPrepareStatement.addBatch();
                                return true;
                            } catch (SQLException e) {
                                HabboInfo.LOGGER.error("Caught SQL exception", e);
                                return true;
                            }
                        }
                    });
                    preparedStatementPrepareStatement.executeBatch();
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

    private void loadSavedSearches() {
        this.savedSearches = new ArrayList();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_saved_searches WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.savedSearches.add(new NavigatorSavedSearch(resultSetExecuteQuery.getString("search_code"), resultSetExecuteQuery.getString("filter"), resultSetExecuteQuery.getInt("id")));
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

    public void addSavedSearch(NavigatorSavedSearch navigatorSavedSearch) {
        this.savedSearches.add(navigatorSavedSearch);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_saved_searches (search_code, filter, user_id) VALUES (?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setString(1, navigatorSavedSearch.getSearchCode());
                    preparedStatementPrepareStatement.setString(2, navigatorSavedSearch.getFilter());
                    preparedStatementPrepareStatement.setInt(3, this.id);
                    if (preparedStatementPrepareStatement.executeUpdate() == 0) {
                        throw new SQLException("Creating saved search failed, no rows affected.");
                    }
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Creating saved search failed, no ID found.");
                        }
                        navigatorSavedSearch.setId(generatedKeys.getInt(1));
                        if (generatedKeys != null) {
                            generatedKeys.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void deleteSavedSearch(NavigatorSavedSearch navigatorSavedSearch) {
        this.savedSearches.remove(navigatorSavedSearch);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_saved_searches WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, navigatorSavedSearch.getId());
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

    private void loadMessengerCategories() {
        this.messengerCategories = new ArrayList();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM messenger_categories WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.messengerCategories.add(new MessengerCategory(resultSetExecuteQuery.getString("name"), resultSetExecuteQuery.getInt("user_id"), resultSetExecuteQuery.getInt("id")));
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

    public void addMessengerCategory(MessengerCategory messengerCategory) {
        this.messengerCategories.add(messengerCategory);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO messenger_categories (name, user_id) VALUES (?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setString(1, messengerCategory.getName());
                    preparedStatementPrepareStatement.setInt(2, this.id);
                    if (preparedStatementPrepareStatement.executeUpdate() == 0) {
                        throw new SQLException("Creating messenger category failed, no rows affected.");
                    }
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Creating messenger category failed, no ID found.");
                        }
                        messengerCategory.setId(generatedKeys.getInt(1));
                        if (generatedKeys != null) {
                            generatedKeys.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void deleteMessengerCategory(MessengerCategory messengerCategory) {
        this.messengerCategories.remove(messengerCategory);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM messenger_categories WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, messengerCategory.getId());
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

    public int getCurrencyAmount(int i) {
        return this.currencies.get(i);
    }

    public TIntIntHashMap getCurrencies() {
        return this.currencies;
    }

    public void addCurrencyAmount(int i, int i2) {
        this.currencies.adjustOrPutValue(i, i2, i2);
        run();
    }

    public void setCurrencyAmount(int i, int i2) {
        this.currencies.put(i, i2);
        run();
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String str) {
        this.username = str;
    }

    public String getMotto() {
        return this.motto;
    }

    public void setMotto(String str) {
        this.motto = str;
    }

    public Rank getRank() {
        return this.rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getLook() {
        return this.look;
    }

    public void setLook(String str) {
        this.look = str;
    }

    public HabboGender getGender() {
        return this.gender;
    }

    public void setGender(HabboGender habboGender) {
        this.gender = habboGender;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMail(String str) {
        this.mail = str;
    }

    public String getSso() {
        return this.sso;
    }

    public void setSso(String str) {
        this.sso = str;
    }

    public String getIpRegister() {
        return this.ipRegister;
    }

    public void setIpRegister(String str) {
        this.ipRegister = str;
    }

    public String getIpLogin() {
        return this.ipLogin;
    }

    public void setIpLogin(String str) {
        this.ipLogin = str;
    }

    public int getAccountCreated() {
        return this.accountCreated;
    }

    public void setAccountCreated(int i) {
        this.accountCreated = i;
    }

    public boolean canBuy(CatalogItem catalogItem) {
        return this.credits >= catalogItem.getCredits() && getCurrencies().get(catalogItem.getPointsType()) >= catalogItem.getPoints();
    }

    public int getCredits() {
        return this.credits;
    }

    public void setCredits(int i) {
        this.credits = i;
        run();
    }

    public void addCredits(int i) {
        this.credits += i;
        run();
    }

    public int getPixels() {
        return getCurrencyAmount(0);
    }

    public void setPixels(int i) {
        setCurrencyAmount(0, i);
        run();
    }

    public void addPixels(int i) {
        addCurrencyAmount(0, i);
        run();
    }

    public int getLastOnline() {
        return this.lastOnline;
    }

    public void setLastOnline(int i) {
        this.lastOnline = i;
    }

    public int getHomeRoom() {
        return this.homeRoom;
    }

    public void setHomeRoom(int i) {
        this.homeRoom = i;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(boolean z) {
        this.online = z;
    }

    public int getLoadingRoom() {
        return this.loadingRoom;
    }

    public void setLoadingRoom(int i) {
        this.loadingRoom = i;
    }

    public Room getCurrentRoom() {
        return this.currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public int getRoomQueueId() {
        return this.roomQueueId;
    }

    public void setRoomQueueId(int i) {
        this.roomQueueId = i;
    }

    public RideablePet getRiding() {
        return this.riding;
    }

    public void setRiding(RideablePet rideablePet) {
        this.riding = rideablePet;
    }

    public void dismountPet() {
        dismountPet(false);
    }

    public void dismountPet(boolean z) {
        Habbo habbo;
        if (getRiding() == null || (habbo = getCurrentRoom().getHabbo(getId())) == null) {
            return;
        }
        RideablePet riding = getRiding();
        riding.setRider(null);
        riding.setTask(PetTasks.FREE);
        setRiding(null);
        Room currentRoom = getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.giveEffect(habbo, 0, -1);
        }
        RoomUnit roomUnit = habbo.getRoomUnit();
        if (roomUnit == null) {
            return;
        }
        roomUnit.setZ(riding.getRoomUnit().getZ());
        roomUnit.setPreviousLocationZ(riding.getRoomUnit().getZ());
        roomUnit.stopWalking();
        currentRoom.sendComposer(new RoomUserStatusComposer(roomUnit).compose());
        List<RoomTile> arrayList = z ? new ArrayList<>() : getCurrentRoom().getLayout().getWalkableTilesAround(roomUnit.getCurrentLocation());
        roomUnit.setGoalLocation(arrayList.isEmpty() ? roomUnit.getCurrentLocation() : arrayList.get(0));
        roomUnit.statusUpdate(true);
    }

    public Class<? extends Game> getCurrentGame() {
        return this.currentGame;
    }

    public void setCurrentGame(Class<? extends Game> cls) {
        this.currentGame = cls;
    }

    public boolean isInGame() {
        return this.currentGame != null;
    }

    public synchronized GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public synchronized void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public int getPhotoRoomId() {
        return this.photoRoomId;
    }

    public void setPhotoRoomId(int i) {
        this.photoRoomId = i;
    }

    public int getPhotoTimestamp() {
        return this.photoTimestamp;
    }

    public void setPhotoTimestamp(int i) {
        this.photoTimestamp = i;
    }

    public String getPhotoURL() {
        return this.photoURL;
    }

    public void setPhotoURL(String str) {
        this.photoURL = str;
    }

    public String getPhotoJSON() {
        return this.photoJSON;
    }

    public void setPhotoJSON(String str) {
        this.photoJSON = str;
    }

    public int getWebPublishTimestamp() {
        return this.webPublishTimestamp;
    }

    public void setWebPublishTimestamp(int i) {
        this.webPublishTimestamp = i;
    }

    public String getMachineID() {
        return this.machineID;
    }

    public void setMachineID(String str) {
        this.machineID = str;
    }

    public List<NavigatorSavedSearch> getSavedSearches() {
        return this.savedSearches;
    }

    public List<MessengerCategory> getMessengerCategories() {
        return this.messengerCategories;
    }

    @Override // java.lang.Runnable
    public void run() {
        saveCurrencies();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET motto = ?, online = ?, look = ?, gender = ?, credits = ?, last_login = ?, last_online = ?, home_room = ?, ip_current = ?, `rank` = ?, machine_id = ?, username = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setString(1, this.motto);
                    preparedStatementPrepareStatement.setString(2, this.online ? "1" : "0");
                    preparedStatementPrepareStatement.setString(3, this.look);
                    preparedStatementPrepareStatement.setString(4, this.gender.name());
                    preparedStatementPrepareStatement.setInt(5, this.credits);
                    preparedStatementPrepareStatement.setInt(7, this.lastOnline);
                    preparedStatementPrepareStatement.setInt(6, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.setInt(8, this.homeRoom);
                    preparedStatementPrepareStatement.setString(9, this.ipLogin);
                    preparedStatementPrepareStatement.setInt(10, this.rank != null ? this.rank.getId() : 1);
                    preparedStatementPrepareStatement.setString(11, this.machineID);
                    preparedStatementPrepareStatement.setString(12, this.username);
                    preparedStatementPrepareStatement.setInt(13, this.id);
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

    public int getBonusRarePoints() {
        return getCurrencyAmount(Emulator.getConfig().getInt("hotelview.promotional.points.type"));
    }

    public HabboStats getHabboStats() {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getId());
        return habbo != null ? habbo.getHabboStats() : HabboStats.load(this);
    }
}
