package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectTile.class */
public class InteractionEffectTile extends InteractionPressurePlate {
    public InteractionEffectTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionEffectTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Emulator.getThreading().run(() -> {
            updateState(room);
        }, 100L);
        if (objArr == null || objArr.length <= 0) {
            return;
        }
        WiredHandler.handle(WiredTriggerType.WALKS_OFF_FURNI, roomUnit, room, new Object[]{this});
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalk(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Bot bot;
        super.onWalkOn(roomUnit, room, objArr);
        if (roomUnit.getRoomUnitType() == RoomUnitType.USER) {
            Habbo habbo = room.getHabbo(roomUnit);
            if (habbo != null) {
                giveEffect(room, roomUnit, habbo.getHabboInfo().getGender());
                return;
            }
            return;
        }
        if (roomUnit.getRoomUnitType() != RoomUnitType.BOT || (bot = room.getBot(roomUnit)) == null) {
            return;
        }
        giveEffect(room, roomUnit, bot.getGender());
    }

    private void giveEffect(Room room, RoomUnit roomUnit, HabboGender habboGender) {
        if (habboGender.equals(HabboGender.M)) {
            room.giveEffect(roomUnit, getBaseItem().getEffectM(), -1);
        } else {
            room.giveEffect(roomUnit, getBaseItem().getEffectF(), -1);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return false;
    }
}
