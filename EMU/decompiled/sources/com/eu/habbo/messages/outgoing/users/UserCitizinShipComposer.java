package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserCitizinShipComposer.class */
public class UserCitizinShipComposer extends MessageComposer {
    private final String name;

    public UserCitizinShipComposer(String str) {
        this.name = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserCitizinShipComposer);
        this.response.appendString(this.name);
        this.response.appendInt((Integer) 4);
        this.response.appendInt((Integer) 4);
        return this.response;
    }
}
