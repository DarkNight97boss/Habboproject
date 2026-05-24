package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/AlertPurchaseFailedComposer.class */
public class AlertPurchaseFailedComposer extends MessageComposer {
    public static final int SERVER_ERROR = 0;
    public static final int ALREADY_HAVE_BADGE = 1;
    private final int error;

    public AlertPurchaseFailedComposer(int i) {
        this.error = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AlertPurchaseFailedComposer);
        this.response.appendInt(Integer.valueOf(this.error));
        return this.response;
    }
}
