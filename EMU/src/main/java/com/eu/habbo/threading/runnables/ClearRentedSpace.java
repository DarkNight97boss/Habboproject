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

public class ClearRentedSpace implements Runnable {
   private final InteractionRentableSpace item;
   private final Room room;

   public ClearRentedSpace(InteractionRentableSpace item, Room room) {
      this.item = item;
      this.room = room;
   }

   @Override
   public void run() {
      THashSet<HabboItem> items = new THashSet();
      TObjectHashIterator owner = this.room
         .getLayout()
         .getTilesAt(
            this.room.getLayout().getTile(this.item.getX(), this.item.getY()),
            this.item.getBaseItem().getWidth(),
            this.item.getBaseItem().getLength(),
            this.item.getRotation()
         )
         .iterator();

      while (owner.hasNext()) {
         RoomTile t = (RoomTile)owner.next();
         TObjectHashIterator i = this.room.getItemsAt(t).iterator();

         while (i.hasNext()) {
            HabboItem ix = (HabboItem)i.next();
            if (ix.getUserId() == this.item.getRenterId()) {
               items.add(ix);
               ix.setRoomId(0);
               ix.needsUpdate(true);
            }
         }
      }

      Habbo ownerx = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.item.getRenterId());
      if (ownerx != null) {
         ownerx.getClient().sendResponse(new AddHabboItemComposer(items));
         ownerx.getHabboStats().rentedItemId = 0;
         ownerx.getHabboStats().rentedTimeEnd = 0;
      } else {
         TObjectHashIterator var7 = items.iterator();

         while (var7.hasNext()) {
            HabboItem i = (HabboItem)var7.next();
            i.run();
         }
      }
   }
}
