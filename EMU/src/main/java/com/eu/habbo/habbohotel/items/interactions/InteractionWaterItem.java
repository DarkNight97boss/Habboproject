package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionWaterItem extends InteractionMultiHeight {
   public InteractionWaterItem(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
   }

   public InteractionWaterItem(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
   }

   @Override
   public void onPlace(Room room) {
      this.update();
      super.onPlace(room);
   }

   @Override
   public void onPickUp(Room room) {
      super.onPickUp(room);
      this.setExtradata("0");
      this.needsUpdate(true);
   }

   @Override
   public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
      super.onMove(room, oldLocation, newLocation);
      this.update();
   }

   @Override
   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      super.onClick(client, room, new Object[0]);
   }

   public void update() {
      Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
      if (room != null) {
         Rectangle rectangle = this.getRectangle();
         boolean foundWater = true;

         for (short x = (short)rectangle.x; x < rectangle.getWidth() + rectangle.x && foundWater; x++) {
            for (short y = (short)rectangle.y; y < rectangle.getHeight() + rectangle.y && foundWater; y++) {
               boolean tile = false;
               THashSet<HabboItem> items = room.getItemsAt(room.getLayout().getTile(x, y));
               TObjectHashIterator var8 = items.iterator();

               while (true) {
                  if (var8.hasNext()) {
                     HabboItem item = (HabboItem)var8.next();
                     if (!(item instanceof InteractionWater)) {
                        continue;
                     }

                     tile = true;
                  }

                  if (!tile) {
                     foundWater = false;
                  }
                  break;
               }
            }
         }

         String updatedData = foundWater ? "1" : "0";
         if (!this.getExtradata().equals(updatedData)) {
            this.setExtradata(updatedData);
            this.needsUpdate(true);
            room.updateItemState(this);
         }
      }
   }

   @Override
   public boolean allowWiredResetState() {
      return false;
   }
}
