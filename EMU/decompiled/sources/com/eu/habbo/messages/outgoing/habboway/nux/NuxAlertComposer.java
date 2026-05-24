package com.eu.habbo.messages.outgoing.habboway.nux;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/habboway/nux/NuxAlertComposer.class */
public class NuxAlertComposer extends MessageComposer {
    private final String link;

    public NuxAlertComposer(String str) {
        this.link = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NuxAlertComposer);
        this.response.appendString(this.link);
        return this.response;
    }
}
