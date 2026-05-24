package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumDataEvent.class */
public class GuildForumDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild == null) {
            return;
        }
        this.client.sendResponse(new GuildForumDataComposer(guild, this.client.getHabbo()));
        if (Emulator.getGameEnvironment().getGuildManager().hasViewedForum(this.client.getHabbo().getHabboInfo().getId(), iIntValue)) {
            return;
        }
        Emulator.getGameEnvironment().getGuildManager().addView(this.client.getHabbo().getHabboInfo().getId(), iIntValue);
    }
}
