package com.eu.habbo.messages.incoming.catalog.marketplace;

import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlace;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceOffersComposer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/marketplace/RequestOffersEvent.class */
public class RequestOffersEvent extends MessageHandler {
    public static final Map<Integer, ServerMessage> cachedResults = new ConcurrentHashMap(0);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ServerMessage serverMessage;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue3 = this.packet.readInt().intValue();
        boolean z = false;
        if (iIntValue == -1 && iIntValue2 == -1 && string.isEmpty()) {
            z = true;
        }
        if (z && (serverMessage = cachedResults.get(Integer.valueOf(iIntValue3))) != null) {
            this.client.sendResponse(serverMessage);
            return;
        }
        ServerMessage serverMessageCompose = new MarketplaceOffersComposer(MarketPlace.getOffers(iIntValue, iIntValue2, string, iIntValue3)).compose();
        if (z) {
            cachedResults.put(Integer.valueOf(iIntValue3), serverMessageCompose);
        }
        this.client.sendResponse(serverMessageCompose);
    }
}
