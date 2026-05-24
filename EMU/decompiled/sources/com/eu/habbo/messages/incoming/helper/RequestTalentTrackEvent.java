package com.eu.habbo.messages.incoming.helper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.TalentTrackType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.achievements.talenttrack.TalentTrackComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/helper/RequestTalentTrackEvent.class */
public class RequestTalentTrackEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (Emulator.getConfig().getBoolean("hotel.talenttrack.enabled")) {
            this.client.sendResponse(new TalentTrackComposer(this.client.getHabbo(), TalentTrackType.valueOf(this.packet.readString().toUpperCase())));
        }
    }
}
