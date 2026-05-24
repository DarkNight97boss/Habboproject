package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.users.UserTakeStepEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomTrashing.class */
public class RoomTrashing implements Runnable {
    public static RoomTrashing INSTANCE;
    private Habbo habbo;
    private Room room;

    public RoomTrashing(Habbo habbo, Room room) {
        this.habbo = habbo;
        this.room = room;
        INSTANCE = this;
    }

    @EventHandler
    public static void onUserWalkEvent(UserTakeStepEvent userTakeStepEvent) {
        if (INSTANCE == null || INSTANCE.habbo == null) {
            return;
        }
        if (!INSTANCE.habbo.isOnline()) {
            INSTANCE.habbo = null;
        }
        if (INSTANCE.habbo != userTakeStepEvent.habbo || userTakeStepEvent.habbo.getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        if (!userTakeStepEvent.habbo.getHabboInfo().getCurrentRoom().equals(INSTANCE.room)) {
            INSTANCE.habbo = null;
            INSTANCE.room = null;
            return;
        }
        THashSet tHashSet = new THashSet();
        THashSet<HabboItem> itemsAt = INSTANCE.room.getItemsAt(userTakeStepEvent.toLocation);
        RoomTile tileInFront = null;
        for (int iNextInt = Emulator.getRandom().nextInt(4) + 2; iNextInt > 0; iNextInt--) {
            tileInFront = INSTANCE.room.getLayout().getTileInFront(INSTANCE.room.getLayout().getTile(userTakeStepEvent.toLocation.x, userTakeStepEvent.toLocation.y), userTakeStepEvent.habbo.getRoomUnit().getBodyRotation().getValue(), (short) iNextInt);
            if (INSTANCE.room.getLayout().tileWalkable(tileInFront.x, tileInFront.y)) {
                break;
            }
        }
        TObjectHashIterator it = itemsAt.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            tHashSet.add(new FloorItemOnRollerComposer(habboItem, null, tileInFront, INSTANCE.room.getTopHeightAt(tileInFront.x, tileInFront.y) - habboItem.getZ(), INSTANCE.room).compose());
        }
        RoomTile tileInFront2 = null;
        for (int iNextInt2 = Emulator.getRandom().nextInt(4) + 2; iNextInt2 > 0; iNextInt2--) {
            tileInFront2 = INSTANCE.room.getLayout().getTileInFront(INSTANCE.room.getLayout().getTile(userTakeStepEvent.toLocation.x, userTakeStepEvent.toLocation.y), userTakeStepEvent.habbo.getRoomUnit().getBodyRotation().getValue() + 7, (short) iNextInt2);
            if (INSTANCE.room.getLayout().tileWalkable(tileInFront2.x, tileInFront2.y)) {
                break;
            }
        }
        RoomTile tileInFront3 = INSTANCE.room.getLayout().getTileInFront(INSTANCE.habbo.getRoomUnit().getCurrentLocation(), INSTANCE.habbo.getRoomUnit().getBodyRotation().getValue() + 7);
        if (tileInFront3 != null) {
            itemsAt = INSTANCE.room.getItemsAt(tileInFront3);
        }
        TObjectHashIterator it2 = itemsAt.iterator();
        while (it2.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it2.next();
            tHashSet.add(new FloorItemOnRollerComposer(habboItem2, null, tileInFront2, INSTANCE.room.getTopHeightAt(tileInFront2.x, tileInFront2.y) - habboItem2.getZ(), INSTANCE.room).compose());
        }
        RoomTile tileInFront4 = null;
        for (int iNextInt3 = Emulator.getRandom().nextInt(4) + 2; iNextInt3 > 0; iNextInt3--) {
            tileInFront4 = INSTANCE.getRoom().getLayout().getTileInFront(userTakeStepEvent.toLocation, userTakeStepEvent.habbo.getRoomUnit().getBodyRotation().getValue() + 1, (short) iNextInt3);
            if (INSTANCE.room.getLayout().tileWalkable(tileInFront4.x, tileInFront4.y)) {
                break;
            }
        }
        TObjectHashIterator it3 = INSTANCE.room.getItemsAt(INSTANCE.getRoom().getLayout().getTileInFront(INSTANCE.habbo.getRoomUnit().getCurrentLocation(), INSTANCE.habbo.getRoomUnit().getBodyRotation().getValue() + 1)).iterator();
        while (it3.hasNext()) {
            HabboItem habboItem3 = (HabboItem) it3.next();
            tHashSet.add(new FloorItemOnRollerComposer(habboItem3, null, tileInFront4, INSTANCE.room.getTopHeightAt(tileInFront4.x, tileInFront4.y) - habboItem3.getZ(), INSTANCE.room).compose());
        }
        TObjectHashIterator it4 = tHashSet.iterator();
        while (it4.hasNext()) {
            INSTANCE.room.sendComposer((ServerMessage) it4.next());
        }
    }

    @Override // java.lang.Runnable
    public void run() {
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public void setHabbo(Habbo habbo) {
        this.habbo = habbo;
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
