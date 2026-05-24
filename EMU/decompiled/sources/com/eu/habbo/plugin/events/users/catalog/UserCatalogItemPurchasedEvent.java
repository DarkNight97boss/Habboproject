package com.eu.habbo.plugin.events.users.catalog;

import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/catalog/UserCatalogItemPurchasedEvent.class */
public class UserCatalogItemPurchasedEvent extends UserCatalogEvent {
    public final THashSet<HabboItem> itemsList;
    public int totalCredits;
    public int totalPoints;
    public List<String> badges;

    public UserCatalogItemPurchasedEvent(Habbo habbo, CatalogItem catalogItem, THashSet<HabboItem> tHashSet, int i, int i2, List<String> list) {
        super(habbo, catalogItem);
        this.itemsList = tHashSet;
        this.totalCredits = i;
        this.totalPoints = i2;
        this.badges = list;
    }
}
