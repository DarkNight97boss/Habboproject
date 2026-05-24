package com.eu.habbo.plugin.events.users.achievements;

import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/achievements/UserAchievementProgressEvent.class */
public class UserAchievementProgressEvent extends UserAchievementEvent {
    public final int progressed;

    public UserAchievementProgressEvent(Habbo habbo, Achievement achievement, int i) {
        super(habbo, achievement);
        this.progressed = i;
    }
}
