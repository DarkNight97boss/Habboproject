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
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HabboStats implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(HabboStats.class);
   public final TIntArrayList secretRecipes;
   public final HabboNavigatorWindowSettings navigatorWindowSettings;
   public final THashMap<String, Object> cache;
   public final ArrayList<CalendarRewardClaimed> calendarRewardsClaimed;
   public final TIntObjectMap<HabboOfferPurchase> offerCache = new TIntObjectHashMap();
   private final AtomicInteger lastOnlineTime = new AtomicInteger(Emulator.getIntUnixTimestamp());
   private final THashMap<Achievement, Integer> achievementProgress;
   private final THashMap<Achievement, Integer> achievementCache;
   private final THashMap<Integer, CatalogItem> recentPurchases;
   private final TIntArrayList favoriteRooms;
   private final TIntArrayList ignoredUsers;
   private TIntArrayList roomsVists;
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
   public List<Integer> guilds;
   public String[] tags;
   public TIntArrayStack votedRooms;
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
   public AtomicInteger chatCounter = new AtomicInteger(0);
   public long lastChat;
   public long lastUsersSearched;
   public boolean nux;
   public boolean nuxReward;
   public int nuxStep = 1;
   public int mutedCount = 0;
   public boolean mutedBubbleTracker = false;
   public String changeNameChecked = "";
   public boolean allowNameChange;
   public boolean isPurchasingFurniture = false;
   public int forumPostsCount;
   public THashMap<Integer, List<Integer>> ltdPurchaseLog = new THashMap(0);
   public long lastTradeTimestamp = Emulator.getIntUnixTimestamp();
   public long lastGiftTimestamp = Emulator.getIntUnixTimestamp();
   public long lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
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
   public int hcMessageLastModified = Emulator.getIntUnixTimestamp();
   public THashSet<Subscription> subscriptions;

   private HabboStats(ResultSet set, HabboInfo habboInfo) throws SQLException {
      this.cache = new THashMap(0);
      this.achievementProgress = new THashMap(0);
      this.achievementCache = new THashMap(0);
      this.recentPurchases = new THashMap(0);
      this.favoriteRooms = new TIntArrayList(0);
      this.ignoredUsers = new TIntArrayList(0);
      this.roomsVists = new TIntArrayList(0);
      this.secretRecipes = new TIntArrayList(0);
      this.calendarRewardsClaimed = new ArrayList<>();
      this.habboInfo = habboInfo;
      this.achievementScore = set.getInt("achievement_score");
      this.respectPointsReceived = set.getInt("respects_received");
      this.respectPointsGiven = set.getInt("respects_given");
      this.petRespectPointsToGive = set.getInt("daily_pet_respect_points");
      this.respectPointsToGive = set.getInt("daily_respect_points");
      this.blockFollowing = set.getString("block_following").equals("1");
      this.blockFriendRequests = set.getString("block_friendrequests").equals("1");
      this.blockRoomInvites = set.getString("block_roominvites").equals("1");
      this.preferOldChat = set.getString("old_chat").equals("1");
      this.blockCameraFollow = set.getString("block_camera_follow").equals("1");
      this.guild = set.getInt("guild_id");
      this.guilds = new ArrayList<>();
      this.tags = set.getString("tags").split(";");
      this.allowTrade = set.getString("can_trade").equals("1");
      this.votedRooms = new TIntArrayStack();
      this.clubExpireTimestamp = set.getInt("club_expire_timestamp");
      this.loginStreak = set.getInt("login_streak");
      this.rentedItemId = set.getInt("rent_space_id");
      this.rentedTimeEnd = set.getInt("rent_space_endtime");
      this.volumeSystem = set.getInt("volume_system");
      this.volumeFurni = set.getInt("volume_furni");
      this.volumeTrax = set.getInt("volume_trax");
      this.chatColor = RoomChatMessageBubbles.getBubble(set.getInt("chat_color"));
      this.hofPoints = set.getInt("hof_points");
      this.blockStaffAlerts = set.getString("block_alerts").equals("1");
      this.citizenshipLevel = set.getInt("talent_track_citizenship_level");
      this.helpersLevel = set.getInt("talent_track_helpers_level");
      this.ignoreBots = set.getString("ignore_bots").equalsIgnoreCase("1");
      this.ignorePets = set.getString("ignore_pets").equalsIgnoreCase("1");
      this.nux = set.getString("nux").equals("1");
      this.muteEndTime = set.getInt("mute_end_timestamp");
      this.allowNameChange = set.getString("allow_name_change").equalsIgnoreCase("1");
      this.perkTrade = set.getString("perk_trade").equalsIgnoreCase("1");
      this.forumPostsCount = set.getInt("forums_post_count");
      this.uiFlags = set.getInt("ui_flags");
      this.hasGottenDefaultSavedSearches = set.getInt("has_gotten_default_saved_searches") == 1;
      this.maxFriends = set.getInt("max_friends");
      this.maxRooms = set.getInt("max_rooms");
      this.lastHCPayday = set.getInt("last_hc_payday");
      this.hcGiftsClaimed = set.getInt("hc_gifts_claimed");
      this.nuxReward = this.nux;
      this.subscriptions = Emulator.getGameEnvironment().getSubscriptionManager().getSubscriptionsForUser(this.habboInfo.getId());
      PreparedStatement statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM user_window_settings WHERE user_id = ? LIMIT 1");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet nSet = statement.executeQuery();

         try {
            if (nSet.next()) {
               this.navigatorWindowSettings = new HabboNavigatorWindowSettings(nSet);
            } else {
               PreparedStatement stmt = statement.getConnection().prepareStatement("INSERT INTO user_window_settings (user_id) VALUES (?)");

               try {
                  stmt.setInt(1, this.habboInfo.getId());
                  stmt.executeUpdate();
               } catch (Throwable var27) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var26) {
                        var27.addSuppressed(var26);
                     }
                  }

                  throw var27;
               }

               if (stmt != null) {
                  stmt.close();
               }

               this.navigatorWindowSettings = new HabboNavigatorWindowSettings(habboInfo.getId());
            }
         } catch (Throwable var28) {
            if (nSet != null) {
               try {
                  nSet.close();
               } catch (Throwable var25) {
                  var28.addSuppressed(var25);
               }
            }

            throw var28;
         }

         if (nSet != null) {
            nSet.close();
         }
      } catch (Throwable var29) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var24) {
               var29.addSuppressed(var24);
            }
         }

         throw var29;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM users_navigator_settings WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet nSet = statement.executeQuery();

         try {
            while (nSet.next()) {
               this.navigatorWindowSettings.addDisplayMode(nSet.getString("caption"), new HabboNavigatorPersonalDisplayMode(nSet));
            }
         } catch (Throwable var44) {
            if (nSet != null) {
               try {
                  nSet.close();
               } catch (Throwable var23) {
                  var44.addSuppressed(var23);
               }
            }

            throw var44;
         }

         if (nSet != null) {
            nSet.close();
         }
      } catch (Throwable var45) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var22) {
               var45.addSuppressed(var22);
            }
         }

         throw var45;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM users_favorite_rooms WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet favoriteSet = statement.executeQuery();

         try {
            while (favoriteSet.next()) {
               this.favoriteRooms.add(favoriteSet.getInt("room_id"));
            }
         } catch (Throwable var42) {
            if (favoriteSet != null) {
               try {
                  favoriteSet.close();
               } catch (Throwable var21) {
                  var42.addSuppressed(var21);
               }
            }

            throw var42;
         }

         if (favoriteSet != null) {
            favoriteSet.close();
         }
      } catch (Throwable var43) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var20) {
               var43.addSuppressed(var20);
            }
         }

         throw var43;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM users_recipes WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet recipeSet = statement.executeQuery();

         try {
            while (recipeSet.next()) {
               this.secretRecipes.add(recipeSet.getInt("recipe"));
            }
         } catch (Throwable var40) {
            if (recipeSet != null) {
               try {
                  recipeSet.close();
               } catch (Throwable var19) {
                  var40.addSuppressed(var19);
               }
            }

            throw var40;
         }

         if (recipeSet != null) {
            recipeSet.close();
         }
      } catch (Throwable var41) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var18) {
               var41.addSuppressed(var18);
            }
         }

         throw var41;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM calendar_rewards_claimed WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet rewardSet = statement.executeQuery();

         try {
            while (rewardSet.next()) {
               this.calendarRewardsClaimed.add(new CalendarRewardClaimed(rewardSet));
            }
         } catch (Throwable var38) {
            if (rewardSet != null) {
               try {
                  rewardSet.close();
               } catch (Throwable var17) {
                  var38.addSuppressed(var17);
               }
            }

            throw var38;
         }

         if (rewardSet != null) {
            rewardSet.close();
         }
      } catch (Throwable var39) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var16) {
               var39.addSuppressed(var16);
            }
         }

         throw var39;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement()
         .getConnection()
         .prepareStatement("SELECT catalog_item_id, timestamp FROM catalog_items_limited WHERE user_id = ? AND timestamp > ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         statement.setInt(2, Emulator.getIntUnixTimestamp() - 86400);
         ResultSet ltdSet = statement.executeQuery();

         try {
            while (ltdSet.next()) {
               this.addLtdLog(ltdSet.getInt("catalog_item_id"), ltdSet.getInt("timestamp"));
            }
         } catch (Throwable var36) {
            if (ltdSet != null) {
               try {
                  ltdSet.close();
               } catch (Throwable var15) {
                  var36.addSuppressed(var15);
               }
            }

            throw var36;
         }

         if (ltdSet != null) {
            ltdSet.close();
         }
      } catch (Throwable var37) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var14) {
               var37.addSuppressed(var14);
            }
         }

         throw var37;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT target_id FROM users_ignored WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet ignoredSet = statement.executeQuery();

         try {
            while (ignoredSet.next()) {
               this.ignoredUsers.add(ignoredSet.getInt(1));
            }
         } catch (Throwable var34) {
            if (ignoredSet != null) {
               try {
                  ignoredSet.close();
               } catch (Throwable var13) {
                  var34.addSuppressed(var13);
               }
            }

            throw var34;
         }

         if (ignoredSet != null) {
            ignoredSet.close();
         }
      } catch (Throwable var35) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var12) {
               var35.addSuppressed(var12);
            }
         }

         throw var35;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT * FROM users_target_offer_purchases WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet offerSet = statement.executeQuery();

         try {
            while (offerSet.next()) {
               this.offerCache.put(offerSet.getInt("offer_id"), new HabboOfferPurchase(offerSet));
            }
         } catch (Throwable var32) {
            if (offerSet != null) {
               try {
                  offerSet.close();
               } catch (Throwable var11) {
                  var32.addSuppressed(var11);
               }
            }

            throw var32;
         }

         if (offerSet != null) {
            offerSet.close();
         }
      } catch (Throwable var33) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var10) {
               var33.addSuppressed(var10);
            }
         }

         throw var33;
      }

      if (statement != null) {
         statement.close();
      }

      statement = set.getStatement().getConnection().prepareStatement("SELECT DISTINCT room_id FROM room_enter_log WHERE user_id = ?");

      try {
         statement.setInt(1, this.habboInfo.getId());
         ResultSet roomSet = statement.executeQuery();

         try {
            while (roomSet.next()) {
               this.roomsVists.add(roomSet.getInt("room_id"));
            }
         } catch (Throwable var30) {
            if (roomSet != null) {
               try {
                  roomSet.close();
               } catch (Throwable var9) {
                  var30.addSuppressed(var9);
               }
            }

            throw var30;
         }

         if (roomSet != null) {
            roomSet.close();
         }
      } catch (Throwable var31) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var8) {
               var31.addSuppressed(var8);
            }
         }

         throw var31;
      }

      if (statement != null) {
         statement.close();
      }
   }

   private static HabboStats createNewStats(HabboInfo habboInfo) {
      habboInfo.firstVisit = true;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users_settings (user_id) VALUES (?)");

            try {
               statement.setInt(1, habboInfo.getId());
               statement.executeUpdate();
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

      return load(habboInfo);
   }

   public static HabboStats load(HabboInfo habboInfo) {
      HabboStats stats = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_settings WHERE user_id = ? LIMIT 1");

            try {
               statement.setInt(1, habboInfo.getId());
               ResultSet set = statement.executeQuery();

               try {
                  set.next();
                  if (set.getRow() != 0) {
                     stats = new HabboStats(set, habboInfo);
                  } else {
                     stats = createNewStats(habboInfo);
                  }
               } catch (Throwable var16) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var17) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var14) {
                     var17.addSuppressed(var14);
                  }
               }

               throw var17;
            }

            if (statement != null) {
               statement.close();
            }

            if (stats != null) {
               statement = connection.prepareStatement("SELECT guild_id FROM guilds_members WHERE user_id = ? AND level_id < 3 LIMIT 100");

               try {
                  statement.setInt(1, habboInfo.getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     for (int i = 0; set.next(); i++) {
                        stats.guilds.add(set.getInt("guild_id"));
                     }
                  } catch (Throwable var22) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var13) {
                           var22.addSuppressed(var13);
                        }
                     }

                     throw var22;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var23) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var12) {
                        var23.addSuppressed(var12);
                     }
                  }

                  throw var23;
               }

               if (statement != null) {
                  statement.close();
               }

               Collections.sort(stats.guilds);
               statement = connection.prepareStatement("SELECT room_id FROM room_votes WHERE user_id = ?");

               try {
                  statement.setInt(1, habboInfo.getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        stats.votedRooms.push(set.getInt("room_id"));
                     }
                  } catch (Throwable var20) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var11) {
                           var20.addSuppressed(var11);
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
                     } catch (Throwable var10) {
                        var21.addSuppressed(var10);
                     }
                  }

                  throw var21;
               }

               if (statement != null) {
                  statement.close();
               }

               statement = connection.prepareStatement("SELECT * FROM users_achievements WHERE user_id = ?");

               try {
                  statement.setInt(1, habboInfo.getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(set.getString("achievement_name"));
                        if (achievement != null) {
                           stats.achievementProgress.put(achievement, set.getInt("progress"));
                        }
                     }
                  } catch (Throwable var18) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var9) {
                           var18.addSuppressed(var9);
                        }
                     }

                     throw var18;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var19) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var8) {
                        var19.addSuppressed(var8);
                     }
                  }

                  throw var19;
               }

               if (statement != null) {
                  statement.close();
               }
            }
         } catch (Throwable var24) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var24.addSuppressed(var7);
               }
            }

            throw var24;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return stats;
   }

   @Override
   public void run() {
      int onlineTimeLast = this.lastOnlineTime.getAndUpdate(operand -> Emulator.getIntUnixTimestamp());
      int onlineTime = Emulator.getIntUnixTimestamp() - onlineTimeLast;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "UPDATE users_settings SET achievement_score = ?, respects_received = ?, respects_given = ?, daily_respect_points = ?, block_following = ?, block_friendrequests = ?, online_time = online_time + ?, guild_id = ?, daily_pet_respect_points = ?, club_expire_timestamp = ?, login_streak = ?, rent_space_id = ?, rent_space_endtime = ?, volume_system = ?, volume_furni = ?, volume_trax = ?, block_roominvites = ?, old_chat = ?, block_camera_follow = ?, chat_color = ?, hof_points = ?, block_alerts = ?, talent_track_citizenship_level = ?, talent_track_helpers_level = ?, ignore_bots = ?, ignore_pets = ?, nux = ?, mute_end_timestamp = ?, allow_name_change = ?, perk_trade = ?, can_trade = ?, `forums_post_count` = ?, ui_flags = ?, has_gotten_default_saved_searches = ?, max_friends = ?, max_rooms = ?, last_hc_payday = ?, hc_gifts_claimed = ? WHERE user_id = ? LIMIT 1"
            );

            try {
               statement.setInt(1, this.achievementScore);
               statement.setInt(2, this.respectPointsReceived);
               statement.setInt(3, this.respectPointsGiven);
               statement.setInt(4, this.respectPointsToGive);
               statement.setString(5, this.blockFollowing ? "1" : "0");
               statement.setString(6, this.blockFriendRequests ? "1" : "0");
               statement.setInt(7, onlineTime);
               statement.setInt(8, this.guild);
               statement.setInt(9, this.petRespectPointsToGive);
               statement.setInt(10, this.clubExpireTimestamp);
               statement.setInt(11, this.loginStreak);
               statement.setInt(12, this.rentedItemId);
               statement.setInt(13, this.rentedTimeEnd);
               statement.setInt(14, this.volumeSystem);
               statement.setInt(15, this.volumeFurni);
               statement.setInt(16, this.volumeTrax);
               statement.setString(17, this.blockRoomInvites ? "1" : "0");
               statement.setString(18, this.preferOldChat ? "1" : "0");
               statement.setString(19, this.blockCameraFollow ? "1" : "0");
               statement.setInt(20, this.chatColor.getType());
               statement.setInt(21, this.hofPoints);
               statement.setString(22, this.blockStaffAlerts ? "1" : "0");
               statement.setInt(23, this.citizenshipLevel);
               statement.setInt(24, this.helpersLevel);
               statement.setString(25, this.ignoreBots ? "1" : "0");
               statement.setString(26, this.ignorePets ? "1" : "0");
               statement.setString(27, this.nux ? "1" : "0");
               statement.setInt(28, this.muteEndTime);
               statement.setString(29, this.allowNameChange ? "1" : "0");
               statement.setString(30, this.perkTrade ? "1" : "0");
               statement.setString(31, this.allowTrade ? "1" : "0");
               statement.setInt(32, this.forumPostsCount);
               statement.setInt(33, this.uiFlags);
               statement.setInt(34, this.hasGottenDefaultSavedSearches ? 1 : 0);
               statement.setInt(35, this.maxFriends);
               statement.setInt(36, this.maxRooms);
               statement.setInt(37, this.lastHCPayday);
               statement.setInt(38, this.hcGiftsClaimed);
               statement.setInt(39, this.habboInfo.getId());
               statement.executeUpdate();
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var10) {
                     var12.addSuppressed(var10);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }

            statement = connection.prepareStatement(
               "UPDATE user_window_settings SET x = ?, y = ?, width = ?, height = ?, open_searches = ? WHERE user_id = ? LIMIT 1"
            );

            try {
               statement.setInt(1, this.navigatorWindowSettings.x);
               statement.setInt(2, this.navigatorWindowSettings.y);
               statement.setInt(3, this.navigatorWindowSettings.width);
               statement.setInt(4, this.navigatorWindowSettings.height);
               statement.setString(5, this.navigatorWindowSettings.openSearches ? "1" : "0");
               statement.setInt(6, this.habboInfo.getId());
               statement.executeUpdate();
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var11.addSuppressed(var9);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }

            if (!this.offerCache.isEmpty()) {
               statement = connection.prepareStatement(
                  "UPDATE users_target_offer_purchases SET state = ?, amount = ?, last_purchase = ? WHERE user_id = ? AND offer_id = ?"
               );

               try {
                  for (HabboOfferPurchase purchase : this.offerCache.valueCollection()) {
                     if (purchase.needsUpdate()) {
                        statement.setInt(1, purchase.getState());
                        statement.setInt(2, purchase.getAmount());
                        statement.setInt(3, purchase.getLastPurchaseTimestamp());
                        statement.setInt(4, this.habboInfo.getId());
                        statement.setInt(5, purchase.getOfferId());
                        statement.execute();
                     }
                  }
               } catch (Throwable var13) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var8) {
                        var13.addSuppressed(var8);
                     }
                  }

                  throw var13;
               }

               if (statement != null) {
                  statement.close();
               }
            }

            this.navigatorWindowSettings.save(connection);
         } catch (Throwable var14) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var14.addSuppressed(var7);
               }
            }

            throw var14;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void dispose() {
      this.run();
      this.habboInfo = null;
      this.recentPurchases.clear();
   }

   public void addGuild(int guildId) {
      if (!this.guilds.contains(guildId)) {
         this.guilds.add(guildId);
      }
   }

   public void removeGuild(int guildId) {
      this.guilds.remove(Integer.valueOf(guildId));
   }

   public boolean hasGuild(int guildId) {
      for (int i : this.guilds) {
         if (i == guildId) {
            return true;
         }
      }

      return false;
   }

   public int getAchievementScore() {
      return this.achievementScore;
   }

   public void addAchievementScore(int achievementScore) {
      this.achievementScore += achievementScore;
   }

   public int getAchievementProgress(Achievement achievement) {
      return this.achievementProgress.containsKey(achievement) ? (Integer)this.achievementProgress.get(achievement) : -1;
   }

   public void setProgress(Achievement achievement, int progress) {
      this.achievementProgress.put(achievement, progress);
   }

   public int getRentedTimeEnd() {
      return this.rentedTimeEnd;
   }

   public void setRentedTimeEnd(int rentedTimeEnd) {
      this.rentedTimeEnd = rentedTimeEnd;
   }

   public int getRentedItemId() {
      return this.rentedItemId;
   }

   public void setRentedItemId(int rentedItemId) {
      this.rentedItemId = rentedItemId;
   }

   public boolean isRentingSpace() {
      return this.rentedTimeEnd >= Emulator.getIntUnixTimestamp();
   }

   public Subscription getSubscription(String subscriptionType) {
      TObjectHashIterator var2 = this.subscriptions.iterator();

      while (var2.hasNext()) {
         Subscription subscription = (Subscription)var2.next();
         if (subscription.getSubscriptionType().equalsIgnoreCase(subscriptionType) && subscription.isActive() && subscription.getRemaining() > 0) {
            return subscription;
         }
      }

      return null;
   }

   public boolean hasSubscription(String subscriptionType) {
      Subscription subscription = this.getSubscription(subscriptionType);
      return subscription != null;
   }

   public int getSubscriptionExpireTimestamp(String subscriptionType) {
      Subscription subscription = this.getSubscription(subscriptionType);
      return subscription == null ? 0 : subscription.getTimestampEnd();
   }

   public Subscription createSubscription(String subscriptionType, int duration) {
      Subscription subscription = this.getSubscription(subscriptionType);
      if (subscription != null) {
         if (!Emulator.getPluginManager().fireEvent(new UserSubscriptionExtendedEvent(this.habboInfo.getId(), subscription, duration)).isCancelled()) {
            subscription.addDuration(duration);
            subscription.onExtended(duration);
         }

         return subscription;
      } else {
         if (!Emulator.getPluginManager().fireEvent(new UserSubscriptionCreatedEvent(this.habboInfo.getId(), subscriptionType, duration)).isCancelled()) {
            int startTimestamp = Emulator.getIntUnixTimestamp();

            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               Subscription var11;
               label136: {
                  try {
                     PreparedStatement statement;
                     label124: {
                        statement = connection.prepareStatement(
                           "INSERT INTO `users_subscriptions` (`user_id`, `subscription_type`, `timestamp_start`, `duration`, `active`) VALUES (?, ?, ?, ?, ?)",
                           1
                        );

                        try {
                           statement.setInt(1, this.habboInfo.getId());
                           statement.setString(2, subscriptionType);
                           statement.setInt(3, startTimestamp);
                           statement.setInt(4, duration);
                           statement.setInt(5, 1);
                           statement.execute();
                           ResultSet set = statement.getGeneratedKeys();

                           label102: {
                              try {
                                 if (set.next()) {
                                    Class<? extends Subscription> subClazz = Emulator.getGameEnvironment()
                                       .getSubscriptionManager()
                                       .getSubscriptionClass(subscriptionType);

                                    try {
                                       Constructor<? extends Subscription> c = subClazz.getConstructor(
                                          Integer.class, Integer.class, String.class, Integer.class, Integer.class, Boolean.class
                                       );
                                       c.setAccessible(true);
                                       Subscription sub = c.newInstance(set.getInt(1), this.habboInfo.getId(), subscriptionType, startTimestamp, duration, true);
                                       this.subscriptions.add(sub);
                                       sub.onCreated();
                                       var11 = sub;
                                       break label102;
                                    } catch (Exception e) {
                                       LOGGER.error("Caught exception", e);
                                    }
                                 }
                              } catch (Throwable var16) {
                                 if (set != null) {
                                    try {
                                       set.close();
                                    } catch (Throwable var14) {
                                       var16.addSuppressed(var14);
                                    }
                                 }

                                 throw var16;
                              }

                              if (set != null) {
                                 set.close();
                              }
                              break label124;
                           }

                           if (set != null) {
                              set.close();
                           }
                        } catch (Throwable var17) {
                           if (statement != null) {
                              try {
                                 statement.close();
                              } catch (Throwable var13) {
                                 var17.addSuppressed(var13);
                              }
                           }

                           throw var17;
                        }

                        if (statement != null) {
                           statement.close();
                        }
                        break label136;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                  } catch (Throwable var18) {
                     if (connection != null) {
                        try {
                           connection.close();
                        } catch (Throwable var12) {
                           var18.addSuppressed(var12);
                        }
                     }

                     throw var18;
                  }

                  if (connection != null) {
                     connection.close();
                  }

                  return null;
               }

               if (connection != null) {
                  connection.close();
               }

               return var11;
            } catch (SQLException e) {
               LOGGER.error("Caught SQL exception", e);
            }
         }

         return null;
      }
   }

   public int getClubExpireTimestamp() {
      return this.getSubscriptionExpireTimestamp("HABBO_CLUB");
   }

   public void setClubExpireTimestamp(int clubExpireTimestamp) {
      Subscription subscription = this.getSubscription("HABBO_CLUB");
      int duration = clubExpireTimestamp - Emulator.getIntUnixTimestamp();
      if (subscription != null) {
         duration = clubExpireTimestamp - subscription.getTimestampStart();
      }

      if (duration > 0) {
         this.createSubscription("HABBO_CLUB", duration);
      }
   }

   public boolean hasActiveClub() {
      return this.hasSubscription("HABBO_CLUB");
   }

   public int getPastTimeAsClub() {
      int pastTimeAsHC = 0;
      TObjectHashIterator var2 = this.subscriptions.iterator();

      while (var2.hasNext()) {
         Subscription subs = (Subscription)var2.next();
         if (subs.getSubscriptionType().equalsIgnoreCase("HABBO_CLUB")) {
            pastTimeAsHC += subs.getDuration() - Math.max(subs.getRemaining(), 0);
         }
      }

      return pastTimeAsHC;
   }

   public int getTimeTillNextClubGift() {
      int pastTimeAsClub = this.getPastTimeAsClub();
      int totalGifts = (int)Math.ceil(pastTimeAsClub / 2678400.0);
      return totalGifts * 2678400 - pastTimeAsClub;
   }

   public int getRemainingClubGifts() {
      int totalGifts = (int)Math.ceil(this.getPastTimeAsClub() / 2678400.0);
      return totalGifts - this.hcGiftsClaimed;
   }

   public THashMap<Achievement, Integer> getAchievementProgress() {
      return this.achievementProgress;
   }

   public THashMap<Achievement, Integer> getAchievementCache() {
      return this.achievementCache;
   }

   public void addPurchase(CatalogItem item) {
      if (!this.recentPurchases.containsKey(item.getId())) {
         this.recentPurchases.put(item.getId(), item);
      }
   }

   public THashMap<Integer, CatalogItem> getRecentPurchases() {
      return this.recentPurchases;
   }

   public void disposeRecentPurchases() {
      this.recentPurchases.clear();
   }

   public boolean addFavoriteRoom(int roomId) {
      if (this.favoriteRooms.contains(roomId)) {
         return false;
      }

      if (Emulator.getConfig().getInt("hotel.rooms.max.favorite") <= this.favoriteRooms.size()) {
         return false;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users_favorite_rooms (user_id, room_id) VALUES (?, ?)");

            try {
               statement.setInt(1, this.habboInfo.getId());
               statement.setInt(2, roomId);
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

      this.favoriteRooms.add(roomId);
      return true;
   }

   public void removeFavoriteRoom(int roomId) {
      if (this.favoriteRooms.remove(roomId)) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("DELETE FROM users_favorite_rooms WHERE user_id = ? AND room_id = ? LIMIT 1");

               try {
                  statement.setInt(1, this.habboInfo.getId());
                  statement.setInt(2, roomId);
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
   }

   public boolean hasFavoriteRoom(int roomId) {
      return this.favoriteRooms.contains(roomId);
   }

   public boolean visitedRoom(int roomId) {
      return this.roomsVists.contains(roomId);
   }

   public void addVisitRoom(int roomId) {
      this.roomsVists.add(roomId);
   }

   public TIntArrayList getFavoriteRooms() {
      return this.favoriteRooms;
   }

   public boolean hasRecipe(int id) {
      return this.secretRecipes.contains(id);
   }

   public boolean addRecipe(int id) {
      if (this.secretRecipes.contains(id)) {
         return false;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users_recipes (user_id, recipe) VALUES (?, ?)");

            try {
               statement.setInt(1, this.habboInfo.getId());
               statement.setInt(2, id);
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

      this.secretRecipes.add(id);
      return true;
   }

   public int talentTrackLevel(TalentTrackType type) {
      if (type == TalentTrackType.CITIZENSHIP) {
         return this.citizenshipLevel;
      } else {
         return type == TalentTrackType.HELPER ? this.helpersLevel : -1;
      }
   }

   public void setTalentLevel(TalentTrackType type, int level) {
      if (type == TalentTrackType.CITIZENSHIP) {
         this.citizenshipLevel = level;
      } else if (type == TalentTrackType.HELPER) {
         this.helpersLevel = level;
      }
   }

   public int getMuteEndTime() {
      return this.muteEndTime;
   }

   public int addMuteTime(int seconds) {
      if (this.remainingMuteTime() == 0) {
         this.muteEndTime = Emulator.getIntUnixTimestamp();
      }

      this.mutedBubbleTracker = true;
      this.muteEndTime += seconds;
      return this.remainingMuteTime();
   }

   public int remainingMuteTime() {
      return Math.max(0, this.muteEndTime - Emulator.getIntUnixTimestamp());
   }

   public boolean allowTalk() {
      return this.remainingMuteTime() == 0;
   }

   public void unMute() {
      this.muteEndTime = 0;
      this.mutedBubbleTracker = false;
   }

   public void addLtdLog(int catalogItemId, int timestamp) {
      if (!this.ltdPurchaseLog.containsKey(catalogItemId)) {
         this.ltdPurchaseLog.put(catalogItemId, new ArrayList(1));
      }

      ((List)this.ltdPurchaseLog.get(catalogItemId)).add(timestamp);
   }

   public int totalLtds() {
      int total = 0;

      for (Entry<Integer, List<Integer>> entry : this.ltdPurchaseLog.entrySet()) {
         total += entry.getValue().size();
      }

      return total;
   }

   public int totalLtds(int catalogItemId) {
      return this.ltdPurchaseLog.containsKey(catalogItemId) ? ((List)this.ltdPurchaseLog.get(catalogItemId)).size() : 0;
   }

   public boolean ignoreUser(GameClient gameClient, int userId) {
      Habbo target = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);
      if (!Emulator.getConfig().getBoolean("hotel.allow.ignore.staffs")) {
         int ownRank = gameClient.getHabbo().getHabboInfo().getRank().getId();
         int targetRank = target.getHabboInfo().getRank().getId();
         if (targetRank >= ownRank) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.error.ignore_higher_rank"), RoomChatMessageBubbles.ALERT);
            return false;
         }
      }

      if (!this.userIgnored(userId)) {
         this.ignoredUsers.add(userId);

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO users_ignored (user_id, target_id) VALUES (?, ?)");

               try {
                  statement.setInt(1, this.habboInfo.getId());
                  statement.setInt(2, userId);
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

      return true;
   }

   public void unignoreUser(int userId) {
      if (this.userIgnored(userId)) {
         this.ignoredUsers.remove(userId);

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("DELETE FROM users_ignored WHERE user_id = ? AND target_id = ?");

               try {
                  statement.setInt(1, this.habboInfo.getId());
                  statement.setInt(2, userId);
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
   }

   public boolean userIgnored(int userId) {
      return this.ignoredUsers.contains(userId);
   }

   public boolean allowTrade() {
      return AchievementManager.TALENTTRACK_ENABLED && RoomTrade.TRADING_REQUIRES_PERK ? this.perkTrade && this.allowTrade : this.allowTrade;
   }

   public void setAllowTrade(boolean allowTrade) {
      this.allowTrade = allowTrade;
   }

   public HabboOfferPurchase getHabboOfferPurchase(int offerId) {
      return (HabboOfferPurchase)this.offerCache.get(offerId);
   }

   public void addHabboOfferPurchase(HabboOfferPurchase offerPurchase) {
      this.offerCache.put(offerPurchase.getOfferId(), offerPurchase);
   }
}
