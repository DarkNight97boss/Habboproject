package com.eu.habbo.habbohotel.campaign.calendar;

import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/campaign/calendar/CalendarCampaign.class */
public class CalendarCampaign {
    private int id;
    private final String name;
    private final String image;
    private Map<Integer, CalendarRewardObject> rewards = new THashMap();
    private final Integer start_timestamp;
    private final int total_days;
    private final boolean lock_expired;

    public CalendarCampaign(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.image = resultSet.getString("image");
        this.start_timestamp = Integer.valueOf(resultSet.getInt("start_timestamp"));
        this.total_days = resultSet.getInt("total_days");
        this.lock_expired = resultSet.getInt("lock_expired") == 1;
    }

    public CalendarCampaign(int i, String str, String str2, Integer num, int i2, boolean z) {
        this.id = i;
        this.name = str;
        this.image = str2;
        this.start_timestamp = num;
        this.total_days = i2;
        this.lock_expired = z;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getImage() {
        return this.image;
    }

    public Integer getStartTimestamp() {
        return this.start_timestamp;
    }

    public int getTotalDays() {
        return this.total_days;
    }

    public boolean getLockExpired() {
        return this.lock_expired;
    }

    public Map<Integer, CalendarRewardObject> getRewards() {
        return this.rewards;
    }

    public void setId(int i) {
        this.id = i;
    }

    public void setRewards(Map<Integer, CalendarRewardObject> map) {
        this.rewards = map;
    }

    public void addReward(CalendarRewardObject calendarRewardObject) {
        this.rewards.put(Integer.valueOf(calendarRewardObject.getId()), calendarRewardObject);
    }
}
