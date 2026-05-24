package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.guilds.SettingsState;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumThreadsComposer;
import com.eu.habbo.messages.outgoing.guilds.forums.ThreadUpdatedMessageComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumThreadUpdateEvent.class */
public class GuildForumThreadUpdateEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        boolean z = this.packet.readBoolean();
        boolean z2 = this.packet.readBoolean();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        ForumThread byId = ForumThread.getById(iIntValue2);
        if (guild == null || byId == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
        GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, this.client.getHabbo().getHabboInfo().getId());
        if (guildMember == null) {
            this.client.sendResponse(new ConnectionErrorComposer(401));
            return;
        }
        boolean z3 = guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || guildMember.getRank().type < GuildRank.MEMBER.type;
        if ((guild.canModForum() == SettingsState.OWNER && guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() && !zHasPermission) || (guild.canModForum() == SettingsState.ADMINS && !z3 && !zHasPermission)) {
            this.client.sendResponse(new ConnectionErrorComposer(403));
            return;
        }
        boolean z4 = z != byId.isPinned();
        if (z4) {
            this.client.sendResponse(new BubbleAlertComposer(z ? BubbleAlertKeys.FORUMS_THREAD_PINNED.key : BubbleAlertKeys.FORUMS_THREAD_UNPINNED.key).compose());
        }
        if (z2 != byId.isLocked()) {
            this.client.sendResponse(new BubbleAlertComposer(z2 ? BubbleAlertKeys.FORUMS_THREAD_LOCKED.key : BubbleAlertKeys.FORUMS_THREAD_UNLOCKED.key).compose());
        }
        byId.setPinned(z);
        byId.setLocked(z2);
        byId.run();
        this.client.sendResponse(new ThreadUpdatedMessageComposer(guild, byId, this.client.getHabbo(), z, z2));
        if (z4) {
            this.client.sendResponse(new GuildForumThreadsComposer(guild, 0));
        }
    }
}
