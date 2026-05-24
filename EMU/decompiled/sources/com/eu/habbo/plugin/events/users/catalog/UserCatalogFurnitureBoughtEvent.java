package com.eu.habbo.plugin.events.users.catalog;

import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/catalog/UserCatalogFurnitureBoughtEvent.class */
public class UserCatalogFurnitureBoughtEvent extends UserCatalogEvent {
    public final THashSet<HabboItem> furniture;

    public UserCatalogFurnitureBoughtEvent(Habbo habbo, CatalogItem catalogItem, THashSet<HabboItem> tHashSet) {
        super(habbo, catalogItem);
        this.furniture = tHashSet;
    }
}
