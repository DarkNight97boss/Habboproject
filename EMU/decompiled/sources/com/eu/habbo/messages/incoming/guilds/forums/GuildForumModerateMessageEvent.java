package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.habbohotel.guilds.forums.ForumThreadState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.guilds.forums.PostUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumModerateMessageEvent.class */
public class GuildForumModerateMessageEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        int iIntValue4 = this.packet.readInt().intValue();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        ForumThread byId = ForumThread.getById(iIntValue2);
        if (guild == null || byId == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        ForumThreadComment commentById = byId.getCommentById(iIntValue3);
        if (commentById == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
        }
        boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
        GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, this.client.getHabbo().getHabboInfo().getId());
        if (guildMember == null) {
            this.client.sendResponse(new ConnectionErrorComposer(401));
            return;
        }
        if (!(guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || guildMember.getRank().equals(GuildRank.ADMIN)) && !zHasPermission) {
            this.client.sendResponse(new ConnectionErrorComposer(403));
            return;
        }
        if (iIntValue4 == ForumThreadState.HIDDEN_BY_GUILD_ADMIN.getStateId() && !zHasPermission) {
            this.client.sendResponse(new ConnectionErrorComposer(403));
            return;
        }
        commentById.setState(ForumThreadState.fromValue(iIntValue4));
        commentById.setAdminId(this.client.getHabbo().getHabboInfo().getId());
        this.client.sendResponse(new PostUpdateMessageComposer(guild.getId(), byId.getThreadId(), commentById));
        Emulator.getThreading().run(commentById);
        switch (iIntValue4) {
            case 1:
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FORUMS_MESSAGE_RESTORED.key).compose());
                break;
            case 10:
            case 20:
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FORUMS_MESSAGE_HIDDEN.key).compose());
                break;
        }
    }
}
