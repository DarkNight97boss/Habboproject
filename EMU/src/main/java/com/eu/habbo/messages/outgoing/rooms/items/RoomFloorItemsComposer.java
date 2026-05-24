package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.hash.THashSet;
import java.util.NoSuchElementException;

public class RoomFloorItemsComposer extends MessageComposer {
   private final TIntObjectMap<String> furniOwnerNames;
   private final THashSet<? extends HabboItem> items;

   public RoomFloorItemsComposer(TIntObjectMap<String> furniOwnerNames, THashSet<? extends HabboItem> items) {
      this.furniOwnerNames = furniOwnerNames;
      this.items = items;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(1778);
      TIntObjectIterator<String> iterator = this.furniOwnerNames.iterator();
      this.response.appendInt(this.furniOwnerNames.size());
      int i = this.furniOwnerNames.size();

      while (i-- > 0) {
         try {
            iterator.advance();
            this.response.appendInt(iterator.key());
            this.response.appendString((String)iterator.value());
         } catch (NoSuchElementException e) {
            break;
         }
      }

      this.response.appendInt(this.items.size());
      TObjectHashIterator var5 = this.items.iterator();

      while (var5.hasNext()) {
         HabboItem item = (HabboItem)var5.next();
         item.serializeFloorData(this.response);
         this.response
            .appendInt(
               item instanceof InteractionGift
                  ? ((InteractionGift)item).getColorId() * 1000 + ((InteractionGift)item).getRibbonId()
                  : (item instanceof InteractionMusicDisc ? ((InteractionMusicDisc)item).getSongId() : 1)
            );
         item.serializeExtradata(this.response);
         this.response.appendInt(-1);
         this.response.appendInt(item.isUsable() ? 1 : 0);
         this.response.appendInt(item.getUserId());
      }

      return this.response;
   }
}
