package com.eu.habbo.habbohotel.campaign.calendar;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.events.calendar.AdventCalendarProductComposer;
import com.eu.habbo.plugin.events.users.calendar.UserClaimRewardEvent;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(CalendarCampaign.class);
   private static final Map<Integer, CalendarCampaign> calendarCampaigns = new THashMap();
   public static double HC_MODIFIER;

   public CalendarManager() {
      long millis = System.currentTimeMillis();
      this.reload();
      LOGGER.info("Calendar Manager -> Loaded! ({} MS)", System.currentTimeMillis() - millis);
   }

   public void dispose() {
      calendarCampaigns.clear();
   }

   public boolean reload() {
      this.dispose();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM calendar_campaigns WHERE enabled = 1");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     calendarCampaigns.put(set.getInt("id"), new CalendarCampaign(set));
                  }
               } catch (Throwable var16) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var11) {
                        var16.addSuppressed(var11);
                     }
                  }

                  throw var16;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var17) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var10) {
                     var17.addSuppressed(var10);
                  }
               }

               throw var17;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var18) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var9) {
                  var18.addSuppressed(var9);
               }
            }

            throw var18;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM calendar_rewards");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     CalendarCampaign campaign = calendarCampaigns.get(set.getInt("campaign_id"));
                     if (campaign != null) {
                        campaign.addReward(new CalendarRewardObject(set));
                     }
                  }
               } catch (Throwable var12) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var8) {
                        var12.addSuppressed(var8);
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
                  } catch (Throwable var7) {
                     var13.addSuppressed(var7);
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
               } catch (Throwable var6) {
                  var14.addSuppressed(var6);
               }
            }

            throw var14;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }

      HC_MODIFIER = Emulator.getConfig().getDouble("hotel.calendar.pixels.hc_modifier", 2.0);
      return true;
   }

   public void addCampaign(CalendarCampaign campaign) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO calendar_campaigns ( name, image, start_timestamp, total_days, lock_expired) VALUES (?, ?, ?, ? , ?)", 1
            );

            try {
               statement.setString(1, campaign.getName());
               statement.setString(2, campaign.getImage());
               statement.setInt(3, campaign.getStartTimestamp());
               statement.setInt(4, campaign.getTotalDays());
               statement.setBoolean(5, campaign.getLockExpired());
               int affectedRows = statement.executeUpdate();
               if (affectedRows == 0) {
                  throw new SQLException("Creating calendar campaign failed, no rows affected.");
               }

               ResultSet generatedKeys = statement.getGeneratedKeys();

               try {
                  if (!generatedKeys.next()) {
                     throw new SQLException("Creating calendar campaign failed, no ID found.");
                  }

                  campaign.setId(generatedKeys.getInt(1));
               } catch (Throwable var11) {
                  if (generatedKeys != null) {
                     try {
                        generatedKeys.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (generatedKeys != null) {
                  generatedKeys.close();
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

      calendarCampaigns.put(campaign.getId(), campaign);
   }

   public boolean deleteCampaign(CalendarCampaign campaign) {
      calendarCampaigns.remove(campaign.getId());

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var4;
         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM calendar_campaigns WHERE id = ? LIMIT 1");

            try {
               statement.setInt(1, campaign.getId());
               var4 = statement.execute();
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

         return var4;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public CalendarCampaign getCalendarCampaign(String campaignName) {
      return calendarCampaigns.values().stream().filter(cc -> Objects.equals(cc.getName(), campaignName)).findFirst().orElse(null);
   }

   public Map<Integer, CalendarCampaign> getCalendarCampaigns() {
      return calendarCampaigns;
   }

   public void claimCalendarReward(Habbo habbo, String campaignName, int day, boolean force) {
      CalendarCampaign campaign = calendarCampaigns.values().stream().filter(cc -> Objects.equals(cc.getName(), campaignName)).findFirst().orElse(null);
      if (campaign != null) {
         if (habbo.getHabboStats().calendarRewardsClaimed.stream().noneMatch(claimed -> claimed.getCampaignId() == campaign.getId() && claimed.getDay() == day)
            )
          {
            Set<Integer> keys = campaign.getRewards().keySet();
            Map<Integer, Integer> rewards = new THashMap();
            if (keys.isEmpty()) {
               return;
            }

            keys.forEach(key -> rewards.put(rewards.size() + 1, key));
            int rand = Emulator.getRandom().nextInt(rewards.size() - 1 + 1) + 1;
            int random = rewards.get(rand);
            CalendarRewardObject object = campaign.getRewards().get(random);
            if (object == null) {
               return;
            }

            int daysBetween = (int)ChronoUnit.DAYS.between(new Timestamp(campaign.getStartTimestamp().intValue() * 1000L).toInstant(), new Date().toInstant());
            if (daysBetween >= 0 && daysBetween <= campaign.getTotalDays()) {
               int diff = daysBetween - day;
               if ((diff <= 2 || !campaign.getLockExpired()) && diff >= 0 || force && habbo.hasPermission("acc_calendar_force")) {
                  if (Emulator.getPluginManager().fireEvent(new UserClaimRewardEvent(habbo, campaign, day, object, force)).isCancelled()) {
                     return;
                  }

                  habbo.getHabboStats()
                     .calendarRewardsClaimed
                     .add(
                        new CalendarRewardClaimed(
                           habbo.getHabboInfo().getId(), campaign.getId(), day, object.getId(), new Timestamp(System.currentTimeMillis())
                        )
                     );
                  habbo.getClient().sendResponse(new AdventCalendarProductComposer(true, object, habbo));
                  object.give(habbo);

                  try {
                     Connection connection = Emulator.getDatabase().getDataSource().getConnection();

                     try {
                        PreparedStatement statement = connection.prepareStatement(
                           "INSERT INTO calendar_rewards_claimed (user_id, campaign_id, day, reward_id, timestamp) VALUES (?, ?, ?, ?, ?)"
                        );

                        try {
                           statement.setInt(1, habbo.getHabboInfo().getId());
                           statement.setInt(2, campaign.getId());
                           statement.setInt(3, day);
                           statement.setInt(4, object.getId());
                           statement.setInt(5, Emulator.getIntUnixTimestamp());
                           statement.execute();
                        } catch (Throwable var19) {
                           if (statement != null) {
                              try {
                                 statement.close();
                              } catch (Throwable var18) {
                                 var19.addSuppressed(var18);
                              }
                           }

                           throw var19;
                        }

                        if (statement != null) {
                           statement.close();
                        }
                     } catch (Throwable var20) {
                        if (connection != null) {
                           try {
                              connection.close();
                           } catch (Throwable var17) {
                              var20.addSuppressed(var17);
                           }
                        }

                        throw var20;
                     }

                     if (connection != null) {
                        connection.close();
                     }
                  } catch (SQLException e) {
                     LOGGER.error("Caught SQL exception", e);
                  }
               }
            }
         }
      }
   }
}
