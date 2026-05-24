package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

public class ClubGiftReceivedComposer extends MessageComposer {
   private final String name;
   private final THashSet<Item> items;

   public ClubGiftReceivedComposer(String name, THashSet<Item> items) {
      this.name = name;
      this.items = items;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(659);
      this.response.appendString(this.name);
      this.response.appendInt(this.items.size());
      TObjectHashIterator var1 = this.items.iterator();

      while (var1.hasNext()) {
         Item item = (Item)var1.next();
         item.serialize(this.response);
      }

      return this.response;
   }
}
