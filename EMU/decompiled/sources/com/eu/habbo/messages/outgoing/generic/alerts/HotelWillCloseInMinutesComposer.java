package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/HotelWillCloseInMinutesComposer.class */
public class HotelWillCloseInMinutesComposer extends MessageComposer {
    private final int minutes;

    public HotelWillCloseInMinutesComposer(int i) {
        this.minutes = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelWillCloseInMinutesComposer);
        this.response.appendInt(Integer.valueOf(this.minutes));
        return this.response;
    }
}
