package com.eu.habbo.habbohotel.messenger;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.friends.UpdateFriendComposer;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import com.eu.habbo.plugin.events.users.friends.UserAcceptFriendRequestEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/messenger/Messenger.class */
public class Messenger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Messenger.class);
    public static boolean SAVE_PRIVATE_CHATS = false;
    public static int MAXIMUM_FRIENDS = RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS;
    public static int MAXIMUM_FRIENDS_HC = 500;
    private final ConcurrentHashMap<Integer, MessengerBuddy> friends = new ConcurrentHashMap<>();
    private final THashSet<FriendRequest> friendRequests = new THashSet<>();

    public static void unfriend(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM messenger_friendships WHERE (user_one_id = ? AND user_two_id = ?) OR (user_one_id = ? AND user_two_id = ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, i2);
                    preparedStatementPrepareStatement.setInt(3, i2);
                    preparedStatementPrepareStatement.setInt(4, i);
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

    public static THashSet<MessengerBuddy> searchUsers(String str) {
        Connection connection;
        THashSet<MessengerBuddy> tHashSet = new THashSet<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users WHERE username LIKE ? ORDER BY username ASC LIMIT " + Emulator.getConfig().getInt("hotel.messenger.search.maxresults"));
            try {
                preparedStatementPrepareStatement.setString(1, str + "%");
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        tHashSet.add(new MessengerBuddy(resultSetExecuteQuery, false));
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
                return tHashSet;
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
    }

    @Deprecated
    public static boolean canFriendRequest(Habbo habbo, String str) {
        Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
        HabboInfo habboInfo = habbo2 != null ? habbo2.getHabboInfo() : HabboManager.getOfflineHabboInfo(str);
        if (habboInfo == null) {
            return false;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM messenger_friendrequests WHERE (user_to_id = ? AND user_from_id = ?) OR (user_to_id = ? AND user_from_id = ?) LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(2, habboInfo.getId());
                    preparedStatementPrepareStatement.setInt(3, habboInfo.getId());
                    preparedStatementPrepareStatement.setInt(4, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return false;
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
                        return true;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public static boolean friendRequested(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM messenger_friendrequests WHERE user_to_id = ? AND user_from_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, i2);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return true;
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
                        return false;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public static void makeFriendRequest(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO messenger_friendrequests (user_to_id, user_from_id) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
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

    public static int getFriendCount(int i) {
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(i);
        if (habbo != null) {
            return habbo.getMessenger().getFriends().size();
        }
        int i2 = 0;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT count(id) as count FROM messenger_friendships WHERE user_one_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            i2 = resultSetExecuteQuery.getInt("count");
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
        return i2;
    }

    public static THashMap<Integer, THashSet<MessengerBuddy>> getFriends(int i) {
        THashMap<Integer, THashSet<MessengerBuddy>> tHashMap = new THashMap<>();
        tHashMap.put(1, new THashSet());
        tHashMap.put(2, new THashSet());
        tHashMap.put(3, new THashSet());
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.id, users.look, users.username, messenger_friendships.relation FROM messenger_friendships INNER JOIN users ON users.id = messenger_friendships.user_two_id WHERE user_one_id = ? ORDER BY RAND() LIMIT 50");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (resultSetExecuteQuery.getInt("relation") != 0) {
                                MessengerBuddy messengerBuddy = new MessengerBuddy(resultSetExecuteQuery.getInt("id"), resultSetExecuteQuery.getString("username"), resultSetExecuteQuery.getString("look"), Short.valueOf((short) resultSetExecuteQuery.getInt("relation")), i);
                                ((THashSet) tHashMap.get(Integer.valueOf(messengerBuddy.getRelation()))).add(messengerBuddy);
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
        return tHashMap;
    }

    public static void checkFriendSizeProgress(Habbo habbo) {
        int achievementProgress = habbo.getHabboStats().getAchievementProgress(Emulator.getGameEnvironment().getAchievementManager().getAchievement("FriendListSize"));
        int size = 1;
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("FriendListSize");
        if (achievement == null) {
            return;
        }
        if (achievementProgress > 0) {
            size = habbo.getMessenger().getFriends().size() - achievementProgress;
            if (size < 0) {
                return;
            }
        }
        AchievementManager.progressAchievement(habbo, achievement, size);
    }

    public void loadFriends(Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.id, users.username, users.gender, users.online, users.look, users.motto, messenger_friendships.* FROM messenger_friendships INNER JOIN users ON messenger_friendships.user_two_id = users.id WHERE user_one_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            MessengerBuddy messengerBuddy = new MessengerBuddy(resultSetExecuteQuery);
                            if (messengerBuddy.getId() != habbo.getHabboInfo().getId()) {
                                this.friends.putIfAbsent(Integer.valueOf(resultSetExecuteQuery.getInt("id")), messengerBuddy);
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

    public MessengerBuddy loadFriend(Habbo habbo, int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        MessengerBuddy messengerBuddy = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.id, users.username, users.gender, users.online, users.look, users.motto, messenger_friendships.* FROM messenger_friendships INNER JOIN users ON messenger_friendships.user_two_id = users.id WHERE user_one_id = ? AND user_two_id = ? LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
            preparedStatementPrepareStatement.setInt(2, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    messengerBuddy = new MessengerBuddy(resultSetExecuteQuery);
                    this.friends.putIfAbsent(Integer.valueOf(resultSetExecuteQuery.getInt("id")), messengerBuddy);
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
            return messengerBuddy;
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

    public void loadFriendRequests(Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.id, users.username, users.look FROM messenger_friendrequests INNER JOIN users ON user_from_id = users.id WHERE user_to_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.friendRequests.add(new FriendRequest(resultSetExecuteQuery));
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

    public void removeBuddy(int i) {
        this.friends.remove(Integer.valueOf(i));
    }

    public void removeBuddy(Habbo habbo) {
        this.friends.remove(Integer.valueOf(habbo.getHabboInfo().getId()));
    }

    public void addBuddy(MessengerBuddy messengerBuddy) {
        this.friends.put(Integer.valueOf(messengerBuddy.getId()), messengerBuddy);
    }

    public THashSet<MessengerBuddy> getFriends(String str) {
        THashSet<MessengerBuddy> tHashSet = new THashSet<>();
        for (Map.Entry<Integer, MessengerBuddy> entry : this.friends.entrySet()) {
            if (StringUtils.containsIgnoreCase(entry.getValue().getUsername(), str)) {
                tHashSet.add(entry.getValue());
            }
        }
        return tHashSet;
    }

    public void connectionChanged(Habbo habbo, boolean z, boolean z2) {
        Habbo habbo2;
        MessengerBuddy friend;
        if (habbo != null) {
            for (Map.Entry<Integer, MessengerBuddy> entry : getFriends().entrySet()) {
                if (entry.getValue().getOnline() != 0 && (habbo2 = Emulator.getGameServer().getGameClientManager().getHabbo(entry.getKey().intValue())) != null && habbo2.getMessenger() != null && (friend = habbo2.getMessenger().getFriend(habbo.getHabboInfo().getId())) != null) {
                    friend.setOnline(z);
                    friend.inRoom(z2);
                    friend.setLook(habbo.getHabboInfo().getLook());
                    friend.setGender(habbo.getHabboInfo().getGender());
                    friend.setUsername(habbo.getHabboInfo().getUsername());
                    habbo2.getClient().sendResponse(new UpdateFriendComposer(habbo2, friend, (Integer) 0));
                }
            }
        }
    }

    public void deleteAllFriendRequests(int i) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM messenger_friendrequests WHERE user_to_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
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

    public int deleteFriendRequests(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM messenger_friendrequests WHERE (user_to_id = ? AND user_from_id = ?) OR (user_to_id = ? AND user_from_id = ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.setInt(4, i2);
                    preparedStatementPrepareStatement.setInt(3, i);
                    int iExecuteUpdate = preparedStatementPrepareStatement.executeUpdate();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return iExecuteUpdate;
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
            return 0;
        }
    }

    public void acceptFriendRequest(int i, int i2) {
        if (deleteFriendRequests(i, i2) > 0) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO messenger_friendships (user_one_id, user_two_id, friends_since) VALUES (?, ?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i);
                        preparedStatementPrepareStatement.setInt(2, i2);
                        preparedStatementPrepareStatement.setInt(3, Emulator.getIntUnixTimestamp());
                        preparedStatementPrepareStatement.execute();
                        preparedStatementPrepareStatement.setInt(1, i2);
                        preparedStatementPrepareStatement.setInt(2, i);
                        preparedStatementPrepareStatement.setInt(3, Emulator.getIntUnixTimestamp());
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
            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(i2);
            Habbo habbo2 = Emulator.getGameServer().getGameClientManager().getHabbo(i);
            if (habbo == null || habbo2 == null) {
                if (habbo != null) {
                    habbo.getClient().sendResponse(new UpdateFriendComposer(habbo, loadFriend(habbo, i), (Integer) 1));
                    return;
                } else {
                    if (habbo2 != null) {
                        habbo2.getClient().sendResponse(new UpdateFriendComposer(habbo2, loadFriend(habbo2, i2), (Integer) 1));
                        return;
                    }
                    return;
                }
            }
            MessengerBuddy messengerBuddy = new MessengerBuddy(habbo2, habbo.getHabboInfo().getId());
            MessengerBuddy messengerBuddy2 = new MessengerBuddy(habbo, habbo2.getHabboInfo().getId());
            habbo.getMessenger().friends.putIfAbsent(Integer.valueOf(habbo2.getHabboInfo().getId()), messengerBuddy);
            habbo2.getMessenger().friends.putIfAbsent(Integer.valueOf(habbo.getHabboInfo().getId()), messengerBuddy2);
            if (((UserAcceptFriendRequestEvent) Emulator.getPluginManager().fireEvent(new UserAcceptFriendRequestEvent(habbo, messengerBuddy2))).isCancelled()) {
                removeBuddy(i2);
            } else {
                habbo.getClient().sendResponse(new UpdateFriendComposer(habbo, messengerBuddy, (Integer) 1));
                habbo2.getClient().sendResponse(new UpdateFriendComposer(habbo2, messengerBuddy2, (Integer) 1));
            }
        }
    }

    public ConcurrentHashMap<Integer, MessengerBuddy> getFriends() {
        return this.friends;
    }

    public THashSet<FriendRequest> getFriendRequests() {
        THashSet<FriendRequest> tHashSet;
        synchronized (this.friendRequests) {
            tHashSet = this.friendRequests;
        }
        return tHashSet;
    }

    public FriendRequest findFriendRequest(String str) {
        synchronized (this.friendRequests) {
            TObjectHashIterator it = this.friendRequests.iterator();
            while (it.hasNext()) {
                FriendRequest friendRequest = (FriendRequest) it.next();
                if (friendRequest.getUsername().equalsIgnoreCase(str)) {
                    return friendRequest;
                }
            }
            return null;
        }
    }

    public MessengerBuddy getFriend(int i) {
        return this.friends.get(Integer.valueOf(i));
    }

    public void dispose() {
        synchronized (this.friends) {
            this.friends.clear();
        }
        synchronized (this.friendRequests) {
            this.friendRequests.clear();
        }
    }
}
