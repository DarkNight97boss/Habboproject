package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.outgoing.generic.alerts.CustomNotificationComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionCostumeHopper.class */
public class InteractionCostumeHopper extends InteractionHopper {
    public InteractionCostumeHopper(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionCostumeHopper(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionHopper, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient.getHabbo().getRoomUnit().getEffectId() > 0) {
            super.onClick(gameClient, room, objArr);
        } else {
            gameClient.sendResponse(new CustomNotificationComposer(1));
        }
    }
}
