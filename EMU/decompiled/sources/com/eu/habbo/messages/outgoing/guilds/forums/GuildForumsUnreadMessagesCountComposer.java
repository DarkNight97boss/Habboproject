package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumsUnreadMessagesCountComposer.class */
public class GuildForumsUnreadMessagesCountComposer extends MessageComposer {
    public final int count;

    public GuildForumsUnreadMessagesCountComposer(int i) {
        this.count = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildForumsUnreadMessagesCountComposer);
        this.response.appendInt(Integer.valueOf(this.count));
        return this.response;
    }
}
