package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/FloodCounterComposer.class */
public class FloodCounterComposer extends MessageComposer {
    private final int time;

    public FloodCounterComposer(int i) {
        this.time = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FloodCounterComposer);
        this.response.appendInt(Integer.valueOf(this.time));
        return this.response;
    }
}
