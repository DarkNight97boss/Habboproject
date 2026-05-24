package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitWalkToRoomUnit.class */
public class RoomUnitWalkToRoomUnit implements Runnable {
    private final int minDistance;
    private RoomUnit walker;
    private RoomUnit target;
    private Room room;
    private List<Runnable> targetReached;
    private List<Runnable> failedReached;
    private RoomTile goalTile;

    public RoomUnitWalkToRoomUnit(RoomUnit roomUnit, RoomUnit roomUnit2, Room room, List<Runnable> list, List<Runnable> list2) {
        this.goalTile = null;
        this.walker = roomUnit;
        this.target = roomUnit2;
        this.room = room;
        this.targetReached = list;
        this.failedReached = list2;
        this.minDistance = 1;
    }

    public RoomUnitWalkToRoomUnit(RoomUnit roomUnit, RoomUnit roomUnit2, Room room, List<Runnable> list, List<Runnable> list2, int i) {
        this.goalTile = null;
        this.walker = roomUnit;
        this.target = roomUnit2;
        this.room = room;
        this.targetReached = list;
        this.failedReached = list2;
        this.minDistance = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.goalTile == null) {
            findNewLocation();
            Emulator.getThreading().run(this, 500L);
        } else if (this.walker.getGoal().equals(this.goalTile)) {
            if (this.walker.getCurrentLocation().distance(this.goalTile) > this.minDistance) {
                Emulator.getThreading().run(this, 500L);
                return;
            }
            Iterator<Runnable> it = this.targetReached.iterator();
            while (it.hasNext()) {
                Emulator.getThreading().run(it.next());
                WiredHandler.handle(WiredTriggerType.BOT_REACHED_AVTR, this.target, this.room, new Object[]{this.walker});
            }
        }
    }

    private void findNewLocation() {
        this.goalTile = this.walker.getClosestAdjacentTile(this.target.getCurrentLocation().x, this.target.getCurrentLocation().y, true);
        if (this.goalTile == null) {
            if (this.failedReached != null) {
                Iterator<Runnable> it = this.failedReached.iterator();
                while (it.hasNext()) {
                    Emulator.getThreading().run(it.next());
                }
                return;
            }
            return;
        }
        this.walker.setGoalLocation(this.goalTile);
        if (!this.walker.getPath().isEmpty() || this.failedReached == null) {
            return;
        }
        Iterator<Runnable> it2 = this.failedReached.iterator();
        while (it2.hasNext()) {
            Emulator.getThreading().run(it2.next());
        }
    }
}
