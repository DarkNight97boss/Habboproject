package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/UserActivityEvent.class */
public class UserActivityEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string;
        String string2 = this.packet.readString();
        string = this.packet.readString();
        String string3 = this.packet.readString();
        switch (string2) {
            case "Quiz":
                if (string.equalsIgnoreCase("7")) {
                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SafetyQuizGraduate"));
                    break;
                }
                break;
        }
        switch (string3) {
            case "forum.can.read.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModForumCanReadSeen"));
                break;
            case "forum.can.post.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModForumCanPostSeen"));
                break;
            case "forum.can.start.thread.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModForumCanPostThrdSeen"));
                break;
            case "forum.can.moderate.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModForumCanModerateSeen"));
                break;
            case "room.settings.doormode.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModDoorModeSeen"));
                break;
            case "room.settings.walkthrough.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModWalkthroughSeen"));
                break;
            case "room.settings.chat.scrollspeed.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModChatScrollSpeedSeen"));
                break;
            case "room.settings.chat.hearrange.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModChatHearRangeSeen"));
                break;
            case "room.settings.chat.floodfilter.seen":
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModChatFloodFilterSeen"));
                break;
        }
    }
}
