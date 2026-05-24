package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildConfirmRemoveMemberComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildConfirmRemoveMemberEvent.class */
public class GuildConfirmRemoveMemberEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            if (iIntValue2 == this.client.getHabbo().getHabboInfo().getId() || guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || ((guildMember != null && guildMember.getRank().equals(GuildRank.OWNER)) || guildMember.getRank().equals(GuildRank.ADMIN) || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN))) {
                Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(guild.getRoomId());
                int userFurniCount = 0;
                if (roomLoadRoom != null) {
                    userFurniCount = roomLoadRoom.getUserFurniCount(iIntValue2);
                }
                this.client.sendResponse(new GuildConfirmRemoveMemberComposer(iIntValue2, userFurniCount));
            }
        }
    }
}
