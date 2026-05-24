package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.hopper.HopperActionOne;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionHopper.class */
public class InteractionHopper extends HabboItem {
    public InteractionHopper(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionHopper(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
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
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        RoomTile squareInFront;
        super.onClick(gameClient, room, objArr);
        if (room == null || (squareInFront = HabboItem.getSquareInFront(room.getLayout(), this)) == null) {
            return;
        }
        if (!canUseTeleport(gameClient, squareInFront, room)) {
            gameClient.getHabbo().getRoomUnit().setGoalLocation(squareInFront);
            return;
        }
        gameClient.getHabbo().getRoomUnit().isTeleporting = true;
        setExtradata("1");
        room.updateItemState(this);
        Emulator.getThreading().run(new HopperActionOne(this, room, gameClient), 500L);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        if (!getExtradata().equals("0")) {
            setExtradata("0");
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
            if (room != null) {
                room.updateItemState(this);
            }
        }
        super.run();
    }

    protected boolean canUseTeleport(GameClient gameClient, RoomTile roomTile, Room room) {
        if (gameClient.getHabbo().getRoomUnit().getX() == roomTile.x && gameClient.getHabbo().getRoomUnit().getY() == roomTile.y && !gameClient.getHabbo().getRoomUnit().isTeleporting && room.getHabbosAt(getX(), getY()).isEmpty()) {
            return getExtradata().equals("0");
        }
        return false;
    }
}
