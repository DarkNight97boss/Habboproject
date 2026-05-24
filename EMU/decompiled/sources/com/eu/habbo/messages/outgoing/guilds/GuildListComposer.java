package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildListComposer.class */
public class GuildListComposer extends MessageComposer {
    private final THashSet<Guild> guilds;
    private final Habbo habbo;

    public GuildListComposer(THashSet<Guild> tHashSet, Habbo habbo) {
        this.guilds = tHashSet;
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildListComposer);
        this.response.appendInt(Integer.valueOf(this.guilds.size()));
        TObjectHashIterator it = this.guilds.iterator();
        while (it.hasNext()) {
            Guild guild = (Guild) it.next();
            this.response.appendInt(Integer.valueOf(guild.getId()));
            this.response.appendString(guild.getName());
            this.response.appendString(guild.getBadge());
            this.response.appendString(Emulator.getGameEnvironment().getGuildManager().getSymbolColor(guild.getColorOne()).valueA);
            this.response.appendString(Emulator.getGameEnvironment().getGuildManager().getBackgroundColor(guild.getColorTwo()).valueA);
            this.response.appendBoolean(Boolean.valueOf(this.habbo.getHabboStats().guild == guild.getId()));
            this.response.appendInt(Integer.valueOf(guild.getOwnerId()));
            this.response.appendBoolean(Boolean.valueOf(guild.hasForum()));
        }
        return this.response;
    }
}
