package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideCloseHelpRequestEvent.class */
public class GuideCloseHelpRequestEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByHabbo = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHabbo(this.client.getHabbo());
        if (guideTourByHabbo != null) {
            Emulator.getGameEnvironment().getGuideManager().endSession(guideTourByHabbo);
        }
    }
}
