package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

public class InventoryBadgesComposer extends MessageComposer {
   private final Habbo habbo;

   public InventoryBadgesComposer(Habbo habbo) {
      this.habbo = habbo;
   }

   @Override
   protected ServerMessage composeInternal() {
      if (this.habbo == null) {
         return null;
      }

      THashSet<HabboBadge> equippedBadges = new THashSet();
      this.response.init(717);
      this.response.appendInt(this.habbo.getInventory().getBadgesComponent().getBadges().size());
      TObjectHashIterator var2 = this.habbo.getInventory().getBadgesComponent().getBadges().iterator();

      while (var2.hasNext()) {
         HabboBadge badge = (HabboBadge)var2.next();
         this.response.appendInt(badge.getId());
         this.response.appendString(badge.getCode());
         if (badge.getSlot() > 0) {
            equippedBadges.add(badge);
         }
      }

      this.response.appendInt(equippedBadges.size());
      var2 = equippedBadges.iterator();

      while (var2.hasNext()) {
         HabboBadge badge = (HabboBadge)var2.next();
         this.response.appendInt(badge.getSlot());
         this.response.appendString(badge.getCode());
      }

      return this.response;
   }
}
