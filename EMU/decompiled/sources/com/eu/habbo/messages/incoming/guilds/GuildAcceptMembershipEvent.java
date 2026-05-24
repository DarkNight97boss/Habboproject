package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildAcceptMemberErrorComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildRefreshMembersListComposer;
import com.eu.habbo.plugin.events.guilds.GuildAcceptedMembershipEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildAcceptMembershipEvent.class */
public class GuildAcceptMembershipEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
        if (guild != null) {
            GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            if (iIntValue2 == this.client.getHabbo().getHabboInfo().getId() || guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || guildMember.getRank().equals(GuildRank.ADMIN) || guildMember.getRank().equals(GuildRank.OWNER) || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                if (habbo == null) {
                    Emulator.getGameEnvironment().getGuildManager().joinGuild(guild, this.client, iIntValue2, true);
                    return;
                }
                if (habbo.getHabboStats().hasGuild(guild.getId())) {
                    this.client.sendResponse(new GuildAcceptMemberErrorComposer(guild.getId(), 2));
                    return;
                }
                GuildMember guildMember2 = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, habbo);
                if (guildMember2 == null || guildMember2.getRank().type != GuildRank.REQUESTED.type) {
                    this.client.sendResponse(new GuildAcceptMemberErrorComposer(guild.getId(), 0));
                    return;
                }
                GuildAcceptedMembershipEvent guildAcceptedMembershipEvent = new GuildAcceptedMembershipEvent(guild, iIntValue2, habbo);
                Emulator.getPluginManager().fireEvent(guildAcceptedMembershipEvent);
                if (guildAcceptedMembershipEvent.isCancelled()) {
                    return;
                }
                habbo.getHabboStats().addGuild(guild.getId());
                Emulator.getGameEnvironment().getGuildManager().joinGuild(guild, this.client, habbo.getHabboInfo().getId(), true);
                guild.decreaseRequestCount();
                guild.increaseMemberCount();
                this.client.sendResponse(new GuildRefreshMembersListComposer(guild));
                Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
                if (currentRoom == null || currentRoom.getGuildId() != iIntValue) {
                    return;
                }
                habbo.getClient().sendResponse(new GuildInfoComposer(guild, habbo.getClient(), false, Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, iIntValue2)));
                currentRoom.refreshRightsForHabbo(habbo);
            }
        }
    }
}
