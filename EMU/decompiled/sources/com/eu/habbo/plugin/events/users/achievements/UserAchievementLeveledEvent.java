package com.eu.habbo.plugin.events.users.achievements;

import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/achievements/UserAchievementLeveledEvent.class */
public class UserAchievementLeveledEvent extends UserAchievementEvent {
    public final AchievementLevel oldLevel;
    public final AchievementLevel newLevel;

    public UserAchievementLeveledEvent(Habbo habbo, Achievement achievement, AchievementLevel achievementLevel, AchievementLevel achievementLevel2) {
        super(habbo, achievement);
        this.oldLevel = achievementLevel;
        this.newLevel = achievementLevel2;
    }
}
