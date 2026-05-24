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
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiredHighscoreManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(WiredHighscoreManager.class);
   private final HashMap<Integer, List<WiredHighscoreDataEntry>> data = new HashMap<>();
   private static final String locale = System.getProperty("user.language") != null ? System.getProperty("user.language") : "en";
   private static final String country = System.getProperty("user.country") != null ? System.getProperty("user.country") : "US";
   private static final DayOfWeek firstDayOfWeek = WeekFields.of(new Locale(locale, country)).getFirstDayOfWeek();
   private static final DayOfWeek lastDayOfWeek = DayOfWeek.of((firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length + 1);
   private static final ZoneId zoneId = ZoneId.systemDefault();
   public static ScheduledFuture midnightUpdater = null;

   public void load() {
      long millis = System.currentTimeMillis();
      this.data.clear();
      this.loadHighscoreData();
      LOGGER.info("Highscore Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS, " + this.data.size() + " items)");
   }

   @EventHandler
   public static void onEmulatorLoaded(EmulatorLoadedEvent event) {
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
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM items_highscore_data");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     WiredHighscoreDataEntry entry = new WiredHighscoreDataEntry(set);
                     if (!this.data.containsKey(entry.getItemId())) {
                        this.data.put(entry.getItemId(), new ArrayList<>());
                     }

                     this.data.get(entry.getItemId()).add(entry);
                  }
               } catch (Throwable var9) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var11.addSuppressed(var6);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void addHighscoreData(WiredHighscoreDataEntry entry) {
      if (!this.data.containsKey(entry.getItemId())) {
         this.data.put(entry.getItemId(), new ArrayList<>());
      }

      this.data.get(entry.getItemId()).add(entry);

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO `items_highscore_data` (`item_id`, `user_ids`, `score`, `is_win`, `timestamp`) VALUES (?, ?, ?, ?, ?)"
            );

            try {
               statement.setInt(1, entry.getItemId());
               statement.setString(2, String.join(",", entry.getUserIds().stream().map(Object::toString).collect(Collectors.toList())));
               statement.setInt(3, entry.getScore());
               statement.setInt(4, entry.isWin() ? 1 : 0);
               statement.setInt(5, entry.getTimestamp());
               statement.execute();
            } catch (Throwable var8) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var9) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public List<WiredHighscoreRow> getHighscoreRowsForItem(int itemId, WiredHighscoreClearType clearType, WiredHighscoreScoreType scoreType) {
      if (!this.data.containsKey(itemId)) {
         return null;
      } else {
         Stream<WiredHighscoreRow> highscores = new ArrayList<>(this.data.get(itemId))
            .stream()
            .filter(entry -> this.timeMatchesEntry(entry, clearType) && (scoreType != WiredHighscoreScoreType.MOSTWIN || entry.isWin()))
            .map(
               entry -> new WiredHighscoreRow(
                  entry.getUserIds()
                     .stream()
                     .map(id -> Emulator.getGameEnvironment().getHabboManager().getHabboInfo(id).getUsername())
                     .collect(Collectors.toList()),
                  entry.getScore()
               )
            );
         if (scoreType == WiredHighscoreScoreType.CLASSIC) {
            return highscores.sorted(WiredHighscoreRow::compareTo).collect(Collectors.toList());
         } else if (scoreType == WiredHighscoreScoreType.PERTEAM) {
            return highscores.collect(Collectors.groupingBy(h -> h.getUsers().hashCode()))
               .entrySet()
               .stream()
               .map(e -> e.getValue().stream().sorted(WiredHighscoreRow::compareTo).collect(Collectors.toList()).get(0))
               .sorted(WiredHighscoreRow::compareTo)
               .collect(Collectors.toList());
         } else {
            return scoreType == WiredHighscoreScoreType.MOSTWIN
               ? highscores.collect(Collectors.groupingBy(h -> h.getUsers().hashCode()))
                  .entrySet()
                  .stream()
                  .map(e -> new WiredHighscoreRow(e.getValue().get(0).getUsers(), e.getValue().size()))
                  .sorted(WiredHighscoreRow::compareTo)
                  .collect(Collectors.toList())
               : null;
         }
      }
   }

   private boolean timeMatchesEntry(WiredHighscoreDataEntry entry, WiredHighscoreClearType timeType) {
      switch (timeType) {
         case DAILY:
            return entry.getTimestamp() > this.getTodayStartTimestamp() && entry.getTimestamp() < this.getTodayEndTimestamp();
         case WEEKLY:
            return entry.getTimestamp() > this.getWeekStartTimestamp() && entry.getTimestamp() < this.getWeekEndTimestamp();
         case MONTHLY:
            return entry.getTimestamp() > this.getMonthStartTimestamp() && entry.getTimestamp() < this.getMonthEndTimestamp();
         case ALLTIME:
            return true;
         default:
            return false;
      }
   }

   private long getTodayStartTimestamp() {
      return LocalDateTime.now().with(LocalTime.MIDNIGHT).atZone(zoneId).toEpochSecond();
   }

   private long getTodayEndTimestamp() {
      return LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).atZone(zoneId).toEpochSecond();
   }

   private long getWeekStartTimestamp() {
      return LocalDateTime.now().with(LocalTime.MIDNIGHT).with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).atZone(zoneId).toEpochSecond();
   }

   private long getWeekEndTimestamp() {
      return LocalDateTime.now()
         .with(LocalTime.MIDNIGHT)
         .plusDays(1L)
         .plusSeconds(-1L)
         .with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
         .atZone(zoneId)
         .toEpochSecond();
   }

   private long getMonthStartTimestamp() {
      return LocalDateTime.now().with(LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth()).atZone(zoneId).toEpochSecond();
   }

   private long getMonthEndTimestamp() {
      return LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).with(TemporalAdjusters.lastDayOfMonth()).atZone(zoneId).toEpochSecond();
   }
}
