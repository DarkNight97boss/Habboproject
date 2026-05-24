package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewNextLTDAvailableComposer.class */
public class HotelViewNextLTDAvailableComposer extends MessageComposer {
    private final int time;
    private final int pageId;
    private final int itemId;
    private final String itemName;

    public HotelViewNextLTDAvailableComposer(int i, int i2, int i3, String str) {
        this.time = i;
        this.pageId = i2;
        this.itemId = i3;
        this.itemName = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(44);
        this.response.appendInt(Integer.valueOf(this.time));
        this.response.appendInt(Integer.valueOf(this.pageId));
        this.response.appendInt(Integer.valueOf(this.itemId));
        this.response.appendString(this.itemName);
        return this.response;
    }
}
