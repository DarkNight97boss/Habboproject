package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideCancelHelpRequestEvent.class */
public class GuideCancelHelpRequestEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByNoob = Emulator.getGameEnvironment().getGuideManager().getGuideTourByNoob(this.client.getHabbo());
        if (guideTourByNoob != null) {
            guideTourByNoob.end();
            Emulator.getGameEnvironment().getGuideManager().endSession(guideTourByNoob);
        }
    }
}
