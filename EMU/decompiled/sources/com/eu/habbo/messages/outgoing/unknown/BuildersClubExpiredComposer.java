package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/BuildersClubExpiredComposer.class */
public class BuildersClubExpiredComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BuildersClubExpiredComposer);
        this.response.appendInt((Integer) Integer.MAX_VALUE);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 100);
        this.response.appendInt((Integer) Integer.MAX_VALUE);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
