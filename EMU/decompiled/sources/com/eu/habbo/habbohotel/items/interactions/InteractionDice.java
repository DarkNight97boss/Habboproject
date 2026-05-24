package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.plugin.events.furniture.FurnitureDiceRolledEvent;
import com.eu.habbo.threading.runnables.RandomDiceNumber;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionDice.class */
public class InteractionDice extends HabboItem {
    public InteractionDice(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public InteractionDice(ResultSet resultSet, Item item) throws SQLException {
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
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (gameClient == null || !RoomLayout.tilesAdjecent(room.getLayout().getTile(getX(), getY()), gameClient.getHabbo().getRoomUnit().getCurrentLocation()) || getExtradata().equalsIgnoreCase("-1")) {
            return;
        }
        FurnitureDiceRolledEvent furnitureDiceRolledEvent = (FurnitureDiceRolledEvent) Emulator.getPluginManager().fireEvent(new FurnitureDiceRolledEvent(this, gameClient.getHabbo(), -1));
        if (furnitureDiceRolledEvent.isCancelled()) {
            return;
        }
        setExtradata("-1");
        room.updateItemState(this);
        Emulator.getThreading().run(this);
        if (furnitureDiceRolledEvent.result > 0) {
            Emulator.getThreading().run(new RandomDiceNumber(room, this, furnitureDiceRolledEvent.result), 1500L);
        } else {
            Emulator.getThreading().run(new RandomDiceNumber(this, room, getBaseItem().getStateCount()), 1500L);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }
}
