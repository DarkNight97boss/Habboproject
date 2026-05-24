package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureMovedEvent.class */
public class FurnitureMovedEvent extends FurnitureUserEvent {
    public final RoomTile oldPosition;
    public final RoomTile newPosition;
    private boolean pluginHelper;

    public FurnitureMovedEvent(HabboItem habboItem, Habbo habbo, RoomTile roomTile, RoomTile roomTile2) {
        super(habboItem, habbo);
        this.oldPosition = roomTile;
        this.newPosition = roomTile2;
        this.pluginHelper = false;
    }

    public void setPluginHelper(boolean z) {
        this.pluginHelper = z;
    }

    public boolean hasPluginHelper() {
        return this.pluginHelper;
    }
}
