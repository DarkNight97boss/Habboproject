package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.ItemIntStateComposer;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionOneWayGate.class */
public class InteractionOneWayGate extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionOneWayGate.class);
    private boolean walkable;

    public InteractionOneWayGate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.walkable = false;
        setExtradata("0");
    }

    public InteractionOneWayGate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.walkable = false;
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return getBaseItem().allowWalk();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return this.walkable;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        if (getExtradata().length() == 0) {
            setExtradata("0");
            needsUpdate(true);
        }
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        RoomTile tileInFront;
        RoomTile tile;
        RoomUnit roomUnit;
        super.onClick(gameClient, room, objArr);
        if (gameClient == null || (tileInFront = room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), getRotation())) == null || (tile = room.getLayout().getTile(getX(), getY())) == null || (roomUnit = gameClient.getHabbo().getRoomUnit()) == null || tileInFront.x != roomUnit.getX() || tileInFront.y != roomUnit.getY() || tile.hasUnits()) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList.add(() -> {
            roomUnit.setCanLeaveRoomByDoor(false);
            this.walkable = getBaseItem().allowWalk();
            RoomTile tileInFront2 = room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), getRotation() + 4);
            roomUnit.setGoalLocation(tileInFront2);
            Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, tileInFront2, room, (List<Runnable>) arrayList2, (List<Runnable>) arrayList2));
            Emulator.getThreading().run(() -> {
                WiredHandler.handle(WiredTriggerType.WALKS_ON_FURNI, roomUnit, room, new Object[]{this});
            }, 500L);
        });
        arrayList2.add(() -> {
            roomUnit.setCanLeaveRoomByDoor(true);
            this.walkable = getBaseItem().allowWalk();
            room.updateTile(tile);
            room.sendComposer(new ItemIntStateComposer(getId(), 0).compose());
            roomUnit.removeOverrideTile(tile);
        });
        this.walkable = true;
        room.updateTile(tile);
        roomUnit.addOverrideTile(tile);
        roomUnit.setGoalLocation(tile);
        Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, tile, room, arrayList, arrayList2));
        room.sendComposer(new ItemIntStateComposer(getId(), 1).compose());
    }

    private void refresh(Room room) {
        setExtradata("0");
        room.sendComposer(new ItemIntStateComposer(getId(), 0).compose());
        room.updateTile(room.getLayout().getTile(getX(), getY()));
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
        refresh(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        refresh(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        refresh(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        refresh(room);
    }
}
