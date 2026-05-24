package com.eu.habbo.habbohotel.items.interactions.games.tag.icetag;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.tag.IceTagGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/tag/icetag/InteractionIceTagField.class */
public class InteractionIceTagField extends InteractionTagField {
    private final HashMap<Habbo, Integer> stepTimes;

    public InteractionIceTagField(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, IceTagGame.class);
        this.stepTimes = new HashMap<>();
    }

    public InteractionIceTagField(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, IceTagGame.class);
        this.stepTimes = new HashMap<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            this.stepTimes.put(habbo, Integer.valueOf(Emulator.getIntUnixTimestamp()));
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || !this.stepTimes.containsKey(habbo)) {
            return;
        }
        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("TagC"), (Emulator.getIntUnixTimestamp() - this.stepTimes.get(habbo).intValue()) / 60);
        this.stepTimes.remove(habbo);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId());
        if (habbo != null) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("TagA"));
        }
    }
}
