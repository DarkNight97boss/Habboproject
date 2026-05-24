package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/subscriptions/Subscription.class */
public class Subscription {
    public static final String HABBO_CLUB = "HABBO_CLUB";
    private final int id;
    private final int userId;
    private final String subscriptionType;
    private final int timestampStart;
    private int duration;
    private boolean active;

    public Subscription(Integer num, Integer num2, String str, Integer num3, Integer num4, Boolean bool) {
        this.id = num.intValue();
        this.userId = num2.intValue();
        this.subscriptionType = str;
        this.timestampStart = num3.intValue();
        this.duration = num4.intValue();
        this.active = bool.booleanValue();
    }

    public int getSubscriptionId() {
        return this.id;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getSubscriptionType() {
        return this.subscriptionType;
    }

    public int getDuration() {
        return this.duration;
    }

    public void addDuration(int i) {
        this.duration += i;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE `users_subscriptions` SET `duration` = ? WHERE `id` = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.duration);
                    preparedStatementPrepareStatement.setInt(2, this.id);
                    preparedStatementPrepareStatement.executeUpdate();
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
            SubscriptionManager.LOGGER.error("Caught SQL exception", e);
        }
    }

    public void setActive(boolean z) {
        this.active = z;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE `users_subscriptions` SET `active` = ? WHERE `id` = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.active ? 1 : 0);
                    preparedStatementPrepareStatement.setInt(2, this.id);
                    preparedStatementPrepareStatement.executeUpdate();
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
            SubscriptionManager.LOGGER.error("Caught SQL exception", e);
        }
    }

    public int getRemaining() {
        return (this.timestampStart + this.duration) - Emulator.getIntUnixTimestamp();
    }

    public int getTimestampStart() {
        return this.timestampStart;
    }

    public int getTimestampEnd() {
        return this.timestampStart + this.duration;
    }

    public boolean isActive() {
        return this.active;
    }

    public void onCreated() {
    }

    public void onExtended(int i) {
    }

    public void onExpired() {
    }
}
