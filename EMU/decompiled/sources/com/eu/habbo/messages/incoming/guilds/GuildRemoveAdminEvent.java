package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildMemberUpdateComposer;
import com.eu.habbo.plugin.events.guilds.GuildRemovedAdminEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildRemoveAdminEvent.class */
public class GuildRemoveAdminEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                int iIntValue2 = this.packet.readInt().intValue();
                Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
                GuildRemovedAdminEvent guildRemovedAdminEvent = new GuildRemovedAdminEvent(guild, iIntValue2, habbo);
                Emulator.getPluginManager().fireEvent(guildRemovedAdminEvent);
                if (guildRemovedAdminEvent.isCancelled()) {
                    return;
                }
                Emulator.getGameEnvironment().getGuildManager().removeAdmin(guild, iIntValue2);
                if (habbo != null) {
                    habbo.getClient().sendResponse(new GuildInfoComposer(guild, this.client, false, Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild.getId(), iIntValue2)));
                    if (room != null && habbo.getHabboInfo().getCurrentRoom() != null && habbo.getHabboInfo().getCurrentRoom() == room) {
                        room.refreshRightsForHabbo(habbo);
                    }
                }
                this.client.sendResponse(new GuildMemberUpdateComposer(guild, Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, iIntValue2)));
            }
        }
    }
}
