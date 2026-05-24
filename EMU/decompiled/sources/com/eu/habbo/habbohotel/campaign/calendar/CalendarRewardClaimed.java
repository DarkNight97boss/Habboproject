package com.eu.habbo.habbohotel.campaign.calendar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/campaign/calendar/CalendarRewardClaimed.class */
public class CalendarRewardClaimed {
    private final int user_id;
    private final int campaign;
    private final int day;
    private final int reward_id;
    private final Timestamp timestamp;

    public CalendarRewardClaimed(ResultSet resultSet) throws SQLException {
        this.user_id = resultSet.getInt("user_id");
        this.campaign = resultSet.getInt("campaign_id");
        this.day = resultSet.getInt("day");
        this.reward_id = resultSet.getInt("reward_id");
        this.timestamp = new Timestamp(((long) resultSet.getInt("timestamp")) * 1000);
    }

    public CalendarRewardClaimed(int i, int i2, int i3, int i4, Timestamp timestamp) {
        this.user_id = i;
        this.campaign = i2;
        this.day = i3;
        this.reward_id = i4;
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return this.user_id;
    }

    public int getCampaignId() {
        return this.campaign;
    }

    public int getDay() {
        return this.day;
    }

    public int getRewardId() {
        return this.reward_id;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }
}
