package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewExpiringCatalogPageCommposer.class */
public class HotelViewExpiringCatalogPageCommposer extends MessageComposer {
    private final CatalogPage page;
    private final String image;

    public HotelViewExpiringCatalogPageCommposer(CatalogPage catalogPage, String str) {
        this.page = catalogPage;
        this.image = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewExpiringCatalogPageCommposer);
        this.response.appendString(this.page.getCaption());
        this.response.appendInt(Integer.valueOf(this.page.getId()));
        this.response.appendString(this.image);
        return this.response;
    }
}
