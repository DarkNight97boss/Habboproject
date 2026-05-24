package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionColorPlate.class */
public class InteractionColorPlate extends InteractionDefault {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionColorPlate.class);

    public InteractionColorPlate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionColorPlate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        change(room, 1);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        change(room, -1);
    }

    private void change(Room room, int i) {
        int iIntValue = 0;
        if (getExtradata() == null || getExtradata().isEmpty()) {
            setExtradata("0");
        }
        try {
            iIntValue = Integer.valueOf(getExtradata()).intValue();
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        int stateCount = iIntValue + i;
        if (stateCount > getBaseItem().getStateCount()) {
            stateCount = getBaseItem().getStateCount();
        }
        if (stateCount < 0) {
            stateCount = 0;
        }
        setExtradata(stateCount + Emulator.PREVIEW);
        needsUpdate(true);
        room.updateItemState(this);
    }
}
