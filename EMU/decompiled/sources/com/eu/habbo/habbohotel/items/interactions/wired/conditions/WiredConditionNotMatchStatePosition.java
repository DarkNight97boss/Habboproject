package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionNotMatchStatePosition.class */
public class WiredConditionNotMatchStatePosition extends WiredConditionMatchStatePosition {
    public static final WiredConditionType type = WiredConditionType.NOT_MATCH_SSHOT;

    public WiredConditionNotMatchStatePosition(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredConditionNotMatchStatePosition(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.conditions.WiredConditionMatchStatePosition, com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return !super.execute(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.conditions.WiredConditionMatchStatePosition, com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public WiredConditionType getType() {
        return type;
    }
}
