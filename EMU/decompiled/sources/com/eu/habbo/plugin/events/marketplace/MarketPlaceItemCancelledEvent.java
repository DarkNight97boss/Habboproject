package com.eu.habbo.plugin.events.marketplace;

import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlaceOffer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/marketplace/MarketPlaceItemCancelledEvent.class */
public class MarketPlaceItemCancelledEvent extends MarketPlaceEvent {
    public final MarketPlaceOffer offer;

    public MarketPlaceItemCancelledEvent(MarketPlaceOffer marketPlaceOffer) {
        this.offer = marketPlaceOffer;
    }
}
