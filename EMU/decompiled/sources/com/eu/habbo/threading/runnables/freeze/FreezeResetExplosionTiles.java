package com.eu.habbo.threading.runnables.freeze;

import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/freeze/FreezeResetExplosionTiles.class */
class FreezeResetExplosionTiles implements Runnable {
    private final THashSet<InteractionFreezeTile> tiles;
    private final Room room;

    public FreezeResetExplosionTiles(THashSet<InteractionFreezeTile> tHashSet, Room room) {
        this.tiles = tHashSet;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        TObjectHashIterator it = this.tiles.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            habboItem.setExtradata("0");
            this.room.updateItem(habboItem);
        }
    }
}
