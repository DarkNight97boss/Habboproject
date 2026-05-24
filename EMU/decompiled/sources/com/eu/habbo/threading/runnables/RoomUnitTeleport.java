package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitTeleport.class */
public class RoomUnitTeleport implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUnitTeleport.class);
    private RoomUnit roomUnit;
    private Room room;
    private int x;
    private int y;
    private double z;
    private int newEffect;

    public RoomUnitTeleport(RoomUnit roomUnit, Room room, int i, int i2, double d, int i3) {
        this.roomUnit = roomUnit;
        this.room = room;
        this.x = i;
        this.y = i2;
        this.z = d;
        this.newEffect = i3;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.roomUnit == null || this.roomUnit.getRoom() == null || this.room.getLayout() == null || this.roomUnit.isLeavingTeleporter) {
            return;
        }
        RoomTile currentLocation = this.roomUnit.getCurrentLocation();
        RoomTile tile = this.room.getLayout().getTile((short) this.x, (short) this.y);
        HabboItem topItemAt = this.room.getTopItemAt(this.roomUnit.getCurrentLocation().x, this.roomUnit.getCurrentLocation().y);
        if (topItemAt != null) {
            try {
                topItemAt.onWalkOff(this.roomUnit, this.room, new Object[]{this});
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
        this.roomUnit.setPath(new LinkedList());
        this.roomUnit.setCurrentLocation(tile);
        this.roomUnit.setPreviousLocation(tile);
        this.roomUnit.setZ(this.z);
        this.roomUnit.setPreviousLocationZ(this.z);
        this.roomUnit.removeStatus(RoomUnitStatus.MOVE);
        this.roomUnit.setLocation(tile);
        this.roomUnit.statusUpdate(true);
        this.roomUnit.isWiredTeleporting = false;
        this.room.updateHabbosAt(tile.x, tile.y);
        this.room.updateBotsAt(tile.x, tile.y);
        HabboItem topItemAt2 = this.room.getTopItemAt(this.x, this.y);
        if (topItemAt2 == null || !this.roomUnit.getCurrentLocation().equals(this.room.getLayout().getTile((short) this.x, (short) this.y))) {
            return;
        }
        try {
            topItemAt2.onWalkOn(this.roomUnit, this.room, new Object[]{currentLocation, tile, this});
        } catch (Exception e2) {
        }
    }
}
