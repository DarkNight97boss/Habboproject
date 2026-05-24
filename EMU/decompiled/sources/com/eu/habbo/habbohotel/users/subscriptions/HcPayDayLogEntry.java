package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.DatabaseLoggable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/subscriptions/HcPayDayLogEntry.class */
public class HcPayDayLogEntry implements Runnable, DatabaseLoggable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HcPayDayLogEntry.class);
    private static final String QUERY = "INSERT INTO `logs_hc_payday` (`timestamp`, `user_id`, `hc_streak`, `total_coins_spent`, `reward_coins_spent`, `reward_streak`, `total_payout`, `currency`, `claimed`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public final int timestamp;
    public final int userId;
    public final int hcStreak;
    public final int totalCoinsSpent;
    public final int rewardCoinsSpent;
    public final int rewardStreak;
    public final int totalPayout;
    public final String currency;
    public final boolean claimed;

    public HcPayDayLogEntry(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str, boolean z) {
        this.timestamp = i;
        this.userId = i2;
        this.hcStreak = i3;
        this.totalCoinsSpent = i4;
        this.rewardCoinsSpent = i5;
        this.rewardStreak = i6;
        this.totalPayout = i7;
        this.currency = str;
        this.claimed = z;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public String getQuery() {
        return QUERY;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public void log(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.timestamp);
        preparedStatement.setInt(2, this.userId);
        preparedStatement.setInt(3, this.hcStreak);
        preparedStatement.setInt(4, this.totalCoinsSpent);
        preparedStatement.setInt(5, this.rewardCoinsSpent);
        preparedStatement.setInt(6, this.rewardStreak);
        preparedStatement.setInt(7, this.totalPayout);
        preparedStatement.setString(8, this.currency);
        preparedStatement.setInt(9, this.claimed ? 1 : 0);
        preparedStatement.addBatch();
    }

    @Override // java.lang.Runnable
    public void run() {
        Emulator.getDatabaseLogger().store(this);
    }
}
