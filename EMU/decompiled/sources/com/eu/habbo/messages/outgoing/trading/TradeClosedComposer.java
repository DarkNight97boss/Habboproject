package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/trading/TradeClosedComposer.class */
public class TradeClosedComposer extends MessageComposer {
    public static final int USER_CANCEL_TRADE = 0;
    public static final int ITEMS_NOT_FOUND = 1;
    private final int userId;
    private final int errorCode;

    public TradeClosedComposer(int i, int i2) {
        this.userId = i;
        this.errorCode = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TradeStoppedComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
