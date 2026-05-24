package com.eu.habbo.messages.outgoing.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/achievements/AchievementProgressComposer.class */
public class AchievementProgressComposer extends MessageComposer {
    private final Habbo habbo;
    private final Achievement achievement;

    public AchievementProgressComposer(Habbo habbo, Achievement achievement) {
        this.habbo = habbo;
        this.achievement = achievement;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AchievementProgressComposer);
        int achievementProgress = this.habbo.getHabboStats().getAchievementProgress(this.achievement);
        AchievementLevel levelForProgress = this.achievement.getLevelForProgress(achievementProgress);
        AchievementLevel nextLevel = this.achievement.getNextLevel(levelForProgress != null ? levelForProgress.level : 0);
        if (levelForProgress != null && levelForProgress.level == this.achievement.levels.size()) {
            nextLevel = null;
        }
        int i = 1;
        if (nextLevel != null) {
            i = nextLevel.level;
        }
        if (levelForProgress != null && levelForProgress.level == this.achievement.levels.size()) {
            i = levelForProgress.level;
        }
        this.response.appendInt(Integer.valueOf(this.achievement.id));
        this.response.appendInt(Integer.valueOf(i));
        this.response.appendString("ACH_" + this.achievement.name + i);
        this.response.appendInt(Integer.valueOf(levelForProgress != null ? levelForProgress.progress : 0));
        this.response.appendInt(Integer.valueOf(nextLevel != null ? nextLevel.progress : 0));
        this.response.appendInt(Integer.valueOf(nextLevel != null ? nextLevel.rewardAmount : 0));
        this.response.appendInt(Integer.valueOf(nextLevel != null ? nextLevel.rewardType : 0));
        this.response.appendInt(Integer.valueOf(achievementProgress == -1 ? 0 : achievementProgress));
        this.response.appendBoolean(Boolean.valueOf(AchievementManager.hasAchieved(this.habbo, this.achievement)));
        this.response.appendString(this.achievement.category.toString().toLowerCase());
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendInt(Integer.valueOf(this.achievement.levels.size()));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
