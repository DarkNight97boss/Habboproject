package com.eu.habbo.habbohotel.items.interactions.totems;

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

public class InteractionTotemHead extends InteractionDefault {
   public InteractionTotemHead(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
   }

   public InteractionTotemHead(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
   }

   public TotemType getTotemType() {
      int extraData;
      try {
         extraData = Integer.parseInt(this.getExtradata());
      } catch (NumberFormatException ex) {
         extraData = 0;
      }

      return extraData < 3 ? TotemType.fromInt(extraData + 1) : TotemType.fromInt((int)Math.ceil((extraData - 2) / 4.0F));
   }

   public TotemColor getTotemColor() {
      int extraData;
      try {
         extraData = Integer.parseInt(this.getExtradata());
      } catch (NumberFormatException ex) {
         extraData = 0;
      }

      return extraData < 3 ? TotemColor.NONE : TotemColor.fromInt(extraData - 3 - 4 * (this.getTotemType().type - 1));
   }

   private void update(Room room, RoomTile tile) {
      InteractionTotemLegs legs = null;
      TObjectHashIterator var4 = room.getItemsAt(tile).iterator();

      while (var4.hasNext()) {
         HabboItem item = (HabboItem)var4.next();
         if (item instanceof InteractionTotemLegs && item.getZ() < this.getZ()) {
            legs = (InteractionTotemLegs)item;
         }
      }

      if (legs != null) {
         this.setExtradata(4 * this.getTotemType().type + legs.getTotemColor().color - 1 + "");
      }
   }

   public void updateTotemState(Room room) {
      this.updateTotemState(room, room.getLayout().getTile(this.getX(), this.getY()));
   }

   public void updateTotemState(Room room, RoomTile tile) {
      this.setExtradata(this.getTotemType().type - 1 + "");
      this.update(room, tile);
      this.needsUpdate(true);
      room.updateItem(this);
   }

   @Override
   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      if (client != null && room != null && room.hasRights(client.getHabbo()) || objects.length >= 2 && objects[1] instanceof WiredEffectType) {
         TotemType newType = TotemType.fromInt(this.getTotemType().type + 1);
         if (newType == TotemType.NONE) {
            newType = TotemType.TROLL;
         }

         this.setExtradata(newType.type - 1 + "");
         this.updateTotemState(room);
      }
   }

   @Override
   public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
      super.onMove(room, oldLocation, newLocation);
      this.updateTotemState(room, newLocation);
   }
}
