package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionVikingCotie.class */
public class InteractionVikingCotie extends InteractionDefault {
    public InteractionVikingCotie(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionVikingCotie(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (getExtradata().isEmpty()) {
            setExtradata("0");
        }
        if (gameClient == null || gameClient.getHabbo().getHabboInfo().getId() != getUserId()) {
            return;
        }
        if (gameClient.getHabbo().getRoomUnit().getEffectId() == 172 || gameClient.getHabbo().getRoomUnit().getEffectId() == 173) {
            int iIntValue = Integer.valueOf(getExtradata()).intValue();
            if (iIntValue < 5) {
                int i = iIntValue + 1;
                setExtradata(i + Emulator.PREVIEW);
                room.updateItem(this);
                if (i == 5) {
                    AchievementManager.progressAchievement(gameClient.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("ViciousViking"));
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }
}
