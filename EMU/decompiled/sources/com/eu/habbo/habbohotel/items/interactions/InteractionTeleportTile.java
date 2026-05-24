package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionTeleportTile.class */
public class InteractionTeleportTile extends InteractionTeleport {
    public InteractionTeleportTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionTeleportTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionTeleport, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionTeleport, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        if (roomUnit == null || !canWalkOn(roomUnit, room, objArr) || (habbo = room.getHabbo(roomUnit)) == null || !canUseTeleport(habbo.getClient(), room) || habbo.getRoomUnit().isTeleporting) {
            return;
        }
        habbo.getRoomUnit().setGoalLocation(habbo.getRoomUnit().getCurrentLocation());
        startTeleport(room, habbo, Outgoing.CraftableProductsComposer);
    }
}
