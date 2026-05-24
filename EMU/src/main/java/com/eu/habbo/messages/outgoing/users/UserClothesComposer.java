package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClothItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.procedure.TIntProcedure;
import java.util.ArrayList;

public class UserClothesComposer extends MessageComposer {
   private final ArrayList<Integer> idList = new ArrayList<>();
   private final ArrayList<String> nameList = new ArrayList<>();

   public UserClothesComposer(Habbo habbo) {
      habbo.getInventory().getWardrobeComponent().getClothing().forEach(new TIntProcedure() {
         public boolean execute(int value) {
            ClothItem item = (ClothItem)Emulator.getGameEnvironment().getCatalogManager().clothing.get(value);
            if (item != null) {
               int[] var3 = item.setId;
               int var4 = var3.length;

               for (int var5 = 0; var5 < var4; var5++) {
                  Integer j = var3[var5];
                  UserClothesComposer.this.idList.add(j);
               }

               UserClothesComposer.this.nameList.add(item.name);
            }

            return true;
         }
      });
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(1450);
      this.response.appendInt(this.idList.size());
      this.idList.forEach(this.response::appendInt);
      this.response.appendInt(this.nameList.size());
      this.nameList.forEach(this.response::appendString);
      return this.response;
   }
}
