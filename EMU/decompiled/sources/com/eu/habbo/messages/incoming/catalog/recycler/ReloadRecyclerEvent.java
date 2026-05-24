package com.eu.habbo.messages.incoming.catalog.recycler;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.ReloadRecyclerComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/recycler/ReloadRecyclerEvent.class */
public class ReloadRecyclerEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new ReloadRecyclerComposer());
    }
}
