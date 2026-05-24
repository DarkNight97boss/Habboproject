package com.eu.habbo.habbohotel.items.interactions.totems;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/totems/InteractionTotemHead.class */
public class InteractionTotemHead extends InteractionDefault {
    public InteractionTotemHead(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionTotemHead(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public TotemType getTotemType() {
        int i;
        try {
            i = Integer.parseInt(getExtradata());
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i < 3 ? TotemType.fromInt(i + 1) : TotemType.fromInt((int) Math.ceil((i - 2) / 4.0f));
    }

    public TotemColor getTotemColor() {
        int i;
        try {
            i = Integer.parseInt(getExtradata());
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i < 3 ? TotemColor.NONE : TotemColor.fromInt((i - 3) - (4 * (getTotemType().type - 1)));
    }

    private void update(Room room, RoomTile roomTile) {
        InteractionTotemLegs interactionTotemLegs = null;
        TObjectHashIterator it = room.getItemsAt(roomTile).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if ((habboItem instanceof InteractionTotemLegs) && habboItem.getZ() < getZ()) {
                interactionTotemLegs = (InteractionTotemLegs) habboItem;
            }
        }
        if (interactionTotemLegs == null) {
            return;
        }
        setExtradata((((4 * getTotemType().type) + interactionTotemLegs.getTotemColor().color) - 1) + Emulator.PREVIEW);
    }

    public void updateTotemState(Room room) {
        updateTotemState(room, room.getLayout().getTile(getX(), getY()));
    }

    public void updateTotemState(Room room, RoomTile roomTile) {
        setExtradata((getTotemType().type - 1) + Emulator.PREVIEW);
        update(room, roomTile);
        needsUpdate(true);
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if ((gameClient == null || room == null || !room.hasRights(gameClient.getHabbo())) && (objArr.length < 2 || !(objArr[1] instanceof WiredEffectType))) {
            return;
        }
        TotemType totemTypeFromInt = TotemType.fromInt(getTotemType().type + 1);
        if (totemTypeFromInt == TotemType.NONE) {
            totemTypeFromInt = TotemType.TROLL;
        }
        setExtradata((totemTypeFromInt.type - 1) + Emulator.PREVIEW);
        updateTotemState(room);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        updateTotemState(room, roomTile2);
    }
}
