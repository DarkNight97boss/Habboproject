package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/RentableItemBuyOutPriceComposer.class */
public class RentableItemBuyOutPriceComposer extends MessageComposer {
    private final boolean unknownBoolean1;
    private final String unknownString1;
    private final boolean unknownBoolean2;
    private final int credits;
    private final int points;
    private final int pointsType;

    public RentableItemBuyOutPriceComposer(boolean z, String str, boolean z2, int i, int i2, int i3) {
        this.unknownBoolean1 = z;
        this.unknownString1 = str;
        this.unknownBoolean2 = z2;
        this.credits = i;
        this.points = i2;
        this.pointsType = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(35);
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean1));
        this.response.appendString(this.unknownString1);
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean2));
        this.response.appendInt(Integer.valueOf(this.credits));
        this.response.appendInt(Integer.valueOf(this.points));
        this.response.appendInt(Integer.valueOf(this.pointsType));
        return this.response;
    }
}
