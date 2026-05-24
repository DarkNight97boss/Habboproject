package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.guilds.GuildChangedNameEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildChangeNameDescEvent.class */
public class GuildChangeNameDescEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.packet.readInt().intValue());
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                GuildChangedNameEvent guildChangedNameEvent = new GuildChangedNameEvent(guild, this.packet.readString(), this.packet.readString());
                Emulator.getPluginManager().fireEvent(guildChangedNameEvent);
                if (guildChangedNameEvent.isCancelled()) {
                    return;
                }
                if (!(guild.getName().equals(guildChangedNameEvent.name) && guild.getDescription().equals(guildChangedNameEvent.description)) && guildChangedNameEvent.name.length() <= 29 && guildChangedNameEvent.description.length() <= 254) {
                    guild.setName(guildChangedNameEvent.name);
                    guild.setDescription(guildChangedNameEvent.description);
                    guild.needsUpdate = true;
                    guild.run();
                    Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
                    if (room == null || room.getCurrentHabbos().isEmpty()) {
                        return;
                    }
                    room.refreshGuild(guild);
                }
            }
        }
    }
}
