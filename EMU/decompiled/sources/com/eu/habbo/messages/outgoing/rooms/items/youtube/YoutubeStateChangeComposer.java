package com.eu.habbo.messages.outgoing.rooms.items.youtube;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/youtube/YoutubeStateChangeComposer.class */
public class YoutubeStateChangeComposer extends MessageComposer {
    private final int furniId;
    private final int state;

    public YoutubeStateChangeComposer(int i, int i2) {
        this.furniId = i;
        this.state = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.YoutubeMessageComposer3);
        this.response.appendInt(Integer.valueOf(this.furniId));
        this.response.appendInt(Integer.valueOf(this.state));
        return this.response;
    }
}
