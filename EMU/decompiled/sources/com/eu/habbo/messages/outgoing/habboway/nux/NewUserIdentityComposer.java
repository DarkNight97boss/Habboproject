package com.eu.habbo.messages.outgoing.habboway.nux;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/habboway/nux/NewUserIdentityComposer.class */
public class NewUserIdentityComposer extends MessageComposer {
    private final Habbo habbo;

    public NewUserIdentityComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewUserIdentityComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.noobStatus()));
        return this.response;
    }
}
