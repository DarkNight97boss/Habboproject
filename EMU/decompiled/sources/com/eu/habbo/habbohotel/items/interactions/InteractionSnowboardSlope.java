package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionSnowboardSlope.class */
public class InteractionSnowboardSlope extends InteractionMultiHeight {
    public InteractionSnowboardSlope(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public InteractionSnowboardSlope(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        room.giveEffect(roomUnit, 97, -1);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        if (roomUnit.getEffectId() == 97) {
            room.giveEffect(roomUnit, 0, -1);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        THashSet<HabboItem> itemsOfType = room.getRoomSpecialTypes().getItemsOfType(InteractionSnowboardSlope.class);
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("snowBoardBuild");
        if (achievement == null) {
            return;
        }
        Habbo habbo = room.getHabbo(room.getOwnerId());
        if (Math.max(itemsOfType.size() - (habbo != null ? habbo.getHabboStats().getAchievementProgress(achievement) : AchievementManager.getAchievementProgressForHabbo(room.getOwnerId(), achievement)), 0) > 0) {
            AchievementManager.progressAchievement(room.getOwnerId(), achievement);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        TObjectHashIterator it = room.getHabbosOnItem(this).iterator();
        while (it.hasNext()) {
            Habbo habbo = (Habbo) it.next();
            if (habbo.getRoomUnit().getEffectId() == 97) {
                room.giveEffect(habbo, 0, -1);
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        Rectangle rectangle = RoomLayout.getRectangle(roomTile2.x, roomTile2.y, getBaseItem().getWidth(), getBaseItem().getLength(), getRotation());
        TObjectHashIterator it = room.getHabbosOnItem(this).iterator();
        while (it.hasNext()) {
            Habbo habbo = (Habbo) it.next();
            if (habbo.getRoomUnit().getEffectId() == 97 && !rectangle.contains(habbo.getRoomUnit().getCurrentLocation().x, habbo.getRoomUnit().getCurrentLocation().y)) {
                room.giveEffect(habbo, 0, -1);
            }
        }
    }
}
