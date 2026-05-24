package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewCommunityGoalComposer.class */
public class HotelViewCommunityGoalComposer extends MessageComposer {
    private final boolean achieved;
    private final int personalContributionScore;
    private final int personalRank;
    private final int totalAmount;
    private final int communityHighestAchievedLevel;
    private final int scoreRemainingUntilNextLevel;
    private final int percentCompletionTowardsNextLevel;
    private final String competitionName;
    private final int timeLeft;
    private final int[] rankData;

    public HotelViewCommunityGoalComposer(boolean z, int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int[] iArr) {
        this.achieved = z;
        this.personalContributionScore = i;
        this.personalRank = i2;
        this.totalAmount = i3;
        this.communityHighestAchievedLevel = i4;
        this.scoreRemainingUntilNextLevel = i5;
        this.percentCompletionTowardsNextLevel = i6;
        this.competitionName = str;
        this.timeLeft = i7;
        this.rankData = iArr;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewCommunityGoalComposer);
        this.response.appendBoolean(Boolean.valueOf(this.achieved));
        this.response.appendInt(Integer.valueOf(this.personalContributionScore));
        this.response.appendInt(Integer.valueOf(this.personalRank));
        this.response.appendInt(Integer.valueOf(this.personalRank));
        this.response.appendInt(Integer.valueOf(this.totalAmount));
        this.response.appendInt(Integer.valueOf(this.communityHighestAchievedLevel));
        this.response.appendInt(Integer.valueOf(this.scoreRemainingUntilNextLevel));
        this.response.appendString(this.competitionName);
        this.response.appendInt(Integer.valueOf(this.timeLeft));
        this.response.appendInt(Integer.valueOf(this.rankData.length));
        for (int i : this.rankData) {
            this.response.appendInt(Integer.valueOf(i));
        }
        return this.response;
    }
}
