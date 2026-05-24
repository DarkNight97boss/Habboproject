package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurniturePlacedEvent.class */
public class FurniturePlacedEvent extends FurnitureUserEvent {
    public final RoomTile location;
    private boolean pluginHelper;

    public FurniturePlacedEvent(HabboItem habboItem, Habbo habbo, RoomTile roomTile) {
        super(habboItem, habbo);
        this.location = roomTile;
        this.pluginHelper = false;
    }

    public void setPluginHelper(boolean z) {
        this.pluginHelper = z;
    }

    public boolean hasPluginHelper() {
        return this.pluginHelper;
    }
}
