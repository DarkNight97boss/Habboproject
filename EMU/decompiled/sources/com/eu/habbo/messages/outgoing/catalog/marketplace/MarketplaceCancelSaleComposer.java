package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlaceOffer;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceCancelSaleComposer.class */
public class MarketplaceCancelSaleComposer extends MessageComposer {
    private final MarketPlaceOffer offer;
    private final boolean success;

    public MarketplaceCancelSaleComposer(MarketPlaceOffer marketPlaceOffer, Boolean bool) {
        this.offer = marketPlaceOffer;
        this.success = bool.booleanValue();
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceCancelSaleComposer);
        this.response.appendInt(Integer.valueOf(this.offer.getOfferId()));
        this.response.appendBoolean(Boolean.valueOf(this.success));
        return this.response;
    }
}
