package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ProgressAchievement.class */
public class ProgressAchievement extends RCONMessage<ProgressAchievementJSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ProgressAchievement$ProgressAchievementJSON.class */
    static class ProgressAchievementJSON {
        public int user_id;
        public int achievement_id;
        public int progress;

        ProgressAchievementJSON() {
        }
    }

    public ProgressAchievement() {
        super(ProgressAchievementJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, ProgressAchievementJSON progressAchievementJSON) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(progressAchievementJSON.user_id);
        if (habbo == null) {
            this.status = 2;
            return;
        }
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(progressAchievementJSON.achievement_id);
        if (achievement != null) {
            AchievementManager.progressAchievement(habbo, achievement, progressAchievementJSON.progress);
        } else {
            this.status = 1;
        }
    }
}
