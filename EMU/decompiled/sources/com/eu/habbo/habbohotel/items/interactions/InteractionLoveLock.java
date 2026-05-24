package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.lovelock.LoveLockFurniStartComposer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionLoveLock.class */
public class InteractionLoveLock extends HabboItem {
    public int userOneId;
    public int userTwoId;

    public InteractionLoveLock(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionLoveLock(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(2 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt((Integer) 6);
        String[] strArrSplit = getExtradata().split("\t");
        if (strArrSplit.length == 6) {
            serverMessage.appendString("1");
            serverMessage.appendString(strArrSplit[1]);
            serverMessage.appendString(strArrSplit[2]);
            serverMessage.appendString(strArrSplit[3]);
            serverMessage.appendString(strArrSplit[4]);
            serverMessage.appendString(strArrSplit[5]);
        } else {
            serverMessage.appendString("0");
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString(Emulator.PREVIEW);
        }
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
        if (getExtradata().contains("\t") || gameClient == null || !RoomLayout.tilesAdjecent(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), room.getLayout().getTile(getX(), getY()))) {
            return;
        }
        if (this.userOneId == 0) {
            this.userOneId = gameClient.getHabbo().getHabboInfo().getId();
            gameClient.sendResponse(new LoveLockFurniStartComposer(this));
        } else {
            if (this.userOneId == gameClient.getHabbo().getHabboInfo().getId() || room.getHabbo(this.userOneId) == null) {
                return;
            }
            this.userTwoId = gameClient.getHabbo().getHabboInfo().getId();
            gameClient.sendResponse(new LoveLockFurniStartComposer(this));
        }
    }

    public boolean lock(Habbo habbo, Habbo habbo2, Room room) {
        RoomTile tile = room.getLayout().getTile(getX(), getY());
        if (!RoomLayout.tilesAdjecent(habbo.getRoomUnit().getCurrentLocation(), tile) || !RoomLayout.tilesAdjecent(habbo2.getRoomUnit().getCurrentLocation(), tile)) {
            return false;
        }
        setExtradata(((((((((("1\t") + habbo.getHabboInfo().getUsername()) + "\t") + habbo2.getHabboInfo().getUsername()) + "\t") + habbo.getHabboInfo().getLook()) + "\t") + habbo2.getHabboInfo().getLook()) + "\t") + Calendar.getInstance().get(5) + "-" + (Calendar.getInstance().get(2) + 1) + "-" + Calendar.getInstance().get(1));
        needsUpdate(true);
        Emulator.getThreading().run(this);
        room.updateItem(this);
        return true;
    }
}
