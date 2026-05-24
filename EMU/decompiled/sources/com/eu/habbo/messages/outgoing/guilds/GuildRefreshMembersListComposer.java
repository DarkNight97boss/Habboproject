package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildRefreshMembersListComposer.class */
public class GuildRefreshMembersListComposer extends MessageComposer {
    private final Guild guild;

    public GuildRefreshMembersListComposer(Guild guild) {
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildRefreshMembersListComposer);
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
