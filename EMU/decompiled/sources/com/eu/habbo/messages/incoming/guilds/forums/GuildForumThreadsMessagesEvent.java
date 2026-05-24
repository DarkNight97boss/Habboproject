package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.guilds.forums.ForumThreadState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumCommentsComposer;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumDataComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumThreadsMessagesEvent.class */
public class GuildForumThreadsMessagesEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        int iIntValue4 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        ForumThread byId = ForumThread.getById(iIntValue2);
        boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
        if (guild == null || byId == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        boolean z = guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, this.client.getHabbo().getHabboInfo().getId()).getRank().equals(GuildRank.ADMIN);
        if (byId.getState() == ForumThreadState.HIDDEN_BY_GUILD_ADMIN && !zHasPermission && !z) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FORUMS_ACCESS_DENIED.key).compose());
        } else {
            this.client.sendResponse(new GuildForumCommentsComposer(iIntValue, iIntValue2, iIntValue3, byId.getComments(iIntValue4, iIntValue3)));
            this.client.sendResponse(new GuildForumDataComposer(guild, this.client.getHabbo()));
        }
    }
}
