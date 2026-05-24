package com.eu.habbo.threading.runnables.hopper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/hopper/HopperActionFour.class */
class HopperActionFour implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public HopperActionFour(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.currentTeleport.setExtradata("1");
        this.room.updateItem(this.currentTeleport);
        Emulator.getThreading().run(new HopperActionFive(this.currentTeleport, this.room, this.client), 500L);
    }
}
