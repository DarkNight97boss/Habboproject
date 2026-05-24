package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildMembersComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestGuildMembersEvent.class */
public class RequestGuildMembersEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue3 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN);
            if (!zHasPermission && this.client.getHabbo().getHabboStats().hasGuild(guild.getId())) {
                GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
                zHasPermission = guildMember != null && (guildMember.getRank().equals(GuildRank.OWNER) || guildMember.getRank().equals(GuildRank.ADMIN));
            }
            this.client.sendResponse(new GuildMembersComposer(guild, Emulator.getGameEnvironment().getGuildManager().getGuildMembers(guild, iIntValue2, iIntValue3, string), this.client.getHabbo(), iIntValue2, iIntValue3, string, zHasPermission, Emulator.getGameEnvironment().getGuildManager().getGuildMembersCount(guild, iIntValue2, iIntValue3, string)));
        }
    }
}
