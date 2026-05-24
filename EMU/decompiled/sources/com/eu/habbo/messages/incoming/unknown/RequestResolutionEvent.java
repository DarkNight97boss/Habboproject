package com.eu.habbo.messages.incoming.unknown;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.events.resolution.NewYearResolutionComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/unknown/RequestResolutionEvent.class */
public class RequestResolutionEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.packet.readInt().intValue();
        if (this.packet.readInt().intValue() == 0) {
            this.client.sendResponse(new NewYearResolutionComposer());
        }
    }
}
