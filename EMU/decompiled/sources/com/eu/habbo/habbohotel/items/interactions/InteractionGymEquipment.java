package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionGymEquipment.class */
public class InteractionGymEquipment extends InteractionEffectTile implements ICycleable {
    private int startTime;
    private int roomUnitId;

    public InteractionGymEquipment(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.startTime = 0;
        this.roomUnitId = -1;
    }

    public InteractionGymEquipment(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.startTime = 0;
        this.roomUnitId = -1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionEffectTile, com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return this.roomUnitId == -1 && super.canWalkOn(roomUnit, room, objArr) && (roomUnit.getRoomUnitType().equals(RoomUnitType.USER) || roomUnit.getRoomUnitType().equals(RoomUnitType.BOT));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionEffectTile, com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return this.roomUnitId == -1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionEffectTile, com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        if (forceRotation()) {
            roomUnit.setRotation(RoomUserRotation.fromValue(getRotation()));
            roomUnit.canRotate = false;
        }
        this.roomUnitId = roomUnit.getId();
        if (roomUnit.getRoomUnitType() != RoomUnitType.USER || room.getHabbo(roomUnit) == null) {
            return;
        }
        this.startTime = Emulator.getIntUnixTimestamp();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionEffectTile, com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        room.giveEffect(roomUnit, 0, -1);
        if (forceRotation()) {
            roomUnit.canRotate = true;
        }
        reset(room);
    }

    public String achievementName() {
        return Emulator.getConfig().getValue("hotel.furni.gym.achievement." + getBaseItem().getName(), Emulator.PREVIEW);
    }

    public boolean forceRotation() {
        return Emulator.getConfig().getBoolean("hotel.furni.gym.forcerot." + getBaseItem().getName(), true);
    }

    @Override // com.eu.habbo.habbohotel.items.ICycleable
    public void cycle(Room room) {
        Habbo habboByRoomUnitId;
        if (this.roomUnitId == -1 || (habboByRoomUnitId = room.getHabboByRoomUnitId(this.roomUnitId)) == null) {
            return;
        }
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        if (intUnixTimestamp - this.startTime >= 120) {
            String strAchievementName = achievementName();
            if (!strAchievementName.isEmpty()) {
                AchievementManager.progressAchievement(habboByRoomUnitId.getHabboInfo().getId(), Emulator.getGameEnvironment().getAchievementManager().getAchievement(strAchievementName));
            }
            this.startTime = intUnixTimestamp;
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void setRotation(int i) {
        Room room;
        RoomUnit currentRoomUnit;
        super.setRotation(i);
        if (!forceRotation() || this.roomUnitId == -1 || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId())) == null || (currentRoomUnit = getCurrentRoomUnit(room)) == null) {
            return;
        }
        currentRoomUnit.setRotation(RoomUserRotation.fromValue(i));
        room.updateRoomUnit(currentRoomUnit);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        if (this.roomUnitId != -1) {
            setEffect(room, 0);
        }
        reset(room);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPressurePlate, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        if (roomTile.equals(roomTile2)) {
            return;
        }
        setEffect(room, 0);
        reset(room);
    }

    private void setEffect(Room room, int i) {
        if (this.roomUnitId == -1) {
            return;
        }
        room.giveEffect(getCurrentRoomUnit(room), i, -1);
    }

    private void reset(Room room) {
        this.roomUnitId = -1;
        this.startTime = 0;
        setExtradata("0");
        room.updateItem(this);
    }

    private RoomUnit getCurrentRoomUnit(Room room) {
        Habbo habboByRoomUnitId = room.getHabboByRoomUnitId(this.roomUnitId);
        if (habboByRoomUnitId != null) {
            return habboByRoomUnitId.getRoomUnit();
        }
        Bot botByRoomUnitId = room.getBotByRoomUnitId(this.roomUnitId);
        if (botByRoomUnitId != null) {
            return botByRoomUnitId.getRoomUnit();
        }
        return null;
    }
}
