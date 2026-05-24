package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionFireworks.class */
public class InteractionFireworks extends InteractionDefault {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionFireworks.class);
    private static final String STATE_EMPTY = "0";
    private static final String STATE_CHARGED = "1";
    private static final String STATE_EXPLOSION = "2";

    public InteractionFireworks(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionFireworks(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (room == null) {
            return;
        }
        if (objArr.length >= 2 && (objArr[1] instanceof WiredEffectType) && objArr[1] == WiredEffectType.TOGGLE_STATE) {
            if (getExtradata().equalsIgnoreCase(STATE_CHARGED)) {
                super.onClick(gameClient, room, objArr);
                if (getExtradata().equalsIgnoreCase(STATE_EXPLOSION)) {
                    reCharge(room);
                    return;
                }
                return;
            }
            return;
        }
        if (gameClient == null) {
            return;
        }
        if (!canToggle(gameClient.getHabbo(), room)) {
            RoomTile roomTile = null;
            for (RoomTile roomTile2 : room.getLayout().getTilesAround(room.getLayout().getTile(getX(), getY()))) {
                if (roomTile2.isWalkable() && (roomTile == null || roomTile.distance(gameClient.getHabbo().getRoomUnit().getCurrentLocation()) > roomTile2.distance(gameClient.getHabbo().getRoomUnit().getCurrentLocation()))) {
                    roomTile = roomTile2;
                }
            }
            if (roomTile != null && !roomTile.equals(gameClient.getHabbo().getRoomUnit().getCurrentLocation())) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(() -> {
                    try {
                        onClick(gameClient, room, objArr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                gameClient.getHabbo().getRoomUnit().setGoalLocation(roomTile);
                Emulator.getThreading().run(new RoomUnitWalkToLocation(gameClient.getHabbo().getRoomUnit(), roomTile, room, arrayList, new ArrayList()));
            }
        }
        if (getExtradata().equalsIgnoreCase(STATE_CHARGED)) {
            super.onClick(gameClient, room, objArr);
            if (getExtradata().equalsIgnoreCase(STATE_EXPLOSION)) {
                reCharge(room);
                AchievementManager.progressAchievement(gameClient.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FireworksCharger"));
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        setExtradata(STATE_CHARGED);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault
    public boolean canToggle(Habbo habbo, Room room) {
        return room.hasRights(habbo) || RoomLayout.tilesAdjecent(room.getLayout().getTile(getX(), getY()), habbo.getRoomUnit().getCurrentLocation());
    }

    private void reCharge(Room room) {
        int i = 5000;
        if (!getBaseItem().getCustomParams().isEmpty()) {
            try {
                i = Integer.parseInt(getBaseItem().getCustomParams());
            } catch (NumberFormatException e) {
                LOGGER.error("Incorrect customparams (" + getBaseItem().getCustomParams() + ") for base item ID (" + getBaseItem().getId() + ") of type (" + getBaseItem().getName() + ")");
            }
        }
        Emulator.getThreading().run(() -> {
            setExtradata(STATE_CHARGED);
            needsUpdate(true);
            room.updateItemState(this);
        }, i);
    }
}
