package com.eu.habbo.messages.outgoing;

import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/MessageComposer.class */
public abstract class MessageComposer {
    private ServerMessage composed = null;
    protected final ServerMessage response = new ServerMessage();

    protected MessageComposer() {
    }

    protected abstract ServerMessage composeInternal();

    public ServerMessage compose() {
        if (this.composed == null) {
            this.composed = composeInternal();
        }
        return this.composed;
    }
}
