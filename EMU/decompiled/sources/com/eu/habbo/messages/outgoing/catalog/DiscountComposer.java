package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/DiscountComposer.class */
public class DiscountComposer extends MessageComposer {
    public static int MAXIMUM_ALLOWED_ITEMS = 100;
    public static int DISCOUNT_BATCH_SIZE = 6;
    public static int DISCOUNT_AMOUNT_PER_BATCH = 1;
    public static int MINIMUM_DISCOUNTS_FOR_BONUS = 1;
    public static int[] ADDITIONAL_DISCOUNT_THRESHOLDS = {40, 99};

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.DiscountComposer);
        this.response.appendInt(Integer.valueOf(MAXIMUM_ALLOWED_ITEMS));
        this.response.appendInt(Integer.valueOf(DISCOUNT_BATCH_SIZE));
        this.response.appendInt(Integer.valueOf(DISCOUNT_AMOUNT_PER_BATCH));
        this.response.appendInt(Integer.valueOf(MINIMUM_DISCOUNTS_FOR_BONUS));
        this.response.appendInt(Integer.valueOf(ADDITIONAL_DISCOUNT_THRESHOLDS.length));
        for (int i : ADDITIONAL_DISCOUNT_THRESHOLDS) {
            this.response.appendInt(Integer.valueOf(i));
        }
        return this.response;
    }
}
