package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionGate.class */
public class InteractionGate extends HabboItem {
    public InteractionGate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionGate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
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
        return getExtradata().equals("1");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        boolean z = objArr.length >= 2 && (objArr[1] instanceof WiredEffectType) && objArr[1] == WiredEffectType.TOGGLE_STATE;
        if (gameClient == null || room.hasRights(gameClient.getHabbo()) || z) {
            TObjectHashIterator it = room.getLayout().getTilesAt(room.getLayout().getTile(getX(), getY()), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()).iterator();
            while (it.hasNext()) {
                RoomTile roomTile = (RoomTile) it.next();
                if (room.hasHabbosAt(roomTile.x, roomTile.y)) {
                    return;
                }
            }
            if (getExtradata().length() == 0) {
                setExtradata("0");
            }
            setExtradata(((Integer.parseInt(getExtradata()) + 1) % 2) + Emulator.PREVIEW);
            room.updateTile(room.getLayout().getTile(getX(), getY()));
            needsUpdate(true);
            room.updateItemState(this);
            super.onClick(gameClient, room, new Object[]{"TOGGLE_OVERRIDE"});
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }
}
