package com.eu.habbo.habbohotel.items.interactions.games.tag.bunnyrun;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.tag.BunnyrunGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/tag/bunnyrun/InteractionBunnyrunField.class */
public class InteractionBunnyrunField extends InteractionTagField {
    public InteractionBunnyrunField(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, BunnyrunGame.class);
    }

    public InteractionBunnyrunField(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, BunnyrunGame.class);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId());
        if (habbo != null) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("RbBunnyTag"));
        }
    }
}
