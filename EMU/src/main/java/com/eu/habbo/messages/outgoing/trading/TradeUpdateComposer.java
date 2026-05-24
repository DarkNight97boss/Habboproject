package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.rooms.RoomTradeUser;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;

public class TradeUpdateComposer extends MessageComposer {
   private final RoomTrade roomTrade;

   public TradeUpdateComposer(RoomTrade roomTrade) {
      this.roomTrade = roomTrade;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2024);

      for (RoomTradeUser roomTradeUser : this.roomTrade.getRoomTradeUsers()) {
         this.response.appendInt(roomTradeUser.getUserId());
         this.response.appendInt(roomTradeUser.getItems().size());
         TObjectHashIterator var3 = roomTradeUser.getItems().iterator();

         while (var3.hasNext()) {
            HabboItem item = (HabboItem)var3.next();
            this.response.appendInt(item.getId());
            this.response.appendString(item.getBaseItem().getType().code);
            this.response.appendInt(item.getId());
            this.response.appendInt(item.getBaseItem().getSpriteId());
            this.response.appendInt(0);
            this.response.appendBoolean(item.getBaseItem().allowInventoryStack() && !item.isLimited());
            item.serializeExtradata(this.response);
            this.response.appendInt(0);
            this.response.appendInt(0);
            this.response.appendInt(0);
            if (item.getBaseItem().getType() == FurnitureType.FLOOR) {
               this.response.appendInt(0);
            }
         }

         this.response.appendInt(roomTradeUser.getItems().size());
         this.response.appendInt(roomTradeUser.getItems().stream().mapToInt(RoomTrade::getCreditsByItem).sum());
      }

      return this.response;
   }
}
