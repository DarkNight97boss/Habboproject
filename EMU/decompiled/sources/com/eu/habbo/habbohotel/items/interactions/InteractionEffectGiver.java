package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectGiver.class */
public class InteractionEffectGiver extends InteractionDefault {
    public InteractionEffectGiver(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionEffectGiver(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (RoomLayout.tilesAdjecent(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), room.getLayout().getTile(getX(), getY())) || (gameClient.getHabbo().getRoomUnit().getCurrentLocation().x == getX() && gameClient.getHabbo().getRoomUnit().getCurrentLocation().y == getY())) {
            handle(room, gameClient.getHabbo().getRoomUnit());
        }
    }

    protected void handle(Room room, RoomUnit roomUnit) {
        if (getExtradata().isEmpty()) {
            setExtradata("0");
        }
        if (getExtradata().equals("0")) {
            room.giveEffect(roomUnit, getBaseItem().getRandomVendingItem(), -1);
            if (getBaseItem().getStateCount() > 1) {
                setExtradata("1");
                room.updateItem(this);
                Emulator.getThreading().run(() -> {
                    setExtradata("0");
                    room.updateItem(this);
                }, 500L);
            }
        }
    }
}
