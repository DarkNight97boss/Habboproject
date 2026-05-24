package com.eu.habbo.plugin.events.roomunit;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/roomunit/RoomUnitSetGoalEvent.class */
public class RoomUnitSetGoalEvent extends RoomUnitEvent {
    public final RoomTile goal;

    public RoomUnitSetGoalEvent(Room room, RoomUnit roomUnit, RoomTile roomTile) {
        super(room, roomUnit);
        this.goal = roomTile;
    }

    public void setGoal(RoomTile roomTile) {
        this.roomUnit.setGoalLocation(roomTile);
    }
}
