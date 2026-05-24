package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeOfferMultipleItemsEvent.class */
public class TradeOfferMultipleItemsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTrade activeTradeForHabbo;
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (activeTradeForHabbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(this.client.getHabbo())) == null) {
            return;
        }
        THashSet<HabboItem> tHashSet = new THashSet<>();
        int iIntValue = this.packet.readInt().intValue();
        for (int i = 0; i < iIntValue; i++) {
            HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue());
            if (habboItem != null && habboItem.getBaseItem().allowTrade()) {
                tHashSet.add(habboItem);
            }
        }
        activeTradeForHabbo.offerMultipleItems(this.client.getHabbo(), tHashSet);
    }
}
