package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumDataComposer.class */
public class GuildForumDataComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildForumDataComposer.class);
    public final Guild guild;
    public Habbo habbo;

    public GuildForumDataComposer(Guild guild, Habbo habbo) {
        this.guild = guild;
        this.habbo = habbo;
    }

    public static void serializeForumData(ServerMessage serverMessage, Guild guild, Habbo habbo) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashSet<ForumThread> byGuildId = ForumThread.getByGuildId(guild.getId());
        int postsCount = 0;
        int i = 0;
        int i2 = 0;
        ForumThreadComment forumThreadComment = null;
        synchronized (byGuildId) {
            TObjectHashIterator it = byGuildId.iterator();
            while (it.hasNext()) {
                ForumThread forumThread = (ForumThread) it.next();
                i2++;
                postsCount += forumThread.getPostsCount();
                ForumThreadComment lastComment = forumThread.getLastComment();
                if (lastComment != null && (forumThreadComment == null || forumThreadComment.getCreatedAt() < lastComment.getCreatedAt())) {
                    forumThreadComment = lastComment;
                }
            }
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) FROM guilds_forums_threads A JOIN ( SELECT * FROM `guilds_forums_comments` WHERE `id` IN ( SELECT id FROM `guilds_forums_comments` B ORDER BY B.`id` ASC ) ORDER BY `id` DESC ) B ON A.`id` = B.`thread_id` WHERE A.`guild_id` = ? AND B.`created_at` > ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, guild.getId());
            preparedStatementPrepareStatement.setInt(2, 0);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                i = resultSetExecuteQuery.getInt(1);
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            serverMessage.appendInt(Integer.valueOf(guild.getId()));
            serverMessage.appendString(guild.getName());
            serverMessage.appendString(guild.getDescription());
            serverMessage.appendString(guild.getBadge());
            serverMessage.appendInt(Integer.valueOf(i2));
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(postsCount));
            serverMessage.appendInt(Integer.valueOf(i));
            serverMessage.appendInt(Integer.valueOf(forumThreadComment != null ? forumThreadComment.getThreadId() : -1));
            serverMessage.appendInt(Integer.valueOf(forumThreadComment != null ? forumThreadComment.getUserId() : -1));
            serverMessage.appendString((forumThreadComment == null || forumThreadComment.getHabbo() == null) ? Emulator.PREVIEW : forumThreadComment.getHabbo().getHabboInfo().getUsername());
            serverMessage.appendInt(Integer.valueOf(forumThreadComment != null ? Emulator.getIntUnixTimestamp() - forumThreadComment.getCreatedAt() : 0));
        } catch (Throwable th) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            this.response.init(Outgoing.GuildForumDataComposer);
            serializeForumData(this.response, this.guild, this.habbo);
            GuildMember guildMember = Emulator.getGameEnvironment().getGuildManager().getGuildMember(this.guild, this.habbo);
            boolean z = guildMember != null && (guildMember.getRank().type < GuildRank.MEMBER.type || this.guild.getOwnerId() == this.habbo.getHabboInfo().getId());
            boolean zHasPermission = this.habbo.hasPermission(Permission.ACC_MODTOOL_TICKET_Q);
            String str = Emulator.PREVIEW;
            String str2 = Emulator.PREVIEW;
            String str3 = Emulator.PREVIEW;
            String str4 = Emulator.PREVIEW;
            if (this.guild.canReadForum().state == 1 && guildMember == null && !zHasPermission) {
                str = "not_member";
            } else if (this.guild.canReadForum().state == 2 && !z && !zHasPermission) {
                str = "not_admin";
            }
            if (this.guild.canPostMessages().state == 1 && guildMember == null && !zHasPermission) {
                str2 = "not_member";
            } else if (this.guild.canPostMessages().state == 2 && !z && !zHasPermission) {
                str2 = "not_admin";
            } else if (this.guild.canPostMessages().state == 3 && this.guild.getOwnerId() != this.habbo.getHabboInfo().getId() && !zHasPermission) {
                str2 = "not_owner";
            }
            if (this.guild.canPostThreads().state == 1 && guildMember == null && !zHasPermission) {
                str3 = "not_member";
            } else if (this.guild.canPostThreads().state == 2 && !z && !zHasPermission) {
                str3 = "not_admin";
            } else if (this.guild.canPostThreads().state == 3 && this.guild.getOwnerId() != this.habbo.getHabboInfo().getId() && !zHasPermission) {
                str3 = "not_owner";
            }
            if (this.guild.canModForum().state == 3 && this.guild.getOwnerId() != this.habbo.getHabboInfo().getId() && !zHasPermission) {
                str4 = "not_owner";
            } else if (!z && !zHasPermission) {
                str4 = "not_admin";
            }
            this.response.appendInt(Integer.valueOf(this.guild.canReadForum().state));
            this.response.appendInt(Integer.valueOf(this.guild.canPostMessages().state));
            this.response.appendInt(Integer.valueOf(this.guild.canPostThreads().state));
            this.response.appendInt(Integer.valueOf(this.guild.canModForum().state));
            this.response.appendString(str);
            this.response.appendString(str2);
            this.response.appendString(str3);
            this.response.appendString(str4);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendBoolean(Boolean.valueOf(this.guild.getOwnerId() == this.habbo.getHabboInfo().getId()));
            if (this.guild.canModForum().state == 3) {
                this.response.appendBoolean(Boolean.valueOf(this.guild.getOwnerId() == this.habbo.getHabboInfo().getId() || zHasPermission));
            } else {
                this.response.appendBoolean(Boolean.valueOf(this.guild.getOwnerId() == this.habbo.getHabboInfo().getId() || zHasPermission || z));
            }
            return this.response;
        } catch (Exception e) {
            e.printStackTrace();
            return new ConnectionErrorComposer(500).compose();
        }
    }
}
