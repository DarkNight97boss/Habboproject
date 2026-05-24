package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerTeamLoses.class */
public class WiredTriggerTeamLoses extends WiredTriggerGameStarts {
    public WiredTriggerTeamLoses(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredTriggerTeamLoses(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerGameStarts, com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public WiredTriggerType getType() {
        return WiredTriggerType.CUSTOM;
    }
}
