package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWaterItem.class */
public class InteractionWaterItem extends InteractionMultiHeight {
    public InteractionWaterItem(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionWaterItem(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        update();
        super.onPlace(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        setExtradata("0");
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        update();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, new Object[0]);
    }

    public void update() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return;
        }
        Rectangle rectangle = getRectangle();
        boolean z = true;
        int i = rectangle.x;
        while (true) {
            short s = (short) i;
            if (s >= rectangle.getWidth() + ((double) rectangle.x) || !z) {
                break;
            }
            int i2 = rectangle.y;
            while (true) {
                short s2 = (short) i2;
                if (s2 >= rectangle.getHeight() + ((double) rectangle.y) || !z) {
                    break;
                }
                boolean z2 = false;
                TObjectHashIterator it = room.getItemsAt(room.getLayout().getTile(s, s2)).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (((HabboItem) it.next()) instanceof InteractionWater) {
                        z2 = true;
                        break;
                    }
                }
                if (!z2) {
                    z = false;
                }
                i2 = s2 + 1;
            }
            i = s + 1;
        }
        String str = z ? "1" : "0";
        if (getExtradata().equals(str)) {
            return;
        }
        setExtradata(str);
        needsUpdate(true);
        room.updateItemState(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }
}
