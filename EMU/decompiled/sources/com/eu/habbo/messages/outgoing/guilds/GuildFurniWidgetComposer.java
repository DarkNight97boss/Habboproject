package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildFurniWidgetComposer.class */
public class GuildFurniWidgetComposer extends MessageComposer {
    private final HabboItem item;
    private final Guild guild;
    private final Habbo habbo;

    public GuildFurniWidgetComposer(Habbo habbo, Guild guild, HabboItem habboItem) {
        this.habbo = habbo;
        this.item = habboItem;
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildFurniWidgetComposer);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendString(this.guild.getName());
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendBoolean(Boolean.valueOf(Emulator.getGameEnvironment().getGuildManager().getGuildMember(this.guild, this.habbo) != null));
        this.response.appendBoolean(Boolean.valueOf(this.guild.hasForum()));
        return this.response;
    }
}
