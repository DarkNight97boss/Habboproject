package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.users.ClubGiftReceivedComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogSelectClubGiftEvent.class */
public class CatalogSelectClubGiftEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogSelectClubGiftEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (string.isEmpty()) {
            LOGGER.error("itemName is empty");
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        if (this.client.getHabbo().getHabboStats().getRemainingClubGifts() < 1) {
            LOGGER.error("User has no remaining club gifts");
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        CatalogPage catalogPageByLayout = Emulator.getGameEnvironment().getCatalogManager().getCatalogPageByLayout(CatalogPageLayouts.club_gift.name().toLowerCase());
        if (catalogPageByLayout == null) {
            LOGGER.error("Catalog page not found");
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        CatalogItem catalogItem = (CatalogItem) catalogPageByLayout.getCatalogItems().valueCollection().stream().filter(catalogItem2 -> {
            return catalogItem2.getName().equalsIgnoreCase(string);
        }).findAny().orElse(null);
        if (catalogItem == null) {
            LOGGER.error("Catalog item not found");
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        int i = 0;
        try {
            i = Integer.parseInt(catalogItem.getExtradata());
        } catch (NumberFormatException e) {
        }
        if (i > ((int) Math.floor(((double) this.client.getHabbo().getHabboStats().getPastTimeAsClub()) / 86400.0d))) {
            LOGGER.error("Not been member for long enough");
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = catalogItem.getBaseItems().iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            if (Emulator.getGameEnvironment().getItemManager().createGift(this.client.getHabbo().getHabboInfo().getId(), item, Emulator.PREVIEW, 0, 0) != null) {
                tHashSet.add(item);
            }
        }
        this.client.getHabbo().getHabboStats().hcGiftsClaimed++;
        Emulator.getThreading().run(this.client.getHabbo().getHabboStats());
        this.client.sendResponse(new ClubGiftReceivedComposer(string, tHashSet));
    }
}
