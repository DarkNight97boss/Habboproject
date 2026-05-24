package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionGroupPressurePlate.class */
public class InteractionGroupPressurePlate extends InteractionPressurePlate {
    public InteractionGroupPressurePlate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionGroupPressurePlate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate
    public boolean requiresAllTilesOccupied() {
        return true;
    }
}
