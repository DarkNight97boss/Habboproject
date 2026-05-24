package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewBadgeButtonConfigComposer.class */
public class HotelViewBadgeButtonConfigComposer extends MessageComposer {
    private final String badge;
    private final boolean enabled;

    public HotelViewBadgeButtonConfigComposer(String str, boolean z) {
        this.badge = str;
        this.enabled = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewBadgeButtonConfigComposer);
        this.response.appendString(this.badge);
        this.response.appendBoolean(Boolean.valueOf(this.enabled));
        return this.response;
    }
}
