package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.guilds.GuildChangedColorsEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildChangeColorsEvent.class */
public class GuildChangeColorsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.packet.readInt().intValue());
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                GuildChangedColorsEvent guildChangedColorsEvent = new GuildChangedColorsEvent(guild, this.packet.readInt().intValue(), this.packet.readInt().intValue());
                Emulator.getPluginManager().fireEvent(guildChangedColorsEvent);
                if (guildChangedColorsEvent.isCancelled()) {
                    return;
                }
                if (guild.getColorOne() == guildChangedColorsEvent.colorOne && guild.getColorTwo() == guildChangedColorsEvent.colorTwo) {
                    return;
                }
                guild.setColorOne(guildChangedColorsEvent.colorOne);
                guild.setColorTwo(guildChangedColorsEvent.colorTwo);
                Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
                if (room != null && room.getUserCount() > 0) {
                    room.refreshGuild(guild);
                    room.refreshGuildColors(guild);
                }
                guild.needsUpdate = true;
                Emulator.getThreading().run(guild);
            }
        }
    }
}
