package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildConfirmRemoveMemberComposer.class */
public class GuildConfirmRemoveMemberComposer extends MessageComposer {
    private int userId;
    private int furniCount;

    public GuildConfirmRemoveMemberComposer(int i, int i2) {
        this.userId = i;
        this.furniCount = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildConfirmRemoveMemberComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendInt(Integer.valueOf(this.furniCount));
        return this.response;
    }
}
