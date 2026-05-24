package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectAlert.class */
public class WiredEffectAlert extends WiredEffectWhisper {
    public WiredEffectAlert(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredEffectAlert(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper, com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null) {
            return false;
        }
        habbo.alert(this.message.replace("%online%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + Emulator.PREVIEW).replace("%username%", habbo.getHabboInfo().getUsername()).replace("%roomsloaded%", Emulator.getGameEnvironment().getRoomManager().loadedRoomsCount() + Emulator.PREVIEW));
        return true;
    }
}
