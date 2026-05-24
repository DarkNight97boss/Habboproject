package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogFeaturedPage;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.layouts.FrontPageFeaturedLayout;
import com.eu.habbo.habbohotel.catalog.layouts.FrontpageLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecentPurchasesLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/CatalogPageComposer.class */
public class CatalogPageComposer extends MessageComposer {
    private final CatalogPage page;
    private final Habbo habbo;
    private final int offerId;
    private final String mode;

    public CatalogPageComposer(CatalogPage catalogPage, Habbo habbo, int i, String str) {
        this.page = catalogPage;
        this.habbo = habbo;
        this.offerId = i;
        this.mode = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CatalogPageComposer);
        this.response.appendInt(Integer.valueOf(this.page.getId()));
        this.response.appendString(this.mode);
        this.page.serialize(this.response);
        if (this.page instanceof RecentPurchasesLayout) {
            this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().getRecentPurchases().size()));
            Iterator it = this.habbo.getHabboStats().getRecentPurchases().entrySet().iterator();
            while (it.hasNext()) {
                ((CatalogItem) ((Map.Entry) it.next()).getValue()).serialize(this.response);
            }
        } else {
            this.response.appendInt(Integer.valueOf(this.page.getCatalogItems().size()));
            ArrayList arrayList = new ArrayList(this.page.getCatalogItems().valueCollection());
            Collections.sort(arrayList);
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                ((CatalogItem) it2.next()).serialize(this.response);
            }
        }
        this.response.appendInt(Integer.valueOf(this.offerId));
        this.response.appendBoolean(false);
        if ((this.page instanceof FrontPageFeaturedLayout) || (this.page instanceof FrontpageLayout)) {
            serializeExtra(this.response);
        }
        return this.response;
    }

    public void serializeExtra(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getCatalogManager().getCatalogFeaturedPages().size()));
        Iterator it = Emulator.getGameEnvironment().getCatalogManager().getCatalogFeaturedPages().valueCollection().iterator();
        while (it.hasNext()) {
            ((CatalogFeaturedPage) it.next()).serialize(serverMessage);
        }
    }
}
