package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Map.Entry;

public class RecyclerLogicComposer extends MessageComposer {
   @Override
   protected ServerMessage composeInternal() {
      this.response.init(3164);
      this.response.appendInt(Emulator.getGameEnvironment().getCatalogManager().prizes.size());

      for (Entry<Integer, THashSet<Item>> map : Emulator.getGameEnvironment().getCatalogManager().prizes.entrySet()) {
         this.response.appendInt(map.getKey());
         this.response.appendInt(Integer.valueOf(Emulator.getConfig().getValue("hotel.ecotron.rarity.chance." + map.getKey())));
         this.response.appendInt(map.getValue().size());
         TObjectHashIterator var3 = map.getValue().iterator();

         while (var3.hasNext()) {
            Item item = (Item)var3.next();
            this.response.appendString(item.getName());
            this.response.appendInt(1);
            this.response.appendString(item.getType().code.toLowerCase());
            this.response.appendInt(item.getSpriteId());
         }
      }

      return this.response;
   }
}
