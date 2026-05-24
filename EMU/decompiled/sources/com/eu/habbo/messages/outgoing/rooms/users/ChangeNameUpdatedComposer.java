package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/ChangeNameUpdatedComposer.class */
public class ChangeNameUpdatedComposer extends MessageComposer {
    private final Habbo habbo;

    public ChangeNameUpdatedComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ChangeNameUpdateComposer);
        this.response.appendInt((Integer) 0);
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
