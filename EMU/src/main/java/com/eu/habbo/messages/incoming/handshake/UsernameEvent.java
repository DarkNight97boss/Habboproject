package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarCampaign;
import com.eu.habbo.habbohotel.catalog.TargetOffer;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.TargetedOfferComposer;
import com.eu.habbo.messages.outgoing.events.calendar.AdventCalendarDataComposer;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class UsernameEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      boolean calendar = false;
      if (!this.client
         .getHabbo()
         .getHabboStats()
         .getAchievementProgress()
         .containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"))) {
         AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"));
         calendar = true;
      } else {
         long daysBetween = ChronoUnit.DAYS
            .between(new Date(this.client.getHabbo().getHabboInfo().getLastOnline() * 1000L).toInstant(), new Date().toInstant());
         Date lastLogin = new Date(this.client.getHabbo().getHabboInfo().getLastOnline());
         Calendar c1 = Calendar.getInstance();
         c1.add(6, -1);
         Calendar c2 = Calendar.getInstance();
         c2.setTime(lastLogin);
         if (daysBetween == 1L) {
            if ((Integer)this.client
                  .getHabbo()
                  .getHabboStats()
                  .getAchievementProgress()
                  .get(Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"))
               == this.client.getHabbo().getHabboStats().loginStreak) {
               AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"));
            }

            this.client.getHabbo().getHabboStats().loginStreak++;
            calendar = true;
         } else if (daysBetween >= 1L) {
            calendar = true;
         } else if (lastLogin.getTime() / 1000L - Emulator.getIntUnixTimestamp() > 86400L) {
            this.client.getHabbo().getHabboStats().loginStreak = 0;
         }
      }

      if (!this.client
         .getHabbo()
         .getHabboStats()
         .getAchievementProgress()
         .containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"))) {
         AchievementManager.progressAchievement(
            this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"), 0
         );
      } else {
         int daysRegistered = (Emulator.getIntUnixTimestamp() - this.client.getHabbo().getHabboInfo().getAccountCreated()) / 86400;
         int days = this.client
            .getHabbo()
            .getHabboStats()
            .getAchievementProgress(Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"));
         if (daysRegistered - days > 0) {
            AchievementManager.progressAchievement(
               this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"), daysRegistered - days
            );
         }
      }

      if (!this.client
         .getHabbo()
         .getHabboStats()
         .getAchievementProgress()
         .containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("TraderPass"))) {
         AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("TraderPass"));
      }

      Connection connection = Emulator.getDatabase().getDataSource().getConnection();

      try {
         PreparedStatement achievementQueueStatement = connection.prepareStatement("SELECT * FROM users_achievements_queue WHERE user_id = ?");

         try {
            achievementQueueStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
            ResultSet achievementSet = achievementQueueStatement.executeQuery();

            try {
               while (achievementSet.next()) {
                  AchievementManager.progressAchievement(
                     this.client.getHabbo(),
                     Emulator.getGameEnvironment().getAchievementManager().getAchievement(achievementSet.getInt("achievement_id")),
                     achievementSet.getInt("amount")
                  );
               }
            } catch (Throwable var12) {
               if (achievementSet != null) {
                  try {
                     achievementSet.close();
                  } catch (Throwable var10) {
                     var12.addSuppressed(var10);
                  }
               }

               throw var12;
            }

            if (achievementSet != null) {
               achievementSet.close();
            }

            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM users_achievements_queue WHERE user_id = ?");

            try {
               deleteStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
               deleteStatement.execute();
            } catch (Throwable var11) {
               if (deleteStatement != null) {
                  try {
                     deleteStatement.close();
                  } catch (Throwable var9) {
                     var11.addSuppressed(var9);
                  }
               }

               throw var11;
            }

            if (deleteStatement != null) {
               deleteStatement.close();
            }
         } catch (Throwable var13) {
            if (achievementQueueStatement != null) {
               try {
                  achievementQueueStatement.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (achievementQueueStatement != null) {
            achievementQueueStatement.close();
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

      if (Emulator.getConfig().getBoolean("hotel.calendar.enabled")) {
         CalendarCampaign campaign = Emulator.getGameEnvironment()
            .getCalendarManager()
            .getCalendarCampaign(Emulator.getConfig().getValue("hotel.calendar.default"));
         if (campaign != null) {
            long daysBetween = ChronoUnit.DAYS.between(new Timestamp(campaign.getStartTimestamp().intValue() * 1000L).toInstant(), new Date().toInstant());
            if (daysBetween >= 0L) {
               this.client
                  .sendResponse(
                     new AdventCalendarDataComposer(
                        campaign.getName(),
                        campaign.getImage(),
                        campaign.getTotalDays(),
                        (int)daysBetween,
                        this.client.getHabbo().getHabboStats().calendarRewardsClaimed,
                        campaign.getLockExpired()
                     )
                  );
               this.client.sendResponse(new NuxAlertComposer("openView/calendar"));
            }
         }
      }

      if (TargetOffer.ACTIVE_TARGET_OFFER_ID > 0) {
         TargetOffer offer = Emulator.getGameEnvironment().getCatalogManager().getTargetOffer(TargetOffer.ACTIVE_TARGET_OFFER_ID);
         if (offer != null) {
            this.client.sendResponse(new TargetedOfferComposer(this.client.getHabbo(), offer));
         }
      }

      this.client.getHabbo().getHabboInfo().setLastOnline(Emulator.getIntUnixTimestamp());
   }
}
