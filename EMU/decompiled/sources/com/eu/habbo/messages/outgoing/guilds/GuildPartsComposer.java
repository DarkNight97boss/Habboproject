package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.GuildPart;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildPartsComposer.class */
public class GuildPartsComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GroupPartsComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuildManager().getBases().size()));
        for (GuildPart guildPart : Emulator.getGameEnvironment().getGuildManager().getBases()) {
            this.response.appendInt(Integer.valueOf(guildPart.id));
            this.response.appendString(guildPart.valueA);
            this.response.appendString(guildPart.valueB);
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuildManager().getSymbols().size()));
        for (GuildPart guildPart2 : Emulator.getGameEnvironment().getGuildManager().getSymbols()) {
            this.response.appendInt(Integer.valueOf(guildPart2.id));
            this.response.appendString(guildPart2.valueA);
            this.response.appendString(guildPart2.valueB);
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuildManager().getBaseColors().size()));
        for (GuildPart guildPart3 : Emulator.getGameEnvironment().getGuildManager().getBaseColors()) {
            this.response.appendInt(Integer.valueOf(guildPart3.id));
            this.response.appendString(guildPart3.valueA);
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuildManager().getSymbolColors().size()));
        for (GuildPart guildPart4 : Emulator.getGameEnvironment().getGuildManager().getSymbolColors()) {
            this.response.appendInt(Integer.valueOf(guildPart4.id));
            this.response.appendString(guildPart4.valueA);
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuildManager().getBackgroundColors().size()));
        for (GuildPart guildPart5 : Emulator.getGameEnvironment().getGuildManager().getBackgroundColors()) {
            this.response.appendInt(Integer.valueOf(guildPart5.id));
            this.response.appendString(guildPart5.valueA);
        }
        return this.response;
    }
}
