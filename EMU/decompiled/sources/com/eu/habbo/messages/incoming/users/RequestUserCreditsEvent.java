package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserCreditsComposer;
import com.eu.habbo.messages.outgoing.users.UserCurrencyComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/RequestUserCreditsEvent.class */
public class RequestUserCreditsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() {
        this.client.sendResponse(new UserCreditsComposer(this.client.getHabbo()));
        this.client.sendResponse(new UserCurrencyComposer(this.client.getHabbo()));
    }
}
