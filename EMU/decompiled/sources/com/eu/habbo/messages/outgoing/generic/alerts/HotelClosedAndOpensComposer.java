package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/HotelClosedAndOpensComposer.class */
public class HotelClosedAndOpensComposer extends MessageComposer {
    private final int hour;
    private final int minute;

    public HotelClosedAndOpensComposer(int i, int i2) {
        this.hour = i;
        this.minute = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelClosedAndOpensComposer);
        this.response.appendInt(Integer.valueOf(this.hour));
        this.response.appendInt(Integer.valueOf(this.minute));
        return this.response;
    }
}
