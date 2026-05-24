package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildFavoriteRoomUserUpdateComposer;
import com.eu.habbo.messages.outgoing.users.UserProfileComposer;
import com.eu.habbo.plugin.events.guilds.GuildRemovedFavoriteEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/GuildRemoveFavoriteEvent.class */
public class GuildRemoveFavoriteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboStats().hasGuild(iIntValue)) {
            Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
            GuildRemovedFavoriteEvent guildRemovedFavoriteEvent = new GuildRemovedFavoriteEvent(guild, this.client.getHabbo());
            Emulator.getPluginManager().fireEvent(guildRemovedFavoriteEvent);
            if (guildRemovedFavoriteEvent.isCancelled()) {
                return;
            }
            this.client.getHabbo().getHabboStats().guild = 0;
            if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null && guild != null) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new GuildFavoriteRoomUserUpdateComposer(this.client.getHabbo().getRoomUnit(), null).compose());
            }
            this.client.sendResponse(new UserProfileComposer(this.client.getHabbo(), this.client));
        }
    }
}
