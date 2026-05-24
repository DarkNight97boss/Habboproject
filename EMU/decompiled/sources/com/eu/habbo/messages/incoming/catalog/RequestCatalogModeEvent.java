package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.CatalogModeComposer;
import com.eu.habbo.messages.outgoing.catalog.CatalogPagesListComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/RequestCatalogModeEvent.class */
public class RequestCatalogModeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (string.equalsIgnoreCase("normal")) {
            this.client.sendResponse(new CatalogModeComposer(0));
            this.client.sendResponse(new CatalogPagesListComposer(this.client.getHabbo(), string));
        } else {
            this.client.sendResponse(new CatalogModeComposer(1));
            this.client.sendResponse(new CatalogPagesListComposer(this.client.getHabbo(), string));
        }
    }
}
