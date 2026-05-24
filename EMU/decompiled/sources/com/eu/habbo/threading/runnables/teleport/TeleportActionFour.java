package com.eu.habbo.threading.runnables.teleport;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/teleport/TeleportActionFour.class */
class TeleportActionFour implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public TeleportActionFour(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != this.room) {
            this.client.getHabbo().getRoomUnit().setCanWalk(true);
            this.currentTeleport.setExtradata("0");
            this.room.updateItem(this.currentTeleport);
        } else {
            if (this.client.getHabbo().getRoomUnit() != null) {
                this.client.getHabbo().getRoomUnit().isLeavingTeleporter = true;
            }
            Emulator.getThreading().run(new TeleportActionFive(this.currentTeleport, this.room, this.client), 500L);
        }
    }
}
