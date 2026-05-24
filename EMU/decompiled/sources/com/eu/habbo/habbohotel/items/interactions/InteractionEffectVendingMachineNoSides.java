package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectVendingMachineNoSides.class */
public class InteractionEffectVendingMachineNoSides extends InteractionVendingMachine {
    public InteractionEffectVendingMachineNoSides(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionEffectVendingMachineNoSides(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionVendingMachine
    public void giveVendingMachineItem(Room room, RoomUnit roomUnit) {
        room.giveEffect(roomUnit, getBaseItem().getRandomVendingItem(), 30);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionVendingMachine
    public THashSet<RoomTile> getActivatorTiles(Room room) {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                RoomTile tile = room.getLayout().getTile((short) (getX() + i), (short) (getY() + i2));
                if (tile != null) {
                    tHashSet.add(tile);
                }
            }
        }
        return tHashSet;
    }
}
