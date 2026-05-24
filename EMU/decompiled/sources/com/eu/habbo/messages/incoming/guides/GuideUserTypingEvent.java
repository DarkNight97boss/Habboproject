package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionPartnerIsTypingComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideUserTypingEvent.class */
public class GuideUserTypingEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        boolean z = this.packet.readBoolean();
        GuideTour guideTourByHabbo = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHabbo(this.client.getHabbo());
        if (guideTourByHabbo != null) {
            if (guideTourByHabbo.getHelper() == this.client.getHabbo()) {
                guideTourByHabbo.getNoob().getClient().sendResponse(new GuideSessionPartnerIsTypingComposer(z));
            } else {
                guideTourByHabbo.getHelper().getClient().sendResponse(new GuideSessionPartnerIsTypingComposer(z));
            }
        }
    }
}
