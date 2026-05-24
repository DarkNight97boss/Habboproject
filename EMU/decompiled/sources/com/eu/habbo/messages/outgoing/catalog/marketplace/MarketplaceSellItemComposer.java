package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceSellItemComposer.class */
public class MarketplaceSellItemComposer extends MessageComposer {
    public static final int NOT_ALLOWED = 2;
    public static final int NO_TRADE_PASS = 3;
    public static final int NO_ADS_LEFT = 4;
    private final int errorCode;
    private final int valueA;
    private final int valueB;

    public MarketplaceSellItemComposer(int i, int i2, int i3) {
        this.errorCode = i;
        this.valueA = i2;
        this.valueB = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(54);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendInt(Integer.valueOf(this.valueA));
        this.response.appendInt(Integer.valueOf(this.valueB));
        return this.response;
    }
}
