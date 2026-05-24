package com.eu.habbo.threading.runnables.hopper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.threading.runnables.HabboItemNewState;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/hopper/HopperActionFive.class */
class HopperActionFive implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public HopperActionFive(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.client.getHabbo().getRoomUnit().isTeleporting = false;
        RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(this.currentTeleport.getX(), this.currentTeleport.getY()), this.currentTeleport.getRotation());
        if (tileInFront != null) {
            this.client.getHabbo().getRoomUnit().setGoalLocation(tileInFront);
        }
        Emulator.getThreading().run(new HabboItemNewState(this.currentTeleport, this.room, "0"), 1000L);
    }
}
