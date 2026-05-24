package com.eu.habbo.messages.outgoing.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/achievements/AchievementUnlockedComposer.class */
public class AchievementUnlockedComposer extends MessageComposer {
    private final Achievement achievement;
    private final Habbo habbo;

    public AchievementUnlockedComposer(Habbo habbo, Achievement achievement) {
        this.achievement = achievement;
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AchievementUnlockedComposer);
        AchievementLevel levelForProgress = this.achievement.getLevelForProgress(this.habbo.getHabboStats().getAchievementProgress(this.achievement));
        this.response.appendInt(Integer.valueOf(this.achievement.id));
        this.response.appendInt(Integer.valueOf(levelForProgress.level));
        this.response.appendInt((Integer) 144);
        this.response.appendString("ACH_" + this.achievement.name + levelForProgress.level);
        this.response.appendInt(Integer.valueOf(levelForProgress.rewardAmount));
        this.response.appendInt(Integer.valueOf(levelForProgress.rewardType));
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 21);
        this.response.appendString(levelForProgress.level > 1 ? "ACH_" + this.achievement.name + (levelForProgress.level - 1) : Emulator.PREVIEW);
        this.response.appendString(this.achievement.category.name());
        this.response.appendBoolean(true);
        return this.response;
    }
}
