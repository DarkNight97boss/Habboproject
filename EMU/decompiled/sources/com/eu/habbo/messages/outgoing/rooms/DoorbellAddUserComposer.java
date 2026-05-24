package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/DoorbellAddUserComposer.class */
public class DoorbellAddUserComposer extends MessageComposer {
    private final String habbo;

    public DoorbellAddUserComposer(String str) {
        this.habbo = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.DoorbellAddUserComposer);
        this.response.appendString(this.habbo);
        return this.response;
    }
}
