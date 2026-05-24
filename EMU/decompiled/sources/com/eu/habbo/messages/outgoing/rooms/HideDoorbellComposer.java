package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/HideDoorbellComposer.class */
public class HideDoorbellComposer extends MessageComposer {
    private final String username;

    public HideDoorbellComposer(String str) {
        this.username = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HideDoorbellComposer);
        this.response.appendString(this.username);
        return this.response;
    }
}
