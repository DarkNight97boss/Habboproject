package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import com.eu.habbo.threading.runnables.teleport.TeleportActionOne;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionTeleport.class */
public class InteractionTeleport extends HabboItem {
    private int targetId;
    private int targetRoomId;
    private int roomUnitID;
    private boolean walkable;

    public InteractionTeleport(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.roomUnitID = -1;
        this.walkable = item.allowWalk();
        setExtradata("0");
    }

    public InteractionTeleport(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.roomUnitID = -1;
        this.walkable = item.allowWalk();
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return getBaseItem().allowWalk() || roomUnit.getId() == this.roomUnitID;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return this.walkable;
    }

    private void tryTeleport(GameClient gameClient, Room room) {
        RoomUnit roomUnit;
        RoomTile tile;
        Habbo habbo = gameClient.getHabbo();
        if (habbo == null || (roomUnit = habbo.getRoomUnit()) == null || (tile = room.getLayout().getTile(getX(), getY())) == null) {
            return;
        }
        RoomTile tileInFront = room.getLayout().getTileInFront(tile, getRotation());
        if (canUseTeleport(gameClient, room)) {
            if (this.roomUnitID == roomUnit.getId() && roomUnit.getCurrentLocation().equals(tile)) {
                startTeleport(room, habbo);
                this.walkable = true;
                try {
                    super.onClick(gameClient, room, new Object[]{"TOGGLE_OVERRIDE"});
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (!roomUnit.getCurrentLocation().equals(tile) && !roomUnit.getCurrentLocation().equals(tileInFront)) {
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                arrayList.add(() -> {
                    tryTeleport(gameClient, room);
                });
                roomUnit.setGoalLocation(tileInFront);
                Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, tileInFront, room, arrayList, arrayList2));
                return;
            }
            this.roomUnitID = roomUnit.getId();
            setExtradata("1");
            room.updateItemState(this);
            roomUnit.setGoalLocation(tileInFront);
            ArrayList arrayList3 = new ArrayList();
            ArrayList arrayList4 = new ArrayList();
            arrayList3.add(() -> {
                room.updateTile(tile);
                tryTeleport(gameClient, room);
                roomUnit.removeOverrideTile(tile);
                roomUnit.setCanLeaveRoomByDoor(true);
                this.walkable = getBaseItem().allowWalk();
            });
            arrayList4.add(() -> {
                this.walkable = getBaseItem().allowWalk();
                room.updateTile(tile);
                setExtradata("0");
                room.updateItemState(this);
                this.roomUnitID = -1;
                roomUnit.removeOverrideTile(tile);
                roomUnit.setCanLeaveRoomByDoor(true);
            });
            this.walkable = true;
            room.updateTile(tile);
            roomUnit.addOverrideTile(tile);
            roomUnit.setGoalLocation(tile);
            roomUnit.setCanLeaveRoomByDoor(false);
            Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, tile, room, arrayList3, arrayList4));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (room == null || gameClient == null || objArr == null || objArr.length > 1) {
            return;
        }
        tryTeleport(gameClient, room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        if (!getExtradata().equals("0")) {
            setExtradata("0");
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
            if (room != null) {
                room.updateItem(this);
            }
        }
        super.run();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        this.targetId = 0;
        this.targetRoomId = 0;
        this.roomUnitID = -1;
        setExtradata("0");
    }

    public int getTargetId() {
        return this.targetId;
    }

    public void setTargetId(int i) {
        this.targetId = i;
    }

    public int getTargetRoomId() {
        return this.targetRoomId;
    }

    public void setTargetRoomId(int i) {
        this.targetRoomId = i;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    public boolean canUseTeleport(GameClient gameClient, Room room) {
        Habbo habbo = gameClient.getHabbo();
        return (habbo == null || habbo.getRoomUnit() == null || habbo.getHabboInfo().getRiding() != null) ? false : true;
    }

    public void startTeleport(Room room, Habbo habbo) {
        startTeleport(room, habbo, 500);
    }

    public void startTeleport(Room room, Habbo habbo, int i) {
        if (habbo.getRoomUnit().isTeleporting) {
            this.walkable = getBaseItem().allowWalk();
            return;
        }
        this.roomUnitID = -1;
        habbo.getRoomUnit().isTeleporting = true;
        Emulator.getThreading().run(new TeleportActionOne(this, room, habbo.getClient()), i);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean invalidatesToRoomKick() {
        return true;
    }
}
