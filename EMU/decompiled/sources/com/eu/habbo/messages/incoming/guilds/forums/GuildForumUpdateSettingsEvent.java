package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.SettingsState;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumDataComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumUpdateSettingsEvent.class */
public class GuildForumUpdateSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        int iIntValue4 = this.packet.readInt().intValue();
        int iIntValue5 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        if (guild.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()) {
            this.client.sendResponse(new ConnectionErrorComposer(403));
            return;
        }
        guild.setReadForum(SettingsState.fromValue(iIntValue2));
        guild.setPostMessages(SettingsState.fromValue(iIntValue3));
        guild.setPostThreads(SettingsState.fromValue(iIntValue4));
        guild.setModForum(SettingsState.fromValue(iIntValue5));
        guild.needsUpdate = true;
        Emulator.getThreading().run(guild);
        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FORUMS_FORUM_SETTINGS_UPDATED.key).compose());
        this.client.sendResponse(new GuildForumDataComposer(guild, this.client.getHabbo()));
    }
}
