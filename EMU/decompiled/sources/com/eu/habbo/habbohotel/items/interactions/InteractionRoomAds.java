package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionRoomAds.class */
public class InteractionRoomAds extends InteractionCustomValues {
    public static final THashMap<String, String> defaultValues = new THashMap<String, String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionRoomAds.1
        {
            put("imageUrl", Emulator.PREVIEW);
            put("clickUrl", Emulator.PREVIEW);
            put("offsetX", "0");
            put("offsetY", "0");
            put("offsetZ", "0");
        }
    };

    public InteractionRoomAds(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, defaultValues);
    }

    public InteractionRoomAds(int i, int i2, Item item, String str, int i3, int i4) {
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
}
