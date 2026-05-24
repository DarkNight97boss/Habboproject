package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/CatalogSearchResultComposer.class */
public class CatalogSearchResultComposer extends MessageComposer {
    private final CatalogItem item;

    public CatalogSearchResultComposer(CatalogItem catalogItem) {
        this.item = catalogItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CatalogSearchResultComposer);
        this.item.serialize(this.response);
        return this.response;
    }
}
