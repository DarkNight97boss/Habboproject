package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionDefault.class */
public class InteractionDefault extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionDefault.class);

    public InteractionDefault(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionDefault(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return getBaseItem().allowWalk();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        if (room.getItemsAt(roomTile).stream().noneMatch(habboItem -> {
            return habboItem.getClass().isAssignableFrom(InteractionRoller.class);
        })) {
            TObjectHashIterator it = room.getRoomUnits().iterator();
            while (it.hasNext()) {
                RoomUnit roomUnit = (RoomUnit) it.next();
                if (roomTile.unitIsOnFurniOnTile(roomUnit, getBaseItem()) && !roomTile2.unitIsOnFurniOnTile(roomUnit, getBaseItem())) {
                    try {
                        onWalkOff(roomUnit, room, new Object[]{roomTile, roomTile2});
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (room != null) {
            if (gameClient == null || canToggle(gameClient.getHabbo(), room) || (objArr.length >= 2 && (objArr[1] instanceof WiredEffectType) && objArr[1] == WiredEffectType.TOGGLE_STATE)) {
                super.onClick(gameClient, room, objArr);
                if (objArr == null || objArr.length <= 0 || !(objArr[0] instanceof Integer)) {
                    return;
                }
                if (getExtradata().length() == 0) {
                    setExtradata("0");
                }
                if (getBaseItem().getStateCount() > 0) {
                    int iIntValue = 0;
                    try {
                        iIntValue = Integer.valueOf(getExtradata()).intValue();
                    } catch (NumberFormatException e) {
                        LOGGER.error("Incorrect extradata (" + getExtradata() + ") for item ID (" + getId() + ") of type (" + getBaseItem().getName() + ")");
                    }
                    setExtradata(Emulator.PREVIEW + ((iIntValue + 1) % getBaseItem().getStateCount()));
                    needsUpdate(true);
                    room.updateItemState(this);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Bot bot;
        super.onWalkOn(roomUnit, room, objArr);
        if (roomUnit != null) {
            if (getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) {
                if (roomUnit.getRoomUnitType().equals(RoomUnitType.USER)) {
                    Habbo habbo = room.getHabbo(roomUnit);
                    if (habbo != null) {
                        if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0 && habbo.getRoomUnit().getEffectId() != getBaseItem().getEffectM()) {
                            room.giveEffect(habbo, getBaseItem().getEffectM(), -1);
                            return;
                        } else {
                            if (!habbo.getHabboInfo().getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0 || habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                                return;
                            }
                            room.giveEffect(habbo, getBaseItem().getEffectF(), -1);
                            return;
                        }
                    }
                    return;
                }
                if (!roomUnit.getRoomUnitType().equals(RoomUnitType.BOT) || (bot = room.getBot(roomUnit)) == null) {
                    return;
                }
                if (bot.getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0 && roomUnit.getEffectId() != getBaseItem().getEffectM()) {
                    room.giveEffect(bot.getRoomUnit(), getBaseItem().getEffectM(), -1);
                } else {
                    if (!bot.getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0 || roomUnit.getEffectId() == getBaseItem().getEffectF()) {
                        return;
                    }
                    room.giveEffect(bot.getRoomUnit(), getBaseItem().getEffectF(), -1);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Bot bot;
        super.onWalkOff(roomUnit, room, objArr);
        if (roomUnit != null) {
            if (getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) {
                int effectM = 0;
                int effectF = 0;
                if (objArr != null && objArr.length == 2 && (objArr[0] instanceof RoomTile) && (objArr[1] instanceof RoomTile)) {
                    RoomTile roomTile = (RoomTile) objArr[0];
                    HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y, objArr[0] != objArr[1] ? this : null);
                    if (topItemAt != null && (topItemAt.getBaseItem().getEffectM() == getBaseItem().getEffectM() || topItemAt.getBaseItem().getEffectF() == getBaseItem().getEffectF())) {
                        return;
                    }
                    if (topItemAt != null) {
                        effectM = topItemAt.getBaseItem().getEffectM();
                        effectF = topItemAt.getBaseItem().getEffectF();
                    }
                }
                if (roomUnit.getRoomUnitType().equals(RoomUnitType.USER)) {
                    Habbo habbo = room.getHabbo(roomUnit);
                    if (habbo != null) {
                        if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0) {
                            room.giveEffect(habbo, effectM, -1);
                            return;
                        } else {
                            if (!habbo.getHabboInfo().getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0) {
                                return;
                            }
                            room.giveEffect(habbo, effectF, -1);
                            return;
                        }
                    }
                    return;
                }
                if (!roomUnit.getRoomUnitType().equals(RoomUnitType.BOT) || (bot = room.getBot(roomUnit)) == null) {
                    return;
                }
                if (bot.getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0) {
                    room.giveEffect(roomUnit, effectM, -1);
                } else {
                    if (!bot.getGender().equals(HabboGender.F) || getBaseItem().getEffectF() <= 0) {
                        return;
                    }
                    room.giveEffect(roomUnit, effectF, -1);
                }
            }
        }
    }

    public boolean canToggle(Habbo habbo, Room room) {
        HabboItem habboItem;
        if (room.hasRights(habbo)) {
            return true;
        }
        return habbo.getHabboStats().isRentingSpace() && (habboItem = room.getHabboItem(habbo.getHabboStats().rentedItemId)) != null && RoomLayout.squareInSquare(RoomLayout.getRectangle(habboItem.getX(), habboItem.getY(), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()), RoomLayout.getRectangle(getX(), getY(), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()));
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }
}
