package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.RoomUnitGiveHanditem;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import com.eu.habbo.util.pathfinding.Rotation;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionVendingMachine.class */
public class InteractionVendingMachine extends HabboItem {
    public InteractionVendingMachine(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionVendingMachine(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    public THashSet<RoomTile> getActivatorTiles(Room room) {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        RoomTile squareInFront = getSquareInFront(room.getLayout(), this);
        if (squareInFront != null) {
            tHashSet.add(squareInFront);
        }
        tHashSet.add(room.getLayout().getTile(getX(), getY()));
        return tHashSet;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    private void tryInteract(GameClient gameClient, Room room, RoomUnit roomUnit) {
        THashSet<RoomTile> activatorTiles = getActivatorTiles(room);
        if (activatorTiles.size() == 0) {
            return;
        }
        boolean z = false;
        TObjectHashIterator it = activatorTiles.iterator();
        while (it.hasNext()) {
            if (roomUnit.getCurrentLocation().is(roomUnit.getX(), roomUnit.getY())) {
                z = true;
            }
        }
        if (z) {
            useVendingMachine(gameClient, room, roomUnit);
        }
    }

    private void useVendingMachine(GameClient gameClient, Room room, RoomUnit roomUnit) {
        setExtradata("1");
        room.updateItem(this);
        try {
            super.onClick(gameClient, room, new Object[]{"TOGGLE_OVERRIDE"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!roomUnit.isWalking() && !roomUnit.hasStatus(RoomUnitStatus.SIT) && !roomUnit.hasStatus(RoomUnitStatus.LAY)) {
            rotateToMachine(room, roomUnit);
        }
        Emulator.getThreading().run(() -> {
            giveVendingMachineItem(room, roomUnit);
            if (getBaseItem().getEffectM() > 0 && gameClient.getHabbo().getHabboInfo().getGender() == HabboGender.M) {
                room.giveEffect(gameClient.getHabbo(), getBaseItem().getEffectM(), -1);
            }
            if (getBaseItem().getEffectF() > 0 && gameClient.getHabbo().getHabboInfo().getGender() == HabboGender.F) {
                room.giveEffect(gameClient.getHabbo(), getBaseItem().getEffectF(), -1);
            }
            Emulator.getThreading().run(this, 500L);
        }, 1500L);
    }

    public void giveVendingMachineItem(Room room, RoomUnit roomUnit) {
        Emulator.getThreading().run(new RoomUnitGiveHanditem(roomUnit, room, getBaseItem().getRandomVendingItem()));
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient == null) {
            return;
        }
        RoomUnit roomUnit = gameClient.getHabbo().getRoomUnit();
        THashSet<RoomTile> activatorTiles = getActivatorTiles(room);
        if (activatorTiles.size() == 0) {
            return;
        }
        boolean z = false;
        TObjectHashIterator it = activatorTiles.iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            if (roomUnit.getCurrentLocation().is(roomTile.x, roomTile.y)) {
                z = true;
            }
        }
        if (z) {
            useVendingMachine(gameClient, room, roomUnit);
            return;
        }
        RoomTile roomTile2 = null;
        TObjectHashIterator it2 = activatorTiles.iterator();
        while (it2.hasNext()) {
            RoomTile roomTile3 = (RoomTile) it2.next();
            if (roomTile3.state == RoomTileState.OPEN || roomTile3.state == RoomTileState.SIT) {
                if (roomTile2 == null || roomTile2.distance(roomUnit.getCurrentLocation()) > roomTile3.distance(roomUnit.getCurrentLocation())) {
                    roomTile2 = roomTile3;
                }
            }
        }
        if (roomTile2 != null) {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            arrayList.add(() -> {
                tryInteract(gameClient, room, roomUnit);
            });
            roomUnit.setGoalLocation(roomTile2);
            Emulator.getThreading().run(new RoomUnitWalkToLocation(roomUnit, roomTile2, room, arrayList, arrayList2));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        super.run();
        if (getExtradata().equals("1")) {
            setExtradata("0");
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
            if (room != null) {
                room.updateItem(this);
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return getBaseItem().allowWalk();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }

    private void rotateToMachine(Room room, RoomUnit roomUnit) {
        RoomUserRotation roomUserRotation = RoomUserRotation.values()[Rotation.Calculate(roomUnit.getX(), roomUnit.getY(), getX(), getY())];
        if (Math.abs(roomUnit.getBodyRotation().getValue() - roomUserRotation.getValue()) > 1) {
            roomUnit.setRotation(roomUserRotation);
            roomUnit.statusUpdate(true);
        }
    }
}
