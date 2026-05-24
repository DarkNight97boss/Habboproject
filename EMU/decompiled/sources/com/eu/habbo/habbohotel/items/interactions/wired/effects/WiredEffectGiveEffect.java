package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveEffect.class */
public class WiredEffectGiveEffect extends WiredEffectWhisper {
    public WiredEffectGiveEffect(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredEffectGiveEffect(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper, com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        try {
            int iIntValue = Integer.valueOf(this.message).intValue();
            if (iIntValue < 0) {
                return false;
            }
            room.giveEffect(roomUnit, iIntValue, Integer.MAX_VALUE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
