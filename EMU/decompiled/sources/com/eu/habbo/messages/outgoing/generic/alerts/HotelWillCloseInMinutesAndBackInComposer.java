package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/HotelWillCloseInMinutesAndBackInComposer.class */
public class HotelWillCloseInMinutesAndBackInComposer extends MessageComposer {
    private final int closeInMinutes;
    private final int reopenInMinutes;

    public HotelWillCloseInMinutesAndBackInComposer(int i, int i2) {
        this.closeInMinutes = i;
        this.reopenInMinutes = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelWillCloseInMinutesAndBackInComposer);
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(this.closeInMinutes));
        this.response.appendInt(Integer.valueOf(this.reopenInMinutes));
        return this.response;
    }
}
