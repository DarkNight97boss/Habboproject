package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildState;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildJoinErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestGuildJoinEvent.class */
public class RequestGuildJoinEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Guild guild;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboStats().hasGuild(iIntValue) || (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue)) == null) {
            return;
        }
        if (guild.getState() == GuildState.CLOSED || guild.getState() == GuildState.LARGE_CLOSED) {
            this.client.sendResponse(new GuildJoinErrorComposer(2));
            return;
        }
        Emulator.getGameEnvironment().getGuildManager().joinGuild(guild, this.client, 0, false);
        this.client.sendResponse(new GuildInfoComposer(guild, this.client, false, Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo())));
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || currentRoom.getGuildId() != iIntValue) {
            return;
        }
        currentRoom.refreshRightsForHabbo(this.client.getHabbo());
    }
}
