package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionTileEffectProvider.class */
public class InteractionTileEffectProvider extends InteractionCustomValues {
    public static THashMap<String, String> defaultValues = new THashMap<String, String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionTileEffectProvider.1
        {
            put("effectId", "0");
        }
    };

    public InteractionTileEffectProvider(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, defaultValues);
    }

    public InteractionTileEffectProvider(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, defaultValues);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCustomValues, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCustomValues, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        int iIntValue = Integer.valueOf((String) this.values.get("effectId")).intValue();
        if (roomUnit.getEffectId() == iIntValue) {
            iIntValue = 0;
        }
        this.values.put("state", "1");
        room.updateItem(this);
        Emulator.getThreading().run(() -> {
            this.values.put("state", "0");
            room.updateItem(this);
        }, 500L);
        room.giveEffect(roomUnit, iIntValue, -1);
    }
}
