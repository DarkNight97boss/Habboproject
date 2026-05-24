package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeAcceptEvent.class */
public class TradeAcceptEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTrade activeTradeForHabbo;
        Habbo habbo = this.client.getHabbo();
        if (habbo == null || habbo.getHabboInfo() == null || habbo.getHabboInfo().getCurrentRoom() == null || (activeTradeForHabbo = habbo.getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(habbo)) == null) {
            return;
        }
        activeTradeForHabbo.accept(habbo, true);
    }
}
