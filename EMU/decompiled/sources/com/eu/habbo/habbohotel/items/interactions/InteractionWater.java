package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWater.class */
public class InteractionWater extends InteractionDefault {
    private static final String DEEP_WATER_NAME = "bw_water_2";
    private final boolean isDeepWater;
    private boolean isInRoom;

    public InteractionWater(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.isDeepWater = item.getName().equalsIgnoreCase(DEEP_WATER_NAME);
        this.isInRoom = getRoomId() != 0;
    }

    public InteractionWater(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.isDeepWater = false;
        this.isInRoom = getRoomId() != 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        updateWaters(room, roomTile);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        this.isInRoom = false;
        updateWaters(room, null);
        Object[] objArr = new Object[0];
        TObjectHashIterator it = room.getHabbosOnItem(this).iterator();
        while (it.hasNext()) {
            try {
                onWalkOff(((Habbo) it.next()).getRoomUnit(), room, objArr);
            } catch (Exception e) {
            }
        }
        TObjectHashIterator it2 = room.getBotsOnItem(this).iterator();
        while (it2.hasNext()) {
            try {
                onWalkOff(((Bot) it2.next()).getRoomUnit(), room, objArr);
            } catch (Exception e2) {
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        this.isInRoom = true;
        updateWaters(room, null);
        super.onPlace(room);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        Pet pet = room.getPet(roomUnit);
        if (pet == null || pet.getRoomUnit().hasStatus(RoomUnitStatus.SWIM) || !pet.getPetData().canSwim) {
            return;
        }
        pet.getRoomUnit().setStatus(RoomUnitStatus.SWIM, Emulator.PREVIEW);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        Pet pet = room.getPet(roomUnit);
        if (pet == null) {
            return;
        }
        pet.getRoomUnit().removeStatus(RoomUnitStatus.SWIM);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault
    public boolean canToggle(Habbo habbo, Room room) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canStackAt(Room room, List<Pair<RoomTile, THashSet<HabboItem>>> list) {
        Iterator<Pair<RoomTile, THashSet<HabboItem>>> it = list.iterator();
        while (it.hasNext()) {
            TObjectHashIterator it2 = ((THashSet) it.next().getValue()).iterator();
            while (it2.hasNext()) {
                if (!(((HabboItem) it2.next()) instanceof InteractionWater)) {
                    return false;
                }
            }
        }
        return super.canStackAt(room, list);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (!super.canWalkOn(roomUnit, room, objArr)) {
            return false;
        }
        Pet pet = room.getPet(roomUnit);
        return pet == null || pet.getPetData().canSwim;
    }

    private void updateWaters(Room room, RoomTile roomTile) {
        updateWater(room);
        Rectangle rectangle = getRectangle(1, 1);
        Rectangle rectangle2 = null;
        if (roomTile != null) {
            rectangle2 = RoomLayout.getRectangle(roomTile.x - 1, roomTile.y - 1, getBaseItem().getWidth() + 2, getBaseItem().getLength() + 2, getRotation());
        }
        TObjectHashIterator it = room.getRoomSpecialTypes().getItemsOfType(InteractionWater.class).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem != this) {
                Rectangle rectangle3 = habboItem.getRectangle();
                if (rectangle.intersects(rectangle3) || (rectangle2 != null && rectangle2.intersects(rectangle3))) {
                    ((InteractionWater) habboItem).updateWater(room);
                }
            }
        }
        if (rectangle2 != null) {
            TObjectHashIterator it2 = room.getRoomSpecialTypes().getItemsOfType(InteractionWaterItem.class).iterator();
            while (it2.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it2.next();
                if (rectangle2.intersects(habboItem2.getRectangle())) {
                    ((InteractionWaterItem) habboItem2).update();
                }
            }
        }
    }

    private void updateWater(Room room) {
        Rectangle rectangle = getRectangle();
        TObjectHashIterator it = room.getRoomSpecialTypes().getItemsOfType(InteractionWaterItem.class).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (rectangle.intersects(habboItem.getRectangle())) {
                ((InteractionWaterItem) habboItem).update();
            }
        }
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        if (isValidForMask(room, getX() - 1, getY() - 1, getZ(), true)) {
            i = 1;
        }
        if (isValidForMask(room, getX(), getY() - 1, getZ())) {
            i2 = 1;
        }
        if (isValidForMask(room, getX() + 1, getY() - 1, getZ())) {
            i3 = 1;
        }
        if (isValidForMask(room, getX() + 2, getY() - 1, getZ(), true)) {
            i4 = 1;
        }
        if (isValidForMask(room, getX() - 1, getY(), getZ())) {
            i5 = 1;
        }
        if (isValidForMask(room, getX() + 2, getY(), getZ())) {
            i6 = 1;
        }
        if (isValidForMask(room, getX() - 1, getY() + 1, getZ())) {
            i7 = 1;
        }
        if (isValidForMask(room, getX() + 2, getY() + 1, getZ())) {
            i8 = 1;
        }
        if (isValidForMask(room, getX() - 1, getY() + 2, getZ(), true)) {
            i9 = 1;
        }
        if (isValidForMask(room, getX(), getY() + 2, getZ())) {
            i10 = 1;
        }
        if (isValidForMask(room, getX() + 1, getY() + 2, getZ())) {
            i11 = 1;
        }
        if (isValidForMask(room, getX() + 2, getY() + 2, getZ(), true)) {
            i12 = 1;
        }
        if (i2 == 0 && room.getLayout().isVoidTile(getX(), (short) (getY() - 1))) {
            i2 = 1;
        }
        if (i3 == 0 && room.getLayout().isVoidTile((short) (getX() + 1), (short) (getY() - 1))) {
            i3 = 1;
        }
        if (i5 == 0 && room.getLayout().isVoidTile((short) (getX() - 1), getY())) {
            i5 = 1;
        }
        if (i6 == 0 && room.getLayout().isVoidTile((short) (getX() + 2), getY())) {
            i6 = 1;
        }
        if (i7 == 0 && room.getLayout().isVoidTile((short) (getX() - 1), (short) (getY() + 1))) {
            i7 = 1;
        }
        if (i8 == 0 && room.getLayout().isVoidTile((short) (getX() + 2), (short) (getY() + 1))) {
            i8 = 1;
        }
        if (i10 == 0 && room.getLayout().isVoidTile(getX(), (short) (getY() + 2))) {
            i10 = 1;
        }
        if (i11 == 0 && room.getLayout().isVoidTile((short) (getX() + 1), (short) (getY() + 2))) {
            i11 = 1;
        }
        String strValueOf = String.valueOf((i << 11) | (i2 << 10) | (i3 << 9) | (i4 << 8) | (i5 << 7) | (i6 << 6) | (i7 << 5) | (i8 << 4) | (i9 << 3) | (i10 << 2) | (i11 << 1) | i12);
        if (getExtradata().equals(strValueOf)) {
            return;
        }
        setExtradata(strValueOf);
        needsUpdate(true);
        room.updateItem(this);
    }

    private boolean isValidForMask(Room room, int i, int i2, double d) {
        return isValidForMask(room, i, i2, d, false);
    }

    private boolean isValidForMask(Room room, int i, int i2, double d, boolean z) {
        TObjectHashIterator it = room.getItemsAt(i, i2, d).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem instanceof InteractionWater) {
                InteractionWater interactionWater = (InteractionWater) habboItem;
                if (interactionWater.isInRoom && ((z && !this.isDeepWater) || interactionWater.isDeepWater == this.isDeepWater)) {
                    return true;
                }
            }
        }
        return false;
    }
}
