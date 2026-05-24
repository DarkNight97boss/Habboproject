package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboGender;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectToggle.class */
public class InteractionEffectToggle extends InteractionDefault {
    public InteractionEffectToggle(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionEffectToggle(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (getExtradata().isEmpty()) {
            setExtradata("0");
        }
        if (gameClient == null || !room.hasRights(gameClient.getHabbo()) || Integer.valueOf(getExtradata()).intValue() >= getBaseItem().getStateCount() - 1) {
            return;
        }
        if ((gameClient.getHabbo().getHabboInfo().getGender() == HabboGender.M && gameClient.getHabbo().getRoomUnit().getEffectId() == getBaseItem().getEffectM()) || (gameClient.getHabbo().getHabboInfo().getGender() == HabboGender.F && gameClient.getHabbo().getRoomUnit().getEffectId() == getBaseItem().getEffectF())) {
            super.onClick(gameClient, room, objArr);
        }
    }
}
