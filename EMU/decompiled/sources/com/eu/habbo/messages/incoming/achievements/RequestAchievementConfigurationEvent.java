package com.eu.habbo.messages.incoming.achievements;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.InventoryAchievementsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/achievements/RequestAchievementConfigurationEvent.class */
public class RequestAchievementConfigurationEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new InventoryAchievementsComposer());
    }
}
