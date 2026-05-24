package com.eu.habbo.habbohotel.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.guilds.forums.ForumView;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.guilds.GuildJoinErrorComposer;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);
   private final THashMap<GuildPartType, THashMap<Integer, GuildPart>> guildParts;
   private final TIntObjectMap<Guild> guilds;
   private final THashSet<ForumView> views = new THashSet();

   public GuildManager() {
      long millis = System.currentTimeMillis();
      this.guildParts = new THashMap();
      this.guilds = TCollections.synchronizedMap(new TIntObjectHashMap());
      this.loadGuildParts();
      this.loadGuildViews();
      LOGGER.info("Guild Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public void loadGuildParts() {
      this.guildParts.clear();

      for (GuildPartType t : GuildPartType.values()) {
         this.guildParts.put(t, new THashMap());
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM guilds_elements");

               try {
                  while (set.next()) {
                     ((THashMap)this.guildParts.get(GuildPartType.valueOf(set.getString("type").toUpperCase()))).put(set.getInt("id"), new GuildPart(set));
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

   public void loadGuildViews() {
      this.views.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM guild_forum_views");

               try {
                  while (set.next()) {
                     this.views.add(new ForumView(set));
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

   public Guild createGuild(Habbo habbo, int roomId, String roomName, String name, String description, String badge, int colorOne, int colorTwo) {
      Guild guild = new Guild(habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername(), roomId, roomName, name, description, colorOne, colorTwo, badge);

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO guilds (name, description, room_id, user_id, color_one, color_two, badge, date_created) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 1
            );

            try {
               statement.setString(1, name);
               statement.setString(2, description);
               statement.setInt(3, roomId);
               statement.setInt(4, guild.getOwnerId());
               statement.setInt(5, colorOne);
               statement.setInt(6, colorTwo);
               statement.setString(7, badge);
               statement.setInt(8, Emulator.getIntUnixTimestamp());
               statement.execute();
               ResultSet set = statement.getGeneratedKeys();

               try {
                  if (set.next()) {
                     guild.setId(set.getInt(1));
                  }
               } catch (Throwable var20) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var19) {
                        var20.addSuppressed(var19);
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
                  } catch (Throwable var18) {
                     var21.addSuppressed(var18);
                  }
               }

               throw var21;
            }

            if (statement != null) {
               statement.close();
            }

            statement = connection.prepareStatement("INSERT INTO guilds_members (guild_id, user_id, level_id, member_since) VALUES (?, ?, ?, ?)", 1);

            try {
               statement.setInt(1, guild.getId());
               statement.setInt(2, habbo.getHabboInfo().getId());
               statement.setInt(3, 0);
               statement.setInt(4, Emulator.getIntUnixTimestamp());
               statement.execute();
               ResultSet set = statement.getGeneratedKeys();

               try {
                  if (set.next()) {
                     guild.increaseMemberCount();
                  }
               } catch (Throwable var22) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var17) {
                        var22.addSuppressed(var17);
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
                  } catch (Throwable var16) {
                     var23.addSuppressed(var16);
                  }
               }

               throw var23;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var24) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var15) {
                  var24.addSuppressed(var15);
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

      habbo.getHabboStats().addGuild(guild.getId());
      return guild;
   }

   public void deleteGuild(Guild guild) {
      THashSet<GuildMember> members = this.getGuildMembers(guild);
      TObjectHashIterator e = members.iterator();

      while (e.hasNext()) {
         GuildMember member = (GuildMember)e.next();
         Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(member.getUserId());
         if (habbo != null) {
            habbo.getHabboStats().removeGuild(guild.getId());
            if (habbo.getHabboStats().guild == guild.getId()) {
               habbo.getHabboStats().guild = 0;
            }
         }
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement deleteFavourite = connection.prepareStatement("UPDATE users_settings SET guild_id = ? WHERE guild_id = ?");

            try {
               deleteFavourite.setInt(1, 0);
               deleteFavourite.setInt(2, guild.getId());
               deleteFavourite.execute();
            } catch (Throwable var13) {
               if (deleteFavourite != null) {
                  try {
                     deleteFavourite.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (deleteFavourite != null) {
               deleteFavourite.close();
            }

            deleteFavourite = connection.prepareStatement("DELETE FROM guilds_members WHERE guild_id = ?");

            try {
               deleteFavourite.setInt(1, guild.getId());
               deleteFavourite.execute();
            } catch (Throwable var12) {
               if (deleteFavourite != null) {
                  try {
                     deleteFavourite.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (deleteFavourite != null) {
               deleteFavourite.close();
            }

            deleteFavourite = connection.prepareStatement("DELETE FROM guilds WHERE id = ?");

            try {
               deleteFavourite.setInt(1, guild.getId());
               deleteFavourite.execute();
            } catch (Throwable var11) {
               if (deleteFavourite != null) {
                  try {
                     deleteFavourite.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (deleteFavourite != null) {
               deleteFavourite.close();
            }

            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
            if (room != null) {
               room.setGuild(0);
            }
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
      } catch (SQLException ex) {
         LOGGER.error("Caught SQL exception", ex);
      }
   }

   public void clearInactiveGuilds() {
      List<Integer> toRemove = new ArrayList<>();
      TIntObjectIterator<Guild> guilds = this.guilds.iterator();
      int i = this.guilds.size();

      while (i-- > 0) {
         try {
            guilds.advance();
         } catch (NoSuchElementException e) {
            break;
         }

         if (((Guild)guilds.value()).lastRequested < Emulator.getIntUnixTimestamp() - 300) {
            toRemove.add(((Guild)guilds.value()).getId());
         }
      }

      for (Integer ix : toRemove) {
         this.guilds.remove(ix);
      }
   }

   public void joinGuild(Guild guild, GameClient client, int userId, boolean acceptRequest) {
      boolean error = false;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE user_id = ?");

            try {
               if (userId == 0) {
                  statement.setInt(1, client.getHabbo().getHabboInfo().getId());
               } else {
                  statement.setInt(1, userId);
               }

               ResultSet set = statement.executeQuery();

               try {
                  if (set.next() && set.getInt(1) >= 100) {
                     if (userId == 0) {
                        client.sendResponse(new GuildJoinErrorComposer(1));
                     } else {
                        client.sendResponse(new GuildJoinErrorComposer(5));
                     }

                     error = true;
                  }
               } catch (Throwable var27) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var21) {
                        var27.addSuppressed(var21);
                     }
                  }

                  throw var27;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var28) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var20) {
                     var28.addSuppressed(var20);
                  }
               }

               throw var28;
            }

            if (statement != null) {
               statement.close();
            }

            if (!error) {
               statement = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND level_id < 3");

               try {
                  statement.setInt(1, guild.getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     if (set.next() && set.getInt(1) >= 50000) {
                        client.sendResponse(new GuildJoinErrorComposer(0));
                        error = true;
                     }
                  } catch (Throwable var26) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var19) {
                           var26.addSuppressed(var19);
                        }
                     }

                     throw var26;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var31) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var18) {
                        var31.addSuppressed(var18);
                     }
                  }

                  throw var31;
               }

               if (statement != null) {
                  statement.close();
               }

               if (userId == 0 && !error) {
                  if (guild.getState() == GuildState.EXCLUSIVE) {
                     statement = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND level_id = 3");

                     try {
                        statement.setInt(1, guild.getId());
                        ResultSet set = statement.executeQuery();

                        try {
                           if (set.next() && set.getInt(1) >= 100) {
                              client.sendResponse(new GuildJoinErrorComposer(3));
                              error = true;
                           }
                        } catch (Throwable var25) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var17) {
                                 var25.addSuppressed(var17);
                              }
                           }

                           throw var25;
                        }

                        if (set != null) {
                           set.close();
                        }
                     } catch (Throwable var30) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var16) {
                              var30.addSuppressed(var16);
                           }
                        }

                        throw var30;
                     }

                     if (statement != null) {
                        statement.close();
                     }

                     if (!error) {
                        statement = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND user_id = ? LIMIT 1");

                        try {
                           statement.setInt(1, guild.getId());
                           statement.setInt(2, client.getHabbo().getHabboInfo().getId());
                           ResultSet set = statement.executeQuery();

                           try {
                              if (set.next() && set.getInt(1) >= 1) {
                                 error = true;
                              }
                           } catch (Throwable var24) {
                              if (set != null) {
                                 try {
                                    set.close();
                                 } catch (Throwable var15) {
                                    var24.addSuppressed(var15);
                                 }
                              }

                              throw var24;
                           }

                           if (set != null) {
                              set.close();
                           }
                        } catch (Throwable var29) {
                           if (statement != null) {
                              try {
                                 statement.close();
                              } catch (Throwable var14) {
                                 var29.addSuppressed(var14);
                              }
                           }

                           throw var29;
                        }

                        if (statement != null) {
                           statement.close();
                        }
                     }
                  }

                  if (!error) {
                     statement = connection.prepareStatement("INSERT INTO guilds_members (guild_id, user_id, member_since, level_id) VALUES (?, ?, ?, ?)");

                     try {
                        statement.setInt(1, guild.getId());
                        statement.setInt(2, client.getHabbo().getHabboInfo().getId());
                        statement.setInt(3, Emulator.getIntUnixTimestamp());
                        statement.setInt(4, guild.getState() == GuildState.EXCLUSIVE ? GuildRank.REQUESTED.type : GuildRank.MEMBER.type);
                        statement.execute();
                     } catch (Throwable var23) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var13) {
                              var23.addSuppressed(var13);
                           }
                        }

                        throw var23;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                  }
               } else if (!error) {
                  statement = connection.prepareStatement("UPDATE guilds_members SET level_id = ?, member_since = ? WHERE user_id = ? AND guild_id = ?");

                  try {
                     statement.setInt(1, GuildRank.MEMBER.type);
                     statement.setInt(2, Emulator.getIntUnixTimestamp());
                     statement.setInt(3, userId);
                     statement.setInt(4, guild.getId());
                     statement.execute();
                  } catch (Throwable var22) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var22.addSuppressed(var12);
                        }
                     }

                     throw var22;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               }

               if (userId == 0 && !error) {
                  if (guild.getState() == GuildState.EXCLUSIVE) {
                     guild.increaseRequestCount();
                  } else {
                     guild.increaseMemberCount();
                     client.getHabbo().getHabboStats().addGuild(guild.getId());
                  }
               }
            }
         } catch (Throwable var32) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var32.addSuppressed(var11);
               }
            }

            throw var32;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void setAdmin(Guild guild, int userId) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE guilds_members SET level_id = ? WHERE user_id = ? AND guild_id = ? LIMIT 1");

            try {
               statement.setInt(1, 1);
               statement.setInt(2, userId);
               statement.setInt(3, guild.getId());
               statement.execute();
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void removeAdmin(Guild guild, int userId) {
      if (guild.getOwnerId() != userId) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE guilds_members SET level_id = ? WHERE user_id = ? AND guild_id = ? LIMIT 1");

               try {
                  statement.setInt(1, 2);
                  statement.setInt(2, userId);
                  statement.setInt(3, guild.getId());
                  statement.execute();
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
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public void removeMember(Guild guild, int userId) {
      if (guild.getOwnerId() != userId) {
         Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);
         if (habbo != null && habbo.getHabboStats().guild == guild.getId()) {
            habbo.getHabboStats().removeGuild(guild.getId());
            habbo.getHabboStats().guild = 0;
            habbo.getHabboStats().run();
         }

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("DELETE FROM guilds_members WHERE user_id = ? AND guild_id = ? LIMIT 1");

               try {
                  statement.setInt(1, userId);
                  statement.setInt(2, guild.getId());
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

   public void addGuild(Guild guild) {
      guild.lastRequested = Emulator.getIntUnixTimestamp();
      this.guilds.put(guild.getId(), guild);
   }

   public GuildMember getGuildMember(Guild guild, Habbo habbo) {
      return this.getGuildMember(guild.getId(), habbo.getHabboInfo().getId());
   }

   public GuildMember getGuildMember(int guildId, int habboId) {
      GuildMember member = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ? AND guilds_members.user_id = ? LIMIT 1"
            );

            try {
               statement.setInt(1, guildId);
               statement.setInt(2, habboId);
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     member = new GuildMember(set);
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

      return member;
   }

   public THashSet<GuildMember> getGuildMembers(int guildId) {
      return this.getGuildMembers(this.getGuild(guildId));
   }

   THashSet<GuildMember> getGuildMembers(Guild guild) {
      THashSet<GuildMember> guildMembers = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?"
            );

            try {
               statement.setInt(1, guild.getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     guildMembers.add(new GuildMember(set));
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

      return guildMembers;
   }

   public ArrayList<GuildMember> getGuildMembers(Guild guild, int page, int levelId, String query) {
      ArrayList<GuildMember> guildMembers = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  "
                  + this.rankQuery(levelId)
                  + " AND users.username LIKE ? ORDER BY level_id, member_since ASC LIMIT ?, ?"
            );

            try {
               statement.setInt(1, guild.getId());
               statement.setString(2, "%" + query + "%");
               statement.setInt(3, page * 14);
               statement.setInt(4, page * 14 + 14);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     guildMembers.add(new GuildMember(set));
                  }
               } catch (Throwable var14) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var15) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var12) {
                     var15.addSuppressed(var12);
                  }
               }

               throw var15;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var16) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var16.addSuppressed(var11);
               }
            }

            throw var16;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return guildMembers;
   }

   public int getGuildMembersCount(Guild guild, int page, int levelId, String query) {
      new ArrayList();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         int var9;
         label113: {
            try {
               PreparedStatement statement;
               label106: {
                  statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  "
                        + this.rankQuery(levelId)
                        + " AND users.username LIKE ? ORDER BY level_id, member_since ASC"
                  );

                  try {
                     statement.setInt(1, guild.getId());
                     statement.setString(2, "%" + query + "%");
                     ResultSet set = statement.executeQuery();

                     label86: {
                        try {
                           if (set.next()) {
                              var9 = set.getInt(1);
                              break label86;
                           }
                        } catch (Throwable var14) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var13) {
                                 var14.addSuppressed(var13);
                              }
                           }

                           throw var14;
                        }

                        if (set != null) {
                           set.close();
                        }
                        break label106;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var15) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var15.addSuppressed(var12);
                        }
                     }

                     throw var15;
                  }

                  if (statement != null) {
                     statement.close();
                  }
                  break label113;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var16) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var11) {
                     var16.addSuppressed(var11);
                  }
               }

               throw var16;
            }

            if (connection != null) {
               connection.close();
            }

            return 0;
         }

         if (connection != null) {
            connection.close();
         }

         return var9;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return 0;
      }
   }

   public THashMap<Integer, GuildMember> getOnlyAdmins(Guild guild) {
      THashMap<Integer, GuildMember> guildAdmins = new THashMap();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  "
                  + this.rankQuery(1)
            );

            try {
               statement.setInt(1, guild.getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     guildAdmins.put(set.getInt("user_id"), new GuildMember(set));
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

      return guildAdmins;
   }

   private String rankQuery(int level) {
      switch (level) {
         case 1:
            return "AND (guilds_members.level_id = 0 OR guilds_members.level_id = 1)";
         case 2:
            return "AND guilds_members.level_id = 3";
         default:
            return "AND guilds_members.level_id >= 0 AND guilds_members.level_id <= 2";
      }
   }

   public Guild getGuild(int guildId) {
      Guild g = (Guild)this.guilds.get(guildId);
      if (g == null) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "SELECT users.username, rooms.name as room_name, guilds.* FROM guilds INNER JOIN users ON guilds.user_id = users.id INNER JOIN rooms ON rooms.id = guilds.room_id WHERE guilds.id = ? LIMIT 1"
               );

               try {
                  statement.setInt(1, guildId);
                  ResultSet set = statement.executeQuery();

                  try {
                     if (set.next()) {
                        g = new Guild(set);
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

                  if (g != null) {
                     g.loadMemberCount();
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
      }

      if (g != null) {
         g.lastRequested = Emulator.getIntUnixTimestamp();
         if (!this.guilds.containsKey(guildId)) {
            this.guilds.put(guildId, g);
         }
      }

      return g;
   }

   public List<Guild> getGuilds(int userId) {
      List<Guild> guilds = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT guild_id FROM guilds_members WHERE user_id = ? AND level_id <= 2 ORDER BY member_since ASC"
            );

            try {
               statement.setInt(1, userId);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     Guild guild = this.getGuild(set.getInt("guild_id"));
                     if (guild != null) {
                        guilds.add(guild);
                     }
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

      return guilds;
   }

   public List<Guild> getAllGuilds() {
      List<Guild> guilds = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM guilds ORDER BY id DESC LIMIT 20");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     Guild guild = this.getGuild(set.getInt("id"));
                     if (guild != null) {
                        guilds.add(guild);
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

      return guilds;
   }

   public boolean symbolColor(int colorId) {
      for (GuildPart part : this.getSymbolColors()) {
         if (part.id == colorId) {
            return true;
         }
      }

      return false;
   }

   public boolean backgroundColor(int colorId) {
      for (GuildPart part : this.getBackgroundColors()) {
         if (part.id == colorId) {
            return true;
         }
      }

      return false;
   }

   public THashMap<GuildPartType, THashMap<Integer, GuildPart>> getGuildParts() {
      return this.guildParts;
   }

   public Collection<GuildPart> getBases() {
      return ((THashMap)this.guildParts.get(GuildPartType.BASE)).values();
   }

   public GuildPart getBase(int id) {
      return (GuildPart)((THashMap)this.guildParts.get(GuildPartType.BASE)).get(id);
   }

   public Collection<GuildPart> getSymbols() {
      return ((THashMap)this.guildParts.get(GuildPartType.SYMBOL)).values();
   }

   public GuildPart getSymbol(int id) {
      return (GuildPart)((THashMap)this.guildParts.get(GuildPartType.SYMBOL)).get(id);
   }

   public Collection<GuildPart> getBaseColors() {
      return ((THashMap)this.guildParts.get(GuildPartType.BASE_COLOR)).values();
   }

   public GuildPart getBaseColor(int id) {
      return (GuildPart)((THashMap)this.guildParts.get(GuildPartType.BASE_COLOR)).get(id);
   }

   public Collection<GuildPart> getSymbolColors() {
      return ((THashMap)this.guildParts.get(GuildPartType.SYMBOL_COLOR)).values();
   }

   public GuildPart getSymbolColor(int id) {
      return (GuildPart)((THashMap)this.guildParts.get(GuildPartType.SYMBOL_COLOR)).get(id);
   }

   public Collection<GuildPart> getBackgroundColors() {
      return ((THashMap)this.guildParts.get(GuildPartType.BACKGROUND_COLOR)).values();
   }

   public GuildPart getBackgroundColor(int id) {
      return (GuildPart)((THashMap)this.guildParts.get(GuildPartType.BACKGROUND_COLOR)).get(id);
   }

   public GuildPart getPart(GuildPartType type, int id) {
      return (GuildPart)((THashMap)this.guildParts.get(type)).get(id);
   }

   public void setGuild(InteractionGuildFurni furni, int guildId) {
      furni.setGuildId(guildId);

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE items SET guild_id = ? WHERE id = ?");

            try {
               statement.setInt(1, guildId);
               statement.setInt(2, furni.getId());
               statement.execute();
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
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void dispose() {
      TIntObjectIterator<Guild> guildIterator = this.guilds.iterator();

      for (int i = this.guilds.size(); i-- > 0; guildIterator.remove()) {
         guildIterator.advance();
         if (((Guild)guildIterator.value()).needsUpdate) {
            ((Guild)guildIterator.value()).run();
         }
      }

      LOGGER.info("Guild Manager -> Disposed!");
   }

   public boolean hasViewedForum(int userId, int guildId) {
      return this.views
         .stream()
         .anyMatch(v -> v.getUserId() == userId && v.getGuildId() == guildId && v.getTimestamp() > Emulator.getIntUnixTimestamp() - 604800);
   }

   public void addView(int userId, int guildId) {
      ForumView view = new ForumView(userId, guildId, Emulator.getIntUnixTimestamp());
      this.views.add(view);

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `guild_forum_views`(`user_id`, `guild_id`, `timestamp`) VALUES (?, ?, ?)");

            try {
               statement.setInt(1, view.getUserId());
               statement.setInt(2, view.getGuildId());
               statement.setInt(3, view.getTimestamp());
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

   public Set<Guild> getMostViewed() {
      return this.views
         .stream()
         .filter(v -> v.getTimestamp() > Emulator.getIntUnixTimestamp() - 604800)
         .collect(Collectors.groupingBy(ForumView::getGuildId))
         .entrySet()
         .stream()
         .sorted(Comparator.comparingInt(a -> a.getValue().size()))
         .map(k -> this.getGuild(k.getKey()))
         .filter(g -> g != null && g.canReadForum() == SettingsState.EVERYONE)
         .limit(100L)
         .collect(Collectors.toSet());
   }
}
