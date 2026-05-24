package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureRolledEvent.class */
public class FurnitureRolledEvent extends FurnitureEvent {
    public final HabboItem roller;
    public final RoomTile newLocation;

    public FurnitureRolledEvent(HabboItem habboItem, HabboItem habboItem2, RoomTile roomTile) {
        super(habboItem);
        this.roller = habboItem2;
        this.newLocation = roomTile;
    }
}
