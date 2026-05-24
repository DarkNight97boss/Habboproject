package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownGuild2Composer.class */
public class UnknownGuild2Composer extends MessageComposer {
    private final int guildId;

    public UnknownGuild2Composer(int i) {
        this.guildId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownGuild2Composer);
        this.response.appendInt(Integer.valueOf(this.guildId));
        return this.response;
    }
}
