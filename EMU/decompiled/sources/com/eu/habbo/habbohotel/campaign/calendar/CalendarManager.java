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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/campaign/calendar/CalendarManager.class */
public class CalendarManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarCampaign.class);
    private static final Map<Integer, CalendarCampaign> calendarCampaigns = new THashMap();
    public static double HC_MODIFIER;

    public CalendarManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        reload();
        LOGGER.info("Calendar Manager -> Loaded! ({} MS)", Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
    }

    public void dispose() {
        calendarCampaigns.clear();
    }

    public boolean reload() {
        dispose();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM calendar_campaigns WHERE enabled = 1");
                try {
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            calendarCampaigns.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new CalendarCampaign(resultSetExecuteQuery));
                        } finally {
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    try {
                        connection = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM calendar_rewards");
                            try {
                                resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                                while (resultSetExecuteQuery.next()) {
                                    try {
                                        CalendarCampaign calendarCampaign = calendarCampaigns.get(Integer.valueOf(resultSetExecuteQuery.getInt("campaign_id")));
                                        if (calendarCampaign != null) {
                                            calendarCampaign.addReward(new CalendarRewardObject(resultSetExecuteQuery));
                                        }
                                    } finally {
                                    }
                                }
                                if (resultSetExecuteQuery != null) {
                                    resultSetExecuteQuery.close();
                                }
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                if (connection != null) {
                                    connection.close();
                                }
                                HC_MODIFIER = Emulator.getConfig().getDouble("hotel.calendar.pixels.hc_modifier", Double.valueOf(2.0d));
                                return true;
                            } finally {
                                if (preparedStatementPrepareStatement != null) {
                                    try {
                                        preparedStatementPrepareStatement.close();
                                    } catch (Throwable th) {
                                        th.addSuppressed(th);
                                    }
                                }
                            }
                        } finally {
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                        }
                    } catch (SQLException e) {
                        LOGGER.error("Caught SQL exception", e);
                        return false;
                    }
                } catch (Throwable th3) {
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
            return false;
        }
    }

    public void addCampaign(CalendarCampaign calendarCampaign) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO calendar_campaigns ( name, image, start_timestamp, total_days, lock_expired) VALUES (?, ?, ?, ? , ?)", 1);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setString(1, calendarCampaign.getName());
            preparedStatementPrepareStatement.setString(2, calendarCampaign.getImage());
            preparedStatementPrepareStatement.setInt(3, calendarCampaign.getStartTimestamp().intValue());
            preparedStatementPrepareStatement.setInt(4, calendarCampaign.getTotalDays());
            preparedStatementPrepareStatement.setBoolean(5, calendarCampaign.getLockExpired());
            if (preparedStatementPrepareStatement.executeUpdate() == 0) {
                throw new SQLException("Creating calendar campaign failed, no rows affected.");
            }
            ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
            try {
                if (!generatedKeys.next()) {
                    throw new SQLException("Creating calendar campaign failed, no ID found.");
                }
                calendarCampaign.setId(generatedKeys.getInt(1));
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                calendarCampaigns.put(Integer.valueOf(calendarCampaign.getId()), calendarCampaign);
            } catch (Throwable th) {
                if (generatedKeys != null) {
                    try {
                        generatedKeys.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public boolean deleteCampaign(CalendarCampaign calendarCampaign) {
        calendarCampaigns.remove(Integer.valueOf(calendarCampaign.getId()));
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM calendar_campaigns WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, calendarCampaign.getId());
                    boolean zExecute = preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return zExecute;
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public CalendarCampaign getCalendarCampaign(String str) {
        return calendarCampaigns.values().stream().filter(calendarCampaign -> {
            return Objects.equals(calendarCampaign.getName(), str);
        }).findFirst().orElse(null);
    }

    public Map<Integer, CalendarCampaign> getCalendarCampaigns() {
        return calendarCampaigns;
    }

    public void claimCalendarReward(Habbo habbo, String str, int i, boolean z) {
        int iBetween;
        CalendarCampaign calendarCampaignOrElse = calendarCampaigns.values().stream().filter(calendarCampaign -> {
            return Objects.equals(calendarCampaign.getName(), str);
        }).findFirst().orElse(null);
        if (calendarCampaignOrElse != null && habbo.getHabboStats().calendarRewardsClaimed.stream().noneMatch(calendarRewardClaimed -> {
            return calendarRewardClaimed.getCampaignId() == calendarCampaignOrElse.getId() && calendarRewardClaimed.getDay() == i;
        })) {
            Set<Integer> setKeySet = calendarCampaignOrElse.getRewards().keySet();
            THashMap tHashMap = new THashMap();
            if (setKeySet.isEmpty()) {
                return;
            }
            setKeySet.forEach(num -> {
                tHashMap.put(Integer.valueOf(tHashMap.size() + 1), num);
            });
            CalendarRewardObject calendarRewardObject = calendarCampaignOrElse.getRewards().get(Integer.valueOf(((Integer) tHashMap.get(Integer.valueOf(Emulator.getRandom().nextInt((tHashMap.size() - 1) + 1) + 1))).intValue()));
            if (calendarRewardObject != null && (iBetween = (int) ChronoUnit.DAYS.between(new Timestamp(((long) calendarCampaignOrElse.getStartTimestamp().intValue()) * 1000).toInstant(), new Date().toInstant())) >= 0 && iBetween <= calendarCampaignOrElse.getTotalDays()) {
                int i2 = iBetween - i;
                if ((((i2 <= 2 || !calendarCampaignOrElse.getLockExpired()) && i2 >= 0) || (z && habbo.hasPermission("acc_calendar_force"))) && !((UserClaimRewardEvent) Emulator.getPluginManager().fireEvent(new UserClaimRewardEvent(habbo, calendarCampaignOrElse, i, calendarRewardObject, z))).isCancelled()) {
                    habbo.getHabboStats().calendarRewardsClaimed.add(new CalendarRewardClaimed(habbo.getHabboInfo().getId(), calendarCampaignOrElse.getId(), i, calendarRewardObject.getId(), new Timestamp(System.currentTimeMillis())));
                    habbo.getClient().sendResponse(new AdventCalendarProductComposer(true, calendarRewardObject, habbo));
                    calendarRewardObject.give(habbo);
                    try {
                        Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO calendar_rewards_claimed (user_id, campaign_id, day, reward_id, timestamp) VALUES (?, ?, ?, ?, ?)");
                            try {
                                preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                                preparedStatementPrepareStatement.setInt(2, calendarCampaignOrElse.getId());
                                preparedStatementPrepareStatement.setInt(3, i);
                                preparedStatementPrepareStatement.setInt(4, calendarRewardObject.getId());
                                preparedStatementPrepareStatement.setInt(5, Emulator.getIntUnixTimestamp());
                                preparedStatementPrepareStatement.execute();
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (Throwable th) {
                                if (preparedStatementPrepareStatement != null) {
                                    try {
                                        preparedStatementPrepareStatement.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        } finally {
                        }
                    } catch (SQLException e) {
                        LOGGER.error("Caught SQL exception", e);
                    }
                }
            }
        }
    }
}
