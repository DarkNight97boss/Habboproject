package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionObstacle.class */
public class InteractionObstacle extends HabboItem implements ICycleable {
    private THashSet<RoomTile> middleTiles;

    public InteractionObstacle(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
        this.middleTiles = new THashSet<>();
    }

    public InteractionObstacle(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
        this.middleTiles = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        if (room.getHabbo(roomUnit) == null) {
            Pet pet = room.getPet(roomUnit);
            if ((pet instanceof HorsePet) && ((HorsePet) pet).getRider() != null && roomUnit.getBodyRotation().getValue() % 2 == 0) {
                if (getRotation() == 2) {
                    if (roomUnit.getBodyRotation().equals(RoomUserRotation.WEST)) {
                        ((HorsePet) pet).getRider().getRoomUnit().setGoalLocation(room.getLayout().getTile((short) (roomUnit.getX() - 3), roomUnit.getY()));
                        return;
                    } else {
                        if (roomUnit.getBodyRotation().equals(RoomUserRotation.EAST)) {
                            ((HorsePet) pet).getRider().getRoomUnit().setGoalLocation(room.getLayout().getTile((short) (roomUnit.getX() + 3), roomUnit.getY()));
                            return;
                        }
                        return;
                    }
                }
                if (getRotation() == 4) {
                    if (roomUnit.getBodyRotation().equals(RoomUserRotation.NORTH)) {
                        ((HorsePet) pet).getRider().getRoomUnit().setGoalLocation(room.getLayout().getTile(roomUnit.getX(), (short) (roomUnit.getY() - 3)));
                    } else if (roomUnit.getBodyRotation().equals(RoomUserRotation.SOUTH)) {
                        ((HorsePet) pet).getRider().getRoomUnit().setGoalLocation(room.getLayout().getTile(roomUnit.getX(), (short) (roomUnit.getY() + 3)));
                    }
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        if (room.getHabbo(roomUnit) == null) {
            Pet pet = room.getPet(roomUnit);
            if (!(pet instanceof HorsePet) || ((HorsePet) pet).getRider() == null) {
                return;
            }
            pet.getRoomUnit().removeStatus(RoomUnitStatus.JUMP);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        calculateMiddleTiles(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        this.middleTiles.clear();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        calculateMiddleTiles(room);
    }

    private void calculateMiddleTiles(Room room) {
        this.middleTiles.clear();
        if (getRotation() == 2) {
            this.middleTiles.add(room.getLayout().getTile((short) (getX() + 1), getY()));
            this.middleTiles.add(room.getLayout().getTile((short) (getX() + 1), (short) (getY() + 1)));
        } else if (getRotation() == 4) {
            this.middleTiles.add(room.getLayout().getTile(getX(), (short) (getY() + 1)));
            this.middleTiles.add(room.getLayout().getTile((short) (getX() + 1), (short) (getY() + 1)));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public RoomTileState getOverrideTileState(RoomTile roomTile, Room room) {
        if (this.middleTiles.contains(roomTile)) {
            return RoomTileState.BLOCKED;
        }
        return null;
    }

    @Override // com.eu.habbo.habbohotel.items.ICycleable
    public void cycle(Room room) {
        if (this.middleTiles.size() == 0) {
            calculateMiddleTiles(room);
        }
        TObjectHashIterator it = this.middleTiles.iterator();
        while (it.hasNext()) {
            for (RoomUnit roomUnit : ((RoomTile) it.next()).getUnits()) {
                if (roomUnit.getPath().size() == 0 && !roomUnit.hasStatus(RoomUnitStatus.MOVE) && (roomUnit.getBodyRotation().getValue() == getRotation() || ((RoomUserRotation) Objects.requireNonNull(roomUnit.getBodyRotation().getOpposite())).getValue() == getRotation())) {
                    RoomTile tileInFront = room.getLayout().getTileInFront(roomUnit.getCurrentLocation(), roomUnit.getBodyRotation().getValue());
                    if (tileInFront.state == RoomTileState.INVALID || tileInFront.state == RoomTileState.BLOCKED || room.getRoomUnitsAt(tileInFront).size() != 0) {
                        RoomTile tileInFront2 = room.getLayout().getTileInFront(roomUnit.getCurrentLocation(), ((RoomUserRotation) Objects.requireNonNull(roomUnit.getBodyRotation().getOpposite())).getValue());
                        if (tileInFront2.state != RoomTileState.INVALID && tileInFront2.state != RoomTileState.BLOCKED && room.getRoomUnitsAt(tileInFront2).size() == 0) {
                            roomUnit.setGoalLocation(tileInFront2);
                        }
                    } else {
                        roomUnit.setGoalLocation(tileInFront);
                    }
                }
            }
        }
    }
}
