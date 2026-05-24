package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceConfigComposer.class */
public class MarketplaceConfigComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceConfigComposer);
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 5);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1000000);
        this.response.appendInt((Integer) 48);
        this.response.appendInt((Integer) 7);
        return this.response;
    }
}
