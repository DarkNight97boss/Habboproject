package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/KnockKnockUnknownComposer.class */
public class KnockKnockUnknownComposer extends MessageComposer {
    private final Habbo habbo;

    public KnockKnockUnknownComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(478);
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getId()));
        return this.response;
    }
}
