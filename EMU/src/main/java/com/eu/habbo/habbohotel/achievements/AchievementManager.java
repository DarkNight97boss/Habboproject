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
import com.eu.habbo.plugin.Event;
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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AchievementManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(AchievementManager.class);
   public static boolean TALENTTRACK_ENABLED = false;
   private final THashMap<String, Achievement> achievements = new THashMap();
   private final THashMap<TalentTrackType, LinkedHashMap<Integer, TalentTrackLevel>> talentTrackLevels = new THashMap();

   public static void progressAchievement(int habboId, Achievement achievement) {
      progressAchievement(habboId, achievement, 1);
   }

   public static void progressAchievement(int habboId, Achievement achievement, int amount) {
      if (achievement != null) {
         Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(habboId);
         if (habbo != null) {
            progressAchievement(habbo, achievement, amount);
         } else {
            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users_achievements_queue (user_id, achievement_id, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?"
                  );

                  try {
                     statement.setInt(1, habboId);
                     statement.setInt(2, achievement.id);
                     statement.setInt(3, amount);
                     statement.setInt(4, amount);
                     statement.execute();
                  } catch (Throwable var10) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
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
                     } catch (Throwable var8) {
                        var11.addSuppressed(var8);
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
      }
   }

   public static void progressAchievement(Habbo habbo, Achievement achievement) {
      progressAchievement(habbo, achievement, 1);
   }

   public static void progressAchievement(Habbo habbo, Achievement achievement, int amount) {
      if (achievement != null) {
         if (habbo != null) {
            if (habbo.isOnline()) {
               int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);
               if (currentProgress == -1) {
                  currentProgress = 0;
                  createUserEntry(habbo, achievement);
                  habbo.getHabboStats().setProgress(achievement, 0);
               }

               if (Emulator.getPluginManager().isRegistered(UserAchievementProgressEvent.class, true)) {
                  Event userAchievementProgressedEvent = new UserAchievementProgressEvent(habbo, achievement, amount);
                  Emulator.getPluginManager().fireEvent(userAchievementProgressedEvent);
                  if (userAchievementProgressedEvent.isCancelled()) {
                     return;
                  }
               }

               AchievementLevel oldLevel = achievement.getLevelForProgress(currentProgress);
               if (oldLevel == null || oldLevel.level != achievement.levels.size() || currentProgress < oldLevel.progress) {
                  habbo.getHabboStats().setProgress(achievement, currentProgress + amount);
                  AchievementLevel newLevel = achievement.getLevelForProgress(currentProgress + amount);
                  if (TALENTTRACK_ENABLED) {
                     for (TalentTrackType type : TalentTrackType.values()) {
                        if (Emulator.getGameEnvironment().getAchievementManager().talentTrackLevels.containsKey(type)) {
                           for (Entry<Integer, TalentTrackLevel> entry : ((LinkedHashMap<Integer, TalentTrackLevel>)Emulator.getGameEnvironment()
                                 .getAchievementManager()
                                 .talentTrackLevels
                                 .get(type))
                              .entrySet()) {
                              if (entry.getValue().achievements.containsKey(achievement)) {
                                 Emulator.getGameEnvironment().getAchievementManager().handleTalentTrackAchievement(habbo, type, achievement);
                                 break;
                              }
                           }
                        }
                     }
                  }

                  if (newLevel != null && (oldLevel == null || oldLevel.level != newLevel.level || newLevel.level >= achievement.levels.size())) {
                     if (Emulator.getPluginManager().isRegistered(UserAchievementLeveledEvent.class, true)) {
                        Event userAchievementLeveledEvent = new UserAchievementLeveledEvent(habbo, achievement, oldLevel, newLevel);
                        Emulator.getPluginManager().fireEvent(userAchievementLeveledEvent);
                        if (userAchievementLeveledEvent.isCancelled()) {
                           return;
                        }
                     }

                     habbo.getClient().sendResponse(new AchievementProgressComposer(habbo, achievement));
                     habbo.getClient().sendResponse(new AchievementUnlockedComposer(habbo, achievement));
                     HabboBadge badge = null;
                     if (oldLevel != null) {
                        try {
                           badge = habbo.getInventory().getBadgesComponent().getBadge(("ACH_" + achievement.name + oldLevel.level).toLowerCase());
                        } catch (Exception e) {
                           LOGGER.error("Caught exception", e);
                           return;
                        }
                     }

                     String newBadgCode = "ACH_" + achievement.name + newLevel.level;
                     if (badge != null) {
                        badge.setCode(newBadgCode);
                        badge.needsInsert(false);
                        badge.needsUpdate(true);
                     } else {
                        if (habbo.getInventory().getBadgesComponent().hasBadge(newBadgCode)) {
                           return;
                        }

                        badge = new HabboBadge(0, newBadgCode, 0, habbo);
                        habbo.getClient().sendResponse(new AddUserBadgeComposer(badge));
                        badge.needsInsert(true);
                        badge.needsUpdate(true);
                        habbo.getInventory().getBadgesComponent().addBadge(badge);
                     }

                     Emulator.getThreading().run(badge);
                     if (badge.getSlot() > 0 && habbo.getHabboInfo().getCurrentRoom() != null) {
                        habbo.getHabboInfo()
                           .getCurrentRoom()
                           .sendComposer(
                              new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose()
                           );
                     }

                     habbo.getClient().sendResponse(new AddHabboItemComposer(badge.getId(), AddHabboItemComposer.AddHabboItemCategory.BADGE));
                     habbo.getHabboStats().addAchievementScore(newLevel.points);
                     if (newLevel.rewardAmount > 0) {
                        habbo.givePoints(newLevel.rewardType, newLevel.rewardAmount);
                     }

                     if (habbo.getHabboInfo().getCurrentRoom() != null) {
                        habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
                     }
                  } else {
                     habbo.getClient().sendResponse(new AchievementProgressComposer(habbo, achievement));
                  }
               }
            }
         }
      }
   }

   public static boolean hasAchieved(Habbo habbo, Achievement achievement) {
      int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);
      if (currentProgress == -1) {
         return false;
      }

      AchievementLevel level = achievement.getLevelForProgress(currentProgress);
      if (level == null) {
         return false;
      }

      AchievementLevel nextLevel = (AchievementLevel)achievement.levels.get(level.level + 1);
      return nextLevel == null && currentProgress >= level.progress;
   }

   public static void createUserEntry(Habbo habbo, Achievement achievement) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users_achievements (user_id, achievement_name, progress) VALUES (?, ?, ?)");

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               statement.setString(2, achievement.name);
               statement.setInt(3, 1);
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

   public static void saveAchievements(Habbo habbo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "UPDATE users_achievements SET progress = ? WHERE achievement_name = ? AND user_id = ? LIMIT 1"
            );

            try {
               statement.setInt(3, habbo.getHabboInfo().getId());

               for (Entry<Achievement, Integer> map : habbo.getHabboStats().getAchievementProgress().entrySet()) {
                  statement.setInt(1, map.getValue());
                  statement.setString(2, map.getKey().name);
                  statement.addBatch();
               }

               statement.executeBatch();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var8) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public static int getAchievementProgressForHabbo(int userId, Achievement achievement) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         int var5;
         label109: {
            try {
               PreparedStatement statement;
               label102: {
                  statement = connection.prepareStatement("SELECT progress FROM users_achievements WHERE user_id = ? AND achievement_name = ? LIMIT 1");

                  try {
                     statement.setInt(1, userId);
                     statement.setString(2, achievement.name);
                     ResultSet set = statement.executeQuery();

                     label84: {
                        try {
                           if (set.next()) {
                              var5 = set.getInt("progress");
                              break label84;
                           }
                        } catch (Throwable var10) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var9) {
                                 var10.addSuppressed(var9);
                              }
                           }

                           throw var10;
                        }

                        if (set != null) {
                           set.close();
                        }
                        break label102;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var11) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var8) {
                           var11.addSuppressed(var8);
                        }
                     }

                     throw var11;
                  }

                  if (statement != null) {
                     statement.close();
                  }
                  break label109;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var12) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var7) {
                     var12.addSuppressed(var7);
                  }
               }

               throw var12;
            }

            if (connection != null) {
               connection.close();
            }

            return 0;
         }

         if (connection != null) {
            connection.close();
         }

         return var5;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return 0;
      }
   }

   public void reload() {
      long millis = System.currentTimeMillis();
      synchronized (this.achievements) {
         for (Achievement achievement : this.achievements.values()) {
            achievement.clearLevels();
         }

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               try {
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM achievements");

                     try {
                        while (set.next()) {
                           if (!this.achievements.containsKey(set.getString("name"))) {
                              this.achievements.put(set.getString("name"), new Achievement(set));
                           } else {
                              ((Achievement)this.achievements.get(set.getString("name"))).addLevel(new AchievementLevel(set));
                           }
                        }
                     } catch (Throwable var20) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var16) {
                              var20.addSuppressed(var16);
                           }
                        }

                        throw var20;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var21) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var15) {
                           var21.addSuppressed(var15);
                        }
                     }

                     throw var21;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (SQLException e) {
                  LOGGER.error("Caught SQL exception", e);
               } catch (Exception e) {
                  LOGGER.error("Caught exception", e);
               }

               synchronized (this.talentTrackLevels) {
                  this.talentTrackLevels.clear();
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM achievements_talents ORDER BY level ASC");

                     try {
                        while (set.next()) {
                           TalentTrackLevel level = new TalentTrackLevel(set);
                           if (!this.talentTrackLevels.containsKey(level.type)) {
                              this.talentTrackLevels.put(level.type, new LinkedHashMap());
                           }

                           ((LinkedHashMap)this.talentTrackLevels.get(level.type)).put(level.level, level);
                        }
                     } catch (Throwable var17) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var14) {
                              var17.addSuppressed(var14);
                           }
                        }

                        throw var17;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var18) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var13) {
                           var18.addSuppressed(var13);
                        }
                     }

                     throw var18;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               }
            } catch (Throwable var24) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var12) {
                     var24.addSuppressed(var12);
                  }
               }

               throw var24;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            LOGGER.error("Achievement Manager -> Failed to load!");
            return;
         }
      }

      LOGGER.info("Achievement Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public Achievement getAchievement(String name) {
      return (Achievement)this.achievements.get(name);
   }

   public Achievement getAchievement(int id) {
      synchronized (this.achievements) {
         for (Entry<String, Achievement> set : this.achievements.entrySet()) {
            if (set.getValue().id == id) {
               return set.getValue();
            }
         }

         return null;
      }
   }

   public THashMap<String, Achievement> getAchievements() {
      return this.achievements;
   }

   public LinkedHashMap<Integer, TalentTrackLevel> getTalenTrackLevels(TalentTrackType type) {
      return (LinkedHashMap<Integer, TalentTrackLevel>)this.talentTrackLevels.get(type);
   }

   public TalentTrackLevel calculateTalenTrackLevel(final Habbo habbo, TalentTrackType type) {
      TalentTrackLevel level = null;

      for (Entry<Integer, TalentTrackLevel> entry : ((LinkedHashMap<Integer, TalentTrackLevel>)this.talentTrackLevels.get(type)).entrySet()) {
         final boolean[] allCompleted = new boolean[]{true};
         entry.getValue().achievements.forEachEntry(new TObjectIntProcedure<Achievement>() {
            public boolean execute(Achievement a, int b) {
               if (habbo.getHabboStats().getAchievementProgress(a) < b) {
                  allCompleted[0] = false;
               }

               return allCompleted[0];
            }
         });
         if (!allCompleted[0]) {
            break;
         }

         if (level == null || level.level < entry.getValue().level) {
            level = entry.getValue();
         }
      }

      return level;
   }

   public void handleTalentTrackAchievement(Habbo habbo, TalentTrackType type, Achievement achievement) {
      TalentTrackLevel currentLevel = this.calculateTalenTrackLevel(habbo, type);
      if (currentLevel != null) {
         if (currentLevel.level > habbo.getHabboStats().talentTrackLevel(type)) {
            for (int i = habbo.getHabboStats().talentTrackLevel(type); i <= currentLevel.level; i++) {
               TalentTrackLevel level = this.getTalentTrackLevel(type, i);
               if (level != null) {
                  if (level.items != null && !level.items.isEmpty()) {
                     TObjectHashIterator var7 = level.items.iterator();

                     while (var7.hasNext()) {
                        Item item = (Item)var7.next();
                        HabboItem rewardItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, "");
                        habbo.getInventory().getItemsComponent().addItem(rewardItem);
                        habbo.getClient().sendResponse(new AddHabboItemComposer(rewardItem));
                        habbo.getClient().sendResponse(new InventoryRefreshComposer());
                     }
                  }

                  if (level.badges != null && level.badges.length > 0) {
                     for (String badge : level.badges) {
                        if (!badge.isEmpty() && !habbo.getInventory().getBadgesComponent().hasBadge(badge)) {
                           HabboBadge b = new HabboBadge(0, badge, 0, habbo);
                           Emulator.getThreading().run(b);
                           habbo.getInventory().getBadgesComponent().addBadge(b);
                           habbo.getClient().sendResponse(new AddUserBadgeComposer(b));
                        }
                     }
                  }

                  if (level.perks != null && level.perks.length > 0) {
                     for (String perk : level.perks) {
                        if (perk.equalsIgnoreCase("TRADE")) {
                           habbo.getHabboStats().perkTrade = true;
                        }
                     }
                  }

                  habbo.getClient().sendResponse(new TalentLevelUpdateComposer(type, level));
               }
            }
         }

         habbo.getHabboStats().setTalentLevel(type, currentLevel.level);
      }
   }

   public TalentTrackLevel getTalentTrackLevel(TalentTrackType type, int level) {
      return (TalentTrackLevel)((LinkedHashMap<Integer, TalentTrackLevel>)this.talentTrackLevels.get(type)).get(level);
   }
}
