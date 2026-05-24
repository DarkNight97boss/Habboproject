package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardrobeComponent {
   private static final Logger LOGGER = LoggerFactory.getLogger(WardrobeComponent.class);
   private final THashMap<Integer, WardrobeComponent.WardrobeItem> looks = new THashMap();
   private final TIntSet clothing = new TIntHashSet();
   private final TIntSet clothingSets = new TIntHashSet();

   public WardrobeComponent(Habbo habbo) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_wardrobe WHERE user_id = ?");

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     this.looks.put(set.getInt("slot_id"), new WardrobeComponent.WardrobeItem(set, habbo));
                  }
               } catch (Throwable var17) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var15) {
                        var17.addSuppressed(var15);
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
                  } catch (Throwable var14) {
                     var18.addSuppressed(var14);
                  }
               }

               throw var18;
            }

            if (statement != null) {
               statement.close();
            }

            statement = connection.prepareStatement(
               "SELECT users_clothing.*, catalog_clothing.setid FROM users_clothing LEFT JOIN catalog_clothing ON catalog_clothing.id = users_clothing.clothing_id WHERE users_clothing.user_id = ?"
            );

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     int value = set.getInt("clothing_id");
                     this.clothing.add(value);

                     for (String x : set.getString("setid").split(Pattern.quote(","))) {
                        try {
                           this.clothingSets.add(Integer.parseInt(x));
                        } catch (Exception var16) {
                        }
                     }
                  }
               } catch (Throwable var19) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var13) {
                        var19.addSuppressed(var13);
                     }
                  }

                  throw var19;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var20) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var12) {
                     var20.addSuppressed(var12);
                  }
               }

               throw var20;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var21) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var21.addSuppressed(var11);
               }
            }

            throw var21;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public WardrobeComponent.WardrobeItem createLook(Habbo habbo, int slotId, String look) {
      return new WardrobeComponent.WardrobeItem(habbo.getHabboInfo().getGender(), look, slotId, habbo);
   }

   public THashMap<Integer, WardrobeComponent.WardrobeItem> getLooks() {
      return this.looks;
   }

   public TIntCollection getClothing() {
      return this.clothing;
   }

   public TIntCollection getClothingSets() {
      return this.clothingSets;
   }

   public void dispose() {
      this.looks.values().stream().filter(item -> item.needsInsert || item.needsUpdate).forEach(item -> Emulator.getThreading().run(item));
      this.looks.clear();
   }

   public class WardrobeItem implements Runnable {
      private int slotId;
      private HabboGender gender;
      private Habbo habbo;
      private String look;
      private boolean needsInsert = false;
      private boolean needsUpdate = false;

      private WardrobeItem(ResultSet set, Habbo habbo) throws SQLException {
         this.gender = HabboGender.valueOf(set.getString("gender"));
         this.look = set.getString("look");
         this.slotId = set.getInt("slot_id");
         this.habbo = habbo;
      }

      private WardrobeItem(HabboGender gender, String look, int slotId, Habbo habbo) {
         this.gender = gender;
         this.look = look;
         this.slotId = slotId;
         this.habbo = habbo;
      }

      public HabboGender getGender() {
         return this.gender;
      }

      public void setGender(HabboGender gender) {
         this.gender = gender;
      }

      public Habbo getHabbo() {
         return this.habbo;
      }

      public void setHabbo(Habbo habbo) {
         this.habbo = habbo;
      }

      public String getLook() {
         return this.look;
      }

      public void setLook(String look) {
         this.look = look;
      }

      public void setNeedsInsert(boolean needsInsert) {
         this.needsInsert = needsInsert;
      }

      public void setNeedsUpdate(boolean needsUpdate) {
         this.needsUpdate = needsUpdate;
      }

      public int getSlotId() {
         return this.slotId;
      }

      @Override
      public void run() {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               if (this.needsInsert) {
                  this.needsInsert = false;
                  this.needsUpdate = false;
                  PreparedStatement statement = connection.prepareStatement("INSERT INTO users_wardrobe (slot_id, look, user_id, gender) VALUES (?, ?, ?, ?)");

                  try {
                     statement.setInt(1, this.slotId);
                     statement.setString(2, this.look);
                     statement.setInt(3, this.habbo.getHabboInfo().getId());
                     statement.setString(4, this.gender.name());
                     statement.execute();
                  } catch (Throwable var9) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var7) {
                           var9.addSuppressed(var7);
                        }
                     }

                     throw var9;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               }

               if (this.needsUpdate) {
                  this.needsUpdate = false;
                  PreparedStatement statement = connection.prepareStatement("UPDATE users_wardrobe SET look = ? WHERE slot_id = ? AND user_id = ?");

                  try {
                     statement.setString(1, this.look);
                     statement.setInt(2, this.slotId);
                     statement.setInt(3, this.habbo.getHabboInfo().getId());
                     statement.execute();
                  } catch (Throwable var8) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var6) {
                           var8.addSuppressed(var6);
                        }
                     }

                     throw var8;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               }
            } catch (Throwable var10) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var5) {
                     var10.addSuppressed(var5);
                  }
               }

               throw var10;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            WardrobeComponent.LOGGER.error("Caught SQL exception", e);
         }
      }
   }
}
