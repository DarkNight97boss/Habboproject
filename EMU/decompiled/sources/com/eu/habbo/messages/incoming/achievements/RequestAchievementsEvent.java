package com.eu.habbo.messages.incoming.achievements;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.achievements.AchievementListComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/achievements/RequestAchievementsEvent.class */
public class RequestAchievementsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new AchievementListComposer(this.client.getHabbo()));
    }
}
