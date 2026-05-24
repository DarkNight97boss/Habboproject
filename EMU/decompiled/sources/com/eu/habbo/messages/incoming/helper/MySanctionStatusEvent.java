package com.eu.habbo.messages.incoming.helper;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolSanctionInfoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/helper/MySanctionStatusEvent.class */
public class MySanctionStatusEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new ModToolSanctionInfoComposer(this.client.getHabbo()));
    }
}
