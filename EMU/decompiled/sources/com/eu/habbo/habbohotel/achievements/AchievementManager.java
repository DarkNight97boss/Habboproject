package com.eu.habbo.habbohotel.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.achievements.AchievementProgressComposer;
import com.eu.habbo.messages.outgoing.achievements.AchievementUnlockedComposer;
import com.eu.habbo.messages.outgoing.achievements.talenttrack.TalentLevelUpdateComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementLeveledEvent;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementProgressEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/achievements/AchievementManager.class */
public class AchievementManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchievementManager.class);
    public static boolean TALENTTRACK_ENABLED = false;
    private final THashMap<String, Achievement> achievements = new THashMap<>();
    private final THashMap<TalentTrackType, LinkedHashMap<Integer, TalentTrackLevel>> talentTrackLevels = new THashMap<>();

    public static void progressAchievement(int i, Achievement achievement) {
        progressAchievement(i, achievement, 1);
    }

    public static void progressAchievement(int i, Achievement achievement, int i2) {
        if (achievement != null) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
            if (habbo != null) {
                progressAchievement(habbo, achievement, i2);
                return;
            }
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_achievements_queue (user_id, achievement_id, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i);
                        preparedStatementPrepareStatement.setInt(2, achievement.id);
                        preparedStatementPrepareStatement.setInt(3, i2);
                        preparedStatementPrepareStatement.setInt(4, i2);
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

    public static void progressAchievement(Habbo habbo, Achievement achievement) {
        progressAchievement(habbo, achievement, 1);
    }

    public static void progressAchievement(Habbo habbo, Achievement achievement, int i) {
        if (achievement == null || habbo == null || !habbo.isOnline()) {
            return;
        }
        int achievementProgress = habbo.getHabboStats().getAchievementProgress(achievement);
        if (achievementProgress == -1) {
            achievementProgress = 0;
            createUserEntry(habbo, achievement);
            habbo.getHabboStats().setProgress(achievement, 0);
        }
        if (Emulator.getPluginManager().isRegistered(UserAchievementProgressEvent.class, true)) {
            UserAchievementProgressEvent userAchievementProgressEvent = new UserAchievementProgressEvent(habbo, achievement, i);
            Emulator.getPluginManager().fireEvent(userAchievementProgressEvent);
            if (userAchievementProgressEvent.isCancelled()) {
                return;
            }
        }
        AchievementLevel levelForProgress = achievement.getLevelForProgress(achievementProgress);
        if (levelForProgress == null || levelForProgress.level != achievement.levels.size() || achievementProgress < levelForProgress.progress) {
            habbo.getHabboStats().setProgress(achievement, achievementProgress + i);
            AchievementLevel levelForProgress2 = achievement.getLevelForProgress(achievementProgress + i);
            if (TALENTTRACK_ENABLED) {
                for (TalentTrackType talentTrackType : TalentTrackType.values()) {
                    if (Emulator.getGameEnvironment().getAchievementManager().talentTrackLevels.containsKey(talentTrackType)) {
                        Iterator it = ((LinkedHashMap) Emulator.getGameEnvironment().getAchievementManager().talentTrackLevels.get(talentTrackType)).entrySet().iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (((TalentTrackLevel) ((Map.Entry) it.next()).getValue()).achievements.containsKey(achievement)) {
                                    Emulator.getGameEnvironment().getAchievementManager().handleTalentTrackAchievement(habbo, talentTrackType, achievement);
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (levelForProgress2 == null || (levelForProgress != null && levelForProgress.level == levelForProgress2.level && levelForProgress2.level < achievement.levels.size())) {
                habbo.getClient().sendResponse(new AchievementProgressComposer(habbo, achievement));
                return;
            }
            if (Emulator.getPluginManager().isRegistered(UserAchievementLeveledEvent.class, true)) {
                UserAchievementLeveledEvent userAchievementLeveledEvent = new UserAchievementLeveledEvent(habbo, achievement, levelForProgress, levelForProgress2);
                Emulator.getPluginManager().fireEvent(userAchievementLeveledEvent);
                if (userAchievementLeveledEvent.isCancelled()) {
                    return;
                }
            }
            habbo.getClient().sendResponse(new AchievementProgressComposer(habbo, achievement));
            habbo.getClient().sendResponse(new AchievementUnlockedComposer(habbo, achievement));
            HabboBadge badge = null;
            if (levelForProgress != null) {
                try {
                    badge = habbo.getInventory().getBadgesComponent().getBadge(("ACH_" + achievement.name + levelForProgress.level).toLowerCase());
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                    return;
                }
            }
            String str = "ACH_" + achievement.name + levelForProgress2.level;
            if (badge != null) {
                badge.setCode(str);
                badge.needsInsert(false);
                badge.needsUpdate(true);
            } else {
                if (habbo.getInventory().getBadgesComponent().hasBadge(str)) {
                    return;
                }
                badge = new HabboBadge(0, str, 0, habbo);
                habbo.getClient().sendResponse(new AddUserBadgeComposer(badge));
                badge.needsInsert(true);
                badge.needsUpdate(true);
                habbo.getInventory().getBadgesComponent().addBadge(badge);
            }
            Emulator.getThreading().run(badge);
            if (badge.getSlot() > 0 && habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose());
            }
            habbo.getClient().sendResponse(new AddHabboItemComposer(badge.getId(), AddHabboItemComposer.AddHabboItemCategory.BADGE));
            habbo.getHabboStats().addAchievementScore(levelForProgress2.points);
            if (levelForProgress2.rewardAmount > 0) {
                habbo.givePoints(levelForProgress2.rewardType, levelForProgress2.rewardAmount);
            }
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            }
        }
    }

    public static boolean hasAchieved(Habbo habbo, Achievement achievement) {
        AchievementLevel levelForProgress;
        int achievementProgress = habbo.getHabboStats().getAchievementProgress(achievement);
        return achievementProgress != -1 && (levelForProgress = achievement.getLevelForProgress(achievementProgress)) != null && ((AchievementLevel) achievement.levels.get(Integer.valueOf(levelForProgress.level + 1))) == null && achievementProgress >= levelForProgress.progress;
    }

    public static void createUserEntry(Habbo habbo, Achievement achievement) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_achievements (user_id, achievement_name, progress) VALUES (?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setString(2, achievement.name);
                    preparedStatementPrepareStatement.setInt(3, 1);
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

    public static void saveAchievements(Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_achievements SET progress = ? WHERE achievement_name = ? AND user_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(3, habbo.getHabboInfo().getId());
                    for (Map.Entry entry : habbo.getHabboStats().getAchievementProgress().entrySet()) {
                        preparedStatementPrepareStatement.setInt(1, ((Integer) entry.getValue()).intValue());
                        preparedStatementPrepareStatement.setString(2, ((Achievement) entry.getKey()).name);
                        preparedStatementPrepareStatement.addBatch();
                    }
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

    public static int getAchievementProgressForHabbo(int i, Achievement achievement) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT progress FROM users_achievements WHERE user_id = ? AND achievement_name = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setString(2, achievement.name);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (!resultSetExecuteQuery.next()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return 0;
                        }
                        int i2 = resultSetExecuteQuery.getInt("progress");
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        return i2;
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
            return 0;
        }
    }

    public void reload() {
        Statement statementCreateStatement;
        ResultSet resultSetExecuteQuery;
        long jCurrentTimeMillis = System.currentTimeMillis();
        synchronized (this.achievements) {
            Iterator it = this.achievements.values().iterator();
            while (it.hasNext()) {
                ((Achievement) it.next()).clearLevels();
            }
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    try {
                        statementCreateStatement = connection.createStatement();
                        try {
                            resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM achievements");
                            while (resultSetExecuteQuery.next()) {
                                try {
                                    if (this.achievements.containsKey(resultSetExecuteQuery.getString("name"))) {
                                        ((Achievement) this.achievements.get(resultSetExecuteQuery.getString("name"))).addLevel(new AchievementLevel(resultSetExecuteQuery));
                                    } else {
                                        this.achievements.put(resultSetExecuteQuery.getString("name"), new Achievement(resultSetExecuteQuery));
                                    }
                                } finally {
                                }
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (statementCreateStatement != null) {
                                statementCreateStatement.close();
                            }
                        } finally {
                        }
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
                } catch (SQLException e) {
                    LOGGER.error("Caught SQL exception", e);
                } catch (Exception e2) {
                    LOGGER.error("Caught exception", e2);
                }
                synchronized (this.talentTrackLevels) {
                    this.talentTrackLevels.clear();
                    statementCreateStatement = connection.createStatement();
                    try {
                        resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM achievements_talents ORDER BY level ASC");
                        while (resultSetExecuteQuery.next()) {
                            try {
                                TalentTrackLevel talentTrackLevel = new TalentTrackLevel(resultSetExecuteQuery);
                                if (!this.talentTrackLevels.containsKey(talentTrackLevel.type)) {
                                    this.talentTrackLevels.put(talentTrackLevel.type, new LinkedHashMap());
                                }
                                ((LinkedHashMap) this.talentTrackLevels.get(talentTrackLevel.type)).put(Integer.valueOf(talentTrackLevel.level), talentTrackLevel);
                            } finally {
                            }
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (statementCreateStatement != null) {
                            statementCreateStatement.close();
                        }
                    } finally {
                    }
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e3) {
                LOGGER.error("Caught SQL exception", e3);
                LOGGER.error("Achievement Manager -> Failed to load!");
                return;
            }
        }
        LOGGER.info("Achievement Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public Achievement getAchievement(String str) {
        return (Achievement) this.achievements.get(str);
    }

    public Achievement getAchievement(int i) {
        synchronized (this.achievements) {
            for (Map.Entry entry : this.achievements.entrySet()) {
                if (((Achievement) entry.getValue()).id == i) {
                    return (Achievement) entry.getValue();
                }
            }
            return null;
        }
    }

    public THashMap<String, Achievement> getAchievements() {
        return this.achievements;
    }

    public LinkedHashMap<Integer, TalentTrackLevel> getTalenTrackLevels(TalentTrackType talentTrackType) {
        return (LinkedHashMap) this.talentTrackLevels.get(talentTrackType);
    }

    public TalentTrackLevel calculateTalenTrackLevel(final Habbo habbo, TalentTrackType talentTrackType) {
        TalentTrackLevel talentTrackLevel = null;
        for (Map.Entry entry : ((LinkedHashMap) this.talentTrackLevels.get(talentTrackType)).entrySet()) {
            final boolean[] zArr = {true};
            ((TalentTrackLevel) entry.getValue()).achievements.forEachEntry(new TObjectIntProcedure<Achievement>() { // from class: com.eu.habbo.habbohotel.achievements.AchievementManager.1
                public boolean execute(Achievement achievement, int i) {
                    if (habbo.getHabboStats().getAchievementProgress(achievement) < i) {
                        zArr[0] = false;
                    }
                    return zArr[0];
                }
            });
            if (!zArr[0]) {
                break;
            }
            if (talentTrackLevel == null || talentTrackLevel.level < ((TalentTrackLevel) entry.getValue()).level) {
                talentTrackLevel = (TalentTrackLevel) entry.getValue();
            }
        }
        return talentTrackLevel;
    }

    public void handleTalentTrackAchievement(Habbo habbo, TalentTrackType talentTrackType, Achievement achievement) {
        TalentTrackLevel talentTrackLevelCalculateTalenTrackLevel = calculateTalenTrackLevel(habbo, talentTrackType);
        if (talentTrackLevelCalculateTalenTrackLevel != null) {
            if (talentTrackLevelCalculateTalenTrackLevel.level > habbo.getHabboStats().talentTrackLevel(talentTrackType)) {
                for (int iTalentTrackLevel = habbo.getHabboStats().talentTrackLevel(talentTrackType); iTalentTrackLevel <= talentTrackLevelCalculateTalenTrackLevel.level; iTalentTrackLevel++) {
                    TalentTrackLevel talentTrackLevel = getTalentTrackLevel(talentTrackType, iTalentTrackLevel);
                    if (talentTrackLevel != null) {
                        if (talentTrackLevel.items != null && !talentTrackLevel.items.isEmpty()) {
                            TObjectHashIterator it = talentTrackLevel.items.iterator();
                            while (it.hasNext()) {
                                HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), (Item) it.next(), 0, 0, Emulator.PREVIEW);
                                habbo.getInventory().getItemsComponent().addItem(habboItemCreateItem);
                                habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                            }
                        }
                        if (talentTrackLevel.badges != null && talentTrackLevel.badges.length > 0) {
                            for (String str : talentTrackLevel.badges) {
                                if (!str.isEmpty() && !habbo.getInventory().getBadgesComponent().hasBadge(str)) {
                                    HabboBadge habboBadge = new HabboBadge(0, str, 0, habbo);
                                    Emulator.getThreading().run(habboBadge);
                                    habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
                                    habbo.getClient().sendResponse(new AddUserBadgeComposer(habboBadge));
                                }
                            }
                        }
                        if (talentTrackLevel.perks != null && talentTrackLevel.perks.length > 0) {
                            for (String str2 : talentTrackLevel.perks) {
                                if (str2.equalsIgnoreCase("TRADE")) {
                                    habbo.getHabboStats().perkTrade = true;
                                }
                            }
                        }
                        habbo.getClient().sendResponse(new TalentLevelUpdateComposer(talentTrackType, talentTrackLevel));
                    }
                }
            }
            habbo.getHabboStats().setTalentLevel(talentTrackType, talentTrackLevelCalculateTalenTrackLevel.level);
        }
    }

    public TalentTrackLevel getTalentTrackLevel(TalentTrackType talentTrackType, int i) {
        return (TalentTrackLevel) ((LinkedHashMap) this.talentTrackLevels.get(talentTrackType)).get(Integer.valueOf(i));
    }
}
