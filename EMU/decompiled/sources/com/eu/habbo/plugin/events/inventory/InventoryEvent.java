package com.eu.habbo.plugin.events.inventory;

import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/inventory/InventoryEvent.class */
public abstract class InventoryEvent extends Event {
    public final HabboInventory inventory;

    protected InventoryEvent(HabboInventory habboInventory) {
        this.inventory = habboInventory;
    }
}
