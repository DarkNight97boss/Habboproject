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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/handshake/UsernameEvent.class */
public class UsernameEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        TargetOffer targetOffer;
        CalendarCampaign calendarCampaign;
        if (this.client.getHabbo().getHabboStats().getAchievementProgress().containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"))) {
            long jBetween = ChronoUnit.DAYS.between(new Date(((long) this.client.getHabbo().getHabboInfo().getLastOnline()) * 1000).toInstant(), new Date().toInstant());
            Date date = new Date(this.client.getHabbo().getHabboInfo().getLastOnline());
            Calendar.getInstance().add(6, -1);
            Calendar.getInstance().setTime(date);
            if (jBetween == 1) {
                if (((Integer) this.client.getHabbo().getHabboStats().getAchievementProgress().get(Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"))).intValue() == this.client.getHabbo().getHabboStats().loginStreak) {
                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"));
                }
                this.client.getHabbo().getHabboStats().loginStreak++;
            } else if (jBetween < 1 && (date.getTime() / 1000) - ((long) Emulator.getIntUnixTimestamp()) > 86400) {
                this.client.getHabbo().getHabboStats().loginStreak = 0;
            }
        } else {
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Login"));
        }
        if (this.client.getHabbo().getHabboStats().getAchievementProgress().containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"))) {
            int intUnixTimestamp = (Emulator.getIntUnixTimestamp() - this.client.getHabbo().getHabboInfo().getAccountCreated()) / 86400;
            int achievementProgress = this.client.getHabbo().getHabboStats().getAchievementProgress(Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"));
            if (intUnixTimestamp - achievementProgress > 0) {
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"), intUnixTimestamp - achievementProgress);
            }
        } else {
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RegistrationDuration"), 0);
        }
        if (!this.client.getHabbo().getHabboStats().getAchievementProgress().containsKey(Emulator.getGameEnvironment().getAchievementManager().getAchievement("TraderPass"))) {
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("TraderPass"));
        }
        Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_achievements_queue WHERE user_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement(resultSetExecuteQuery.getInt("achievement_id")), resultSetExecuteQuery.getInt("amount"));
                    } catch (Throwable th) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                }
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("DELETE FROM users_achievements_queue WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement2.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                    preparedStatementPrepareStatement2.execute();
                    if (preparedStatementPrepareStatement2 != null) {
                        preparedStatementPrepareStatement2.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    if (Emulator.getConfig().getBoolean("hotel.calendar.enabled") && (calendarCampaign = Emulator.getGameEnvironment().getCalendarManager().getCalendarCampaign(Emulator.getConfig().getValue("hotel.calendar.default"))) != null) {
                        long jBetween2 = ChronoUnit.DAYS.between(new Timestamp(((long) calendarCampaign.getStartTimestamp().intValue()) * 1000).toInstant(), new Date().toInstant());
                        if (jBetween2 >= 0) {
                            this.client.sendResponse(new AdventCalendarDataComposer(calendarCampaign.getName(), calendarCampaign.getImage(), calendarCampaign.getTotalDays(), (int) jBetween2, this.client.getHabbo().getHabboStats().calendarRewardsClaimed, calendarCampaign.getLockExpired()));
                            this.client.sendResponse(new NuxAlertComposer("openView/calendar"));
                        }
                    }
                    if (TargetOffer.ACTIVE_TARGET_OFFER_ID > 0 && (targetOffer = Emulator.getGameEnvironment().getCatalogManager().getTargetOffer(TargetOffer.ACTIVE_TARGET_OFFER_ID)) != null) {
                        this.client.sendResponse(new TargetedOfferComposer(this.client.getHabbo(), targetOffer));
                    }
                    this.client.getHabbo().getHabboInfo().setLastOnline(Emulator.getIntUnixTimestamp());
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement2 != null) {
                        try {
                            preparedStatementPrepareStatement2.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (Throwable th5) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable th6) {
                    th5.addSuppressed(th6);
                }
            }
            throw th5;
        }
    }
}
