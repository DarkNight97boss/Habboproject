package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildFavoriteRoomUserUpdateComposer;
import com.eu.habbo.messages.outgoing.guilds.RemoveGuildFromRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomDataComposer;
import com.eu.habbo.plugin.events.guilds.GuildDeletedEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildDeleteEvent.class */
public class GuildDeleteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild != null) {
            if (guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) {
                TObjectHashIterator it = Emulator.getGameEnvironment().getGuildManager().getGuildMembers(guild.getId()).iterator();
                while (it.hasNext()) {
                    Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(((GuildMember) it.next()).getUserId());
                    if (habbo != null && habbo.getHabboInfo().getCurrentRoom() != null && habbo.getRoomUnit() != null) {
                        habbo.getHabboInfo().getCurrentRoom().sendComposer(new GuildFavoriteRoomUserUpdateComposer(habbo.getRoomUnit(), null).compose());
                    }
                }
                Emulator.getGameEnvironment().getGuildManager().deleteGuild(guild);
                Emulator.getPluginManager().fireEvent(new GuildDeletedEvent(guild, this.client.getHabbo()));
                Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId()).sendComposer(new RemoveGuildFromRoomComposer(iIntValue).compose());
                if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || guild.getRoomId() != this.client.getHabbo().getHabboInfo().getCurrentRoom().getId()) {
                    return;
                }
                this.client.sendResponse(new RoomDataComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom(), this.client.getHabbo(), false, false));
            }
        }
    }
}
