package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.threading.runnables.BackgroundAnimation;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionBackgroundToner.class */
public class InteractionBackgroundToner extends HabboItem {
    public InteractionBackgroundToner(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionBackgroundToner(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(5 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt((Integer) 4);
        if (getExtradata().split(":").length == 4) {
            String[] strArrSplit = getExtradata().split(":");
            serverMessage.appendInt(Integer.valueOf(strArrSplit[0]));
            serverMessage.appendInt(Integer.valueOf(strArrSplit[1]));
            serverMessage.appendInt(Integer.valueOf(strArrSplit[2]));
            serverMessage.appendInt(Integer.valueOf(strArrSplit[3]));
        } else {
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(Outgoing.IgnoredUsersComposer));
            serverMessage.appendInt(Integer.valueOf(Outgoing.IgnoredUsersComposer));
            serverMessage.appendInt(Integer.valueOf(Outgoing.IgnoredUsersComposer));
            setExtradata("0:126:126:126");
            needsUpdate(true);
            Emulator.getThreading().run(this);
        }
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
        if (gameClient != null) {
            if (!gameClient.getHabbo().getRoomUnit().getRoom().hasRights(gameClient.getHabbo())) {
                ScripterManager.scripterDetected(gameClient, Emulator.getTexts().getValue("scripter.warning.item.bgtoner.permission").replace("%username%", gameClient.getHabbo().getHabboInfo().getUsername()).replace("%room%", room.getName()).replace("%owner%", room.getOwnerName()));
                return;
            } else if (gameClient.getHabbo().getRoomUnit().cmdSit && gameClient.getHabbo().getRoomUnit().getEffectId() == 1337) {
                new BackgroundAnimation(this, room).run();
                return;
            }
        }
        if (getExtradata().split(":").length == 4) {
            String[] strArrSplit = getExtradata().split(":");
            setExtradata((strArrSplit[0].equals("0") ? "1" : "0") + ":" + strArrSplit[1] + ":" + strArrSplit[2] + ":" + strArrSplit[3]);
            room.updateItem(this);
        } else {
            setExtradata("0:126:126:126");
            room.updateItem(this);
        }
        needsUpdate(true);
        Emulator.getThreading().run(this);
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
    public boolean isUsable() {
        return true;
    }
}
