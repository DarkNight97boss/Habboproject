package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildFavoriteRoomUserUpdateComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildRefreshMembersListComposer;
import com.eu.habbo.plugin.events.guilds.GuildRemovedMemberEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildRemoveMemberEvent.class */
public class GuildRemoveMemberEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            if (iIntValue2 == this.client.getHabbo().getHabboInfo().getId() || guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || guildMember.getRank().equals(GuildRank.OWNER) || guildMember.getRank().equals(GuildRank.ADMIN) || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
                GuildRemovedMemberEvent guildRemovedMemberEvent = new GuildRemovedMemberEvent(guild, iIntValue2, habbo);
                Emulator.getPluginManager().fireEvent(guildRemovedMemberEvent);
                if (guildRemovedMemberEvent.isCancelled()) {
                    return;
                }
                Emulator.getGameEnvironment().getGuildManager().removeMember(guild, iIntValue2);
                guild.decreaseMemberCount();
                if (iIntValue2 != this.client.getHabbo().getHabboInfo().getId()) {
                    this.client.sendResponse(new GuildRefreshMembersListComposer(guild));
                }
                Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(guild.getRoomId());
                if (habbo != null) {
                    habbo.getHabboStats().removeGuild(guild.getId());
                    if (habbo.getHabboStats().guild == iIntValue) {
                        habbo.getHabboStats().guild = 0;
                    }
                    if (roomLoadRoom != null) {
                        if (habbo.getHabboInfo().getCurrentRoom() != null && habbo.getRoomUnit() != null) {
                            habbo.getHabboInfo().getCurrentRoom().sendComposer(new GuildFavoriteRoomUserUpdateComposer(habbo.getRoomUnit(), null).compose());
                        }
                        if (habbo.getHabboInfo().getCurrentRoom() == roomLoadRoom) {
                            roomLoadRoom.refreshRightsForHabbo(habbo);
                        }
                    }
                    habbo.getClient().sendResponse(new GuildInfoComposer(guild, habbo.getClient(), false, null));
                }
                if (roomLoadRoom == null || roomLoadRoom.getGuildId() != iIntValue) {
                    return;
                }
                roomLoadRoom.ejectUserFurni(iIntValue2);
            }
        }
    }
}
