package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.habbohotel.catalog.TargetOffer;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.cache.HabboOfferPurchase;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/TargetedOfferComposer.class */
public class TargetedOfferComposer extends MessageComposer {
    private final Habbo habbo;
    private final TargetOffer offer;

    public TargetedOfferComposer(Habbo habbo, TargetOffer targetOffer) {
        this.habbo = habbo;
        this.offer = targetOffer;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TargetedOfferComposer);
        this.offer.serialize(this.response, HabboOfferPurchase.getOrCreate(this.habbo, this.offer.getId()));
        return this.response;
    }
}
