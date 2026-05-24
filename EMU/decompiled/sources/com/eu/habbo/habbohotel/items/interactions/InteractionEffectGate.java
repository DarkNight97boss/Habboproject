package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.interfaces.ConditionalGate;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.threading.runnables.CloseGate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionEffectGate.class */
public class InteractionEffectGate extends InteractionDefault implements ConditionalGate {
    private static final List<Integer> defaultAllowedEnables = new ArrayList(Arrays.asList(114, 115, 116, 117, Integer.valueOf(Outgoing.ChangeNameUpdateComposer), 135));

    public InteractionEffectGate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionEffectGate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (roomUnit == null || room == null) {
            return false;
        }
        String strTrim = getBaseItem().getCustomParams().trim();
        return !strTrim.isEmpty() ? Arrays.asList(strTrim.split(";")).contains(Integer.valueOf(roomUnit.getEffectId()).toString()) : defaultAllowedEnables.contains(Integer.valueOf(roomUnit.getEffectId()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        if (canWalkOn(roomUnit, room, objArr)) {
            setExtradata("1");
            room.updateItemState(this);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        Emulator.getThreading().run(new CloseGate(this, room), 1000L);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.interfaces.ConditionalGate
    public void onRejected(RoomUnit roomUnit, Room room, Object[] objArr) {
    }
}
