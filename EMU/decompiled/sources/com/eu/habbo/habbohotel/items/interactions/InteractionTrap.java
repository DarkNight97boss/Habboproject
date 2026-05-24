package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionTrap.class */
public class InteractionTrap extends InteractionDefault {
    public InteractionTrap(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionTrap(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        if (getExtradata().equals("0")) {
            return;
        }
        Habbo habbo = room.getHabbo(roomUnit);
        int effectId = habbo.getClient().getHabbo().getRoomUnit().getEffectId();
        roomUnit.stopWalking();
        super.onWalkOn(roomUnit, room, objArr);
        int i = Emulator.getConfig().getInt("hotel.item.trap." + getBaseItem().getName());
        if (i == 0) {
            Emulator.getConfig().register("hotel.item.trap." + getBaseItem().getName(), "3000");
            i = 3000;
        }
        if (roomUnit != null) {
            if ((getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) && roomUnit.getRoomUnitType().equals(RoomUnitType.USER) && habbo != null) {
                if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0 && habbo.getRoomUnit().getEffectId() != getBaseItem().getEffectM()) {
                    room.giveEffect(habbo, getBaseItem().getEffectM(), -1);
                    return;
                }
                if (habbo.getHabboInfo().getGender().equals(HabboGender.F) && getBaseItem().getEffectF() > 0 && habbo.getRoomUnit().getEffectId() != getBaseItem().getEffectF()) {
                    room.giveEffect(habbo, getBaseItem().getEffectF(), -1);
                } else {
                    roomUnit.setCanWalk(false);
                    Emulator.getThreading().run(() -> {
                        room.giveEffect(roomUnit, 0, -1);
                        roomUnit.setCanWalk(true);
                        room.giveEffect(roomUnit, effectId, -1);
                    }, i);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }
}
