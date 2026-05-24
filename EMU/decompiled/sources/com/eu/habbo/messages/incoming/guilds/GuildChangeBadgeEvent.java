package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.guilds.GuildChangedBadgeEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildChangeBadgeEvent.class */
public class GuildChangeBadgeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room;
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.packet.readInt().intValue());
        if (guild != null) {
            if ((guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) && (room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId())) != null && room.getId() == guild.getRoomId()) {
                int iIntValue = this.packet.readInt().intValue();
                String str = Emulator.PREVIEW;
                byte b = 1;
                while (true) {
                    byte b2 = b;
                    if (b2 >= iIntValue) {
                        break;
                    }
                    int iIntValue2 = this.packet.readInt().intValue();
                    int iIntValue3 = this.packet.readInt().intValue();
                    str = (b2 == 1 ? str + "b" : str + "s") + (iIntValue2 < 100 ? "0" : Emulator.PREVIEW) + (iIntValue2 < 10 ? "0" : Emulator.PREVIEW) + iIntValue2 + (iIntValue3 < 10 ? "0" : Emulator.PREVIEW) + iIntValue3 + Emulator.PREVIEW + this.packet.readInt().intValue();
                    b = (byte) (b2 + 3);
                }
                if (guild.getBadge().toLowerCase().equals(str.toLowerCase())) {
                    return;
                }
                GuildChangedBadgeEvent guildChangedBadgeEvent = new GuildChangedBadgeEvent(guild, str);
                Emulator.getPluginManager().fireEvent(guildChangedBadgeEvent);
                if (guildChangedBadgeEvent.isCancelled()) {
                    return;
                }
                guild.setBadge(guildChangedBadgeEvent.badge);
                guild.needsUpdate = true;
                if (Emulator.getConfig().getBoolean("imager.internal.enabled")) {
                    Emulator.getBadgeImager().generate(guild);
                }
                room.refreshGuild(guild);
                Emulator.getThreading().run(guild);
            }
        }
    }
}
