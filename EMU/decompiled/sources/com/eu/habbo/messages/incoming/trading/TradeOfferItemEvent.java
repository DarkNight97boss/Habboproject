package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeOfferItemEvent.class */
public class TradeOfferItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTrade activeTradeForHabbo;
        HabboItem habboItem;
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (activeTradeForHabbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(this.client.getHabbo())) == null || (habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue())) == null || !habboItem.getBaseItem().allowTrade()) {
            return;
        }
        activeTradeForHabbo.offerItem(this.client.getHabbo(), habboItem);
    }
}
