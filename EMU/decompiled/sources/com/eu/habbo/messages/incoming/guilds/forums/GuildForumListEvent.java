package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumListComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/forums/GuildForumListEvent.class */
public class GuildForumListEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildForumListEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        this.packet.readInt().intValue();
        THashSet<Guild> myForums = null;
        switch (iIntValue) {
            case 0:
                myForums = getActiveForums();
                break;
            case 1:
                myForums = Emulator.getGameEnvironment().getGuildManager().getMostViewed();
                break;
            case 2:
                myForums = getMyForums(this.client.getHabbo().getHabboInfo().getId());
                break;
        }
        if (myForums != null) {
            this.client.sendResponse(new GuildForumListComposer(myForums, this.client.getHabbo(), iIntValue, iIntValue2));
        }
    }

    private THashSet<Guild> getActiveForums() {
        THashSet<Guild> tHashSet = new THashSet<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT `guilds`.`id`, SUM(`guilds_forums_threads`.`posts_count`) AS `post_count` FROM `guilds_forums_threads` LEFT JOIN `guilds` ON `guilds`.`id` = `guilds_forums_threads`.`guild_id` WHERE `guilds`.`read_forum` = 'EVERYONE' AND `guilds_forums_threads`.`created_at` > ? GROUP BY `guilds`.`id` ORDER BY `post_count` DESC LIMIT 100");
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp() - 604800);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(resultSetExecuteQuery.getInt("id"));
                        if (guild != null) {
                            tHashSet.add(guild);
                        }
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            this.client.sendResponse(new ConnectionErrorComposer(500));
        }
        return tHashSet;
    }

    private THashSet<Guild> getMyForums(int i) {
        THashSet<Guild> tHashSet = new THashSet<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT `guilds`.`id` FROM `guilds_members` LEFT JOIN `guilds` ON `guilds`.`id` = `guilds_members`.`guild_id` WHERE `guilds_members`.`user_id` = ? AND `guilds`.`forum` = '1'");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(resultSetExecuteQuery.getInt("id"));
                        if (guild != null) {
                            tHashSet.add(guild);
                        }
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            this.client.sendResponse(new ConnectionErrorComposer(500));
        }
        return tHashSet;
    }
}
