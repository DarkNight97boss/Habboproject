package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildBoughtComposer.class */
public class GuildBoughtComposer extends MessageComposer {
    private final Guild guild;

    public GuildBoughtComposer(Guild guild) {
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildBoughtComposer);
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        return this.response;
    }
}
