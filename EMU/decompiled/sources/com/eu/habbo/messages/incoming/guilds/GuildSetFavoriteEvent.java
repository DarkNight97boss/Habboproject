package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildFavoriteRoomUserUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersAddGuildBadgeComposer;
import com.eu.habbo.messages.outgoing.users.UserProfileComposer;
import com.eu.habbo.plugin.events.guilds.GuildFavoriteSetEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildSetFavoriteEvent.class */
public class GuildSetFavoriteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (this.client.getHabbo().getHabboStats().hasGuild(iIntValue)) {
            GuildFavoriteSetEvent guildFavoriteSetEvent = new GuildFavoriteSetEvent(guild, this.client.getHabbo());
            Emulator.getPluginManager().fireEvent(guildFavoriteSetEvent);
            if (guildFavoriteSetEvent.isCancelled()) {
                return;
            }
            this.client.getHabbo().getHabboStats().guild = iIntValue;
            if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null && guild != null) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUsersAddGuildBadgeComposer(guild).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new GuildFavoriteRoomUserUpdateComposer(this.client.getHabbo().getRoomUnit(), guild).compose());
            }
            this.client.sendResponse(new UserProfileComposer(this.client.getHabbo(), this.client));
        }
    }
}
