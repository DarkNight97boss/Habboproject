package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionRequesterRoomComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideVisitUserEvent.class */
public class GuideVisitUserEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByHelper = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHelper(this.client.getHabbo());
        if (guideTourByHelper != null) {
            this.client.sendResponse(new GuideSessionRequesterRoomComposer(guideTourByHelper.getNoob().getHabboInfo().getCurrentRoom()));
        }
    }
}
