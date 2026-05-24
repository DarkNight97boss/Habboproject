package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeConfirmEvent.class */
public class TradeConfirmEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo = this.client.getHabbo();
        RoomTrade activeTradeForHabbo = habbo.getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(habbo);
        if (activeTradeForHabbo == null || !activeTradeForHabbo.getRoomTradeUserForHabbo(habbo).getAccepted()) {
            return;
        }
        activeTradeForHabbo.confirm(habbo);
    }
}
