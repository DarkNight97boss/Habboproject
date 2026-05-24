package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/AlertPurchaseUnavailableComposer.class */
public class AlertPurchaseUnavailableComposer extends MessageComposer {
    public static final int ILLEGAL = 0;
    public static final int REQUIRES_CLUB = 1;
    private final int code;

    public AlertPurchaseUnavailableComposer(int i) {
        this.code = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AlertPurchaseUnavailableComposer);
        this.response.appendInt(Integer.valueOf(this.code));
        return this.response;
    }
}
