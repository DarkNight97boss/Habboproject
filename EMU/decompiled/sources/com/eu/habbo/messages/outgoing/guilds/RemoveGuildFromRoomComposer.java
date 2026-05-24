package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/RemoveGuildFromRoomComposer.class */
public class RemoveGuildFromRoomComposer extends MessageComposer {
    private int guildId;

    public RemoveGuildFromRoomComposer(int i) {
        this.guildId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3129);
        this.response.appendInt(Integer.valueOf(this.guildId));
        return this.response;
    }
}
