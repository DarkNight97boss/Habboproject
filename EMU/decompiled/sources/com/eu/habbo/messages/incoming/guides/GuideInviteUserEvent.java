package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionInvitedToGuideRoomComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideInviteUserEvent.class */
public class GuideInviteUserEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByHelper = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHelper(this.client.getHabbo());
        if (guideTourByHelper != null) {
            ServerMessage serverMessageCompose = new GuideSessionInvitedToGuideRoomComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom()).compose();
            guideTourByHelper.getNoob().getClient().sendResponse(serverMessageCompose);
            guideTourByHelper.getHelper().getClient().sendResponse(serverMessageCompose);
        }
    }
}
