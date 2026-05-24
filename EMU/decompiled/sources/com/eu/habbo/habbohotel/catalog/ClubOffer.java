package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/ClubOffer.class */
public class ClubOffer implements ISerialize {
    private final int id;
    private final String name;
    private final int days;
    private final int credits;
    private final int points;
    private final int pointsType;
    private final boolean vip;
    private final boolean deal;

    public ClubOffer(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.days = resultSet.getInt("days");
        this.credits = resultSet.getInt("credits");
        this.points = resultSet.getInt("points");
        this.pointsType = resultSet.getInt("points_type");
        this.vip = resultSet.getString("type").equalsIgnoreCase("vip");
        this.deal = resultSet.getString("deal").equals("1");
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getDays() {
        return this.days;
    }

    public int getCredits() {
        return this.credits;
    }

    public int getPoints() {
        return this.points;
    }

    public int getPointsType() {
        return this.pointsType;
    }

    public boolean isVip() {
        return this.vip;
    }

    public boolean isDeal() {
        return this.deal;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serialize(serverMessage, Emulator.getIntUnixTimestamp());
    }

    public void serialize(ServerMessage serverMessage, int i) {
        int iMax = Math.max(Emulator.getIntUnixTimestamp(), i);
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.name);
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(this.credits));
        serverMessage.appendInt(Integer.valueOf(this.points));
        serverMessage.appendInt(Integer.valueOf(this.pointsType));
        serverMessage.appendBoolean(Boolean.valueOf(this.vip));
        long j = this.days * 86400;
        long jFloor = j - ((long) (((int) Math.floor(((double) ((int) j)) / 3.21408E7d)) * 32140800));
        long jFloor2 = jFloor - ((long) (((int) Math.floor(((double) ((int) jFloor)) / 2678400.0d)) * 2678400));
        long jFloor3 = jFloor2 - ((long) (((int) Math.floor(((double) ((int) jFloor2)) / 86400.0d)) * 86400));
        serverMessage.appendInt(Integer.valueOf((((int) j) / 86400) / 31));
        serverMessage.appendInt(Integer.valueOf((int) jFloor3));
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf((int) jFloor3));
        int i2 = (int) (((long) iMax) + j);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(((long) i2) * 1000);
        serverMessage.appendInt(Integer.valueOf(calendar.get(1)));
        serverMessage.appendInt(Integer.valueOf(calendar.get(2) + 1));
        serverMessage.appendInt(Integer.valueOf(calendar.get(5)));
    }
}
