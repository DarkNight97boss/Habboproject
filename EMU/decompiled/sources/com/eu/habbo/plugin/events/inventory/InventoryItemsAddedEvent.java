package com.eu.habbo.plugin.events.inventory;

import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/inventory/InventoryItemsAddedEvent.class */
public class InventoryItemsAddedEvent extends InventoryEvent {
    public final THashSet<HabboItem> items;

    public InventoryItemsAddedEvent(HabboInventory habboInventory, THashSet<HabboItem> tHashSet) {
        super(habboInventory);
        this.items = tHashSet;
    }
}
