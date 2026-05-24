package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitKick.class */
public class RoomUnitKick implements Runnable {
    private final Habbo habbo;
    private final Room room;
    private final boolean removeEffect;

    public RoomUnitKick(Habbo habbo, Room room, boolean z) {
        this.habbo = habbo;
        this.room = room;
        this.removeEffect = z;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.removeEffect) {
            this.habbo.getRoomUnit().setEffectId(0, 0);
        }
        Emulator.getGameEnvironment().getRoomManager().leaveRoom(this.habbo, this.room);
    }
}
