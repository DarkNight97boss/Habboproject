package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserBCLimitsComposer.class */
public class UserBCLimitsComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(-1);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 500);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
