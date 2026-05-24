package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.CatalogPageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/RequestCatalogPageEvent.class */
public class RequestCatalogPageEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        CatalogPage catalogPage = (CatalogPage) Emulator.getGameEnvironment().getCatalogManager().catalogPages.get(iIntValue);
        if (iIntValue <= 0 || catalogPage == null) {
            return;
        }
        if (catalogPage.getRank() <= this.client.getHabbo().getHabboInfo().getRank().getId() && catalogPage.isEnabled()) {
            this.client.sendResponse(new CatalogPageComposer(catalogPage, this.client.getHabbo(), iIntValue2, string));
        } else {
            if (catalogPage.isVisible()) {
                return;
            }
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.catalog.page").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%pagename%", catalogPage.getCaption()));
        }
    }
}
