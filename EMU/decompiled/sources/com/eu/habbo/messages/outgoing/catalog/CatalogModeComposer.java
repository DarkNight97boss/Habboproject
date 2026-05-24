package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/CatalogModeComposer.class */
public class CatalogModeComposer extends MessageComposer {
    private final int mode;

    public CatalogModeComposer(int i) {
        this.mode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CatalogModeComposer);
        this.response.appendInt(Integer.valueOf(this.mode));
        return this.response;
    }
}
