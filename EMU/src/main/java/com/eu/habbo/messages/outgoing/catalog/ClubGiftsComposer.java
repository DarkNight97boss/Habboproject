package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClubGiftsComposer extends MessageComposer {
   private final int daysTillNextGift;
   private final int availableGifts;
   private final int daysAsHc;

   public ClubGiftsComposer(int daysTillNextGift, int availableGifts, int daysAsHc) {
      this.daysTillNextGift = daysTillNextGift;
      this.availableGifts = availableGifts;
      this.daysAsHc = daysAsHc;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(619);
      this.response.appendInt(this.daysTillNextGift);
      this.response.appendInt(this.availableGifts);
      CatalogPage page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPageByLayout(CatalogPageLayouts.club_gift.name().toLowerCase());
      if (page != null) {
         List<CatalogItem> items = new ArrayList<>(page.getCatalogItems().valueCollection());
         Collections.sort(items);
         this.response.appendInt(items.size());

         for (CatalogItem item : items) {
            item.serialize(this.response);
         }

         this.response.appendInt(items.size());

         for (CatalogItem item : items) {
            int daysRequired = 0;

            try {
               daysRequired = Integer.parseInt(item.getExtradata());
            } catch (NumberFormatException var7) {
            }

            this.response.appendInt(item.getId());
            this.response.appendBoolean(item.isClubOnly());
            this.response.appendInt(daysRequired);
            this.response.appendBoolean(daysRequired <= this.daysAsHc);
         }
      } else {
         this.response.appendInt(0);
         this.response.appendInt(0);
      }

      return this.response;
   }
}
