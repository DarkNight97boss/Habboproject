package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.friends.SearchUserEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsEvent;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.threading.runnables.AchievementUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/CleanerThread.class */
public class CleanerThread implements Runnable {
    private static final int DELAY = 10000;
    private static final int RELOAD_HALL_OF_FAME = 1800;
    private static final int RELOAD_NEWS_LIST = 3600;
    private static final int REMOVE_INACTIVE_ROOMS = 120;
    private static final int REMOVE_INACTIVE_GUILDS = 60;
    private static final int REMOVE_INACTIVE_TOURS = 600;
    private static final int SAVE_ERROR_LOGS = 30;
    private static final int CLEAR_CACHED_VALUES = 3600;
    private static final int CALLBACK_TIME = 900;
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanerThread.class);
    private static int LAST_HOF_RELOAD = Emulator.getIntUnixTimestamp();
    private static int LAST_NL_RELOAD = Emulator.getIntUnixTimestamp();
    private static int LAST_INACTIVE_ROOMS_CLEARED = Emulator.getIntUnixTimestamp();
    private static int LAST_INACTIVE_GUILDS_CLEARED = Emulator.getIntUnixTimestamp();
    private static int LAST_INACTIVE_TOURS_CLEARED = Emulator.getIntUnixTimestamp();
    private static int LAST_ERROR_LOGS_SAVED = Emulator.getIntUnixTimestamp();
    private static int LAST_DAILY_REFILL = Emulator.getIntUnixTimestamp();
    private static int LAST_CALLBACK = Emulator.getIntUnixTimestamp();
    private static int LAST_HABBO_CACHE_CLEARED = Emulator.getIntUnixTimestamp();

    public CleanerThread() {
        databaseCleanup();
        Emulator.getThreading().run(this, 10000L);
        Emulator.getThreading().run(new AchievementUpdater());
    }

    @Override // java.lang.Runnable
    public void run() {
        Emulator.getThreading().run(this, 10000L);
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        if (intUnixTimestamp - LAST_HOF_RELOAD > RELOAD_HALL_OF_FAME) {
            Emulator.getGameEnvironment().getHotelViewManager().getHallOfFame().reload();
            LAST_HOF_RELOAD = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_NL_RELOAD > 3600) {
            Emulator.getGameEnvironment().getHotelViewManager().getNewsList().reload();
            LAST_NL_RELOAD = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_INACTIVE_ROOMS_CLEARED > REMOVE_INACTIVE_ROOMS) {
            Emulator.getGameEnvironment().getRoomManager().clearInactiveRooms();
            LAST_INACTIVE_ROOMS_CLEARED = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_INACTIVE_GUILDS_CLEARED > REMOVE_INACTIVE_GUILDS) {
            Emulator.getGameEnvironment().getGuildManager().clearInactiveGuilds();
            ForumThread.clearCache();
            LAST_INACTIVE_GUILDS_CLEARED = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_INACTIVE_TOURS_CLEARED > REMOVE_INACTIVE_TOURS) {
            Emulator.getGameEnvironment().getGuideManager().cleanup();
            LAST_INACTIVE_TOURS_CLEARED = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_ERROR_LOGS_SAVED > 30) {
            Emulator.getDatabaseLogger().save();
            LAST_ERROR_LOGS_SAVED = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_CALLBACK > CALLBACK_TIME) {
            LAST_CALLBACK = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_DAILY_REFILL > Emulator.getConfig().getInt("hotel.refill.daily")) {
            refillDailyRespects();
            LAST_DAILY_REFILL = intUnixTimestamp;
        }
        if (intUnixTimestamp - LAST_HABBO_CACHE_CLEARED > 3600) {
            clearCachedValues();
            LAST_HABBO_CACHE_CLEARED = intUnixTimestamp;
        }
        SearchRoomsEvent.cachedResults.clear();
        SearchUserEvent.cachedResults.clear();
    }

    void databaseCleanup() {
        Connection connection;
        Statement statementCreateStatement;
        refillDailyRespects();
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                statementCreateStatement = connection.createStatement();
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            statementCreateStatement.execute("UPDATE users SET online = '0' WHERE online = '1'");
            statementCreateStatement.execute("UPDATE rooms SET users = '0' WHERE users > 0");
            statementCreateStatement.execute("DELETE FROM room_mutes WHERE ends < " + intUnixTimestamp);
            statementCreateStatement.execute("DELETE FROM room_bans WHERE ends < " + intUnixTimestamp);
            statementCreateStatement.execute("DELETE users_favorite_rooms FROM users_favorite_rooms LEFT JOIN rooms ON room_id = rooms.id WHERE rooms.id IS NULL");
            if (statementCreateStatement != null) {
                statementCreateStatement.close();
            }
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_effects SET total = total - 1 WHERE activation_timestamp + duration < ? AND activation_timestamp > 0 AND duration > 0");
            try {
                preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp());
                preparedStatementPrepareStatement.execute();
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                statementCreateStatement = connection.createStatement();
                try {
                    statementCreateStatement.execute("DELETE FROM users_effects WHERE total <= 0");
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    LOGGER.info("Database -> Cleaned!");
                } finally {
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
    }

    public void refillDailyRespects() {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET daily_respect_points = ?, daily_pet_respect_points = ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, Emulator.getConfig().getInt("hotel.daily.respect"));
            preparedStatementPrepareStatement.setInt(2, Emulator.getConfig().getInt("hotel.daily.respect.pets"));
            preparedStatementPrepareStatement.executeUpdate();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (Emulator.isReady) {
                for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                    habbo.getHabboStats().respectPointsToGive = Emulator.getConfig().getInt("hotel.daily.respect");
                    habbo.getHabboStats().petRespectPointsToGive = Emulator.getConfig().getInt("hotel.daily.respect.pets");
                    habbo.getClient().sendResponse(new UserDataComposer(habbo));
                }
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
    }

    private void clearCachedValues() {
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            Habbo value = it.next().getValue();
            if (value != null) {
                try {
                    value.clearCaches();
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
    }
}
