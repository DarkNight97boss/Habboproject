package com.eu.habbo.habbohotel.wired.highscores;

import com.eu.habbo.Emulator;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/highscores/WiredHighscoreManager.class */
public class WiredHighscoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredHighscoreManager.class);
    private final HashMap<Integer, List<WiredHighscoreDataEntry>> data = new HashMap<>();
    private static final String locale;
    private static final String country;
    private static final DayOfWeek firstDayOfWeek;
    private static final DayOfWeek lastDayOfWeek;
    private static final ZoneId zoneId;
    public static ScheduledFuture midnightUpdater;

    public void load() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.data.clear();
        loadHighscoreData();
        LOGGER.info("Highscore Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS, " + this.data.size() + " items)");
    }

    @EventHandler
    public static void onEmulatorLoaded(EmulatorLoadedEvent emulatorLoadedEvent) {
        if (midnightUpdater != null) {
            midnightUpdater.cancel(true);
        }
        midnightUpdater = Emulator.getThreading().run(new WiredHighscoreMidnightUpdater(), WiredHighscoreMidnightUpdater.getNextUpdaterRun());
    }

    public void dispose() {
        if (midnightUpdater != null) {
            midnightUpdater.cancel(true);
        }
        this.data.clear();
    }

    private void loadHighscoreData() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM items_highscore_data");
                try {
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            WiredHighscoreDataEntry wiredHighscoreDataEntry = new WiredHighscoreDataEntry(resultSetExecuteQuery);
                            if (!this.data.containsKey(Integer.valueOf(wiredHighscoreDataEntry.getItemId()))) {
                                this.data.put(Integer.valueOf(wiredHighscoreDataEntry.getItemId()), new ArrayList());
                            }
                            this.data.get(Integer.valueOf(wiredHighscoreDataEntry.getItemId())).add(wiredHighscoreDataEntry);
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

    public void addHighscoreData(WiredHighscoreDataEntry wiredHighscoreDataEntry) {
        if (!this.data.containsKey(Integer.valueOf(wiredHighscoreDataEntry.getItemId()))) {
            this.data.put(Integer.valueOf(wiredHighscoreDataEntry.getItemId()), new ArrayList());
        }
        this.data.get(Integer.valueOf(wiredHighscoreDataEntry.getItemId())).add(wiredHighscoreDataEntry);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `items_highscore_data` (`item_id`, `user_ids`, `score`, `is_win`, `timestamp`) VALUES (?, ?, ?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, wiredHighscoreDataEntry.getItemId());
                    preparedStatementPrepareStatement.setString(2, String.join(",", (Iterable<? extends CharSequence>) wiredHighscoreDataEntry.getUserIds().stream().map((v0) -> {
                        return v0.toString();
                    }).collect(Collectors.toList())));
                    preparedStatementPrepareStatement.setInt(3, wiredHighscoreDataEntry.getScore());
                    preparedStatementPrepareStatement.setInt(4, wiredHighscoreDataEntry.isWin() ? 1 : 0);
                    preparedStatementPrepareStatement.setInt(5, wiredHighscoreDataEntry.getTimestamp());
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

    public List<WiredHighscoreRow> getHighscoreRowsForItem(int i, WiredHighscoreClearType wiredHighscoreClearType, WiredHighscoreScoreType wiredHighscoreScoreType) {
        if (!this.data.containsKey(Integer.valueOf(i))) {
            return null;
        }
        Stream map = new ArrayList(this.data.get(Integer.valueOf(i))).stream().filter(wiredHighscoreDataEntry -> {
            return timeMatchesEntry(wiredHighscoreDataEntry, wiredHighscoreClearType) && (wiredHighscoreScoreType != WiredHighscoreScoreType.MOSTWIN || wiredHighscoreDataEntry.isWin());
        }).map(wiredHighscoreDataEntry2 -> {
            return new WiredHighscoreRow((List) wiredHighscoreDataEntry2.getUserIds().stream().map(num -> {
                return Emulator.getGameEnvironment().getHabboManager().getHabboInfo(num.intValue()).getUsername();
            }).collect(Collectors.toList()), wiredHighscoreDataEntry2.getScore());
        });
        if (wiredHighscoreScoreType == WiredHighscoreScoreType.CLASSIC) {
            return (List) map.sorted((v0, v1) -> {
                return v0.compareTo(v1);
            }).collect(Collectors.toList());
        }
        if (wiredHighscoreScoreType == WiredHighscoreScoreType.PERTEAM) {
            return (List) ((Map) map.collect(Collectors.groupingBy(wiredHighscoreRow -> {
                return Integer.valueOf(wiredHighscoreRow.getUsers().hashCode());
            }))).entrySet().stream().map(entry -> {
                return (WiredHighscoreRow) ((List) ((List) entry.getValue()).stream().sorted((v0, v1) -> {
                    return v0.compareTo(v1);
                }).collect(Collectors.toList())).get(0);
            }).sorted((v0, v1) -> {
                return v0.compareTo(v1);
            }).collect(Collectors.toList());
        }
        if (wiredHighscoreScoreType == WiredHighscoreScoreType.MOSTWIN) {
            return (List) ((Map) map.collect(Collectors.groupingBy(wiredHighscoreRow2 -> {
                return Integer.valueOf(wiredHighscoreRow2.getUsers().hashCode());
            }))).entrySet().stream().map(entry2 -> {
                return new WiredHighscoreRow(((WiredHighscoreRow) ((List) entry2.getValue()).get(0)).getUsers(), ((List) entry2.getValue()).size());
            }).sorted((v0, v1) -> {
                return v0.compareTo(v1);
            }).collect(Collectors.toList());
        }
        return null;
    }

    private boolean timeMatchesEntry(WiredHighscoreDataEntry wiredHighscoreDataEntry, WiredHighscoreClearType wiredHighscoreClearType) {
        switch (wiredHighscoreClearType) {
            case DAILY:
                return ((long) wiredHighscoreDataEntry.getTimestamp()) > getTodayStartTimestamp() && ((long) wiredHighscoreDataEntry.getTimestamp()) < getTodayEndTimestamp();
            case WEEKLY:
                return ((long) wiredHighscoreDataEntry.getTimestamp()) > getWeekStartTimestamp() && ((long) wiredHighscoreDataEntry.getTimestamp()) < getWeekEndTimestamp();
            case MONTHLY:
                return ((long) wiredHighscoreDataEntry.getTimestamp()) > getMonthStartTimestamp() && ((long) wiredHighscoreDataEntry.getTimestamp()) < getMonthEndTimestamp();
            case ALLTIME:
                return true;
            default:
                return false;
        }
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [java.time.ZonedDateTime] */
    private long getTodayStartTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).atZone(zoneId).toEpochSecond();
    }

    /* JADX WARN: Type inference failed for: r0v4, types: [java.time.ZonedDateTime] */
    private long getTodayEndTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).atZone(zoneId).toEpochSecond();
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [java.time.ZonedDateTime] */
    private long getWeekStartTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).atZone(zoneId).toEpochSecond();
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [java.time.ZonedDateTime] */
    private long getWeekEndTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).with(TemporalAdjusters.nextOrSame(lastDayOfWeek)).atZone(zoneId).toEpochSecond();
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [java.time.ZonedDateTime] */
    private long getMonthStartTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth()).atZone(zoneId).toEpochSecond();
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [java.time.ZonedDateTime] */
    private long getMonthEndTimestamp() {
        return LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).with(TemporalAdjusters.lastDayOfMonth()).atZone(zoneId).toEpochSecond();
    }

    static {
        locale = System.getProperty("user.language") != null ? System.getProperty("user.language") : "en";
        country = System.getProperty("user.country") != null ? System.getProperty("user.country") : "US";
        firstDayOfWeek = WeekFields.of(new Locale(locale, country)).getFirstDayOfWeek();
        lastDayOfWeek = DayOfWeek.of(((firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length) + 1);
        zoneId = ZoneId.systemDefault();
        midnightUpdater = null;
    }
}
