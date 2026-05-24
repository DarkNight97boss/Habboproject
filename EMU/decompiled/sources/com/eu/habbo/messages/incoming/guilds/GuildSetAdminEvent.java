package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildMemberUpdateComposer;
import com.eu.habbo.plugin.events.guilds.GuildGivenAdminEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildSetAdminEvent.class */
public class GuildSetAdminEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                Emulator.getGameEnvironment().getGuildManager().setAdmin(guild, iIntValue2);
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
                GuildGivenAdminEvent guildGivenAdminEvent = new GuildGivenAdminEvent(guild, iIntValue2, habbo, this.client.getHabbo());
                Emulator.getPluginManager().fireEvent(guildGivenAdminEvent);
                if (guildGivenAdminEvent.isCancelled()) {
                    return;
                }
                if (habbo != null && (currentRoom = habbo.getHabboInfo().getCurrentRoom()) != null && currentRoom.getGuildId() == iIntValue) {
                    currentRoom.refreshRightsForHabbo(habbo);
                }
                this.client.sendResponse(new GuildMemberUpdateComposer(guild, Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, iIntValue2)));
            }
        }
    }
}
