package com.eu.habbo.habbohotel.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.plugin.events.guilds.forums.GuildForumThreadCommentBeforeCreated;
import com.eu.habbo.plugin.events.guilds.forums.GuildForumThreadCommentCreated;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/forums/ForumThreadComment.class */
public class ForumThreadComment implements Runnable, ISerialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumThreadComment.class);
    private static THashMap<Integer, ForumThreadComment> forumCommentsCache = new THashMap<>();
    private final int commentId;
    private final int threadId;
    private final int userId;
    private final String message;
    private final int createdAt;
    private ForumThreadState state;
    private int adminId;
    private int index = -1;
    private boolean needsUpdate = false;

    public ForumThreadComment(int i, int i2, int i3, String str, int i4, ForumThreadState forumThreadState, int i5) {
        this.commentId = i;
        this.threadId = i2;
        this.userId = i3;
        this.message = str;
        this.createdAt = i4;
        this.state = forumThreadState;
        this.adminId = i5;
    }

    public ForumThreadComment(ResultSet resultSet) throws SQLException {
        this.commentId = resultSet.getInt("id");
        this.threadId = resultSet.getInt("thread_id");
        this.userId = resultSet.getInt("user_id");
        this.message = resultSet.getString("message");
        this.createdAt = resultSet.getInt("created_at");
        this.state = ForumThreadState.fromValue(resultSet.getInt("state"));
        this.adminId = resultSet.getInt("admin_id");
    }

    public static ForumThreadComment getById(int i) {
        ForumThreadComment forumThreadComment = (ForumThreadComment) forumCommentsCache.get(Integer.valueOf(i));
        if (forumThreadComment != null) {
            return forumThreadComment;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM `guilds_forums_comments` WHERE `id` = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            forumThreadComment = new ForumThreadComment(resultSetExecuteQuery);
                            cacheComment(forumThreadComment);
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return forumThreadComment;
    }

    public static void cacheComment(ForumThreadComment forumThreadComment) {
        forumCommentsCache.put(Integer.valueOf(forumThreadComment.commentId), forumThreadComment);
    }

    public static void clearCache() {
        forumCommentsCache.clear();
    }

    public static ForumThreadComment create(ForumThread forumThread, Habbo habbo, String str) throws Exception {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ForumThreadComment forumThreadComment = null;
        if (((GuildForumThreadCommentBeforeCreated) Emulator.getPluginManager().fireEvent(new GuildForumThreadCommentBeforeCreated(forumThread, habbo, str))).isCancelled()) {
            return null;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `guilds_forums_comments`(`thread_id`, `user_id`, `message`, `created_at`) VALUES (?, ?, ?, ?);", 1);
            } catch (Throwable th) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            int intUnixTimestamp = Emulator.getIntUnixTimestamp();
            preparedStatementPrepareStatement.setInt(1, forumThread.getThreadId());
            preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
            preparedStatementPrepareStatement.setString(3, str);
            preparedStatementPrepareStatement.setInt(4, intUnixTimestamp);
            if (preparedStatementPrepareStatement.executeUpdate() < 1) {
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                return null;
            }
            ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                forumThreadComment = new ForumThreadComment(generatedKeys.getInt(1), forumThread.getThreadId(), habbo.getHabboInfo().getId(), str, intUnixTimestamp, ForumThreadState.OPEN, 0);
                Emulator.getPluginManager().fireEvent(new GuildForumThreadCommentCreated(forumThreadComment));
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return forumThreadComment;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public int getCommentId() {
        return this.commentId;
    }

    public int getThreadId() {
        return this.threadId;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getMessage() {
        return this.message;
    }

    public int getCreatedAt() {
        return this.createdAt;
    }

    public ForumThreadState getState() {
        return this.state;
    }

    public void setState(ForumThreadState forumThreadState) {
        this.state = forumThreadState;
        this.needsUpdate = true;
    }

    public int getAdminId() {
        return this.adminId;
    }

    public void setAdminId(int i) {
        this.adminId = i;
        this.needsUpdate = true;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public Habbo getHabbo() {
        return Emulator.getGameEnvironment().getHabboManager().getHabbo(this.userId);
    }

    public ForumThread getThread() {
        try {
            return ForumThread.getById(this.threadId);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(this.userId);
        HabboInfo habboInfo2 = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(this.adminId);
        serverMessage.appendInt(Integer.valueOf(this.commentId));
        serverMessage.appendInt(Integer.valueOf(this.index));
        serverMessage.appendInt(Integer.valueOf(this.userId));
        serverMessage.appendString(habboInfo != null ? habboInfo.getUsername() : Emulator.PREVIEW);
        serverMessage.appendString(habboInfo != null ? habboInfo.getLook() : Emulator.PREVIEW);
        serverMessage.appendInt(Integer.valueOf(Emulator.getIntUnixTimestamp() - this.createdAt));
        serverMessage.appendString(this.message);
        serverMessage.appendByte(Integer.valueOf(this.state.getStateId()));
        serverMessage.appendInt(Integer.valueOf(this.adminId));
        serverMessage.appendString(habboInfo2 != null ? habboInfo2.getUsername() : Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(habboInfo != null ? habboInfo.getHabboStats().forumPostsCount : 0));
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE guilds_forums_comments SET `state` = ?, `admin_id` = ? WHERE `id` = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.state.getStateId());
                        preparedStatementPrepareStatement.setInt(2, this.adminId);
                        preparedStatementPrepareStatement.setInt(3, this.commentId);
                        preparedStatementPrepareStatement.execute();
                        this.needsUpdate = false;
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
            }
        }
    }
}
