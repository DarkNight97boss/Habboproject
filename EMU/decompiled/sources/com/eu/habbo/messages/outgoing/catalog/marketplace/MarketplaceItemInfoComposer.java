package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlace;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceItemInfoComposer.class */
public class MarketplaceItemInfoComposer extends MessageComposer {
    private final int itemId;

    public MarketplaceItemInfoComposer(int i) {
        this.itemId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceItemInfoComposer);
        MarketPlace.serializeItemInfo(this.itemId, this.response);
        return this.response;
    }
}
