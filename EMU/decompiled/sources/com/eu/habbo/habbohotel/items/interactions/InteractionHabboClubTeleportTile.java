package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionHabboClubTeleportTile.class */
public class InteractionHabboClubTeleportTile extends InteractionTeleportTile {
    public InteractionHabboClubTeleportTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionHabboClubTeleportTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile, com.eu.habbo.habbohotel.items.interactions.InteractionTeleport, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            return habbo.getHabboStats().hasActiveClub();
        }
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionTeleport
    public boolean canUseTeleport(GameClient gameClient, Room room) {
        return super.canUseTeleport(gameClient, room) && gameClient.getHabbo().getHabboStats().hasActiveClub();
    }
}
