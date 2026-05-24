package com.eu.habbo.plugin.events.marketplace;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/marketplace/MarketPlaceItemOfferedEvent.class */
public class MarketPlaceItemOfferedEvent extends MarketPlaceEvent {
    public final Habbo habbo;
    public final HabboItem item;
    public int price;

    public MarketPlaceItemOfferedEvent(Habbo habbo, HabboItem habboItem, int i) {
        this.habbo = habbo;
        this.item = habboItem;
        this.price = i;
    }
}
