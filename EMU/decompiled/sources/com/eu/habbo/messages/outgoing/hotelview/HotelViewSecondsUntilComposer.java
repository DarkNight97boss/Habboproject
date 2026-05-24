package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewSecondsUntilComposer.class */
public class HotelViewSecondsUntilComposer extends MessageComposer {
    private final String dateString;
    private final int seconds;

    public HotelViewSecondsUntilComposer(String str, int i) {
        this.dateString = str;
        this.seconds = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewSecondsUntilComposer);
        this.response.appendString(this.dateString);
        this.response.appendInt(Integer.valueOf(this.seconds));
        return this.response;
    }
}
