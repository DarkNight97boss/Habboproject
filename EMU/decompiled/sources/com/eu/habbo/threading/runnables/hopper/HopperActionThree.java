package com.eu.habbo.threading.runnables.hopper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionCostumeHopper;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.threading.runnables.HabboItemNewState;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/hopper/HopperActionThree.class */
class HopperActionThree implements Runnable {
    private final HabboItem teleportOne;
    private final Room room;
    private final GameClient client;
    private final int targetRoomId;
    private final int targetItemId;

    public HopperActionThree(HabboItem habboItem, Room room, GameClient gameClient, int i, int i2) {
        this.teleportOne = habboItem;
        this.room = room;
        this.client = gameClient;
        this.targetRoomId = i;
        this.targetItemId = i2;
    }

    @Override // java.lang.Runnable
    public void run() {
        Room roomLoadRoom = this.room;
        if (this.teleportOne.getRoomId() != this.targetRoomId) {
            Emulator.getGameEnvironment().getRoomManager().leaveRoom(this.client.getHabbo(), this.room, false);
            roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.targetRoomId);
            Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), roomLoadRoom.getId(), Emulator.PREVIEW, false);
        }
        HabboItem habboItem = roomLoadRoom.getHabboItem(this.targetItemId);
        if (habboItem == null) {
            this.client.getHabbo().getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
            this.client.getHabbo().getRoomUnit().setCanWalk(true);
            return;
        }
        habboItem.setExtradata("2");
        roomLoadRoom.updateItem(habboItem);
        this.client.getHabbo().getRoomUnit().setLocation(this.room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
        this.client.getHabbo().getRoomUnit().setPreviousLocationZ(habboItem.getZ());
        this.client.getHabbo().getRoomUnit().setZ(habboItem.getZ());
        this.client.getHabbo().getRoomUnit().setRotation(RoomUserRotation.values()[habboItem.getRotation() % 8]);
        this.client.getHabbo().getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        roomLoadRoom.sendComposer(new RoomUserStatusComposer(this.client.getHabbo().getRoomUnit()).compose());
        Emulator.getThreading().run(new HabboItemNewState(this.teleportOne, this.room, "0"), 500L);
        Emulator.getThreading().run(new HopperActionFour(habboItem, roomLoadRoom, this.client), 500L);
        if (habboItem instanceof InteractionCostumeHopper) {
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("CostumeHopper"));
        }
    }
}
