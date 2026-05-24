package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/LoadCustomHeightMap.class */
public class LoadCustomHeightMap implements Runnable {
    private final Room room;

    public LoadCustomHeightMap(Room room) {
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.room.setLayout(Emulator.getGameEnvironment().getRoomManager().loadCustomLayout(this.room));
    }
}
