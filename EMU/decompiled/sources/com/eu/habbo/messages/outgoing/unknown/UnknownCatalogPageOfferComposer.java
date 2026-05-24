package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownCatalogPageOfferComposer.class */
public class UnknownCatalogPageOfferComposer extends MessageComposer {
    private final int pageId;
    private final CatalogItem catalogItem;

    public UnknownCatalogPageOfferComposer(int i, CatalogItem catalogItem) {
        this.pageId = i;
        this.catalogItem = catalogItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownCatalogPageOfferComposer);
        this.response.appendInt(Integer.valueOf(this.pageId));
        this.catalogItem.serialize(this.response);
        return this.response;
    }
}
