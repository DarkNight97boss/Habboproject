package com.eu.habbo.habbohotel.permissions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.HabboPlugin;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionsManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsManager.class);
   private final TIntObjectHashMap<Rank> ranks;
   private final TIntIntHashMap enables;
   private final THashMap<String, List<Rank>> badges;

   public PermissionsManager() {
      long millis = System.currentTimeMillis();
      this.ranks = new TIntObjectHashMap();
      this.enables = new TIntIntHashMap();
      this.badges = new THashMap();
      this.reload();
      LOGGER.info("Permissions Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public void reload() {
      this.loadPermissions();
      this.loadEnables();
   }

   private void loadPermissions() {
      this.badges.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM permissions ORDER BY id ASC");

               try {
                  while (set.next()) {
                     Rank rank = null;
                     if (!this.ranks.containsKey(set.getInt("id"))) {
                        rank = new Rank(set);
                        this.ranks.put(set.getInt("id"), rank);
                     } else {
                        rank = (Rank)this.ranks.get(set.getInt("id"));
                        rank.load(set);
                     }

                     if (rank != null && !rank.getBadge().isEmpty()) {
                        if (!this.badges.containsKey(rank.getBadge())) {
                           this.badges.put(rank.getBadge(), new ArrayList());
                        }

                        ((List)this.badges.get(rank.getBadge())).add(rank);
                     }
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

   private void loadEnables() {
      synchronized (this.enables) {
         this.enables.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM special_enables");

                  try {
                     while (set.next()) {
                        this.enables.put(set.getInt("effect_id"), set.getInt("min_rank"));
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
      }
   }

   public boolean rankExists(int rankId) {
      return this.ranks.containsKey(rankId);
   }

   public Rank getRank(int rankId) {
      return (Rank)this.ranks.get(rankId);
   }

   public Rank getRankByName(String rankName) {
      for (Rank rank : this.ranks.valueCollection()) {
         if (rank.getName().equalsIgnoreCase(rankName)) {
            return rank;
         }
      }

      return null;
   }

   public boolean isEffectBlocked(int effectId, int rank) {
      return this.enables.contains(effectId) && this.enables.get(effectId) > rank;
   }

   public boolean hasPermission(Habbo habbo, String permission) {
      return this.hasPermission(habbo, permission, false);
   }

   public boolean hasPermission(Habbo habbo, String permission, boolean withRoomRights) {
      if (!this.hasPermission(habbo.getHabboInfo().getRank(), permission, withRoomRights)) {
         TObjectHashIterator var4 = Emulator.getPluginManager().getPlugins().iterator();

         while (var4.hasNext()) {
            HabboPlugin plugin = (HabboPlugin)var4.next();
            if (plugin.hasPermission(habbo, permission)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean hasPermission(Rank rank, String permission, boolean withRoomRights) {
      return rank.hasPermission(permission, withRoomRights);
   }

   public Set<String> getStaffBadges() {
      return this.badges.keySet();
   }

   public List<Rank> getRanksByBadgeCode(String code) {
      return (List<Rank>)this.badges.get(code);
   }

   public List<Rank> getAllRanks() {
      return new ArrayList<>(this.ranks.valueCollection());
   }
}
