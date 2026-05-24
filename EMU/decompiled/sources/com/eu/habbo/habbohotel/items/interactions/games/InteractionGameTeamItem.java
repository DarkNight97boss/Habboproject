package com.eu.habbo.habbohotel.items.interactions.games;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/InteractionGameTeamItem.class */
public abstract class InteractionGameTeamItem extends HabboItem {
    public final GameTeamColors teamColor;

    protected InteractionGameTeamItem(ResultSet resultSet, Item item, GameTeamColors gameTeamColors) throws SQLException {
        super(resultSet, item);
        this.teamColor = gameTeamColors;
    }

    protected InteractionGameTeamItem(int i, int i2, Item item, String str, int i3, int i4, GameTeamColors gameTeamColors) {
        super(i, i2, item, str, i3, i4);
        this.teamColor = gameTeamColors;
    }
}
