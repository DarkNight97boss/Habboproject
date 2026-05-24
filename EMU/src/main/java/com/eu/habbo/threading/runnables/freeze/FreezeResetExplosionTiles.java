package com.eu.habbo.threading.runnables.freeze;

import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

class FreezeResetExplosionTiles implements Runnable {
   private final THashSet<InteractionFreezeTile> tiles;
   private final Room room;

   public FreezeResetExplosionTiles(THashSet<InteractionFreezeTile> tiles, Room room) {
      this.tiles = tiles;
      this.room = room;
   }

   @Override
   public void run() {
      TObjectHashIterator var1 = this.tiles.iterator();

      while (var1.hasNext()) {
         HabboItem item = (HabboItem)var1.next();
         item.setExtradata("0");
         this.room.updateItem(item);
      }
   }
}
