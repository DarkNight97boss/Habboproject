package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionMultiHeight.class */
public class InteractionMultiHeight extends HabboItem {
    public InteractionMultiHeight(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public InteractionMultiHeight(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
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
        return getBaseItem().allowWalk();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if ((gameClient == null || room.hasRights(gameClient.getHabbo()) || (objArr.length >= 2 && (objArr[1] instanceof WiredEffectType) && objArr[1] == WiredEffectType.TOGGLE_STATE)) && objArr.length > 0 && (objArr[0] instanceof Integer) && room != null) {
            HabboItem topItemAt = room.getTopItemAt(getX(), getY());
            if (topItemAt == null || topItemAt.equals(this)) {
                needsUpdate(true);
                if (getExtradata().length() == 0) {
                    setExtradata("0");
                }
                if (getBaseItem().getMultiHeights().length > 0) {
                    setExtradata(Emulator.PREVIEW + ((Integer.parseInt(getExtradata()) + 1) % getBaseItem().getMultiHeights().length));
                    needsUpdate(true);
                    room.updateTiles(room.getLayout().getTilesAt(room.getLayout().getTile(getX(), getY()), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()));
                    room.updateItemState(this);
                }
            }
        }
    }

    public void updateUnitsOnItem(Room room) {
        TObjectHashIterator it = room.getLayout().getTilesAt(room.getLayout().getTile(getX(), getY()), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()).iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            Collection<RoomUnit> roomUnitsAt = room.getRoomUnitsAt(room.getLayout().getTile(roomTile.x, roomTile.y));
            new THashSet();
            for (RoomUnit roomUnit : roomUnitsAt) {
                if (!roomUnit.hasStatus(RoomUnitStatus.MOVE) || roomUnit.getGoal() == roomTile) {
                    if (getBaseItem().allowSit() || roomUnit.hasStatus(RoomUnitStatus.SIT)) {
                        roomUnit.sitUpdate = true;
                        roomUnit.statusUpdate(true);
                    } else {
                        roomUnit.setZ(roomUnit.getCurrentLocation().getStackHeight());
                        roomUnit.setPreviousLocationZ(roomUnit.getZ());
                        roomUnit.statusUpdate(true);
                    }
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        super.onWalkOn(roomUnit, room, objArr);
        if (roomUnit != null) {
            if ((getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) && roomUnit.getRoomUnitType().equals(RoomUnitType.USER) && (habbo = room.getHabbo(roomUnit)) != null) {
                if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0 && habbo.getRoomUnit().getEffectId() != getBaseItem().getEffectM()) {
                    room.giveEffect(habbo, getBaseItem().getEffectM(), -1);
                } else {
                    if (!habbo.getHabboInfo().getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0 || habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                        return;
                    }
                    room.giveEffect(habbo, getBaseItem().getEffectF(), -1);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        super.onWalkOff(roomUnit, room, objArr);
        if (roomUnit != null) {
            if ((getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) && roomUnit.getRoomUnitType().equals(RoomUnitType.USER) && (habbo = room.getHabbo(roomUnit)) != null) {
                if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0) {
                    room.giveEffect(habbo, 0, -1);
                } else {
                    if (!habbo.getHabboInfo().getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0) {
                        return;
                    }
                    room.giveEffect(habbo, 0, -1);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }
}
