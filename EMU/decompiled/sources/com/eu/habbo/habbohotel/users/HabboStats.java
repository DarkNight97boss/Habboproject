package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.achievements.TalentTrackType;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarRewardClaimed;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.cache.HabboOfferPurchase;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.plugin.events.users.subscriptions.UserSubscriptionCreatedEvent;
import com.eu.habbo.plugin.events.users.subscriptions.UserSubscriptionExtendedEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.stack.array.TIntArrayStack;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboStats.class */
public class HabboStats implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboStats.class);
    public final HabboNavigatorWindowSettings navigatorWindowSettings;
    public int achievementScore;
    public int respectPointsReceived;
    public int respectPointsGiven;
    public int respectPointsToGive;
    public int petRespectPointsToGive;
    public boolean blockFollowing;
    public boolean blockFriendRequests;
    public boolean blockRoomInvites;
    public boolean blockStaffAlerts;
    public boolean preferOldChat;
    public boolean blockCameraFollow;
    public RoomChatMessageBubbles chatColor;
    public int volumeSystem;
    public int volumeFurni;
    public int volumeTrax;
    public int guild;
    public String[] tags;
    public int loginStreak;
    public int rentedItemId;
    public int rentedTimeEnd;
    public int hofPoints;
    public boolean ignorePets;
    public boolean ignoreBots;
    public int citizenshipLevel;
    public int helpersLevel;
    public boolean perkTrade;
    public long roomEnterTimestamp;
    public long lastChat;
    public long lastUsersSearched;
    public boolean nux;
    public boolean nuxReward;
    public boolean allowNameChange;
    public int forumPostsCount;
    public int uiFlags;
    public boolean hasGottenDefaultSavedSearches;
    private HabboInfo habboInfo;
    private boolean allowTrade;
    private int clubExpireTimestamp;
    private int muteEndTime;
    public int maxFriends;
    public int maxRooms;
    public int lastHCPayday;
    public int hcGiftsClaimed;
    public THashSet<Subscription> subscriptions;
    public final TIntObjectMap<HabboOfferPurchase> offerCache = new TIntObjectHashMap();
    private final AtomicInteger lastOnlineTime = new AtomicInteger(Emulator.getIntUnixTimestamp());
    public AtomicInteger chatCounter = new AtomicInteger(0);
    public int nuxStep = 1;
    public int mutedCount = 0;
    public boolean mutedBubbleTracker = false;
    public String changeNameChecked = Emulator.PREVIEW;
    public boolean isPurchasingFurniture = false;
    public THashMap<Integer, List<Integer>> ltdPurchaseLog = new THashMap<>(0);
    public long lastTradeTimestamp = Emulator.getIntUnixTimestamp();
    public long lastGiftTimestamp = Emulator.getIntUnixTimestamp();
    public long lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
    public int hcMessageLastModified = Emulator.getIntUnixTimestamp();
    public final THashMap<String, Object> cache = new THashMap<>(0);
    private final THashMap<Achievement, Integer> achievementProgress = new THashMap<>(0);
    private final THashMap<Achievement, Integer> achievementCache = new THashMap<>(0);
    private final THashMap<Integer, CatalogItem> recentPurchases = new THashMap<>(0);
    private final TIntArrayList favoriteRooms = new TIntArrayList(0);
    private final TIntArrayList ignoredUsers = new TIntArrayList(0);
    private TIntArrayList roomsVists = new TIntArrayList(0);
    public final TIntArrayList secretRecipes = new TIntArrayList(0);
    public final ArrayList<CalendarRewardClaimed> calendarRewardsClaimed = new ArrayList<>();
    public List<Integer> guilds = new ArrayList();
    public TIntArrayStack votedRooms = new TIntArrayStack();

    private HabboStats(ResultSet resultSet, HabboInfo habboInfo) throws SQLException {
        this.habboInfo = habboInfo;
        this.achievementScore = resultSet.getInt("achievement_score");
        this.respectPointsReceived = resultSet.getInt("respects_received");
        this.respectPointsGiven = resultSet.getInt("respects_given");
        this.petRespectPointsToGive = resultSet.getInt("daily_pet_respect_points");
        this.respectPointsToGive = resultSet.getInt("daily_respect_points");
        this.blockFollowing = resultSet.getString("block_following").equals("1");
        this.blockFriendRequests = resultSet.getString("block_friendrequests").equals("1");
        this.blockRoomInvites = resultSet.getString("block_roominvites").equals("1");
        this.preferOldChat = resultSet.getString("old_chat").equals("1");
        this.blockCameraFollow = resultSet.getString("block_camera_follow").equals("1");
        this.guild = resultSet.getInt("guild_id");
        this.tags = resultSet.getString("tags").split(";");
        this.allowTrade = resultSet.getString("can_trade").equals("1");
        this.clubExpireTimestamp = resultSet.getInt("club_expire_timestamp");
        this.loginStreak = resultSet.getInt("login_streak");
        this.rentedItemId = resultSet.getInt("rent_space_id");
        this.rentedTimeEnd = resultSet.getInt("rent_space_endtime");
        this.volumeSystem = resultSet.getInt("volume_system");
        this.volumeFurni = resultSet.getInt("volume_furni");
        this.volumeTrax = resultSet.getInt("volume_trax");
        this.chatColor = RoomChatMessageBubbles.getBubble(resultSet.getInt("chat_color"));
        this.hofPoints = resultSet.getInt("hof_points");
        this.blockStaffAlerts = resultSet.getString("block_alerts").equals("1");
        this.citizenshipLevel = resultSet.getInt("talent_track_citizenship_level");
        this.helpersLevel = resultSet.getInt("talent_track_helpers_level");
        this.ignoreBots = resultSet.getString("ignore_bots").equalsIgnoreCase("1");
        this.ignorePets = resultSet.getString("ignore_pets").equalsIgnoreCase("1");
        this.nux = resultSet.getString("nux").equals("1");
        this.muteEndTime = resultSet.getInt("mute_end_timestamp");
        this.allowNameChange = resultSet.getString("allow_name_change").equalsIgnoreCase("1");
        this.perkTrade = resultSet.getString("perk_trade").equalsIgnoreCase("1");
        this.forumPostsCount = resultSet.getInt("forums_post_count");
        this.uiFlags = resultSet.getInt("ui_flags");
        this.hasGottenDefaultSavedSearches = resultSet.getInt("has_gotten_default_saved_searches") == 1;
        this.maxFriends = resultSet.getInt("max_friends");
        this.maxRooms = resultSet.getInt("max_rooms");
        this.lastHCPayday = resultSet.getInt("last_hc_payday");
        this.hcGiftsClaimed = resultSet.getInt("hc_gifts_claimed");
        this.nuxReward = this.nux;
        this.subscriptions = Emulator.getGameEnvironment().getSubscriptionManager().getSubscriptionsForUser(this.habboInfo.getId());
        PreparedStatement preparedStatementPrepareStatement = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM user_window_settings WHERE user_id = ? LIMIT 1");
        try {
            preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                if (resultSetExecuteQuery.next()) {
                    this.navigatorWindowSettings = new HabboNavigatorWindowSettings(resultSetExecuteQuery);
                } else {
                    PreparedStatement preparedStatementPrepareStatement2 = preparedStatementPrepareStatement.getConnection().prepareStatement("INSERT INTO user_window_settings (user_id) VALUES (?)");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, this.habboInfo.getId());
                        preparedStatementPrepareStatement2.executeUpdate();
                        if (preparedStatementPrepareStatement2 != null) {
                            preparedStatementPrepareStatement2.close();
                        }
                        this.navigatorWindowSettings = new HabboNavigatorWindowSettings(habboInfo.getId());
                    } finally {
                        if (preparedStatementPrepareStatement2 != null) {
                            try {
                                preparedStatementPrepareStatement2.close();
                            } catch (Throwable th) {
                                th.addSuppressed(th);
                            }
                        }
                    }
                }
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                PreparedStatement preparedStatementPrepareStatement3 = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM users_navigator_settings WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement3.setInt(1, this.habboInfo.getId());
                    ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement3.executeQuery();
                    while (resultSetExecuteQuery2.next()) {
                        try {
                            this.navigatorWindowSettings.addDisplayMode(resultSetExecuteQuery2.getString("caption"), new HabboNavigatorPersonalDisplayMode(resultSetExecuteQuery2));
                        } catch (Throwable th2) {
                            if (resultSetExecuteQuery2 != null) {
                                try {
                                    resultSetExecuteQuery2.close();
                                } catch (Throwable th3) {
                                    th2.addSuppressed(th3);
                                }
                            }
                            throw th2;
                        }
                    }
                    if (resultSetExecuteQuery2 != null) {
                        resultSetExecuteQuery2.close();
                    }
                    if (preparedStatementPrepareStatement3 != null) {
                        preparedStatementPrepareStatement3.close();
                    }
                    PreparedStatement preparedStatementPrepareStatement4 = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM users_favorite_rooms WHERE user_id = ?");
                    try {
                        preparedStatementPrepareStatement4.setInt(1, this.habboInfo.getId());
                        ResultSet resultSetExecuteQuery3 = preparedStatementPrepareStatement4.executeQuery();
                        while (resultSetExecuteQuery3.next()) {
                            try {
                                this.favoriteRooms.add(resultSetExecuteQuery3.getInt("room_id"));
                            } catch (Throwable th4) {
                                if (resultSetExecuteQuery3 != null) {
                                    try {
                                        resultSetExecuteQuery3.close();
                                    } catch (Throwable th5) {
                                        th4.addSuppressed(th5);
                                    }
                                }
                                throw th4;
                            }
                        }
                        if (resultSetExecuteQuery3 != null) {
                            resultSetExecuteQuery3.close();
                        }
                        if (preparedStatementPrepareStatement4 != null) {
                            preparedStatementPrepareStatement4.close();
                        }
                        PreparedStatement preparedStatementPrepareStatement5 = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM users_recipes WHERE user_id = ?");
                        try {
                            preparedStatementPrepareStatement5.setInt(1, this.habboInfo.getId());
                            ResultSet resultSetExecuteQuery4 = preparedStatementPrepareStatement5.executeQuery();
                            while (resultSetExecuteQuery4.next()) {
                                try {
                                    this.secretRecipes.add(resultSetExecuteQuery4.getInt("recipe"));
                                } catch (Throwable th6) {
                                    if (resultSetExecuteQuery4 != null) {
                                        try {
                                            resultSetExecuteQuery4.close();
                                        } catch (Throwable th7) {
                                            th6.addSuppressed(th7);
                                        }
                                    }
                                    throw th6;
                                }
                            }
                            if (resultSetExecuteQuery4 != null) {
                                resultSetExecuteQuery4.close();
                            }
                            if (preparedStatementPrepareStatement5 != null) {
                                preparedStatementPrepareStatement5.close();
                            }
                            PreparedStatement preparedStatementPrepareStatement6 = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM calendar_rewards_claimed WHERE user_id = ?");
                            try {
                                preparedStatementPrepareStatement6.setInt(1, this.habboInfo.getId());
                                ResultSet resultSetExecuteQuery5 = preparedStatementPrepareStatement6.executeQuery();
                                while (resultSetExecuteQuery5.next()) {
                                    try {
                                        this.calendarRewardsClaimed.add(new CalendarRewardClaimed(resultSetExecuteQuery5));
                                    } catch (Throwable th8) {
                                        if (resultSetExecuteQuery5 != null) {
                                            try {
                                                resultSetExecuteQuery5.close();
                                            } catch (Throwable th9) {
                                                th8.addSuppressed(th9);
                                            }
                                        }
                                        throw th8;
                                    }
                                }
                                if (resultSetExecuteQuery5 != null) {
                                    resultSetExecuteQuery5.close();
                                }
                                if (preparedStatementPrepareStatement6 != null) {
                                    preparedStatementPrepareStatement6.close();
                                }
                                PreparedStatement preparedStatementPrepareStatement7 = resultSet.getStatement().getConnection().prepareStatement("SELECT catalog_item_id, timestamp FROM catalog_items_limited WHERE user_id = ? AND timestamp > ?");
                                try {
                                    preparedStatementPrepareStatement7.setInt(1, this.habboInfo.getId());
                                    preparedStatementPrepareStatement7.setInt(2, Emulator.getIntUnixTimestamp() - 86400);
                                    ResultSet resultSetExecuteQuery6 = preparedStatementPrepareStatement7.executeQuery();
                                    while (resultSetExecuteQuery6.next()) {
                                        try {
                                            addLtdLog(resultSetExecuteQuery6.getInt("catalog_item_id"), resultSetExecuteQuery6.getInt("timestamp"));
                                        } catch (Throwable th10) {
                                            if (resultSetExecuteQuery6 != null) {
                                                try {
                                                    resultSetExecuteQuery6.close();
                                                } catch (Throwable th11) {
                                                    th10.addSuppressed(th11);
                                                }
                                            }
                                            throw th10;
                                        }
                                    }
                                    if (resultSetExecuteQuery6 != null) {
                                        resultSetExecuteQuery6.close();
                                    }
                                    if (preparedStatementPrepareStatement7 != null) {
                                        preparedStatementPrepareStatement7.close();
                                    }
                                    PreparedStatement preparedStatementPrepareStatement8 = resultSet.getStatement().getConnection().prepareStatement("SELECT target_id FROM users_ignored WHERE user_id = ?");
                                    try {
                                        preparedStatementPrepareStatement8.setInt(1, this.habboInfo.getId());
                                        ResultSet resultSetExecuteQuery7 = preparedStatementPrepareStatement8.executeQuery();
                                        while (resultSetExecuteQuery7.next()) {
                                            try {
                                                this.ignoredUsers.add(resultSetExecuteQuery7.getInt(1));
                                            } catch (Throwable th12) {
                                                if (resultSetExecuteQuery7 != null) {
                                                    try {
                                                        resultSetExecuteQuery7.close();
                                                    } catch (Throwable th13) {
                                                        th12.addSuppressed(th13);
                                                    }
                                                }
                                                throw th12;
                                            }
                                        }
                                        if (resultSetExecuteQuery7 != null) {
                                            resultSetExecuteQuery7.close();
                                        }
                                        if (preparedStatementPrepareStatement8 != null) {
                                            preparedStatementPrepareStatement8.close();
                                        }
                                        preparedStatementPrepareStatement8 = resultSet.getStatement().getConnection().prepareStatement("SELECT * FROM users_target_offer_purchases WHERE user_id = ?");
                                        try {
                                            preparedStatementPrepareStatement8.setInt(1, this.habboInfo.getId());
                                            ResultSet resultSetExecuteQuery8 = preparedStatementPrepareStatement8.executeQuery();
                                            while (resultSetExecuteQuery8.next()) {
                                                try {
                                                    this.offerCache.put(resultSetExecuteQuery8.getInt("offer_id"), new HabboOfferPurchase(resultSetExecuteQuery8));
                                                } catch (Throwable th14) {
                                                    if (resultSetExecuteQuery8 != null) {
                                                        try {
                                                            resultSetExecuteQuery8.close();
                                                        } catch (Throwable th15) {
                                                            th14.addSuppressed(th15);
                                                        }
                                                    }
                                                    throw th14;
                                                }
                                            }
                                            if (resultSetExecuteQuery8 != null) {
                                                resultSetExecuteQuery8.close();
                                            }
                                            if (preparedStatementPrepareStatement8 != null) {
                                                preparedStatementPrepareStatement8.close();
                                            }
                                            preparedStatementPrepareStatement5 = resultSet.getStatement().getConnection().prepareStatement("SELECT DISTINCT room_id FROM room_enter_log WHERE user_id = ?");
                                            try {
                                                preparedStatementPrepareStatement5.setInt(1, this.habboInfo.getId());
                                                ResultSet resultSetExecuteQuery9 = preparedStatementPrepareStatement5.executeQuery();
                                                while (resultSetExecuteQuery9.next()) {
                                                    try {
                                                        this.roomsVists.add(resultSetExecuteQuery9.getInt("room_id"));
                                                    } catch (Throwable th16) {
                                                        if (resultSetExecuteQuery9 != null) {
                                                            try {
                                                                resultSetExecuteQuery9.close();
                                                            } catch (Throwable th17) {
                                                                th16.addSuppressed(th17);
                                                            }
                                                        }
                                                        throw th16;
                                                    }
                                                }
                                                if (resultSetExecuteQuery9 != null) {
                                                    resultSetExecuteQuery9.close();
                                                }
                                                if (preparedStatementPrepareStatement5 != null) {
                                                    preparedStatementPrepareStatement5.close();
                                                }
                                            } finally {
                                            }
                                        } finally {
                                        }
                                    } finally {
                                        if (preparedStatementPrepareStatement8 != null) {
                                            try {
                                                preparedStatementPrepareStatement8.close();
                                            } catch (Throwable th18) {
                                                th.addSuppressed(th18);
                                            }
                                        }
                                    }
                                } finally {
                                    if (preparedStatementPrepareStatement7 != null) {
                                        try {
                                            preparedStatementPrepareStatement7.close();
                                        } catch (Throwable th19) {
                                            th.addSuppressed(th19);
                                        }
                                    }
                                }
                            } finally {
                                if (preparedStatementPrepareStatement6 != null) {
                                    try {
                                        preparedStatementPrepareStatement6.close();
                                    } catch (Throwable th20) {
                                        th.addSuppressed(th20);
                                    }
                                }
                            }
                        } finally {
                            if (preparedStatementPrepareStatement5 != null) {
                                try {
                                    preparedStatementPrepareStatement5.close();
                                } catch (Throwable th21) {
                                    th.addSuppressed(th21);
                                }
                            }
                        }
                    } finally {
                        if (preparedStatementPrepareStatement4 != null) {
                            try {
                                preparedStatementPrepareStatement4.close();
                            } catch (Throwable th22) {
                                th.addSuppressed(th22);
                            }
                        }
                    }
                } finally {
                    if (preparedStatementPrepareStatement3 != null) {
                        try {
                            preparedStatementPrepareStatement3.close();
                        } catch (Throwable th23) {
                            th.addSuppressed(th23);
                        }
                    }
                }
            } catch (Throwable th24) {
                if (resultSetExecuteQuery != null) {
                    try {
                        resultSetExecuteQuery.close();
                    } catch (Throwable th25) {
                        th24.addSuppressed(th25);
                    }
                }
                throw th24;
            }
        } finally {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th26) {
                    th.addSuppressed(th26);
                }
            }
        }
    }

    private static HabboStats createNewStats(HabboInfo habboInfo) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        habboInfo.firstVisit = true;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_settings (user_id) VALUES (?)");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, habboInfo.getId());
            preparedStatementPrepareStatement.executeUpdate();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return load(habboInfo);
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

    public static HabboStats load(HabboInfo habboInfo) {
        Connection connection;
        HabboStats habboStats = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_settings WHERE user_id = ? LIMIT 1");
            try {
                preparedStatementPrepareStatement.setInt(1, habboInfo.getId());
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                try {
                    resultSetExecuteQuery.next();
                    habboStats = resultSetExecuteQuery.getRow() != 0 ? new HabboStats(resultSetExecuteQuery, habboInfo) : createNewStats(habboInfo);
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (habboStats != null) {
                        PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT guild_id FROM guilds_members WHERE user_id = ? AND level_id < 3 LIMIT 100");
                        try {
                            preparedStatementPrepareStatement2.setInt(1, habboInfo.getId());
                            ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement2.executeQuery();
                            int i = 0;
                            while (resultSetExecuteQuery2.next()) {
                                try {
                                    habboStats.guilds.add(Integer.valueOf(resultSetExecuteQuery2.getInt("guild_id")));
                                    i++;
                                } finally {
                                }
                            }
                            if (resultSetExecuteQuery2 != null) {
                                resultSetExecuteQuery2.close();
                            }
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
                            }
                            Collections.sort(habboStats.guilds);
                            PreparedStatement preparedStatementPrepareStatement3 = connection.prepareStatement("SELECT room_id FROM room_votes WHERE user_id = ?");
                            try {
                                preparedStatementPrepareStatement3.setInt(1, habboInfo.getId());
                                resultSetExecuteQuery = preparedStatementPrepareStatement3.executeQuery();
                                while (resultSetExecuteQuery.next()) {
                                    try {
                                        habboStats.votedRooms.push(resultSetExecuteQuery.getInt("room_id"));
                                    } finally {
                                        if (resultSetExecuteQuery != null) {
                                            try {
                                                resultSetExecuteQuery.close();
                                            } catch (Throwable th) {
                                                th.addSuppressed(th);
                                            }
                                        }
                                    }
                                }
                                if (resultSetExecuteQuery != null) {
                                    resultSetExecuteQuery.close();
                                }
                                if (preparedStatementPrepareStatement3 != null) {
                                    preparedStatementPrepareStatement3.close();
                                }
                                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_achievements WHERE user_id = ?");
                                try {
                                    preparedStatementPrepareStatement.setInt(1, habboInfo.getId());
                                    ResultSet resultSetExecuteQuery3 = preparedStatementPrepareStatement.executeQuery();
                                    while (resultSetExecuteQuery3.next()) {
                                        try {
                                            Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(resultSetExecuteQuery3.getString("achievement_name"));
                                            if (achievement != null) {
                                                habboStats.achievementProgress.put(achievement, Integer.valueOf(resultSetExecuteQuery3.getInt("progress")));
                                            }
                                        } finally {
                                            if (resultSetExecuteQuery3 != null) {
                                                try {
                                                    resultSetExecuteQuery3.close();
                                                } catch (Throwable th2) {
                                                    th.addSuppressed(th2);
                                                }
                                            }
                                        }
                                    }
                                    if (resultSetExecuteQuery3 != null) {
                                        resultSetExecuteQuery3.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                } finally {
                                    if (preparedStatementPrepareStatement != null) {
                                        try {
                                            preparedStatementPrepareStatement.close();
                                        } catch (Throwable th3) {
                                            th.addSuppressed(th3);
                                        }
                                    }
                                }
                            } finally {
                                if (preparedStatementPrepareStatement3 != null) {
                                    try {
                                        preparedStatementPrepareStatement3.close();
                                    } catch (Throwable th4) {
                                        th.addSuppressed(th4);
                                    }
                                }
                            }
                        } finally {
                        }
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return habboStats;
                } finally {
                }
            } finally {
            }
        } finally {
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp() - this.lastOnlineTime.getAndUpdate(i -> {
            return Emulator.getIntUnixTimestamp();
        });
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET achievement_score = ?, respects_received = ?, respects_given = ?, daily_respect_points = ?, block_following = ?, block_friendrequests = ?, online_time = online_time + ?, guild_id = ?, daily_pet_respect_points = ?, club_expire_timestamp = ?, login_streak = ?, rent_space_id = ?, rent_space_endtime = ?, volume_system = ?, volume_furni = ?, volume_trax = ?, block_roominvites = ?, old_chat = ?, block_camera_follow = ?, chat_color = ?, hof_points = ?, block_alerts = ?, talent_track_citizenship_level = ?, talent_track_helpers_level = ?, ignore_bots = ?, ignore_pets = ?, nux = ?, mute_end_timestamp = ?, allow_name_change = ?, perk_trade = ?, can_trade = ?, `forums_post_count` = ?, ui_flags = ?, has_gotten_default_saved_searches = ?, max_friends = ?, max_rooms = ?, last_hc_payday = ?, hc_gifts_claimed = ? WHERE user_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.achievementScore);
                    preparedStatementPrepareStatement.setInt(2, this.respectPointsReceived);
                    preparedStatementPrepareStatement.setInt(3, this.respectPointsGiven);
                    preparedStatementPrepareStatement.setInt(4, this.respectPointsToGive);
                    preparedStatementPrepareStatement.setString(5, this.blockFollowing ? "1" : "0");
                    preparedStatementPrepareStatement.setString(6, this.blockFriendRequests ? "1" : "0");
                    preparedStatementPrepareStatement.setInt(7, intUnixTimestamp);
                    preparedStatementPrepareStatement.setInt(8, this.guild);
                    preparedStatementPrepareStatement.setInt(9, this.petRespectPointsToGive);
                    preparedStatementPrepareStatement.setInt(10, this.clubExpireTimestamp);
                    preparedStatementPrepareStatement.setInt(11, this.loginStreak);
                    preparedStatementPrepareStatement.setInt(12, this.rentedItemId);
                    preparedStatementPrepareStatement.setInt(13, this.rentedTimeEnd);
                    preparedStatementPrepareStatement.setInt(14, this.volumeSystem);
                    preparedStatementPrepareStatement.setInt(15, this.volumeFurni);
                    preparedStatementPrepareStatement.setInt(16, this.volumeTrax);
                    preparedStatementPrepareStatement.setString(17, this.blockRoomInvites ? "1" : "0");
                    preparedStatementPrepareStatement.setString(18, this.preferOldChat ? "1" : "0");
                    preparedStatementPrepareStatement.setString(19, this.blockCameraFollow ? "1" : "0");
                    preparedStatementPrepareStatement.setInt(20, this.chatColor.getType());
                    preparedStatementPrepareStatement.setInt(21, this.hofPoints);
                    preparedStatementPrepareStatement.setString(22, this.blockStaffAlerts ? "1" : "0");
                    preparedStatementPrepareStatement.setInt(23, this.citizenshipLevel);
                    preparedStatementPrepareStatement.setInt(24, this.helpersLevel);
                    preparedStatementPrepareStatement.setString(25, this.ignoreBots ? "1" : "0");
                    preparedStatementPrepareStatement.setString(26, this.ignorePets ? "1" : "0");
                    preparedStatementPrepareStatement.setString(27, this.nux ? "1" : "0");
                    preparedStatementPrepareStatement.setInt(28, this.muteEndTime);
                    preparedStatementPrepareStatement.setString(29, this.allowNameChange ? "1" : "0");
                    preparedStatementPrepareStatement.setString(30, this.perkTrade ? "1" : "0");
                    preparedStatementPrepareStatement.setString(31, this.allowTrade ? "1" : "0");
                    preparedStatementPrepareStatement.setInt(32, this.forumPostsCount);
                    preparedStatementPrepareStatement.setInt(33, this.uiFlags);
                    preparedStatementPrepareStatement.setInt(34, this.hasGottenDefaultSavedSearches ? 1 : 0);
                    preparedStatementPrepareStatement.setInt(35, this.maxFriends);
                    preparedStatementPrepareStatement.setInt(36, this.maxRooms);
                    preparedStatementPrepareStatement.setInt(37, this.lastHCPayday);
                    preparedStatementPrepareStatement.setInt(38, this.hcGiftsClaimed);
                    preparedStatementPrepareStatement.setInt(39, this.habboInfo.getId());
                    preparedStatementPrepareStatement.executeUpdate();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE user_window_settings SET x = ?, y = ?, width = ?, height = ?, open_searches = ? WHERE user_id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, this.navigatorWindowSettings.x);
                        preparedStatementPrepareStatement2.setInt(2, this.navigatorWindowSettings.y);
                        preparedStatementPrepareStatement2.setInt(3, this.navigatorWindowSettings.width);
                        preparedStatementPrepareStatement2.setInt(4, this.navigatorWindowSettings.height);
                        preparedStatementPrepareStatement2.setString(5, this.navigatorWindowSettings.openSearches ? "1" : "0");
                        preparedStatementPrepareStatement2.setInt(6, this.habboInfo.getId());
                        preparedStatementPrepareStatement2.executeUpdate();
                        if (preparedStatementPrepareStatement2 != null) {
                            preparedStatementPrepareStatement2.close();
                        }
                        if (!this.offerCache.isEmpty()) {
                            preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE users_target_offer_purchases SET state = ?, amount = ?, last_purchase = ? WHERE user_id = ? AND offer_id = ?");
                            try {
                                for (HabboOfferPurchase habboOfferPurchase : this.offerCache.valueCollection()) {
                                    if (habboOfferPurchase.needsUpdate()) {
                                        preparedStatementPrepareStatement2.setInt(1, habboOfferPurchase.getState());
                                        preparedStatementPrepareStatement2.setInt(2, habboOfferPurchase.getAmount());
                                        preparedStatementPrepareStatement2.setInt(3, habboOfferPurchase.getLastPurchaseTimestamp());
                                        preparedStatementPrepareStatement2.setInt(4, this.habboInfo.getId());
                                        preparedStatementPrepareStatement2.setInt(5, habboOfferPurchase.getOfferId());
                                        preparedStatementPrepareStatement2.execute();
                                    }
                                }
                                if (preparedStatementPrepareStatement2 != null) {
                                    preparedStatementPrepareStatement2.close();
                                }
                            } finally {
                            }
                        }
                        this.navigatorWindowSettings.save(connection);
                        if (connection != null) {
                            connection.close();
                        }
                    } finally {
                    }
                } finally {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void dispose() {
        run();
        this.habboInfo = null;
        this.recentPurchases.clear();
    }

    public void addGuild(int i) {
        if (this.guilds.contains(Integer.valueOf(i))) {
            return;
        }
        this.guilds.add(Integer.valueOf(i));
    }

    public void removeGuild(int i) {
        this.guilds.remove(Integer.valueOf(i));
    }

    public boolean hasGuild(int i) {
        Iterator<Integer> it = this.guilds.iterator();
        while (it.hasNext()) {
            if (it.next().intValue() == i) {
                return true;
            }
        }
        return false;
    }

    public int getAchievementScore() {
        return this.achievementScore;
    }

    public void addAchievementScore(int i) {
        this.achievementScore += i;
    }

    public int getAchievementProgress(Achievement achievement) {
        if (this.achievementProgress.containsKey(achievement)) {
            return ((Integer) this.achievementProgress.get(achievement)).intValue();
        }
        return -1;
    }

    public void setProgress(Achievement achievement, int i) {
        this.achievementProgress.put(achievement, Integer.valueOf(i));
    }

    public int getRentedTimeEnd() {
        return this.rentedTimeEnd;
    }

    public void setRentedTimeEnd(int i) {
        this.rentedTimeEnd = i;
    }

    public int getRentedItemId() {
        return this.rentedItemId;
    }

    public void setRentedItemId(int i) {
        this.rentedItemId = i;
    }

    public boolean isRentingSpace() {
        return this.rentedTimeEnd >= Emulator.getIntUnixTimestamp();
    }

    public Subscription getSubscription(String str) {
        TObjectHashIterator it = this.subscriptions.iterator();
        while (it.hasNext()) {
            Subscription subscription = (Subscription) it.next();
            if (subscription.getSubscriptionType().equalsIgnoreCase(str) && subscription.isActive() && subscription.getRemaining() > 0) {
                return subscription;
            }
        }
        return null;
    }

    public boolean hasSubscription(String str) {
        return getSubscription(str) != null;
    }

    public int getSubscriptionExpireTimestamp(String str) {
        Subscription subscription = getSubscription(str);
        if (subscription == null) {
            return 0;
        }
        return subscription.getTimestampEnd();
    }

    public Subscription createSubscription(String str, int i) {
        Subscription subscription = getSubscription(str);
        if (subscription != null) {
            if (!((UserSubscriptionExtendedEvent) Emulator.getPluginManager().fireEvent(new UserSubscriptionExtendedEvent(this.habboInfo.getId(), subscription, i))).isCancelled()) {
                subscription.addDuration(i);
                subscription.onExtended(i);
            }
            return subscription;
        }
        if (((UserSubscriptionCreatedEvent) Emulator.getPluginManager().fireEvent(new UserSubscriptionCreatedEvent(this.habboInfo.getId(), str, i))).isCancelled()) {
            return null;
        }
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `users_subscriptions` (`user_id`, `subscription_type`, `timestamp_start`, `duration`, `active`) VALUES (?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
                    preparedStatementPrepareStatement.setString(2, str);
                    preparedStatementPrepareStatement.setInt(3, intUnixTimestamp);
                    preparedStatementPrepareStatement.setInt(4, i);
                    preparedStatementPrepareStatement.setInt(5, 1);
                    preparedStatementPrepareStatement.execute();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            try {
                                Constructor<? extends Subscription> constructor = Emulator.getGameEnvironment().getSubscriptionManager().getSubscriptionClass(str).getConstructor(Integer.class, Integer.class, String.class, Integer.class, Integer.class, Boolean.class);
                                constructor.setAccessible(true);
                                Subscription subscriptionNewInstance = constructor.newInstance(Integer.valueOf(generatedKeys.getInt(1)), Integer.valueOf(this.habboInfo.getId()), str, Integer.valueOf(intUnixTimestamp), Integer.valueOf(i), true);
                                this.subscriptions.add(subscriptionNewInstance);
                                subscriptionNewInstance.onCreated();
                                if (generatedKeys != null) {
                                    generatedKeys.close();
                                }
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                if (connection != null) {
                                    connection.close();
                                }
                                return subscriptionNewInstance;
                            } catch (Exception e) {
                                LOGGER.error("Caught exception", e);
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
                        return null;
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
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
            return null;
        }
    }

    public int getClubExpireTimestamp() {
        return getSubscriptionExpireTimestamp(Subscription.HABBO_CLUB);
    }

    public void setClubExpireTimestamp(int i) {
        Subscription subscription = getSubscription(Subscription.HABBO_CLUB);
        int intUnixTimestamp = i - Emulator.getIntUnixTimestamp();
        if (subscription != null) {
            intUnixTimestamp = i - subscription.getTimestampStart();
        }
        if (intUnixTimestamp > 0) {
            createSubscription(Subscription.HABBO_CLUB, intUnixTimestamp);
        }
    }

    public boolean hasActiveClub() {
        return hasSubscription(Subscription.HABBO_CLUB);
    }

    public int getPastTimeAsClub() {
        int duration = 0;
        TObjectHashIterator it = this.subscriptions.iterator();
        while (it.hasNext()) {
            Subscription subscription = (Subscription) it.next();
            if (subscription.getSubscriptionType().equalsIgnoreCase(Subscription.HABBO_CLUB)) {
                duration += subscription.getDuration() - Math.max(subscription.getRemaining(), 0);
            }
        }
        return duration;
    }

    public int getTimeTillNextClubGift() {
        int pastTimeAsClub = getPastTimeAsClub();
        return (((int) Math.ceil(((double) pastTimeAsClub) / 2678400.0d)) * 2678400) - pastTimeAsClub;
    }

    public int getRemainingClubGifts() {
        return ((int) Math.ceil(((double) getPastTimeAsClub()) / 2678400.0d)) - this.hcGiftsClaimed;
    }

    public THashMap<Achievement, Integer> getAchievementProgress() {
        return this.achievementProgress;
    }

    public THashMap<Achievement, Integer> getAchievementCache() {
        return this.achievementCache;
    }

    public void addPurchase(CatalogItem catalogItem) {
        if (this.recentPurchases.containsKey(Integer.valueOf(catalogItem.getId()))) {
            return;
        }
        this.recentPurchases.put(Integer.valueOf(catalogItem.getId()), catalogItem);
    }

    public THashMap<Integer, CatalogItem> getRecentPurchases() {
        return this.recentPurchases;
    }

    public void disposeRecentPurchases() {
        this.recentPurchases.clear();
    }

    public boolean addFavoriteRoom(int i) {
        if (this.favoriteRooms.contains(i) || Emulator.getConfig().getInt("hotel.rooms.max.favorite") <= this.favoriteRooms.size()) {
            return false;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_favorite_rooms (user_id, room_id) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
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
        this.favoriteRooms.add(i);
        return true;
    }

    public void removeFavoriteRoom(int i) {
        if (this.favoriteRooms.remove(i)) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_favorite_rooms WHERE user_id = ? AND room_id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
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
    }

    public boolean hasFavoriteRoom(int i) {
        return this.favoriteRooms.contains(i);
    }

    public boolean visitedRoom(int i) {
        return this.roomsVists.contains(i);
    }

    public void addVisitRoom(int i) {
        this.roomsVists.add(i);
    }

    public TIntArrayList getFavoriteRooms() {
        return this.favoriteRooms;
    }

    public boolean hasRecipe(int i) {
        return this.secretRecipes.contains(i);
    }

    public boolean addRecipe(int i) {
        if (this.secretRecipes.contains(i)) {
            return false;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_recipes (user_id, recipe) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
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
        this.secretRecipes.add(i);
        return true;
    }

    public int talentTrackLevel(TalentTrackType talentTrackType) {
        if (talentTrackType == TalentTrackType.CITIZENSHIP) {
            return this.citizenshipLevel;
        }
        if (talentTrackType == TalentTrackType.HELPER) {
            return this.helpersLevel;
        }
        return -1;
    }

    public void setTalentLevel(TalentTrackType talentTrackType, int i) {
        if (talentTrackType == TalentTrackType.CITIZENSHIP) {
            this.citizenshipLevel = i;
        } else if (talentTrackType == TalentTrackType.HELPER) {
            this.helpersLevel = i;
        }
    }

    public int getMuteEndTime() {
        return this.muteEndTime;
    }

    public int addMuteTime(int i) {
        if (remainingMuteTime() == 0) {
            this.muteEndTime = Emulator.getIntUnixTimestamp();
        }
        this.mutedBubbleTracker = true;
        this.muteEndTime += i;
        return remainingMuteTime();
    }

    public int remainingMuteTime() {
        return Math.max(0, this.muteEndTime - Emulator.getIntUnixTimestamp());
    }

    public boolean allowTalk() {
        return remainingMuteTime() == 0;
    }

    public void unMute() {
        this.muteEndTime = 0;
        this.mutedBubbleTracker = false;
    }

    public void addLtdLog(int i, int i2) {
        if (!this.ltdPurchaseLog.containsKey(Integer.valueOf(i))) {
            this.ltdPurchaseLog.put(Integer.valueOf(i), new ArrayList(1));
        }
        ((List) this.ltdPurchaseLog.get(Integer.valueOf(i))).add(Integer.valueOf(i2));
    }

    public int totalLtds() {
        int size = 0;
        Iterator it = this.ltdPurchaseLog.entrySet().iterator();
        while (it.hasNext()) {
            size += ((List) ((Map.Entry) it.next()).getValue()).size();
        }
        return size;
    }

    public int totalLtds(int i) {
        if (this.ltdPurchaseLog.containsKey(Integer.valueOf(i))) {
            return ((List) this.ltdPurchaseLog.get(Integer.valueOf(i))).size();
        }
        return 0;
    }

    public boolean ignoreUser(GameClient gameClient, int i) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
        if (!Emulator.getConfig().getBoolean("hotel.allow.ignore.staffs")) {
            if (habbo.getHabboInfo().getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.error.ignore_higher_rank"), RoomChatMessageBubbles.ALERT);
                return false;
            }
        }
        if (userIgnored(i)) {
            return true;
        }
        this.ignoredUsers.add(i);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_ignored (user_id, target_id) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return true;
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
            return true;
        }
    }

    public void unignoreUser(int i) {
        if (userIgnored(i)) {
            this.ignoredUsers.remove(i);
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_ignored WHERE user_id = ? AND target_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
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
    }

    public boolean userIgnored(int i) {
        return this.ignoredUsers.contains(i);
    }

    public boolean allowTrade() {
        return (AchievementManager.TALENTTRACK_ENABLED && RoomTrade.TRADING_REQUIRES_PERK) ? this.perkTrade && this.allowTrade : this.allowTrade;
    }

    public void setAllowTrade(boolean z) {
        this.allowTrade = z;
    }

    public HabboOfferPurchase getHabboOfferPurchase(int i) {
        return (HabboOfferPurchase) this.offerCache.get(i);
    }

    public void addHabboOfferPurchase(HabboOfferPurchase habboOfferPurchase) {
        this.offerCache.put(habboOfferPurchase.getOfferId(), habboOfferPurchase);
    }
}
