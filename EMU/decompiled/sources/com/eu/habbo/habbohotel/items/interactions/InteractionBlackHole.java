package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionBlackHole.class */
public class InteractionBlackHole extends InteractionGate {
    public InteractionBlackHole(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionBlackHole(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoHoleFurniCount");
        Habbo habbo = room.getHabbo(getUserId());
        int size = room.getRoomSpecialTypes().getItemsOfType(InteractionBlackHole.class).size() - (habbo == null ? AchievementManager.getAchievementProgressForHabbo(getUserId(), achievement) : habbo.getHabboStats().getAchievementProgress(achievement));
        if (size > 0) {
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, achievement, size);
            } else {
                AchievementManager.progressAchievement(getUserId(), achievement, size);
            }
        }
        super.onPlace(room);
    }
}
