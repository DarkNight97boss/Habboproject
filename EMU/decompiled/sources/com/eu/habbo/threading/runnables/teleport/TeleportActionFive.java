package com.eu.habbo.threading.runnables.teleport;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.threading.runnables.HabboItemNewState;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/teleport/TeleportActionFive.class */
class TeleportActionFive implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public TeleportActionFive(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        RoomUnit roomUnit = this.client.getHabbo().getRoomUnit();
        roomUnit.isLeavingTeleporter = false;
        roomUnit.isTeleporting = false;
        roomUnit.setCanWalk(true);
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != this.room || this.room.getLayout() == null || this.currentTeleport == null) {
            return;
        }
        RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(this.currentTeleport.getX(), this.currentTeleport.getY()), this.currentTeleport.getRotation());
        if (tileInFront != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(() -> {
                roomUnit.setCanLeaveRoomByDoor(true);
                Emulator.getThreading().run(() -> {
                    roomUnit.isLeavingTeleporter = false;
                }, 300L);
            });
            roomUnit.setCanLeaveRoomByDoor(false);
            roomUnit.setGoalLocation(tileInFront);
            roomUnit.statusUpdate(true);
            roomUnit.isLeavingTeleporter = true;
            Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, tileInFront, this.room, arrayList, arrayList));
        }
        this.currentTeleport.setExtradata("1");
        this.room.updateItem(this.currentTeleport);
        Emulator.getThreading().run(new HabboItemNewState(this.currentTeleport, this.room, "0"), 1000L);
        HabboItem topItemAt = this.room.getTopItemAt(roomUnit.getX(), roomUnit.getY());
        if (topItemAt == null || !(topItemAt instanceof InteractionTeleportTile) || topItemAt == this.currentTeleport) {
            return;
        }
        try {
            topItemAt.onWalkOn(roomUnit, this.room, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
