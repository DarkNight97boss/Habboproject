package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/UpdateFailedComposer.class */
public class UpdateFailedComposer extends MessageComposer {
    private final String message;

    public UpdateFailedComposer(String str) {
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateFailedComposer);
        this.response.appendString(this.message);
        return this.response;
    }
}
