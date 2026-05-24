package com.eu.habbo.plugin.events.inventory;

import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/inventory/InventoryItemEvent.class */
public class InventoryItemEvent extends InventoryEvent {
    public HabboItem item;

    public InventoryItemEvent(HabboInventory habboInventory, HabboItem habboItem) {
        super(habboInventory);
        this.item = habboItem;
    }
}
