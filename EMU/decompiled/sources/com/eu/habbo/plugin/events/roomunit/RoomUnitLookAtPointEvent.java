package com.eu.habbo.plugin.events.roomunit;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/roomunit/RoomUnitLookAtPointEvent.class */
public class RoomUnitLookAtPointEvent extends RoomUnitEvent {
    public final RoomTile location;

    public RoomUnitLookAtPointEvent(Room room, RoomUnit roomUnit, RoomTile roomTile) {
        super(room, roomUnit);
        this.location = roomTile;
    }
}
