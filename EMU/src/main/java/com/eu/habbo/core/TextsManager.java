package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextsManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(TextsManager.class);
   private final Properties texts;

   public TextsManager() {
      long millis = System.currentTimeMillis();
      this.texts = new Properties();

      try {
         this.reload();
         LOGGER.info("Texts Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void reload() throws Exception {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM emulator_texts");

               try {
                  while (set.next()) {
                     if (this.texts.containsKey(set.getString("key"))) {
                        this.texts.setProperty(set.getString("key"), set.getString("value"));
                     } else {
                        this.texts.put(set.getString("key"), set.getString("value"));
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

   public String getValue(String key) {
      return this.getValue(key, "");
   }

   public String getValue(String key, String defaultValue) {
      if (!this.texts.containsKey(key)) {
         LOGGER.error("Text key not found: {}", key);
      }

      return this.texts.getProperty(key, defaultValue);
   }

   public boolean getBoolean(String key) {
      return this.getBoolean(key, false);
   }

   public boolean getBoolean(String key, Boolean defaultValue) {
      try {
         return this.getValue(key, "0").equals("1") || this.getValue(key, "false").equals("true");
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
         return defaultValue;
      }
   }

   public int getInt(String key) {
      return this.getInt(key, 0);
   }

   public int getInt(String key, Integer defaultValue) {
      try {
         return Integer.parseInt(this.getValue(key, defaultValue.toString()));
      } catch (NumberFormatException e) {
         LOGGER.error("Caught exception", e);
         return defaultValue;
      }
   }

   public void update(String key, String value) {
      this.texts.setProperty(key, value);
   }

   public void register(String key, String value) {
      if (this.texts.getProperty(key, null) == null) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO emulator_texts VALUES (?, ?)");

               try {
                  statement.setString(1, key);
                  statement.setString(2, value);
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

         this.update(key, value);
      }
   }
}
