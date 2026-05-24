package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
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

public class GuildForumThreadsMessagesEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      int guildId = this.packet.readInt();
      int threadId = this.packet.readInt();
      int index = this.packet.readInt();
      int limit = this.packet.readInt();
      Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(guildId);
      ForumThread thread = ForumThread.getById(threadId);
      boolean hasStaffPermissions = this.client.getHabbo().hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
      if (guild != null && thread != null) {
         GuildMember member = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guildId, this.client.getHabbo().getHabboInfo().getId());
         boolean isGuildAdministrator = guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || member.getRank().equals(GuildRank.ADMIN);
         if (thread.getState() == ForumThreadState.HIDDEN_BY_GUILD_ADMIN && !hasStaffPermissions && !isGuildAdministrator) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FORUMS_ACCESS_DENIED.key).compose());
         } else {
            this.client.sendResponse(new GuildForumCommentsComposer(guildId, threadId, index, thread.getComments(limit, index)));
            this.client.sendResponse(new GuildForumDataComposer(guild, this.client.getHabbo()));
         }
      } else {
         this.client.sendResponse(new ConnectionErrorComposer(404));
      }
   }
}
