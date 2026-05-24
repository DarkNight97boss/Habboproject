package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public class RoomWallItemsComposer extends MessageComposer {
   private final Room room;

   public RoomWallItemsComposer(Room room) {
      this.room = room;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(1369);
      THashMap<Integer, String> userNames = new THashMap();
      TIntObjectMap<String> furniOwnerNames = this.room.getFurniOwnerNames();
      TIntObjectIterator<String> iterator = furniOwnerNames.iterator();
      int i = furniOwnerNames.size();

      while (i-- > 0) {
         try {
            iterator.advance();
            userNames.put(iterator.key(), (String)iterator.value());
         } catch (NoSuchElementException e) {
            break;
         }
      }

      this.response.appendInt(userNames.size());

      for (Entry<Integer, String> set : userNames.entrySet()) {
         this.response.appendInt(set.getKey());
         this.response.appendString(set.getValue());
      }

      THashSet<HabboItem> items = this.room.getWallItems();
      this.response.appendInt(items.size());
      TObjectHashIterator var10 = items.iterator();

      while (var10.hasNext()) {
         HabboItem item = (HabboItem)var10.next();
         item.serializeWallData(this.response);
      }

      return this.response;
   }
}
