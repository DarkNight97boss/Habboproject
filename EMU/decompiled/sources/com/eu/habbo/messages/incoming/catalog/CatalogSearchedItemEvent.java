package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.CatalogSearchResultComposer;
import gnu.trove.iterator.TIntObjectIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogSearchedItemEvent.class */
public class CatalogSearchedItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CatalogPage catalogPage;
        int iIntValue = this.packet.readInt().intValue();
        int i = Emulator.getGameEnvironment().getCatalogManager().offerDefs.get(iIntValue);
        if (i == 0 || (catalogPage = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(i).getPageId())) == null) {
            return;
        }
        TIntObjectIterator it = catalogPage.getCatalogItems().iterator();
        while (it.hasNext()) {
            it.advance();
            CatalogItem catalogItem = (CatalogItem) it.value();
            if (catalogItem.getOfferId() == iIntValue) {
                this.client.sendResponse(new CatalogSearchResultComposer(catalogItem));
                return;
            }
        }
    }
}
