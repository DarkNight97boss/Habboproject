package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectVendingMachine.class */
public class InteractionEffectVendingMachine extends InteractionVendingMachine {
    public InteractionEffectVendingMachine(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionEffectVendingMachine(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionVendingMachine
    public void giveVendingMachineItem(Room room, RoomUnit roomUnit) {
        room.giveEffect(roomUnit, getBaseItem().getRandomVendingItem(), 30);
    }
}
