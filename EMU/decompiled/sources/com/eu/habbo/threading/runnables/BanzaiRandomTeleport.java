package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/BanzaiRandomTeleport.class */
public class BanzaiRandomTeleport implements Runnable {
    private final HabboItem item;
    private final HabboItem toItem;
    private final RoomUnit habbo;
    private final Room room;

    public BanzaiRandomTeleport(HabboItem habboItem, HabboItem habboItem2, RoomUnit roomUnit, Room room) {
        this.item = habboItem;
        this.toItem = habboItem2;
        this.habbo = roomUnit;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        HabboItem topItemAt = this.room.getTopItemAt(this.habbo.getX(), this.habbo.getY());
        RoomTile currentLocation = this.habbo.getCurrentLocation();
        RoomTile tile = this.room.getLayout().getTile(this.toItem.getX(), this.toItem.getY());
        if (topItemAt != null) {
            try {
                topItemAt.onWalkOff(this.habbo, this.room, new Object[]{currentLocation, tile, this});
            } catch (Exception e) {
                LoggerFactory.getLogger(BanzaiRandomTeleport.class).error("BanzaiRandomTeleport exception", e);
            }
        }
        Emulator.getThreading().run(() -> {
            if (this.item.getExtradata().equals("1")) {
                this.item.setExtradata("0");
                this.room.updateItemState(this.item);
            }
        }, 500L);
        if (!this.toItem.getExtradata().equals("1")) {
            this.toItem.setExtradata("1");
            this.room.updateItemState(this.toItem);
        }
        Emulator.getThreading().run(() -> {
            this.habbo.setCanWalk(true);
            HabboItem topItemAt2 = this.room.getTopItemAt(this.habbo.getX(), this.habbo.getY());
            if (topItemAt2 != null) {
                try {
                    topItemAt2.onWalkOn(this.habbo, this.room, new Object[]{currentLocation, tile, this});
                } catch (Exception e2) {
                    LoggerFactory.getLogger(BanzaiRandomTeleport.class).error("BanzaiRandomTeleport exception", e2);
                }
            }
            if (this.toItem.getExtradata().equals("1")) {
                this.toItem.setExtradata("0");
                this.room.updateItemState(this.toItem);
            }
        }, 750L);
        Emulator.getThreading().run(() -> {
            this.habbo.setRotation(RoomUserRotation.fromValue(Emulator.getRandom().nextInt(8)));
            this.room.teleportRoomUnitToLocation(this.habbo, tile.x, tile.y, tile.getStackHeight());
        }, 250L);
    }
}
