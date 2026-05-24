package com.eu.habbo.threading.runnables.teleport;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/teleport/TeleportActionOne.class */
public class TeleportActionOne implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public TeleportActionOne(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != this.room) {
            return;
        }
        int i = 500;
        if (this.currentTeleport instanceof InteractionTeleportTile) {
            i = 0;
        }
        if (this.client.getHabbo().getRoomUnit().getCurrentLocation() != this.room.getLayout().getTile(this.currentTeleport.getX(), this.currentTeleport.getY())) {
            this.client.getHabbo().getRoomUnit().setLocation(this.room.getLayout().getTile(this.currentTeleport.getX(), this.currentTeleport.getY()));
            this.client.getHabbo().getRoomUnit().setRotation(RoomUserRotation.values()[(this.currentTeleport.getRotation() + 4) % 8]);
            this.client.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.MOVE, ((int) this.currentTeleport.getX()) + "," + ((int) this.currentTeleport.getY()) + "," + this.currentTeleport.getZ());
            this.room.scheduledComposers.add(new RoomUserStatusComposer(this.client.getHabbo().getRoomUnit()).compose());
            this.client.getHabbo().getRoomUnit().setLocation(this.room.getLayout().getTile(this.currentTeleport.getX(), this.currentTeleport.getY()));
        }
        Emulator.getThreading().run(new TeleportActionTwo(this.currentTeleport, this.room, this.client), i);
    }
}
