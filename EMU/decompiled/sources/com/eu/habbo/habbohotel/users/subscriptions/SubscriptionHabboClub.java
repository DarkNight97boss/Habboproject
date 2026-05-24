package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.Database;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.outgoing.catalog.ClubCenterDataComposer;
import com.eu.habbo.messages.outgoing.generic.PickMonthlyClubGiftNotificationComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.eu.habbo.messages.outgoing.users.UserClubComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/subscriptions/SubscriptionHabboClub.class */
public class SubscriptionHabboClub extends Subscription {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionHabboClub.class);
    public static boolean HC_PAYDAY_ENABLED = false;
    public static int HC_PAYDAY_NEXT_DATE = Integer.MAX_VALUE;
    public static String HC_PAYDAY_INTERVAL = Emulator.PREVIEW;
    public static String HC_PAYDAY_QUERY = Emulator.PREVIEW;
    public static TreeMap<Integer, Integer> HC_PAYDAY_STREAK = new TreeMap<>();
    public static String HC_PAYDAY_CURRENCY = Emulator.PREVIEW;
    public static Double HC_PAYDAY_KICKBACK_PERCENTAGE = Double.valueOf(0.1d);
    public static String ACHIEVEMENT_NAME = Emulator.PREVIEW;
    public static boolean DISCOUNT_ENABLED = false;
    public static int DISCOUNT_DAYS_BEFORE_END = 7;
    public static boolean HC_PAYDAY_COINSSPENT_RESET_ON_EXPIRE = false;
    public static boolean isExecuting = false;

    public SubscriptionHabboClub(Integer num, Integer num2, String str, Integer num3, Integer num4, Boolean bool) {
        super(num, num2, str, num3, num4, bool);
    }

    @Override // com.eu.habbo.habbohotel.users.subscriptions.Subscription
    public void onCreated() {
        super.onCreated();
        HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(getUserId());
        HabboStats habboStats = habboInfo.getHabboStats();
        habboStats.maxFriends = Messenger.MAXIMUM_FRIENDS_HC;
        habboStats.maxRooms = RoomManager.MAXIMUM_ROOMS_HC;
        habboStats.lastHCPayday = HC_PAYDAY_COINSSPENT_RESET_ON_EXPIRE ? Emulator.getIntUnixTimestamp() : HC_PAYDAY_NEXT_DATE - Emulator.timeStringToSeconds(HC_PAYDAY_INTERVAL);
        Emulator.getThreading().run(habboStats);
        progressAchievement(habboInfo);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId());
        if (habbo == null || habbo.getClient() == null) {
            return;
        }
        if (habbo.getHabboStats().getRemainingClubGifts() > 0) {
            habbo.getClient().sendResponse(new PickMonthlyClubGiftNotificationComposer(habbo.getHabboStats().getRemainingClubGifts()));
        }
        if (Emulator.getIntUnixTimestamp() - habbo.getHabboStats().hcMessageLastModified < 60) {
            Emulator.getThreading().run(() -> {
                habbo.getClient().sendResponse(new UserClubComposer(habbo));
                habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
            }, Emulator.getIntUnixTimestamp() - habbo.getHabboStats().hcMessageLastModified);
        } else {
            habbo.getClient().sendResponse(new UserClubComposer(habbo, Subscription.HABBO_CLUB, UserClubComposer.RESPONSE_TYPE_NORMAL));
            habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.subscriptions.Subscription
    public void addDuration(int i) {
        Habbo habbo;
        super.addDuration(i);
        if (i >= 0 || (habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId())) == null || habbo.getClient() == null) {
            return;
        }
        habbo.getClient().sendResponse(new UserClubComposer(habbo, Subscription.HABBO_CLUB, UserClubComposer.RESPONSE_TYPE_NORMAL));
        habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
    }

    @Override // com.eu.habbo.habbohotel.users.subscriptions.Subscription
    public void onExtended(int i) {
        super.onExtended(i);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId());
        if (habbo == null || habbo.getClient() == null) {
            return;
        }
        habbo.getClient().sendResponse(new UserClubComposer(habbo, Subscription.HABBO_CLUB, UserClubComposer.RESPONSE_TYPE_NORMAL));
        habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
    }

    @Override // com.eu.habbo.habbohotel.users.subscriptions.Subscription
    public void onExpired() {
        super.onExpired();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId());
        HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(getUserId());
        HabboStats habboStats = habboInfo.getHabboStats();
        habboStats.maxFriends = Messenger.MAXIMUM_FRIENDS;
        habboStats.maxRooms = RoomManager.MAXIMUM_ROOMS_USER;
        Emulator.getThreading().run(habboStats);
        if (habbo != null && ClothingValidationManager.VALIDATE_ON_HC_EXPIRE) {
            habboInfo.setLook(ClothingValidationManager.validateLook(habbo, habboInfo.getLook(), habboInfo.getGender().name()));
            Emulator.getThreading().run(habbo.getHabboInfo());
            if (habbo.getClient() != null) {
                habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
            }
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            }
        }
        if (habbo == null || habbo.getClient() == null) {
            return;
        }
        habbo.getClient().sendResponse(new UserClubComposer(habbo, Subscription.HABBO_CLUB, UserClubComposer.RESPONSE_TYPE_NORMAL));
        habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
    }

    public static ClubCenterDataComposer calculatePayday(HabboInfo habboInfo) {
        Subscription subscription = null;
        Subscription subscription2 = null;
        int iFloor = 0;
        int i = 0;
        int iIntValue = 0;
        int iFloor2 = 0;
        int intUnixTimestamp = 0;
        TObjectHashIterator it = habboInfo.getHabboStats().subscriptions.iterator();
        while (it.hasNext()) {
            Subscription subscription3 = (Subscription) it.next();
            if (subscription3.getSubscriptionType().equalsIgnoreCase(Subscription.HABBO_CLUB)) {
                if (subscription2 == null || subscription3.getTimestampStart() < subscription2.getTimestampStart()) {
                    subscription2 = subscription3;
                }
                if (subscription3.isActive()) {
                    subscription = subscription3;
                }
            }
        }
        if (HC_PAYDAY_ENABLED && subscription != null) {
            iFloor = (int) Math.floor(((double) (Emulator.getIntUnixTimestamp() - subscription.getTimestampStart())) / 86400.0d);
            if (iFloor < 1) {
                iFloor = 0;
            }
            for (Map.Entry<Integer, Integer> entry : HC_PAYDAY_STREAK.entrySet()) {
                if (iFloor >= entry.getKey().intValue() && entry.getValue().intValue() > iIntValue) {
                    iIntValue = entry.getValue().intValue();
                }
            }
            THashMap tHashMap = new THashMap();
            tHashMap.put("@user_id", Integer.valueOf(habboInfo.getId()));
            tHashMap.put("@timestamp_start", Integer.valueOf(habboInfo.getHabboStats().lastHCPayday));
            tHashMap.put("@timestamp_end", Integer.valueOf(HC_PAYDAY_NEXT_DATE));
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPreparedStatementWithParams = Database.preparedStatementWithParams(connection, HC_PAYDAY_QUERY, tHashMap);
                    try {
                        ResultSet resultSetExecuteQuery = preparedStatementPreparedStatementWithParams.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            try {
                                i = resultSetExecuteQuery.getInt("amount_spent");
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
                        if (preparedStatementPreparedStatementWithParams != null) {
                            preparedStatementPreparedStatementWithParams.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th3) {
                        if (preparedStatementPreparedStatementWithParams != null) {
                            try {
                                preparedStatementPreparedStatementWithParams.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } finally {
                }
            } catch (SQLException e) {
                SubscriptionManager.LOGGER.error("Caught SQL exception", e);
            }
            iFloor2 = (int) Math.floor(((double) i) * HC_PAYDAY_KICKBACK_PERCENTAGE.doubleValue());
            intUnixTimestamp = (HC_PAYDAY_NEXT_DATE - Emulator.getIntUnixTimestamp()) / 60;
        }
        return new ClubCenterDataComposer(iFloor, subscription2 != null ? new SimpleDateFormat("dd-MM-yyyy").format(new Date(((long) subscription2.getTimestampStart()) * 1000)) : Emulator.PREVIEW, HC_PAYDAY_KICKBACK_PERCENTAGE.doubleValue(), 0, 0, i, iIntValue, iFloor2, intUnixTimestamp);
    }

    public static void executePayDay() {
        Connection connection;
        isExecuting = true;
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            SubscriptionManager.LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT user_id FROM `users_subscriptions` WHERE subscription_type = 'HABBO_CLUB' AND `active` = 1 AND `timestamp_start` < ? AND (`timestamp_start` + `duration`) > ? GROUP BY user_id");
            try {
                preparedStatementPrepareStatement.setInt(1, intUnixTimestamp);
                preparedStatementPrepareStatement.setInt(2, intUnixTimestamp);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        try {
                            int i = resultSetExecuteQuery.getInt("user_id");
                            HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(i);
                            HabboStats habboStats = habboInfo.getHabboStats();
                            ClubCenterDataComposer clubCenterDataComposerCalculatePayday = calculatePayday(habboInfo);
                            int i2 = clubCenterDataComposerCalculatePayday.creditRewardForMonthlySpent + clubCenterDataComposerCalculatePayday.creditRewardForStreakBonus;
                            if (i2 > 0) {
                                Emulator.getThreading().run(new HcPayDayLogEntry(intUnixTimestamp, i, clubCenterDataComposerCalculatePayday.currentHcStreak, clubCenterDataComposerCalculatePayday.totalCreditsSpent, clubCenterDataComposerCalculatePayday.creditRewardForMonthlySpent, clubCenterDataComposerCalculatePayday.creditRewardForStreakBonus, i2, HC_PAYDAY_CURRENCY, claimPayDay(Emulator.getGameEnvironment().getHabboManager().getHabbo(i), i2, HC_PAYDAY_CURRENCY)));
                            }
                            habboStats.lastHCPayday = intUnixTimestamp;
                            Emulator.getThreading().run(habboStats);
                        } catch (Exception e2) {
                            SubscriptionManager.LOGGER.error("Exception processing HC payday for user #" + resultSetExecuteQuery.getInt("user_id"), e2);
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
                Date dateModifyDate = Emulator.modifyDate(new Date(((long) HC_PAYDAY_NEXT_DATE) * 1000), HC_PAYDAY_INTERVAL);
                HC_PAYDAY_NEXT_DATE = (int) (dateModifyDate.getTime() / 1000);
                PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE `emulator_settings` SET `value` = ? WHERE `key` = ?");
                try {
                    preparedStatementPrepareStatement2.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateModifyDate));
                    preparedStatementPrepareStatement2.setString(2, "subscriptions.hc.payday.next_date");
                    preparedStatementPrepareStatement2.execute();
                    if (preparedStatementPrepareStatement2 != null) {
                        preparedStatementPrepareStatement2.close();
                    }
                    preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE users_settings SET last_hc_payday = ? WHERE user_id IN (SELECT user_id FROM `users_subscriptions` WHERE subscription_type = 'HABBO_CLUB' AND `active` = 1 AND `timestamp_start` < ? AND (`timestamp_start` + `duration`) > ? GROUP BY user_id)");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, intUnixTimestamp);
                        preparedStatementPrepareStatement2.setInt(2, intUnixTimestamp);
                        preparedStatementPrepareStatement2.setInt(3, intUnixTimestamp);
                        preparedStatementPrepareStatement2.execute();
                        if (preparedStatementPrepareStatement2 != null) {
                            preparedStatementPrepareStatement2.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        isExecuting = false;
                    } finally {
                    }
                } finally {
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
    }

    public static void processUnclaimed(Habbo habbo) {
        progressAchievement(habbo.getHabboInfo());
        if (habbo.getHabboStats().getRemainingClubGifts() > 0) {
            habbo.getClient().sendResponse(new PickMonthlyClubGiftNotificationComposer(habbo.getHabboStats().getRemainingClubGifts()));
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM `logs_hc_payday` WHERE user_id = ? AND claimed = 0");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            try {
                                int i = resultSetExecuteQuery.getInt("id");
                                resultSetExecuteQuery.getInt("user_id");
                                if (claimPayDay(habbo, resultSetExecuteQuery.getInt("total_payout"), resultSetExecuteQuery.getString("currency"))) {
                                    preparedStatementPrepareStatement = connection.prepareStatement("UPDATE logs_hc_payday SET claimed = 1 WHERE id = ?");
                                    try {
                                        preparedStatementPrepareStatement.setInt(1, i);
                                        preparedStatementPrepareStatement.execute();
                                        if (preparedStatementPrepareStatement != null) {
                                            preparedStatementPrepareStatement.close();
                                        }
                                    } catch (Throwable th) {
                                        throw th;
                                    }
                                }
                            } catch (Exception e) {
                                SubscriptionManager.LOGGER.error("Exception processing HC payday for user #" + resultSetExecuteQuery.getInt("user_id"), e);
                            }
                        } catch (Throwable th2) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th3) {
                                    th2.addSuppressed(th3);
                                }
                            }
                            throw th2;
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
                } finally {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th.addSuppressed(th4);
                        }
                    }
                }
            } finally {
            }
        } catch (SQLException e2) {
            SubscriptionManager.LOGGER.error("Caught SQL exception", e2);
        }
    }

    public static void processClubBadge(Habbo habbo) {
        progressAchievement(habbo.getHabboInfo());
    }

    public static boolean claimPayDay(Habbo habbo, int i, String str) {
        if (habbo == null) {
            return false;
        }
        switch (str.toLowerCase()) {
            case "credits":
            case "coins":
            case "credit":
            case "coin":
                habbo.getClient().getHabbo().giveCredits(i);
                break;
            case "diamonds":
            case "diamond":
                habbo.getClient().getHabbo().givePoints(5, i);
                break;
            case "duckets":
            case "ducket":
            case "pixels":
            case "pixel":
                habbo.getClient().getHabbo().givePoints(0, i);
                break;
            default:
                int i2 = -1;
                try {
                    i2 = Integer.parseInt(str);
                    break;
                } catch (NumberFormatException e) {
                    LOGGER.error("Couldn't convert the type point currency {} on HC PayDay. The number must be a integer and positive.", Integer.valueOf(i2));
                }
                if (i2 >= 0) {
                    habbo.getClient().getHabbo().givePoints(i2, i);
                    break;
                }
                break;
        }
        habbo.alert(Emulator.getTexts().getValue("subscriptions.hc.payday.message", "Woohoo HC Payday has arrived! You have received %amount% credits to your purse. Enjoy!").replace("%amount%", Emulator.PREVIEW + i));
        return true;
    }

    private static void progressAchievement(HabboInfo habboInfo) {
        HabboStats habboStats = habboInfo.getHabboStats();
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(ACHIEVEMENT_NAME);
        if (achievement != null) {
            int achievementProgress = habboStats.getAchievementProgress(achievement);
            if (achievementProgress == -1) {
                achievementProgress = 0;
            }
            int iMax = Math.max(((int) Math.ceil(((double) habboStats.getPastTimeAsClub()) / 2678400.0d)) - achievementProgress, 0);
            if (iMax > 0) {
                AchievementManager.progressAchievement(habboInfo.getId(), achievement, iMax);
            }
        }
    }
}
