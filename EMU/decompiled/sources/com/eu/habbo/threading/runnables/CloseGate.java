package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/CloseGate.class */
public class CloseGate implements Runnable {
    private final HabboItem gate;
    private final Room room;

    public CloseGate(HabboItem habboItem, Room room) {
        this.gate = habboItem;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.gate.getRoomId() == this.room.getId() && this.room.isLoaded() && this.room.getHabbosAt(this.gate.getX(), this.gate.getY()).isEmpty()) {
            this.gate.setExtradata("0");
            this.room.updateItem(this.gate);
            this.gate.needsUpdate(true);
        }
    }
}
