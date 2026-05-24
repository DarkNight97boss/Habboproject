package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionManager {
   public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);
   public THashMap<String, Class<? extends Subscription>> types = new THashMap();

   public void init() {
      this.types.put("HABBO_CLUB", SubscriptionHabboClub.class);
   }

   public void addSubscriptionType(String type, Class<? extends Subscription> clazz) {
      if (!this.types.containsKey(type) && !this.types.containsValue(clazz)) {
         this.types.put(type, clazz);
      } else {
         throw new RuntimeException(
            "Subscription Type must be unique. An class with type: " + clazz.getName() + " was already added OR the key: " + type + " is already in use."
         );
      }
   }

   public void removeSubscriptionType(String type) {
      this.types.remove(type);
   }

   public Class<? extends Subscription> getSubscriptionClass(String type) {
      if (!this.types.containsKey(type)) {
         LOGGER.debug("Can't find subscription class: {}", type);
         return Subscription.class;
      } else {
         return (Class<? extends Subscription>)this.types.get(type);
      }
   }

   public void dispose() {
      this.types.clear();
   }

   public THashSet<Subscription> getSubscriptionsForUser(int userId) {
      THashSet<Subscription> subscriptions = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_subscriptions WHERE user_id = ?");

            try {
               statement.setInt(1, userId);

               try {
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        Class<? extends Subscription> subClazz = Emulator.getGameEnvironment()
                           .getSubscriptionManager()
                           .getSubscriptionClass(set.getString("subscription_type"));
                        Constructor<? extends Subscription> c = subClazz.getConstructor(
                           Integer.class, Integer.class, String.class, Integer.class, Integer.class, Boolean.class
                        );
                        c.setAccessible(true);
                        Subscription subscription = c.newInstance(
                           set.getInt("id"),
                           set.getInt("user_id"),
                           set.getString("subscription_type"),
                           set.getInt("timestamp_start"),
                           set.getInt("duration"),
                           set.getInt("active") == 1
                        );
                        subscriptions.add(subscription);
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
               } catch (IllegalAccessException e) {
                  LOGGER.error("IllegalAccessException", e);
               } catch (InstantiationException e) {
                  LOGGER.error("InstantiationException", e);
               } catch (InvocationTargetException e) {
                  LOGGER.error("InvocationTargetException", e);
               }
            } catch (Throwable var16) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var10) {
                     var16.addSuppressed(var10);
                  }
               }

               throw var16;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var17) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var9) {
                  var17.addSuppressed(var9);
               }
            }

            throw var17;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      } catch (NoSuchMethodException e) {
         LOGGER.error("Caught NoSuchMethodException", e);
      }

      return subscriptions;
   }
}
