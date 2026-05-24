package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.catalog.CatalogModeComposer;
import com.eu.habbo.messages.outgoing.catalog.CatalogUpdatedComposer;
import com.eu.habbo.messages.outgoing.catalog.DiscountComposer;
import com.eu.habbo.messages.outgoing.catalog.GiftConfigurationComposer;
import com.eu.habbo.messages.outgoing.catalog.RecyclerLogicComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolComposer;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import com.eu.habbo.plugin.events.users.UserRankChangedEvent;
import com.eu.habbo.plugin.events.users.UserRegisteredEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HabboManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(HabboManager.class);
   public static String WELCOME_MESSAGE = "";
   public static boolean NAMECHANGE_ENABLED = false;
   private final ConcurrentHashMap<Integer, Habbo> onlineHabbos;

   public HabboManager() {
      long millis = System.currentTimeMillis();
      this.onlineHabbos = new ConcurrentHashMap<>();
      LOGGER.info("Habbo Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public static HabboInfo getOfflineHabboInfo(int id) {
      HabboInfo info = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1");

            try {
               statement.setInt(1, id);
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     info = new HabboInfo(set);
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

      return info;
   }

   public static HabboInfo getOfflineHabboInfo(String username) {
      HabboInfo info = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1");

            try {
               statement.setString(1, username);
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     info = new HabboInfo(set);
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

      return info;
   }

   public void addHabbo(Habbo habbo) {
      this.onlineHabbos.put(habbo.getHabboInfo().getId(), habbo);
   }

   public void removeHabbo(Habbo habbo) {
      this.onlineHabbos.remove(habbo.getHabboInfo().getId());
   }

   public Habbo getHabbo(int id) {
      return this.onlineHabbos.get(id);
   }

   public Habbo getHabbo(String username) {
      synchronized (this.onlineHabbos) {
         for (Entry<Integer, Habbo> map : this.onlineHabbos.entrySet()) {
            if (map.getValue().getHabboInfo().getUsername().equalsIgnoreCase(username)) {
               return map.getValue();
            }
         }

         return null;
      }
   }

   public Habbo loadHabbo(String sso) {
      int userId = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE auth_ticket = ? LIMIT 1");

            try {
               statement.setString(1, sso);
               ResultSet s = statement.executeQuery();

               try {
                  if (s.next()) {
                     userId = s.getInt("id");
                  }
               } catch (Throwable var25) {
                  if (s != null) {
                     try {
                        s.close();
                     } catch (Throwable var17) {
                        var25.addSuppressed(var17);
                     }
                  }

                  throw var25;
               }

               if (s != null) {
                  s.close();
               }

               statement.close();
            } catch (Throwable var26) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var16) {
                     var26.addSuppressed(var16);
                  }
               }

               throw var26;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var27) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var15) {
                  var27.addSuppressed(var15);
               }
            }

            throw var27;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      Habbo habbo = this.cloneCheck(userId);
      if (habbo != null) {
         habbo.alert(Emulator.getTexts().getValue("loggedin.elsewhere"));
         Emulator.getGameServer().getGameClientManager().disposeClient(habbo.getClient());
         habbo = null;
      }

      ModToolBan ban = Emulator.getGameEnvironment().getModToolManager().checkForBan(userId);
      if (ban != null) {
         return null;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE auth_ticket = ? LIMIT 1");

            try {
               statement.setString(1, sso);
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     habbo = new Habbo(set);
                     if (habbo.getHabboInfo().firstVisit) {
                        Emulator.getPluginManager().fireEvent(new UserRegisteredEvent(habbo));
                     }

                     if (!Emulator.debugging) {
                        try {
                           PreparedStatement stmt = connection.prepareStatement("UPDATE users SET auth_ticket = ? WHERE id = ? LIMIT 1");

                           try {
                              stmt.setString(1, "");
                              stmt.setInt(2, habbo.getHabboInfo().getId());
                              stmt.execute();
                           } catch (Throwable var18) {
                              if (stmt != null) {
                                 try {
                                    stmt.close();
                                 } catch (Throwable var14) {
                                    var18.addSuppressed(var14);
                                 }
                              }

                              throw var18;
                           }

                           if (stmt != null) {
                              stmt.close();
                           }
                        } catch (SQLException e) {
                           LOGGER.error("Caught SQL exception", e);
                        }
                     }
                  }
               } catch (Throwable var20) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var13) {
                        var20.addSuppressed(var13);
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
                  } catch (Throwable var12) {
                     var21.addSuppressed(var12);
                  }
               }

               throw var21;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var22) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var22.addSuppressed(var11);
               }
            }

            throw var22;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      } catch (Exception ex) {
         LOGGER.error("Caught exception", ex);
      }

      return habbo;
   }

   public HabboInfo getHabboInfo(int id) {
      return this.getHabbo(id) == null ? getOfflineHabboInfo(id) : this.getHabbo(id).getHabboInfo();
   }

   public int getOnlineCount() {
      return this.onlineHabbos.size();
   }

   public Habbo cloneCheck(int id) {
      return Emulator.getGameServer().getGameClientManager().getHabbo(id);
   }

   public void sendPacketToHabbosWithPermission(ServerMessage message, String perm) {
      synchronized (this.onlineHabbos) {
         for (Habbo habbo : this.onlineHabbos.values()) {
            if (habbo.hasPermission(perm)) {
               habbo.getClient().sendResponse(message);
            }
         }
      }
   }

   public ConcurrentHashMap<Integer, Habbo> getOnlineHabbos() {
      return this.onlineHabbos;
   }

   public synchronized void dispose() {
      LOGGER.info("Habbo Manager -> Disposed!");
   }

   public ArrayList<HabboInfo> getCloneAccounts(Habbo habbo, int limit) {
      ArrayList<HabboInfo> habboInfo = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT * FROM users WHERE ip_register = ? OR ip_current = ? AND id != ? ORDER BY id DESC LIMIT ?"
            );

            try {
               statement.setString(1, habbo.getHabboInfo().getIpRegister());
               statement.setString(2, habbo.getHabboInfo().getIpLogin());
               statement.setInt(3, habbo.getHabboInfo().getId());
               statement.setInt(4, limit);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     habboInfo.add(new HabboInfo(set));
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

      return habboInfo;
   }

   public List<Entry<Integer, String>> getNameChanges(int userId, int limit) {
      List<Entry<Integer, String>> nameChanges = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT timestamp, new_name FROM namechange_log WHERE user_id = ? ORDER by timestamp DESC LIMIT ?"
            );

            try {
               statement.setInt(1, userId);
               statement.setInt(2, limit);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     nameChanges.add(new SimpleEntry<>(set.getInt("timestamp"), set.getString("new_name")));
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

      return nameChanges;
   }

   public void setRank(int userId, int rankId) throws Exception {
      Habbo habbo = this.getHabbo(userId);
      if (!Emulator.getGameEnvironment().getPermissionsManager().rankExists(rankId)) {
         throw new Exception("Rank ID (" + rankId + ") does not exist");
      }

      Rank newRank = Emulator.getGameEnvironment().getPermissionsManager().getRank(rankId);
      if (habbo != null && habbo.getHabboStats() != null) {
         Rank oldRank = habbo.getHabboInfo().getRank();
         if (!oldRank.getBadge().isEmpty()) {
            habbo.deleteBadge(habbo.getInventory().getBadgesComponent().getBadge(oldRank.getBadge()));
         }

         if (oldRank.getRoomEffect() > 0) {
            habbo.getInventory().getEffectsComponent().effects.remove(oldRank.getRoomEffect());
         }

         habbo.getHabboInfo().setRank(newRank);
         if (!newRank.getBadge().isEmpty()) {
            habbo.addBadge(newRank.getBadge());
         }

         if (newRank.getRoomEffect() > 0) {
            habbo.getInventory().getEffectsComponent().createRankEffect(habbo.getHabboInfo().getRank().getRoomEffect());
         }

         habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
         habbo.getClient().sendResponse(new UserPerksComposer(habbo));
         if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) {
            habbo.getClient().sendResponse(new ModToolComposer(habbo));
         }

         habbo.getHabboInfo().run();
         habbo.getClient().sendResponse(new CatalogUpdatedComposer());
         habbo.getClient().sendResponse(new CatalogModeComposer(0));
         habbo.getClient().sendResponse(new DiscountComposer());
         habbo.getClient().sendResponse(new MarketplaceConfigComposer());
         habbo.getClient().sendResponse(new GiftConfigurationComposer());
         habbo.getClient().sendResponse(new RecyclerLogicComposer());
         habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_give_rank.new_rank").replace("id", newRank.getName()));
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users SET `rank` = ? WHERE id = ? LIMIT 1");

               try {
                  statement.setInt(1, rankId);
                  statement.setInt(2, userId);
                  statement.execute();
               } catch (Throwable var11) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
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
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
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

      Emulator.getPluginManager().fireEvent(new UserRankChangedEvent(habbo));
   }

   public void giveCredits(int userId, int credits) {
      Habbo habbo = this.getHabbo(userId);
      if (habbo != null) {
         habbo.giveCredits(credits);
      } else {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users SET credits = credits + ? WHERE id = ? LIMIT 1");

               try {
                  statement.setInt(1, credits);
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
   }

   public void staffAlert(String message) {
      message = Emulator.getTexts().getValue("commands.generic.cmd_staffalert.title") + "\r\n" + message;
      ServerMessage msg = new GenericAlertComposer(message).compose();
      Emulator.getGameEnvironment().getHabboManager().sendPacketToHabbosWithPermission(msg, "cmd_staffalert");
   }
}
