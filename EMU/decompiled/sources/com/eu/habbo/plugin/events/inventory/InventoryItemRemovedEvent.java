package com.eu.habbo.plugin.events.inventory;

import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/inventory/InventoryItemRemovedEvent.class */
public class InventoryItemRemovedEvent extends InventoryItemEvent {
    public InventoryItemRemovedEvent(HabboInventory habboInventory, HabboItem habboItem) {
        super(habboInventory, habboItem);
    }
}
