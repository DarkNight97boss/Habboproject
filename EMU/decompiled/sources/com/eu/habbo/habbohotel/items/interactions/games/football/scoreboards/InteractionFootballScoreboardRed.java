package com.eu.habbo.habbohotel.items.interactions.games.football.scoreboards;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/football/scoreboards/InteractionFootballScoreboardRed.class */
public class InteractionFootballScoreboardRed extends InteractionFootballScoreboard {
    public InteractionFootballScoreboardRed(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, GameTeamColors.RED);
    }

    public InteractionFootballScoreboardRed(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, GameTeamColors.RED);
    }
}
