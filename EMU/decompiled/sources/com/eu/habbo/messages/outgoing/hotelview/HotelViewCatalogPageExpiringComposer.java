package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewCatalogPageExpiringComposer.class */
public class HotelViewCatalogPageExpiringComposer extends MessageComposer {
    private final String name;
    private final int time;
    private final String image;

    public HotelViewCatalogPageExpiringComposer(String str, int i, String str2) {
        this.name = str;
        this.time = i;
        this.image = str2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewCatalogPageExpiringComposer);
        this.response.appendString(this.name);
        this.response.appendInt(Integer.valueOf(this.time));
        this.response.appendString(this.image);
        return this.response;
    }
}
