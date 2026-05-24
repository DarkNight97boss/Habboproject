package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlace;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlaceOffer;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceOffersComposer.class */
public class MarketplaceOffersComposer extends MessageComposer {
    private final List<MarketPlaceOffer> offers;

    public MarketplaceOffersComposer(List<MarketPlaceOffer> list) {
        this.offers = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceOffersComposer);
        int i = 0;
        this.response.appendInt(Integer.valueOf(this.offers.size()));
        for (MarketPlaceOffer marketPlaceOffer : this.offers) {
            this.response.appendInt(Integer.valueOf(marketPlaceOffer.getOfferId()));
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(marketPlaceOffer.getType()));
            this.response.appendInt(Integer.valueOf(marketPlaceOffer.getItemId()));
            if (marketPlaceOffer.getType() == 3) {
                this.response.appendInt(Integer.valueOf(marketPlaceOffer.getLimitedNumber()));
                this.response.appendInt(Integer.valueOf(marketPlaceOffer.getLimitedStack()));
            } else if (marketPlaceOffer.getType() == 2) {
                this.response.appendString(Emulator.PREVIEW);
            } else {
                this.response.appendInt((Integer) 0);
                this.response.appendString(Emulator.PREVIEW);
            }
            this.response.appendInt(Integer.valueOf(MarketPlace.calculateCommision(marketPlaceOffer.getPrice())));
            this.response.appendInt((Integer) 0);
            this.response.appendInt(Integer.valueOf(MarketPlace.calculateCommision(marketPlaceOffer.avarage)));
            this.response.appendInt(Integer.valueOf(marketPlaceOffer.count));
            i += marketPlaceOffer.count;
        }
        this.response.appendInt(Integer.valueOf(this.offers.size()));
        return this.response;
    }
}
