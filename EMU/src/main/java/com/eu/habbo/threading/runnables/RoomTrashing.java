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
   public static void onUserWalkEvent(UserTakeStepEvent event) {
      if (INSTANCE != null) {
         if (INSTANCE.habbo != null) {
            if (!INSTANCE.habbo.isOnline()) {
               INSTANCE.habbo = null;
            }

            if (INSTANCE.habbo == event.habbo && event.habbo.getHabboInfo().getCurrentRoom() != null) {
               if (event.habbo.getHabboInfo().getCurrentRoom().equals(INSTANCE.room)) {
                  THashSet<ServerMessage> messages = new THashSet();
                  THashSet<HabboItem> items = INSTANCE.room.getItemsAt(event.toLocation);
                  int offset = Emulator.getRandom().nextInt(4) + 2;
                  RoomTile t = null;

                  while (offset > 0) {
                     t = INSTANCE.room
                        .getLayout()
                        .getTileInFront(
                           INSTANCE.room.getLayout().getTile(event.toLocation.x, event.toLocation.y),
                           event.habbo.getRoomUnit().getBodyRotation().getValue(),
                           (short)offset
                        );
                     if (INSTANCE.room.getLayout().tileWalkable(t.x, t.y)) {
                        break;
                     }

                     offset--;
                  }

                  TObjectHashIterator s = items.iterator();

                  while (s.hasNext()) {
                     HabboItem item = (HabboItem)s.next();
                     double offsetZ = INSTANCE.room.getTopHeightAt(t.x, t.y) - item.getZ();
                     messages.add(new FloorItemOnRollerComposer(item, null, t, offsetZ, INSTANCE.room).compose());
                  }

                  offset = Emulator.getRandom().nextInt(4) + 2;
                  t = null;

                  while (offset > 0) {
                     t = INSTANCE.room
                        .getLayout()
                        .getTileInFront(
                           INSTANCE.room.getLayout().getTile(event.toLocation.x, event.toLocation.y),
                           event.habbo.getRoomUnit().getBodyRotation().getValue() + 7,
                           (short)offset
                        );
                     if (INSTANCE.room.getLayout().tileWalkable(t.x, t.y)) {
                        break;
                     }

                     offset--;
                  }

                  RoomTile sx = INSTANCE.room
                     .getLayout()
                     .getTileInFront(INSTANCE.habbo.getRoomUnit().getCurrentLocation(), INSTANCE.habbo.getRoomUnit().getBodyRotation().getValue() + 7);
                  if (sx != null) {
                     items = INSTANCE.room.getItemsAt(sx);
                  }

                  TObjectHashIterator var17 = items.iterator();

                  while (var17.hasNext()) {
                     HabboItem item = (HabboItem)var17.next();
                     double offsetZ = INSTANCE.room.getTopHeightAt(t.x, t.y) - item.getZ();
                     messages.add(new FloorItemOnRollerComposer(item, null, t, offsetZ, INSTANCE.room).compose());
                  }

                  offset = Emulator.getRandom().nextInt(4) + 2;
                  t = null;

                  while (offset > 0) {
                     t = INSTANCE.getRoom()
                        .getLayout()
                        .getTileInFront(event.toLocation, event.habbo.getRoomUnit().getBodyRotation().getValue() + 1, (short)offset);
                     if (INSTANCE.room.getLayout().tileWalkable(t.x, t.y)) {
                        break;
                     }

                     offset--;
                  }

                  RoomTile var16 = INSTANCE.getRoom()
                     .getLayout()
                     .getTileInFront(INSTANCE.habbo.getRoomUnit().getCurrentLocation(), INSTANCE.habbo.getRoomUnit().getBodyRotation().getValue() + 1);
                  items = INSTANCE.room.getItemsAt(var16);
                  var17 = items.iterator();

                  while (var17.hasNext()) {
                     HabboItem item = (HabboItem)var17.next();
                     double offsetZ = INSTANCE.room.getTopHeightAt(t.x, t.y) - item.getZ();
                     messages.add(new FloorItemOnRollerComposer(item, null, t, offsetZ, INSTANCE.room).compose());
                  }

                  var17 = messages.iterator();

                  while (var17.hasNext()) {
                     ServerMessage message = (ServerMessage)var17.next();
                     INSTANCE.room.sendComposer(message);
                  }
               } else {
                  INSTANCE.habbo = null;
                  INSTANCE.room = null;
               }
            }
         }
      }
   }

   @Override
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
