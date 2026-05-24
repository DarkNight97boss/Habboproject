package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.habbohotel.users.cache.HabboOfferPurchase;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/TargetOfferStateEvent.class */
public class TargetOfferStateEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        HabboOfferPurchase habboOfferPurchase = this.client.getHabbo().getHabboStats().getHabboOfferPurchase(iIntValue);
        if (habboOfferPurchase != null) {
            habboOfferPurchase.setState(iIntValue2);
        }
    }
}
