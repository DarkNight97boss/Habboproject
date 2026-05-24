package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/RequestGuideAssistanceEvent.class */
public class RequestGuideAssistanceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.packet.readInt().intValue();
        String string = this.packet.readString();
        if (Emulator.getGameEnvironment().getGuideManager().getGuideTourByHabbo(this.client.getHabbo()) != null) {
            this.client.sendResponse(new GuideSessionErrorComposer(0));
            return;
        }
        GuideTour guideTour = new GuideTour(this.client.getHabbo(), string);
        guideTour.setStartTime(Emulator.getIntUnixTimestamp());
        Emulator.getGameEnvironment().getGuideManager().findHelper(guideTour);
    }
}
