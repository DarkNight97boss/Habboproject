package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideHandleHelpRequestEvent.class */
public class GuideHandleHelpRequestEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByHelper = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHelper(this.client.getHabbo());
        if (guideTourByHelper == null) {
            return;
        }
        if (this.packet.readBoolean()) {
            Emulator.getGameEnvironment().getGuideManager().startSession(guideTourByHelper, this.client.getHabbo());
        } else {
            Emulator.getGameEnvironment().getGuideManager().declineTour(guideTourByHelper);
        }
    }
}
