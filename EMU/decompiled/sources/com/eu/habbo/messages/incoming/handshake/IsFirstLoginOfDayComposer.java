package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/handshake/IsFirstLoginOfDayComposer.class */
public class IsFirstLoginOfDayComposer extends MessageComposer {
    private final boolean isFirstLoginOfDay;

    public IsFirstLoginOfDayComposer(boolean z) {
        this.isFirstLoginOfDay = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.IsFirstLoginOfDayComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isFirstLoginOfDay));
        return this.response;
    }
}
