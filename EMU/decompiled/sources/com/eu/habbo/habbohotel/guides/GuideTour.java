package com.eu.habbo.habbohotel.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guides/GuideTour.class */
public class GuideTour {
    private final Habbo noob;
    private final String helpRequest;
    private Habbo helper;
    private int startTime;
    private int endTime;
    private boolean ended;
    private final THashSet<GuideChatMessage> sendMessages = new THashSet<>();
    private final THashSet<Integer> declinedHelpers = new THashSet<>();
    public int checkSum = 0;
    private GuideRecommendStatus wouldRecommend = GuideRecommendStatus.UNKNOWN;

    public GuideTour(Habbo habbo, String str) {
        this.noob = habbo;
        this.helpRequest = str;
        AchievementManager.progressAchievement(this.noob, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideAdvertisementReader"));
    }

    public void finish() {
    }

    public Habbo getNoob() {
        return this.noob;
    }

    public String getHelpRequest() {
        return this.helpRequest;
    }

    public Habbo getHelper() {
        return this.helper;
    }

    public void setHelper(Habbo habbo) {
        this.helper = habbo;
    }

    public void addMessage(GuideChatMessage guideChatMessage) {
        this.sendMessages.add(guideChatMessage);
    }

    public GuideRecommendStatus getWouldRecommend() {
        return this.wouldRecommend;
    }

    public void setWouldRecommend(GuideRecommendStatus guideRecommendStatus) {
        this.wouldRecommend = guideRecommendStatus;
        if (this.wouldRecommend == GuideRecommendStatus.YES) {
            AchievementManager.progressAchievement(getHelper(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideRecommendation"));
        }
    }

    public void addDeclinedHelper(int i) {
        this.declinedHelpers.add(Integer.valueOf(i));
    }

    public boolean hasDeclined(int i) {
        return this.declinedHelpers.contains(Integer.valueOf(i));
    }

    public void end() {
        this.ended = true;
        this.endTime = Emulator.getIntUnixTimestamp();
        AchievementManager.progressAchievement(this.helper, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideEnrollmentLifetime"));
        AchievementManager.progressAchievement(this.helper, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideRequestHandler"));
        AchievementManager.progressAchievement(this.noob, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideRequester"));
    }

    public boolean isEnded() {
        return this.ended;
    }

    public int getStartTime() {
        return this.startTime;
    }

    public void setStartTime(int i) {
        this.startTime = i;
    }

    public int getEndTime() {
        return this.endTime;
    }
}
