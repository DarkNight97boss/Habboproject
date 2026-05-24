package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionHanditemTile.class */
public class InteractionHanditemTile extends InteractionHanditem {
    public InteractionHanditemTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionHanditemTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Emulator.getThreading().run(() -> {
            if (roomUnit.getCurrentLocation().x == this.getX() && roomUnit.getCurrentLocation().y == this.getY()) {
                this.handle(room, roomUnit);
            }
        }, 3000L);
    }
}
