package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

public class ClubOffer implements ISerialize {
   private final int id;
   private final String name;
   private final int days;
   private final int credits;
   private final int points;
   private final int pointsType;
   private final boolean vip;
   private final boolean deal;

   public ClubOffer(ResultSet set) throws SQLException {
      this.id = set.getInt("id");
      this.name = set.getString("name");
      this.days = set.getInt("days");
      this.credits = set.getInt("credits");
      this.points = set.getInt("points");
      this.pointsType = set.getInt("points_type");
      this.vip = set.getString("type").equalsIgnoreCase("vip");
      this.deal = set.getString("deal").equals("1");
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

   @Override
   public void serialize(ServerMessage message) {
      this.serialize(message, Emulator.getIntUnixTimestamp());
   }

   public void serialize(ServerMessage message, int hcExpireTimestamp) {
      hcExpireTimestamp = Math.max(Emulator.getIntUnixTimestamp(), hcExpireTimestamp);
      message.appendInt(this.id);
      message.appendString(this.name);
      message.appendBoolean(false);
      message.appendInt(this.credits);
      message.appendInt(this.points);
      message.appendInt(this.pointsType);
      message.appendBoolean(this.vip);
      long seconds = this.days * 86400;
      long secondsTotal = seconds;
      int totalYears = (int)Math.floor((int)seconds / 3.21408E7);
      seconds -= totalYears * 32140800;
      int totalMonths = (int)Math.floor((int)seconds / 2678400.0);
      seconds -= totalMonths * 2678400;
      int totalDays = (int)Math.floor((int)seconds / 86400.0);
      seconds -= totalDays * 86400;
      message.appendInt((int)secondsTotal / 86400 / 31);
      message.appendInt((int)seconds);
      message.appendBoolean(false);
      message.appendInt((int)seconds);
      hcExpireTimestamp = (int)(hcExpireTimestamp + secondsTotal);
      Calendar cal = Calendar.getInstance();
      cal.setTimeZone(TimeZone.getTimeZone("UTC"));
      cal.setTimeInMillis(hcExpireTimestamp * 1000L);
      message.appendInt(cal.get(1));
      message.appendInt(cal.get(2) + 1);
      message.appendInt(cal.get(5));
   }
}
