package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/ClubCenterDataComposer.class */
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

    public ClubCenterDataComposer(int i, String str, double d, int i2, int i3, int i4, int i5, int i6, int i7) {
        this.currentHcStreak = i;
        this.firstSubDate = str;
        this.kickbackPercentage = d;
        this.totalCreditsMissed = i2;
        this.totalCreditsRewarded = i3;
        this.totalCreditsSpent = i4;
        this.creditRewardForStreakBonus = i5;
        this.creditRewardForMonthlySpent = i6;
        this.timeUntilPayday = i7;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ClubCenterDataComposer);
        this.response.appendInt(Integer.valueOf(this.currentHcStreak));
        this.response.appendString(this.firstSubDate);
        this.response.appendDouble(this.kickbackPercentage);
        this.response.appendInt(Integer.valueOf(this.totalCreditsMissed));
        this.response.appendInt(Integer.valueOf(this.totalCreditsRewarded));
        this.response.appendInt(Integer.valueOf(this.totalCreditsSpent));
        this.response.appendInt(Integer.valueOf(this.creditRewardForStreakBonus));
        this.response.appendInt(Integer.valueOf(this.creditRewardForMonthlySpent));
        this.response.appendInt(Integer.valueOf(this.timeUntilPayday));
        return this.response;
    }
}
