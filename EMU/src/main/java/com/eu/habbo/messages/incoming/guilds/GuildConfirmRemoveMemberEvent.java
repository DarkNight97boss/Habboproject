package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildConfirmRemoveMemberComposer;

public class GuildConfirmRemoveMemberEvent extends MessageHandler {
    @Override
    public int getRatelimit() {
        return 500;
    }

    @Override
    public void handle() throws Exception {
        int guildId = this.packet.readInt();
        int userId = this.packet.readInt();

        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(guildId);

        if (guild != null) {
            GuildMember member = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            int callerId = this.client.getHabbo().getHabboInfo().getId();
            boolean isPriv = userId == callerId
                    || guild.getOwnerId() == callerId
                    || (member != null && (member.getRank().equals(GuildRank.OWNER) || member.getRank().equals(GuildRank.ADMIN)));
            if (isPriv) {
                // Anti-info-leak: confirm the target IS actually in the guild before
                // returning its furni count (an admin could otherwise enumerate any
                // user-id's furni count in their room).
                GuildMember target = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guildId, userId);
                if (target == null) {
                    return;
                }
                Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(guild.getRoomId());
                int count = 0;
                if (room != null) {
                    count = room.getUserFurniCount(userId);
                }
                this.client.sendResponse(new GuildConfirmRemoveMemberComposer(userId, count));
            }
        }

    }
}
