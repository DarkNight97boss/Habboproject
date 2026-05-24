package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.catalog.TargetOffer;
import com.eu.habbo.habbohotel.users.cache.HabboOfferPurchase;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/PurchaseTargetOfferEvent.class */
public class PurchaseTargetOfferEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        if (iIntValue2 > 0 && ((long) Emulator.getIntUnixTimestamp()) - this.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
            this.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
            TargetOffer targetOffer = Emulator.getGameEnvironment().getCatalogManager().getTargetOffer(iIntValue);
            HabboOfferPurchase orCreate = HabboOfferPurchase.getOrCreate(this.client.getHabbo(), iIntValue);
            if (orCreate != null) {
                int iMin = Math.min(targetOffer.getPurchaseLimit() - orCreate.getAmount(), iIntValue2);
                int intUnixTimestamp = Emulator.getIntUnixTimestamp();
                if (targetOffer.getExpirationTime() > intUnixTimestamp) {
                    orCreate.update(iMin, intUnixTimestamp);
                    CatalogItem catalogItem = Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(targetOffer.getCatalogItem());
                    if (catalogItem.isLimited()) {
                        iMin = 1;
                    }
                    Emulator.getGameEnvironment().getCatalogManager().purchaseItem(null, catalogItem, this.client.getHabbo(), iMin, Emulator.PREVIEW, false);
                }
            }
        }
    }
}
