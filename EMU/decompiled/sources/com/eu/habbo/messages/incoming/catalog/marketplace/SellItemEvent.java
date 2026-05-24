package com.eu.habbo.messages.incoming.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlace;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceItemPostedComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/marketplace/SellItemEvent.class */
public class SellItemEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellItemEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!MarketPlace.MARKETPLACE_ENABLED) {
            this.client.sendResponse(new MarketplaceItemPostedComposer(3));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        this.packet.readInt().intValue();
        HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue());
        if (habboItem != null) {
            if (!habboItem.getBaseItem().allowMarketplace()) {
                String strReplace = Emulator.getTexts().getValue("scripter.warning.marketplace.forbidden").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%itemname%", habboItem.getBaseItem().getName()).replace("%credits%", iIntValue + Emulator.PREVIEW);
                ScripterManager.scripterDetected(this.client, strReplace);
                LOGGER.info(strReplace);
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                return;
            }
            if (iIntValue < 0) {
                String strReplace2 = Emulator.getTexts().getValue("scripter.warning.marketplace.negative").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%itemname%", habboItem.getBaseItem().getName()).replace("%credits%", iIntValue + Emulator.PREVIEW);
                ScripterManager.scripterDetected(this.client, strReplace2);
                LOGGER.info(strReplace2);
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                return;
            }
            if (MarketPlace.sellItem(this.client, habboItem, iIntValue)) {
                this.client.sendResponse(new MarketplaceItemPostedComposer(1));
            } else {
                this.client.sendResponse(new MarketplaceItemPostedComposer(2));
            }
        }
    }
}
