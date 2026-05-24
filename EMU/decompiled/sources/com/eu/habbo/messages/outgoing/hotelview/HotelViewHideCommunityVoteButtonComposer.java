package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewHideCommunityVoteButtonComposer.class */
public class HotelViewHideCommunityVoteButtonComposer extends MessageComposer {
    private final boolean unknownBoolean;

    public HotelViewHideCommunityVoteButtonComposer(boolean z) {
        this.unknownBoolean = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1435);
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean));
        return this.response;
    }
}
