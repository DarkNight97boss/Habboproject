package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionCrackableMaster.class */
public class InteractionCrackableMaster extends InteractionCrackable {
    public InteractionCrackableMaster(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionCrackableMaster(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    protected boolean placeInRoom() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    public boolean resetable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    public boolean allowAnyone() {
        return true;
    }
}
