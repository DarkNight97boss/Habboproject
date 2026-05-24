package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.wired.WiredHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/WiredResetTimers.class */
public class WiredResetTimers implements Runnable {
    private Room room;

    public WiredResetTimers(Room room) {
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (Emulator.isShuttingDown || !Emulator.isReady) {
            return;
        }
        WiredHandler.resetTimers(this.room);
    }
}
