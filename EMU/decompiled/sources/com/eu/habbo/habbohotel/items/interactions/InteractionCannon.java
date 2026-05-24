package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.CannonKickAction;
import com.eu.habbo.threading.runnables.CannonResetCooldownAction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionCannon.class */
public class InteractionCannon extends HabboItem {
    public boolean cooldown;

    public InteractionCannon(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.cooldown = false;
        setExtradata("0");
    }

    public InteractionCannon(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.cooldown = false;
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
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient != null) {
            super.onClick(gameClient, room, objArr);
        }
        if (room == null) {
            return;
        }
        RoomTile tile = room.getLayout().getTile(getX(), getY());
        RoomTile tileInFront = getRotation() >= 4 ? tile : room.getLayout().getTileInFront(tile, ((getRotation() % 2) + 2) % 8);
        List<RoomTile> tilesAround = room.getLayout().getTilesAround(tileInFront);
        tilesAround.remove(room.getLayout().getTileInFront(tile, (getRotation() + (getRotation() >= 4 ? -1 : 0)) % 8));
        tilesAround.remove(room.getLayout().getTileInFront(tile, (getRotation() + (getRotation() >= 4 ? 5 : 4)) % 8));
        if ((gameClient == null || (tilesAround.contains(gameClient.getHabbo().getRoomUnit().getCurrentLocation()) && gameClient.getHabbo().getRoomUnit().canWalk())) && !this.cooldown) {
            if (gameClient != null) {
                gameClient.getHabbo().getRoomUnit().setCanWalk(false);
                gameClient.getHabbo().getRoomUnit().setGoalLocation(gameClient.getHabbo().getRoomUnit().getCurrentLocation());
                gameClient.getHabbo().getRoomUnit().lookAtPoint(tileInFront);
                gameClient.getHabbo().getRoomUnit().statusUpdate(true);
            }
            this.cooldown = true;
            setExtradata(getExtradata().equals("1") ? "0" : "1");
            room.updateItemState(this);
            Emulator.getThreading().run(new CannonKickAction(this, room, gameClient), 750L);
            Emulator.getThreading().run(new CannonResetCooldownAction(this), 2000L);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
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
    public void onPickUp(Room room) {
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }
}
