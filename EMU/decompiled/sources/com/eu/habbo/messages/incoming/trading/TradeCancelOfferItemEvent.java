package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeCancelOfferItemEvent.class */
public class TradeCancelOfferItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        RoomTrade activeTradeForHabbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(this.client.getHabbo());
        if (activeTradeForHabbo != null) {
            HabboItem item = activeTradeForHabbo.getRoomTradeUserForHabbo(this.client.getHabbo()).getItem(iIntValue);
            if (activeTradeForHabbo.getRoomTradeUserForHabbo(this.client.getHabbo()).getAccepted() || item == null) {
                return;
            }
            activeTradeForHabbo.removeItem(this.client.getHabbo(), item);
        }
    }
}
