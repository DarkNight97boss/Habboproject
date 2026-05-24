package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/PurchaseOKComposer.class */
public class PurchaseOKComposer extends MessageComposer {
    private final CatalogItem catalogItem;

    public PurchaseOKComposer(CatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }

    public PurchaseOKComposer() {
        this.catalogItem = null;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(869);
        if (this.catalogItem != null) {
            this.catalogItem.serialize(this.response);
        } else {
            this.response.appendInt((Integer) 0);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendBoolean(false);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendBoolean(true);
            this.response.appendInt((Integer) 1);
            this.response.appendString("s");
            this.response.appendInt((Integer) 0);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 0);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 1);
        }
        return this.response;
    }
}
