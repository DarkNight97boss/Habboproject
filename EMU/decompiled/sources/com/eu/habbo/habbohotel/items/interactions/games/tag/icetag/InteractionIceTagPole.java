package com.eu.habbo.habbohotel.items.interactions.games.tag.icetag;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/tag/icetag/InteractionIceTagPole.class */
public class InteractionIceTagPole extends InteractionTagPole {
    public InteractionIceTagPole(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionIceTagPole(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }
}
