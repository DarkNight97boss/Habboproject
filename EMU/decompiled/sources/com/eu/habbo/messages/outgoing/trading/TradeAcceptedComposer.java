package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.habbohotel.rooms.RoomTradeUser;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/trading/TradeAcceptedComposer.class */
public class TradeAcceptedComposer extends MessageComposer {
    private final RoomTradeUser tradeUser;

    public TradeAcceptedComposer(RoomTradeUser roomTradeUser) {
        this.tradeUser = roomTradeUser;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TradeAcceptedComposer);
        this.response.appendInt(Integer.valueOf(this.tradeUser.getUserId()));
        this.response.appendInt(Boolean.valueOf(this.tradeUser.getAccepted()));
        return this.response;
    }
}
