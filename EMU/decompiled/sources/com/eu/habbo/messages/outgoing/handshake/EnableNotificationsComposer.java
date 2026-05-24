package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/EnableNotificationsComposer.class */
public class EnableNotificationsComposer extends MessageComposer {
    private final boolean enabled;

    public EnableNotificationsComposer(boolean z) {
        this.enabled = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.EnableNotificationsComposer);
        this.response.appendBoolean(Boolean.valueOf(this.enabled));
        return this.response;
    }
}
