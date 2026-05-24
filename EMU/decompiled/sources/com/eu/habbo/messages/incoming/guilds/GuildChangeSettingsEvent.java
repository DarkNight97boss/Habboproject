package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.guilds.GuildChangedSettingsEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildChangeSettingsEvent.class */
public class GuildChangeSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.packet.readInt().intValue());
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                GuildChangedSettingsEvent guildChangedSettingsEvent = new GuildChangedSettingsEvent(guild, this.packet.readInt().intValue(), this.packet.readInt().intValue() == 0);
                Emulator.getPluginManager().fireEvent(guildChangedSettingsEvent);
                if (guildChangedSettingsEvent.isCancelled()) {
                    return;
                }
                guild.setState(GuildState.valueOf(guildChangedSettingsEvent.state));
                guild.setRights(guildChangedSettingsEvent.rights);
                Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
                if (room != null) {
                    room.refreshGuild(guild);
                }
                guild.needsUpdate = true;
                Emulator.getThreading().run(guild);
            }
        }
    }
}
