package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsLoadingArenaComposer.class */
public class SnowWarsLoadingArenaComposer extends MessageComposer {
    private final int count;

    public SnowWarsLoadingArenaComposer(int i) {
        this.count = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3850);
        this.response.appendInt(Integer.valueOf(this.count));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
