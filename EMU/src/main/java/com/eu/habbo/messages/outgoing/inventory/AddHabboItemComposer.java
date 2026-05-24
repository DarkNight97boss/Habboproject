package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AddHabboItemComposer extends MessageComposer {
   private THashSet<HabboItem> itemsList;
   private HabboItem item;
   private int[] ids;
   private AddHabboItemComposer.AddHabboItemCategory category;
   private Map<AddHabboItemComposer.AddHabboItemCategory, List<Integer>> entries;

   public AddHabboItemComposer(THashSet<HabboItem> itemsList) {
      this.itemsList = itemsList;
      this.category = AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI;
   }

   public AddHabboItemComposer(HabboItem item) {
      this.item = item;
      this.category = AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI;
   }

   public AddHabboItemComposer(int[] ids, AddHabboItemComposer.AddHabboItemCategory category) {
      this.ids = ids;
      this.category = category;
   }

   public AddHabboItemComposer(int id, AddHabboItemComposer.AddHabboItemCategory category) {
      this.ids = new int[]{id};
      this.category = category;
   }

   public AddHabboItemComposer(Map<AddHabboItemComposer.AddHabboItemCategory, List<Integer>> entries) {
      this.entries = entries;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2103);
      if (this.ids != null) {
         this.response.appendInt(1);
         this.response.appendInt(this.category.number);
         this.response.appendInt(this.ids.length);

         for (int id : this.ids) {
            this.response.appendInt(id);
         }
      } else if (this.entries != null) {
         this.response.appendInt(this.entries.size());

         for (Entry<AddHabboItemComposer.AddHabboItemCategory, List<Integer>> item : this.entries.entrySet()) {
            this.response.appendInt(item.getKey().number);
            this.response.appendInt(item.getValue().size());

            for (int id : item.getValue()) {
               this.response.appendInt(id);
            }
         }
      } else if (this.item == null) {
         this.response.appendInt(1);
         this.response.appendInt(1);
         this.response.appendInt(this.itemsList.size());
         TObjectHashIterator var6 = this.itemsList.iterator();

         while (var6.hasNext()) {
            HabboItem habboItem = (HabboItem)var6.next();
            this.response.appendInt(habboItem.getId());
         }
      } else {
         this.response.appendInt(1);
         this.response.appendInt(1);
         this.response.appendInt(1);
         this.response.appendInt(this.item.getId());
      }

      return this.response;
   }

   public enum AddHabboItemCategory {
      OWNED_FURNI(1),
      RENTED_FURNI(2),
      PET(3),
      BADGE(4),
      BOT(5),
      GAME(6);

      public final int number;

      AddHabboItemCategory(int number) {
         this.number = number;
      }
   }
}
