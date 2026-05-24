package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildMembersComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildRefreshMembersListComposer;
import com.eu.habbo.plugin.events.guilds.GuildDeclinedMembershipEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildDeclineMembershipEvent.class */
public class GuildDeclineMembershipEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            if (iIntValue2 == this.client.getHabbo().getHabboInfo().getId() || guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || guildMember.getRank().equals(GuildRank.ADMIN) || guildMember.getRank().equals(GuildRank.OWNER) || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                guild.decreaseRequestCount();
                Emulator.getGameEnvironment().getGuildManager().removeMember(guild, iIntValue2);
                this.client.sendResponse(new GuildMembersComposer(guild, Emulator.getGameEnvironment().getGuildManager().getGuildMembers(guild, 0, 0, Emulator.PREVIEW), this.client.getHabbo(), 0, 0, Emulator.PREVIEW, true, Emulator.getGameEnvironment().getGuildManager().getGuildMembersCount(guild, 0, 0, Emulator.PREVIEW)));
                this.client.sendResponse(new GuildRefreshMembersListComposer(guild));
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
                Emulator.getPluginManager().fireEvent(new GuildDeclinedMembershipEvent(guild, iIntValue2, habbo, this.client.getHabbo()));
                if (habbo == null || (currentRoom = habbo.getHabboInfo().getCurrentRoom()) == null || currentRoom.getGuildId() != iIntValue) {
                    return;
                }
                habbo.getClient().sendResponse(new GuildInfoComposer(guild, habbo.getClient(), false, null));
            }
        }
    }
}
