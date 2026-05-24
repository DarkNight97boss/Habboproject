package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeCancelEvent.class */
public class TradeCancelEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTrade activeTradeForHabbo;
        Habbo habbo = this.client.getHabbo();
        Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (activeTradeForHabbo = currentRoom.getActiveTradeForHabbo(habbo)) == null) {
            return;
        }
        activeTradeForHabbo.stopTrade(habbo);
        currentRoom.stopTrade(activeTradeForHabbo);
    }
}
