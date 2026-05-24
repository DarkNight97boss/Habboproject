package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitWalkToLocation.class */
public class RoomUnitWalkToLocation implements Runnable {
    private RoomUnit walker;
    private RoomTile goalTile;
    private Room room;
    private List<Runnable> targetReached;
    private List<Runnable> failedReached;

    public RoomUnitWalkToLocation(RoomUnit roomUnit, RoomTile roomTile, Room room, Runnable runnable, Runnable runnable2) {
        this.walker = roomUnit;
        this.goalTile = roomTile;
        this.room = room;
        this.targetReached = new ArrayList();
        if (runnable != null) {
            this.targetReached.add(runnable);
        }
        this.failedReached = new ArrayList();
        if (runnable2 != null) {
            this.targetReached.add(runnable2);
        }
    }

    public RoomUnitWalkToLocation(RoomUnit roomUnit, RoomTile roomTile, Room room, List<Runnable> list, List<Runnable> list2) {
        this.walker = roomUnit;
        this.goalTile = roomTile;
        this.room = room;
        this.targetReached = list;
        this.failedReached = list2;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.goalTile == null || this.walker == null || this.room == null || this.walker.getRoom() == null || this.walker.getRoom().getId() != this.room.getId()) {
            onFail();
            return;
        }
        if (this.walker.getCurrentLocation().equals(this.goalTile)) {
            onSuccess();
        } else if (!this.walker.getGoal().equals(this.goalTile) || (this.walker.getPath().size() == 0 && !this.walker.hasStatus(RoomUnitStatus.MOVE))) {
            onFail();
        } else {
            Emulator.getThreading().run(this, 250L);
        }
    }

    private void onSuccess() {
        Iterator<Runnable> it = this.targetReached.iterator();
        while (it.hasNext()) {
            Emulator.getThreading().run(it.next());
        }
    }

    private void onFail() {
        Iterator<Runnable> it = this.failedReached.iterator();
        while (it.hasNext()) {
            Emulator.getThreading().run(it.next());
        }
    }
}
