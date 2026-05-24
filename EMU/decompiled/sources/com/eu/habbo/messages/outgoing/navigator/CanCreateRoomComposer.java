package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/CanCreateRoomComposer.class */
public class CanCreateRoomComposer extends MessageComposer {
    private final int count;
    private final int max;

    public CanCreateRoomComposer(int i, int i2) {
        this.count = i;
        this.max = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CanCreateRoomComposer);
        this.response.appendInt(Integer.valueOf(this.count >= this.max ? 1 : 0));
        this.response.appendInt(Integer.valueOf(this.max));
        return this.response;
    }
}
