package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionSwitch.class */
public class InteractionSwitch extends InteractionDefault {
    public InteractionSwitch(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionSwitch(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault
    public boolean canToggle(Habbo habbo, Room room) {
        return RoomLayout.tilesAdjecent(room.getLayout().getTile(getX(), getY()), habbo.getRoomUnit().getCurrentLocation());
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
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
        super.onClick(gameClient, room, objArr);
    }
}
