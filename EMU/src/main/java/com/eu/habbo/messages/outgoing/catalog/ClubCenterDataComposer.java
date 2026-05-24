package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class ClubCenterDataComposer extends MessageComposer {
   public final int currentHcStreak;
   public final String firstSubDate;
   public final double kickbackPercentage;
   public final int totalCreditsMissed;
   public final int totalCreditsRewarded;
   public final int totalCreditsSpent;
   public final int creditRewardForStreakBonus;
   public final int creditRewardForMonthlySpent;
   public final int timeUntilPayday;

   public ClubCenterDataComposer(
      int currentHcStreak,
      String firstSubDate,
      double kickbackPercentage,
      int totalCreditsMissed,
      int totalCreditsRewarded,
      int totalCreditsSpent,
      int creditRewardForStreakBonus,
      int creditRewardForMonthlySpent,
      int timeUntilPayday
   ) {
      this.currentHcStreak = currentHcStreak;
      this.firstSubDate = firstSubDate;
      this.kickbackPercentage = kickbackPercentage;
      this.totalCreditsMissed = totalCreditsMissed;
      this.totalCreditsRewarded = totalCreditsRewarded;
      this.totalCreditsSpent = totalCreditsSpent;
      this.creditRewardForStreakBonus = creditRewardForStreakBonus;
      this.creditRewardForMonthlySpent = creditRewardForMonthlySpent;
      this.timeUntilPayday = timeUntilPayday;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(3277);
      this.response.appendInt(this.currentHcStreak);
      this.response.appendString(this.firstSubDate);
      this.response.appendDouble(this.kickbackPercentage);
      this.response.appendInt(this.totalCreditsMissed);
      this.response.appendInt(this.totalCreditsRewarded);
      this.response.appendInt(this.totalCreditsSpent);
      this.response.appendInt(this.creditRewardForStreakBonus);
      this.response.appendInt(this.creditRewardForMonthlySpent);
      this.response.appendInt(this.timeUntilPayday);
      return this.response;
   }
}
