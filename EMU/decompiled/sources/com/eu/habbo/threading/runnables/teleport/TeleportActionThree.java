package com.eu.habbo.threading.runnables.teleport;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleport;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/teleport/TeleportActionThree.class */
class TeleportActionThree implements Runnable {
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public TeleportActionThree(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != this.room) {
            return;
        }
        Room roomLoadRoom = this.room;
        if (this.currentTeleport.getRoomId() != ((InteractionTeleport) this.currentTeleport).getTargetRoomId()) {
            roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(((InteractionTeleport) this.currentTeleport).getTargetRoomId());
        }
        if (roomLoadRoom == null) {
            Emulator.getThreading().run(new TeleportActionFive(this.currentTeleport, this.room, this.client), 0L);
            return;
        }
        if (roomLoadRoom.isPreLoaded()) {
            roomLoadRoom.loadData();
        }
        HabboItem habboItem = roomLoadRoom.getHabboItem(((InteractionTeleport) this.currentTeleport).getTargetId());
        if (habboItem == null) {
            Emulator.getThreading().run(new TeleportActionFive(this.currentTeleport, this.room, this.client), 0L);
            return;
        }
        RoomTile tile = roomLoadRoom.getLayout().getTile(habboItem.getX(), habboItem.getY());
        if (tile == null) {
            Emulator.getThreading().run(new TeleportActionFive(this.currentTeleport, this.room, this.client), 0L);
            return;
        }
        this.client.getHabbo().getRoomUnit().setLocation(tile);
        this.client.getHabbo().getRoomUnit().getPath().clear();
        this.client.getHabbo().getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        this.client.getHabbo().getRoomUnit().setZ(tile.getStackHeight());
        this.client.getHabbo().getRoomUnit().setPreviousLocationZ(tile.getStackHeight());
        if (roomLoadRoom != this.room) {
            this.room.removeHabbo(this.client.getHabbo(), false);
            Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), roomLoadRoom.getId(), Emulator.PREVIEW, Emulator.getConfig().getBoolean("hotel.teleport.locked.allowed"), tile);
        }
        this.client.getHabbo().getRoomUnit().setRotation(RoomUserRotation.values()[habboItem.getRotation() % 8]);
        this.client.getHabbo().getRoomUnit().statusUpdate(true);
        habboItem.setExtradata("2");
        roomLoadRoom.updateItem(habboItem);
        this.client.getHabbo().getHabboInfo().setCurrentRoom(roomLoadRoom);
        Emulator.getThreading().run(new TeleportActionFour(habboItem, roomLoadRoom, this.client), this.currentTeleport instanceof InteractionTeleportTile ? 0L : 500L);
    }
}
