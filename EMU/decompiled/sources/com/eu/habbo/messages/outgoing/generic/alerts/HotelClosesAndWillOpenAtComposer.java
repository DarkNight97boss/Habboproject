package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/HotelClosesAndWillOpenAtComposer.class */
public class HotelClosesAndWillOpenAtComposer extends MessageComposer {
    private final int hour;
    private final int minute;
    private final boolean disconnected;

    public HotelClosesAndWillOpenAtComposer(int i, int i2, boolean z) {
        this.hour = i;
        this.minute = i2;
        this.disconnected = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2771);
        this.response.appendInt(Integer.valueOf(this.hour));
        this.response.appendInt(Integer.valueOf(this.minute));
        this.response.appendBoolean(Boolean.valueOf(this.disconnected));
        return this.response;
    }
}
