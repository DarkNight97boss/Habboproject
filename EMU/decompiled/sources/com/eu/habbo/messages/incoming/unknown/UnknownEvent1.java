package com.eu.habbo.messages.incoming.unknown;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.unknown.IgnoredUsersComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/unknown/UnknownEvent1.class */
public class UnknownEvent1 extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new IgnoredUsersComposer());
    }
}
