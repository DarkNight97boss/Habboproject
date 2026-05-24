package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/WiredCollissionRunnable.class */
public class WiredCollissionRunnable implements Runnable {
    public final RoomUnit roomUnit;
    public final Room room;
    public final Object[] objects;

    public WiredCollissionRunnable(RoomUnit roomUnit, Room room, Object[] objArr) {
        this.roomUnit = roomUnit;
        this.room = room;
        this.objects = objArr;
    }

    @Override // java.lang.Runnable
    public void run() {
        WiredHandler.handle(WiredTriggerType.COLLISION, this.roomUnit, this.room, this.objects);
    }
}
