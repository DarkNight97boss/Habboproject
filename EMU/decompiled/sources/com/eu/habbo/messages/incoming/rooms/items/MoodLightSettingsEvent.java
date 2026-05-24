package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.MoodLightDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MoodLightSettingsEvent.class */
public class MoodLightSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            this.client.sendResponse(new MoodLightDataComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom().getMoodlightData()));
        }
    }
}
