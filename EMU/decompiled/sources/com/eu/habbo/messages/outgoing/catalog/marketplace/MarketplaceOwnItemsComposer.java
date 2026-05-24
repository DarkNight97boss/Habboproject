package com.eu.habbo.messages.outgoing.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlaceOffer;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlaceState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/marketplace/MarketplaceOwnItemsComposer.class */
public class MarketplaceOwnItemsComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceOwnItemsComposer.class);
    private final Habbo habbo;

    public MarketplaceOwnItemsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MarketplaceOwnItemsComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getInventory().getSoldPriceTotal()));
        this.response.appendInt(Integer.valueOf(this.habbo.getInventory().getMarketplaceItems().size()));
        TObjectHashIterator it = this.habbo.getInventory().getMarketplaceItems().iterator();
        while (it.hasNext()) {
            MarketPlaceOffer marketPlaceOffer = (MarketPlaceOffer) it.next();
            try {
                if (marketPlaceOffer.getState() == MarketPlaceState.OPEN && (marketPlaceOffer.getTimestamp() + 172800) - Emulator.getIntUnixTimestamp() <= 0) {
                    marketPlaceOffer.setState(MarketPlaceState.CLOSED);
                    Emulator.getThreading().run(marketPlaceOffer);
                }
                this.response.appendInt(Integer.valueOf(marketPlaceOffer.getOfferId()));
                this.response.appendInt(Integer.valueOf(marketPlaceOffer.getState().getState()));
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
                this.response.appendInt(Integer.valueOf(marketPlaceOffer.getPrice()));
                if (marketPlaceOffer.getState() == MarketPlaceState.OPEN) {
                    this.response.appendInt(Integer.valueOf(((marketPlaceOffer.getTimestamp() + 172800) - Emulator.getIntUnixTimestamp()) / 60));
                } else {
                    this.response.appendInt((Integer) 0);
                }
                this.response.appendInt((Integer) 0);
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
        return this.response;
    }
}
