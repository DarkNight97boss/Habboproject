package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionPyramid.class */
public class InteractionPyramid extends InteractionGate {
    private int nextChange;

    public InteractionPyramid(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionPyramid(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public void change(Room room) {
        if (!getExtradata().equals("0") && !getExtradata().equals("1")) {
            setExtradata("0");
        }
        if (room == null || !room.getHabbosAt(getX(), getY()).isEmpty()) {
            return;
        }
        setExtradata(Math.abs(Integer.valueOf(getExtradata()).intValue() - 1) + Emulator.PREVIEW);
        room.updateItemState(this);
        this.nextChange = Emulator.getIntUnixTimestamp() + 1 + Emulator.getRandom().nextInt(Emulator.getConfig().getInt("pyramids.max.delay"));
    }

    public int getNextChange() {
        return this.nextChange;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionGate, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionGate, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return false;
    }
}
