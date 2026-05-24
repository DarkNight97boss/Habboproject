package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigatorManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorManager.class);
   public static int MAXIMUM_RESULTS_PER_PAGE = 10;
   public static boolean CATEGORY_SORT_USING_ORDER_NUM = false;
   public final THashMap<Integer, NavigatorPublicCategory> publicCategories = new THashMap();
   public final ConcurrentHashMap<String, NavigatorFilterField> filterSettings = new ConcurrentHashMap<>();
   public final THashMap<String, NavigatorFilter> filters = new THashMap();

   public NavigatorManager() {
      long millis = System.currentTimeMillis();
      this.filters.put("official_view", new NavigatorPublicFilter());
      this.filters.put("hotel_view", new NavigatorHotelFilter());
      this.filters.put("roomads_view", new NavigatorRoomAdsFilter());
      this.filters.put("myworld_view", new NavigatorUserFilter());
      this.filters.put("favorites", new NavigatorFavoriteFilter());
      LOGGER.info("Navigator Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public void loadNavigator() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            synchronized (this.publicCategories) {
               this.publicCategories.clear();

               try {
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM navigator_publiccats WHERE visible = '1' ORDER BY order_num DESC");

                     try {
                        while (set.next()) {
                           this.publicCategories.put(set.getInt("id"), new NavigatorPublicCategory(set));
                        }
                     } catch (Throwable var23) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var19) {
                              var23.addSuppressed(var19);
                           }
                        }

                        throw var23;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var24) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var18) {
                           var24.addSuppressed(var18);
                        }
                     }

                     throw var24;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (SQLException e) {
                  LOGGER.error("Caught SQL exception", e);
               }

               try {
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM navigator_publics WHERE visible = '1'");

                     try {
                        while (set.next()) {
                           NavigatorPublicCategory category = (NavigatorPublicCategory)this.publicCategories.get(set.getInt("public_cat_id"));
                           if (category != null) {
                              Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(set.getInt("room_id"));
                              if (room != null) {
                                 category.addRoom(room);
                              } else {
                                 LOGGER.error("Public room (ID: {} defined in navigator_publics does not exist!", set.getInt("room_id"));
                              }
                           }
                        }
                     } catch (Throwable var20) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var17) {
                              var20.addSuppressed(var17);
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
                        } catch (Throwable var16) {
                           var21.addSuppressed(var16);
                        }
                     }

                     throw var21;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (SQLException e) {
                  LOGGER.error("Caught SQL exception", e);
               }
            }

            synchronized (this.filterSettings) {
               try {
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM navigator_filter");

                     try {
                        label229:
                        while (true) {
                           Method field;
                           label227:
                           while (true) {
                              if (!set.next()) {
                                 break label229;
                              }

                              field = null;
                              Class clazz = Room.class;
                              if (set.getString("field").contains(".")) {
                                 String[] e = set.getString("field").split("\\.");
                                 int var8 = e.length;
                                 int var9 = 0;

                                 while (true) {
                                    if (var9 >= var8) {
                                       break label227;
                                    }

                                    String s = e[var9];

                                    try {
                                       field = clazz.getDeclaredMethod(s);
                                       clazz = field.getReturnType();
                                    } catch (Exception ex) {
                                       LOGGER.error("Caught exception", ex);
                                       break label227;
                                    }

                                    var9++;
                                 }
                              }

                              try {
                                 field = clazz.getDeclaredMethod(set.getString("field"));
                                 break;
                              } catch (Exception e) {
                                 LOGGER.error("Caught exception", e);
                              }
                           }

                           if (field != null) {
                              this.filterSettings
                                 .put(
                                    set.getString("key"),
                                    new NavigatorFilterField(
                                       set.getString("key"),
                                       field,
                                       set.getString("database_query"),
                                       NavigatorFilterComparator.valueOf(set.getString("compare").toUpperCase())
                                    )
                                 );
                           }
                        }
                     } catch (Throwable var29) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var15) {
                              var29.addSuppressed(var15);
                           }
                        }

                        throw var29;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var30) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var14) {
                           var30.addSuppressed(var14);
                        }
                     }

                     throw var30;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (SQLException e) {
                  LOGGER.error("Caught SQL exception", e);
               }
            }
         } catch (Throwable var33) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var13) {
                  var33.addSuppressed(var13);
               }
            }

            throw var33;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      for (Room room : Emulator.getGameEnvironment().getRoomManager().getRoomsStaffPromoted()) {
         ((NavigatorPublicCategory)this.publicCategories.get(Emulator.getConfig().getInt("hotel.navigator.staffpicks.categoryid"))).addRoom(room);
      }
   }

   public NavigatorFilterComparator comperatorForField(Method field) {
      for (Entry<String, NavigatorFilterField> set : this.filterSettings.entrySet()) {
         if (set.getValue().field == field) {
            return set.getValue().comparator;
         }
      }

      return null;
   }

   public List<Room> getRoomsForCategory(String category, Habbo habbo) {
      new ArrayList();
      List rooms;
      switch (category) {
         case "my":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(habbo);
            break;
         case "favorites":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsFavourite(habbo);
            break;
         case "history_freq":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsVisited(habbo, false, 10);
            break;
         case "my_groups":
            rooms = Emulator.getGameEnvironment().getRoomManager().getGroupRooms(habbo, 25);
            break;
         case "with_rights":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsWithRights(habbo);
            break;
         case "official-root":
            rooms = Emulator.getGameEnvironment().getRoomManager().getPublicRooms();
            break;
         case "popular":
            rooms = Emulator.getGameEnvironment().getRoomManager().getPopularRooms(Emulator.getConfig().getInt("hotel.navigator.popular.amount"));
            break;
         case "categories":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsPromoted();
            break;
         case "with_friends":
            rooms = Emulator.getGameEnvironment().getRoomManager().getRoomsWithFriendsIn(habbo, 25);
            break;
         case "highest_score":
            rooms = Emulator.getGameEnvironment().getRoomManager().getTopRatedRooms(25);
            break;
         default:
            return null;
      }

      Collections.sort(rooms);
      return rooms;
   }
}
