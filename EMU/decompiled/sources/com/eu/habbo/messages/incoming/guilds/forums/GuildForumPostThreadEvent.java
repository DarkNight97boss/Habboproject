package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumAddCommentComposer;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumThreadMessagesComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumPostThreadEvent.class */
public class GuildForumPostThreadEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public int getRatelimit() {
        return Outgoing.CraftableProductsComposer;
    }

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
        if (guild == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        if (string2.length() < 10 || string2.length() > 4000 || (iIntValue2 == 0 && (string.length() < 10 || string.length() > 120))) {
            this.client.sendResponse(new ConnectionErrorComposer(RentableSpaceInfoComposer.CANT_RENT_GENERIC));
            return;
        }
        boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
        GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(iIntValue, this.client.getHabbo().getHabboInfo().getId());
        ForumThread byId = ForumThread.getById(iIntValue2);
        if (iIntValue2 == 0) {
            if (guild.canPostThreads().state != 0 && ((guild.canPostThreads().state != 1 || guildMember == null) && ((guild.canPostThreads().state != 2 || guildMember == null || guildMember.getRank().type >= GuildRank.MEMBER.type) && ((guild.canPostThreads().state != 3 || guild.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()) && !zHasPermission)))) {
                this.client.sendResponse(new ConnectionErrorComposer(403));
                return;
            }
            ForumThread forumThreadCreate = ForumThread.create(guild, this.client.getHabbo(), string, string2);
            if (forumThreadCreate == null) {
                this.client.sendResponse(new ConnectionErrorComposer(500));
                return;
            }
            this.client.getHabbo().getHabboStats().forumPostsCount++;
            forumThreadCreate.setPostsCount(forumThreadCreate.getPostsCount() + 1);
            this.client.sendResponse(new GuildForumThreadMessagesComposer(forumThreadCreate));
            return;
        }
        if (byId == null) {
            this.client.sendResponse(new ConnectionErrorComposer(404));
            return;
        }
        if (guild.canPostMessages().state != 0 && ((guild.canPostMessages().state != 1 || guildMember == null) && ((guild.canPostMessages().state != 2 || guildMember == null || guildMember.getRank().type >= GuildRank.MEMBER.type) && ((guild.canPostMessages().state != 3 || guild.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()) && !zHasPermission)))) {
            this.client.sendResponse(new ConnectionErrorComposer(403));
            return;
        }
        ForumThreadComment forumThreadCommentCreate = ForumThreadComment.create(byId, this.client.getHabbo(), string2);
        if (forumThreadCommentCreate == null) {
            this.client.sendResponse(new ConnectionErrorComposer(500));
            return;
        }
        byId.addComment(forumThreadCommentCreate);
        byId.setUpdatedAt(Emulator.getIntUnixTimestamp());
        this.client.getHabbo().getHabboStats().forumPostsCount++;
        byId.setPostsCount(byId.getPostsCount() + 1);
        this.client.sendResponse(new GuildForumAddCommentComposer(forumThreadCommentCreate));
    }
}
