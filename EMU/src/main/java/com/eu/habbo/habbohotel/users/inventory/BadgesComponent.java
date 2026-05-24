package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BadgesComponent {
   private static final Logger LOGGER = LoggerFactory.getLogger(BadgesComponent.class);
   private final THashSet<HabboBadge> badges = new THashSet();

   public BadgesComponent(Habbo habbo) {
      this.badges.addAll(loadBadges(habbo));
   }

   private static THashSet<HabboBadge> loadBadges(Habbo habbo) {
      THashSet<HabboBadge> badgesList = new THashSet();
      Set<String> staffBadges = Emulator.getGameEnvironment().getPermissionsManager().getStaffBadges();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_badges WHERE user_id = ?");

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     HabboBadge badge = new HabboBadge(set, habbo);
                     if (staffBadges.contains(badge.getCode())) {
                        boolean delete = true;

                        for (Rank rank : Emulator.getGameEnvironment().getPermissionsManager().getRanksByBadgeCode(badge.getCode())) {
                           if (rank.getId() == habbo.getHabboInfo().getRank().getId()) {
                              delete = false;
                              break;
                           }
                        }

                        if (delete) {
                           deleteBadge(habbo.getHabboInfo().getId(), badge.getCode());
                           continue;
                        }
                     }

                     badgesList.add(badge);
                  }
               } catch (Throwable var13) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }
                  }

                  throw var13;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var14) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var11) {
                     var14.addSuppressed(var11);
                  }
               }

               throw var14;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var15) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var10) {
                  var15.addSuppressed(var10);
               }
            }

            throw var15;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return badgesList;
   }

   public static void resetSlots(Habbo habbo) {
      TObjectHashIterator var1 = habbo.getInventory().getBadgesComponent().getBadges().iterator();

      while (var1.hasNext()) {
         HabboBadge badge = (HabboBadge)var1.next();
         if (badge.getSlot() != 0) {
            badge.setSlot(0);
            badge.needsUpdate(true);
            Emulator.getThreading().run(badge);
         }
      }
   }

   public static ArrayList<HabboBadge> getBadgesOfflineHabbo(int userId) {
      ArrayList<HabboBadge> badgesList = new ArrayList<>();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_badges WHERE slot_id > 0 AND user_id = ? ORDER BY slot_id ASC");

            try {
               statement.setInt(1, userId);
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     badgesList.add(new HabboBadge(set, null));
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

      return badgesList;
   }

   public static HabboBadge createBadge(String code, Habbo habbo) {
      HabboBadge badge = new HabboBadge(0, code, 0, habbo);
      badge.run();
      habbo.getInventory().getBadgesComponent().addBadge(badge);
      return badge;
   }

   public static void deleteBadge(int userId, String badge) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("DELETE users_badges FROM users_badges WHERE user_id = ? AND badge_code LIKE ?");

            try {
               statement.setInt(1, userId);
               statement.setString(2, badge);
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

   public ArrayList<HabboBadge> getWearingBadges() {
      synchronized (this.badges) {
         ArrayList<HabboBadge> badgesList = new ArrayList<>();
         TObjectHashIterator var3 = this.badges.iterator();

         while (var3.hasNext()) {
            HabboBadge badge = (HabboBadge)var3.next();
            if (badge.getSlot() != 0) {
               badgesList.add(badge);
            }
         }

         badgesList.sort(new Comparator<HabboBadge>() {
            public int compare(HabboBadge o1, HabboBadge o2) {
               return o1.getSlot() - o2.getSlot();
            }
         });
         return badgesList;
      }
   }

   public THashSet<HabboBadge> getBadges() {
      return this.badges;
   }

   public boolean hasBadge(String badge) {
      return this.getBadge(badge) != null;
   }

   public HabboBadge getBadge(String badgeCode) {
      synchronized (this.badges) {
         TObjectHashIterator var3 = this.badges.iterator();

         while (var3.hasNext()) {
            HabboBadge badge = (HabboBadge)var3.next();
            if (badge.getCode().equalsIgnoreCase(badgeCode)) {
               return badge;
            }
         }

         return null;
      }
   }

   public void addBadge(HabboBadge badge) {
      synchronized (this.badges) {
         this.badges.add(badge);
      }
   }

   public HabboBadge removeBadge(String badge) {
      synchronized (this.badges) {
         TObjectHashIterator var3 = this.badges.iterator();

         while (var3.hasNext()) {
            HabboBadge b = (HabboBadge)var3.next();
            if (b.getCode().equalsIgnoreCase(badge)) {
               this.badges.remove(b);
               return b;
            }
         }

         return null;
      }
   }

   public void removeBadge(HabboBadge badge) {
      synchronized (this.badges) {
         this.badges.remove(badge);
      }
   }

   public void dispose() {
      synchronized (this.badges) {
         this.badges.clear();
      }
   }
}
