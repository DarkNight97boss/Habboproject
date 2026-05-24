package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewDataComposer.class */
public class HotelViewDataComposer extends MessageComposer {
    private final String data;
    private final String key;

    public HotelViewDataComposer(String str, String str2) {
        this.data = str;
        this.key = str2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewDataComposer);
        this.response.appendString(this.data);
        this.response.appendString(this.key);
        return this.response;
    }
}
