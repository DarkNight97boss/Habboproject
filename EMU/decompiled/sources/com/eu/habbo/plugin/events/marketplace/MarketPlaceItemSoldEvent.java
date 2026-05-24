package com.eu.habbo.plugin.events.marketplace;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/marketplace/MarketPlaceItemSoldEvent.class */
public class MarketPlaceItemSoldEvent extends MarketPlaceEvent {
    public final Habbo seller;
    public final Habbo purchaser;
    public final HabboItem item;
    public int price;

    public MarketPlaceItemSoldEvent(Habbo habbo, Habbo habbo2, HabboItem habboItem, int i) {
        this.seller = habbo;
        this.purchaser = habbo2;
        this.item = habboItem;
        this.price = i;
    }
}
