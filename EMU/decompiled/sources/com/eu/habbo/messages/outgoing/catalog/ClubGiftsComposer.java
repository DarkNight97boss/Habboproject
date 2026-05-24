package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/ClubGiftsComposer.class */
public class ClubGiftsComposer extends MessageComposer {
    private final int daysTillNextGift;
    private final int availableGifts;
    private final int daysAsHc;

    public ClubGiftsComposer(int i, int i2, int i3) {
        this.daysTillNextGift = i;
        this.availableGifts = i2;
        this.daysAsHc = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ClubGiftsComposer);
        this.response.appendInt(Integer.valueOf(this.daysTillNextGift));
        this.response.appendInt(Integer.valueOf(this.availableGifts));
        CatalogPage catalogPageByLayout = Emulator.getGameEnvironment().getCatalogManager().getCatalogPageByLayout(CatalogPageLayouts.club_gift.name().toLowerCase());
        if (catalogPageByLayout != null) {
            ArrayList<CatalogItem> arrayList = new ArrayList(catalogPageByLayout.getCatalogItems().valueCollection());
            Collections.sort(arrayList);
            this.response.appendInt(Integer.valueOf(arrayList.size()));
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((CatalogItem) it.next()).serialize(this.response);
            }
            this.response.appendInt(Integer.valueOf(arrayList.size()));
            for (CatalogItem catalogItem : arrayList) {
                int i = 0;
                try {
                    i = Integer.parseInt(catalogItem.getExtradata());
                } catch (NumberFormatException e) {
                }
                this.response.appendInt(Integer.valueOf(catalogItem.getId()));
                this.response.appendBoolean(Boolean.valueOf(catalogItem.isClubOnly()));
                this.response.appendInt(Integer.valueOf(i));
                this.response.appendBoolean(Boolean.valueOf(i <= this.daysAsHc));
            }
        } else {
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
        }
        return this.response;
    }
}
