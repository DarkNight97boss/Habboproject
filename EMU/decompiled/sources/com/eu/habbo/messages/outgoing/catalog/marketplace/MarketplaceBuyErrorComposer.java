package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceBuyErrorComposer.class */
public class MarketplaceBuyErrorComposer extends MessageComposer {
    public static final int REFRESH = 1;
    public static final int SOLD_OUT = 2;
    public static final int UPDATES = 3;
    public static final int NOT_ENOUGH_CREDITS = 4;
    private final int errorCode;
    private final int unknown;
    private final int offerId;
    private final int price;

    public MarketplaceBuyErrorComposer(int i, int i2, int i3, int i4) {
        this.errorCode = i;
        this.unknown = i2;
        this.offerId = i3;
        this.price = i4;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceBuyErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendInt(Integer.valueOf(this.unknown));
        this.response.appendInt(Integer.valueOf(this.offerId));
        this.response.appendInt(Integer.valueOf(this.price));
        return this.response;
    }
}
