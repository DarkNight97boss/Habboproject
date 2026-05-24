package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/AvailabilityStatusMessageComposer.class */
public class AvailabilityStatusMessageComposer extends MessageComposer {
    private final boolean isOpen;
    private final boolean isShuttingDown;
    private final boolean isAuthenticHabbo;

    public AvailabilityStatusMessageComposer(boolean z, boolean z2, boolean z3) {
        this.isOpen = z;
        this.isShuttingDown = z2;
        this.isAuthenticHabbo = z3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AvailabilityStatusMessageComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isOpen));
        this.response.appendBoolean(Boolean.valueOf(this.isShuttingDown));
        this.response.appendBoolean(Boolean.valueOf(this.isAuthenticHabbo));
        return this.response;
    }
}
