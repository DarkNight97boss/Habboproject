package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HabboBadge implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(HabboBadge.class);
   private int id;
   private String code;
   private int slot;
   private Habbo habbo;
   private boolean needsUpdate;
   private boolean needsInsert;

   public HabboBadge(ResultSet set, Habbo habbo) throws SQLException {
      this.id = set.getInt("id");
      this.code = set.getString("badge_code");
      this.slot = set.getInt("slot_id");
      this.habbo = habbo;
      this.needsUpdate = false;
      this.needsInsert = false;
   }

   public HabboBadge(int id, String code, int slot, Habbo habbo) {
      this.id = id;
      this.code = code;
      this.slot = slot;
      this.habbo = habbo;
      this.needsUpdate = false;
      this.needsInsert = true;
   }

   public int getId() {
      return this.id;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   @Override
   public void run() {
      try {
         if (this.needsInsert) {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO users_badges (user_id, slot_id, badge_code) VALUES (?, ?, ?)", 1);

               try {
                  statement.setInt(1, this.habbo.getHabboInfo().getId());
                  statement.setInt(2, this.slot);
                  statement.setString(3, this.code);
                  statement.execute();
                  ResultSet set = statement.getGeneratedKeys();

                  try {
                     if (set.next()) {
                        this.id = set.getInt(1);
                     }
                  } catch (Throwable var13) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var13.addSuppressed(var10);
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
                     } catch (Throwable var9) {
                        var14.addSuppressed(var9);
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
                  } catch (Throwable var8) {
                     var15.addSuppressed(var8);
                  }
               }

               throw var15;
            }

            if (connection != null) {
               connection.close();
            }

            this.needsInsert = false;
         } else if (this.needsUpdate) {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("UPDATE users_badges SET slot_id = ?, badge_code = ? WHERE id = ? AND user_id = ?");

               try {
                  statement.setInt(1, this.slot);
                  statement.setString(2, this.code);
                  statement.setInt(3, this.id);
                  statement.setInt(4, this.habbo.getHabboInfo().getId());
                  statement.execute();
               } catch (Throwable var11) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var7) {
                        var11.addSuppressed(var7);
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
                  } catch (Throwable var6) {
                     var12.addSuppressed(var6);
                  }
               }

               throw var12;
            }

            if (connection != null) {
               connection.close();
            }

            this.needsUpdate = false;
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public void needsUpdate(boolean needsUpdate) {
      this.needsUpdate = needsUpdate;
   }

   public void needsInsert(boolean needsInsert) {
      this.needsInsert = needsInsert;
   }
}
