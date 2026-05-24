package com.eu.habbo.habbohotel.messenger;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.friends.UpdateFriendComposer;
import com.eu.habbo.plugin.events.users.friends.UserAcceptFriendRequestEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messenger {
   private static final Logger LOGGER = LoggerFactory.getLogger(Messenger.class);
   public static boolean SAVE_PRIVATE_CHATS = false;
   public static int MAXIMUM_FRIENDS = 200;
   public static int MAXIMUM_FRIENDS_HC = 500;
   private final ConcurrentHashMap<Integer, MessengerBuddy> friends = new ConcurrentHashMap<>();
   private final THashSet<FriendRequest> friendRequests = new THashSet();

   public static void unfriend(int userOne, int userTwo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "DELETE FROM messenger_friendships WHERE (user_one_id = ? AND user_two_id = ?) OR (user_one_id = ? AND user_two_id = ?)"
            );

            try {
               statement.setInt(1, userOne);
               statement.setInt(2, userTwo);
               statement.setInt(3, userTwo);
               statement.setInt(4, userOne);
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

   public static THashSet<MessengerBuddy> searchUsers(String username) {
      THashSet<MessengerBuddy> users = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT * FROM users WHERE username LIKE ? ORDER BY username ASC LIMIT " + Emulator.getConfig().getInt("hotel.messenger.search.maxresults")
            );

            try {
               statement.setString(1, username + "%");
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     users.add(new MessengerBuddy(set, false));
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return users;
   }

   @Deprecated
   public static boolean canFriendRequest(Habbo habbo, String friend) {
      Habbo user = Emulator.getGameEnvironment().getHabboManager().getHabbo(friend);
      HabboInfo habboInfo;
      if (user != null) {
         habboInfo = user.getHabboInfo();
      } else {
         habboInfo = HabboManager.getOfflineHabboInfo(friend);
      }

      if (habboInfo == null) {
         return false;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var7;
         label121: {
            try {
               PreparedStatement statement;
               label113: {
                  statement = connection.prepareStatement(
                     "SELECT * FROM messenger_friendrequests WHERE (user_to_id = ? AND user_from_id = ?) OR (user_to_id = ? AND user_from_id = ?) LIMIT 1"
                  );

                  try {
                     statement.setInt(1, habbo.getHabboInfo().getId());
                     statement.setInt(2, habboInfo.getId());
                     statement.setInt(3, habboInfo.getId());
                     statement.setInt(4, habbo.getHabboInfo().getId());
                     ResultSet set = statement.executeQuery();

                     label91: {
                        try {
                           if (!set.next()) {
                              var7 = true;
                              break label91;
                           }
                        } catch (Throwable var12) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (set != null) {
                           set.close();
                        }
                        break label113;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var13) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var10) {
                           var13.addSuppressed(var10);
                        }
                     }

                     throw var13;
                  }

                  if (statement != null) {
                     statement.close();
                  }
                  break label121;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var14) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var14.addSuppressed(var9);
                  }
               }

               throw var14;
            }

            if (connection != null) {
               connection.close();
            }

            return false;
         }

         if (connection != null) {
            connection.close();
         }

         return var7;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public static boolean friendRequested(int userFrom, int userTo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var5;
         label109: {
            try {
               PreparedStatement statement;
               label102: {
                  statement = connection.prepareStatement("SELECT * FROM messenger_friendrequests WHERE user_to_id = ? AND user_from_id = ? LIMIT 1");

                  try {
                     statement.setInt(1, userFrom);
                     statement.setInt(2, userTo);
                     ResultSet set = statement.executeQuery();

                     label84: {
                        try {
                           if (set.next()) {
                              var5 = true;
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

            return false;
         }

         if (connection != null) {
            connection.close();
         }

         return var5;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public static void makeFriendRequest(int userFrom, int userTo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO messenger_friendrequests (user_to_id, user_from_id) VALUES (?, ?)");

            try {
               statement.setInt(1, userTo);
               statement.setInt(2, userFrom);
               statement.executeUpdate();
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

   public static int getFriendCount(int userId) {
      Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(userId);
      if (habbo != null) {
         return habbo.getMessenger().getFriends().size();
      }

      int count = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT count(id) as count FROM messenger_friendships WHERE user_one_id = ?");

            try {
               statement.setInt(1, userId);
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     count = set.getInt("count");
                  }
               } catch (Throwable var11) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return count;
   }

   public static THashMap<Integer, THashSet<MessengerBuddy>> getFriends(int userId) {
      THashMap<Integer, THashSet<MessengerBuddy>> map = new THashMap();
      map.put(1, new THashSet());
      map.put(2, new THashSet());
      map.put(3, new THashSet());

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.id, users.look, users.username, messenger_friendships.relation FROM messenger_friendships INNER JOIN users ON users.id = messenger_friendships.user_two_id WHERE user_one_id = ? ORDER BY RAND() LIMIT 50"
            );

            try {
               statement.setInt(1, userId);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     if (set.getInt("relation") != 0) {
                        MessengerBuddy buddy = new MessengerBuddy(
                           set.getInt("id"), set.getString("username"), set.getString("look"), (short)set.getInt("relation"), userId
                        );
                        ((THashSet)map.get(Integer.valueOf(buddy.getRelation()))).add(buddy);
                     }
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return map;
   }

   public static void checkFriendSizeProgress(Habbo habbo) {
      int progress = habbo.getHabboStats().getAchievementProgress(Emulator.getGameEnvironment().getAchievementManager().getAchievement("FriendListSize"));
      int toProgress = 1;
      Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("FriendListSize");
      if (achievement != null) {
         if (progress > 0) {
            toProgress = habbo.getMessenger().getFriends().size() - progress;
            if (toProgress < 0) {
               return;
            }
         }

         AchievementManager.progressAchievement(habbo, achievement, toProgress);
      }
   }

   public void loadFriends(Habbo habbo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.id, users.username, users.gender, users.online, users.look, users.motto, messenger_friendships.* FROM messenger_friendships INNER JOIN users ON messenger_friendships.user_two_id = users.id WHERE user_one_id = ?"
            );

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     MessengerBuddy buddy = new MessengerBuddy(set);
                     if (buddy.getId() != habbo.getHabboInfo().getId()) {
                        this.friends.putIfAbsent(set.getInt("id"), buddy);
                     }
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public MessengerBuddy loadFriend(Habbo habbo, int userId) {
      MessengerBuddy buddy = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.id, users.username, users.gender, users.online, users.look, users.motto, messenger_friendships.* FROM messenger_friendships INNER JOIN users ON messenger_friendships.user_two_id = users.id WHERE user_one_id = ? AND user_two_id = ? LIMIT 1"
            );

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               statement.setInt(2, userId);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     buddy = new MessengerBuddy(set);
                     this.friends.putIfAbsent(set.getInt("id"), buddy);
                  }
               } catch (Throwable var12) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var13) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var14) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var9) {
                  var14.addSuppressed(var9);
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

      return buddy;
   }

   public void loadFriendRequests(Habbo habbo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.id, users.username, users.look FROM messenger_friendrequests INNER JOIN users ON user_from_id = users.id WHERE user_to_id = ?"
            );

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     this.friendRequests.add(new FriendRequest(set));
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void removeBuddy(int id) {
      this.friends.remove(id);
   }

   public void removeBuddy(Habbo habbo) {
      this.friends.remove(habbo.getHabboInfo().getId());
   }

   public void addBuddy(MessengerBuddy buddy) {
      this.friends.put(buddy.getId(), buddy);
   }

   public THashSet<MessengerBuddy> getFriends(String username) {
      THashSet<MessengerBuddy> users = new THashSet();

      for (Entry<Integer, MessengerBuddy> map : this.friends.entrySet()) {
         if (StringUtils.containsIgnoreCase(map.getValue().getUsername(), username)) {
            users.add(map.getValue());
         }
      }

      return users;
   }

   public void connectionChanged(Habbo owner, boolean online, boolean inRoom) {
      if (owner != null) {
         for (Entry<Integer, MessengerBuddy> map : this.getFriends().entrySet()) {
            if (map.getValue().getOnline() != 0) {
               Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(map.getKey());
               if (habbo != null && habbo.getMessenger() != null) {
                  MessengerBuddy buddy = habbo.getMessenger().getFriend(owner.getHabboInfo().getId());
                  if (buddy != null) {
                     buddy.setOnline(online);
                     buddy.inRoom(inRoom);
                     buddy.setLook(owner.getHabboInfo().getLook());
                     buddy.setGender(owner.getHabboInfo().getGender());
                     buddy.setUsername(owner.getHabboInfo().getUsername());
                     habbo.getClient().sendResponse(new UpdateFriendComposer(habbo, buddy, 0));
                  }
               }
            }
         }
      }
   }

   public void deleteAllFriendRequests(int userTo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM messenger_friendrequests WHERE user_to_id = ?");

            try {
               statement.setInt(1, userTo);
               statement.executeUpdate();
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

   public int deleteFriendRequests(int userFrom, int userTo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         int var5;
         try {
            PreparedStatement statement = connection.prepareStatement(
               "DELETE FROM messenger_friendrequests WHERE (user_to_id = ? AND user_from_id = ?) OR (user_to_id = ? AND user_from_id = ?)"
            );

            try {
               statement.setInt(1, userTo);
               statement.setInt(2, userFrom);
               statement.setInt(4, userTo);
               statement.setInt(3, userFrom);
               var5 = statement.executeUpdate();
            } catch (Throwable var9) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var10) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
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

   public void acceptFriendRequest(int userFrom, int userTo) {
      int count = this.deleteFriendRequests(userFrom, userTo);
      if (count > 0) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "INSERT INTO messenger_friendships (user_one_id, user_two_id, friends_since) VALUES (?, ?, ?)"
               );

               try {
                  statement.setInt(1, userFrom);
                  statement.setInt(2, userTo);
                  statement.setInt(3, Emulator.getIntUnixTimestamp());
                  statement.execute();
                  statement.setInt(1, userTo);
                  statement.setInt(2, userFrom);
                  statement.setInt(3, Emulator.getIntUnixTimestamp());
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

         Habbo habboTo = Emulator.getGameServer().getGameClientManager().getHabbo(userTo);
         Habbo habboFrom = Emulator.getGameServer().getGameClientManager().getHabbo(userFrom);
         if (habboTo != null && habboFrom != null) {
            MessengerBuddy to = new MessengerBuddy(habboFrom, habboTo.getHabboInfo().getId());
            MessengerBuddy from = new MessengerBuddy(habboTo, habboFrom.getHabboInfo().getId());
            habboTo.getMessenger().friends.putIfAbsent(habboFrom.getHabboInfo().getId(), to);
            habboFrom.getMessenger().friends.putIfAbsent(habboTo.getHabboInfo().getId(), from);
            if (Emulator.getPluginManager().fireEvent(new UserAcceptFriendRequestEvent(habboTo, from)).isCancelled()) {
               this.removeBuddy(userTo);
               return;
            }

            habboTo.getClient().sendResponse(new UpdateFriendComposer(habboTo, to, 1));
            habboFrom.getClient().sendResponse(new UpdateFriendComposer(habboFrom, from, 1));
         } else if (habboTo != null) {
            habboTo.getClient().sendResponse(new UpdateFriendComposer(habboTo, this.loadFriend(habboTo, userFrom), 1));
         } else if (habboFrom != null) {
            habboFrom.getClient().sendResponse(new UpdateFriendComposer(habboFrom, this.loadFriend(habboFrom, userTo), 1));
         }
      }
   }

   public ConcurrentHashMap<Integer, MessengerBuddy> getFriends() {
      return this.friends;
   }

   public THashSet<FriendRequest> getFriendRequests() {
      synchronized (this.friendRequests) {
         return this.friendRequests;
      }
   }

   public FriendRequest findFriendRequest(String username) {
      synchronized (this.friendRequests) {
         TObjectHashIterator var3 = this.friendRequests.iterator();

         while (var3.hasNext()) {
            FriendRequest friendRequest = (FriendRequest)var3.next();
            if (friendRequest.getUsername().equalsIgnoreCase(username)) {
               return friendRequest;
            }
         }

         return null;
      }
   }

   public MessengerBuddy getFriend(int id) {
      return this.friends.get(id);
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
