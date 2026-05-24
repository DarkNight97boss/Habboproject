package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.outgoing.generic.alerts.CustomNotificationComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionHabboClubHopper.class */
public class InteractionHabboClubHopper extends InteractionHopper {
    public InteractionHabboClubHopper(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionHabboClubHopper(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionHopper, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient.getHabbo().getHabboStats().hasActiveClub()) {
            super.onClick(gameClient, room, objArr);
        } else {
            gameClient.sendResponse(new CustomNotificationComposer(2));
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionHopper
    protected boolean canUseTeleport(GameClient gameClient, RoomTile roomTile, Room room) {
        return super.canUseTeleport(gameClient, roomTile, room) && gameClient.getHabbo().getHabboStats().hasActiveClub();
    }
}
