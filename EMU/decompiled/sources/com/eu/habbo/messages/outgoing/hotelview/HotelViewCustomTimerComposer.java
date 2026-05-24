package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewCustomTimerComposer.class */
public class HotelViewCustomTimerComposer extends MessageComposer {
    private final String name;
    private final int seconds;

    public HotelViewCustomTimerComposer(String str, int i) {
        this.name = str;
        this.seconds = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(-1);
        this.response.appendString(this.name);
        this.response.appendInt(Integer.valueOf(this.seconds));
        return this.response;
    }
}
