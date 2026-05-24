package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionRentableSpace;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/ClearRentedSpace.class */
public class ClearRentedSpace implements Runnable {
    private final InteractionRentableSpace item;
    private final Room room;

    public ClearRentedSpace(InteractionRentableSpace interactionRentableSpace, Room room) {
        this.item = interactionRentableSpace;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.room.getLayout().getTilesAt(this.room.getLayout().getTile(this.item.getX(), this.item.getY()), this.item.getBaseItem().getWidth(), this.item.getBaseItem().getLength(), this.item.getRotation()).iterator();
        while (it.hasNext()) {
            TObjectHashIterator it2 = this.room.getItemsAt((RoomTile) it.next()).iterator();
            while (it2.hasNext()) {
                HabboItem habboItem = (HabboItem) it2.next();
                if (habboItem.getUserId() == this.item.getRenterId()) {
                    tHashSet.add(habboItem);
                    habboItem.setRoomId(0);
                    habboItem.needsUpdate(true);
                }
            }
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.item.getRenterId());
        if (habbo != null) {
            habbo.getClient().sendResponse(new AddHabboItemComposer((THashSet<HabboItem>) tHashSet));
            habbo.getHabboStats().rentedItemId = 0;
            habbo.getHabboStats().rentedTimeEnd = 0;
        } else {
            TObjectHashIterator it3 = tHashSet.iterator();
            while (it3.hasNext()) {
                ((HabboItem) it3.next()).run();
            }
        }
    }
}
