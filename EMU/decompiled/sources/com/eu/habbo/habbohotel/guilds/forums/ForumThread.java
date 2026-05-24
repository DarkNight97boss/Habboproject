package com.eu.habbo.habbohotel.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.plugin.events.guilds.forums.GuildForumThreadBeforeCreated;
import com.eu.habbo.plugin.events.guilds.forums.GuildForumThreadCreated;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/forums/ForumThread.class */
public class ForumThread implements Runnable, ISerialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForumThread.class);
    private static final THashMap<Integer, THashSet<ForumThread>> guildThreadsCache = new THashMap<>();
    private static final THashMap<Integer, ForumThread> forumThreadsCache = new THashMap<>();
    private final int threadId;
    private final int guildId;
    private final int openerId;
    private final String subject;
    private final int createdAt;
    private final THashMap<Integer, ForumThreadComment> comments;
    private int postsCount;
    private int updatedAt;
    private ForumThreadState state;
    private boolean pinned;
    private boolean locked;
    private int adminId;
    private boolean needsUpdate;
    private boolean hasCommentsLoaded;
    private int commentIndex;
    private ForumThreadComment lastComment;

    public ForumThread(int i, int i2, int i3, String str, int i4, int i5, int i6, ForumThreadState forumThreadState, boolean z, boolean z2, int i7, ForumThreadComment forumThreadComment) {
        this.threadId = i;
        this.guildId = i2;
        this.openerId = i3;
        this.subject = str;
        this.postsCount = i4;
        this.createdAt = i5;
        this.updatedAt = i6;
        this.state = forumThreadState;
        this.pinned = z;
        this.locked = z2;
        this.adminId = i7;
        this.lastComment = forumThreadComment;
        this.comments = new THashMap<>();
        this.needsUpdate = false;
        this.hasCommentsLoaded = false;
        this.commentIndex = 0;
    }

    public ForumThread(ResultSet resultSet) throws SQLException {
        this.threadId = resultSet.getInt("id");
        this.guildId = resultSet.getInt("guild_id");
        this.openerId = resultSet.getInt("opener_id");
        this.subject = resultSet.getString("subject");
        this.postsCount = resultSet.getInt("posts_count");
        this.createdAt = resultSet.getInt("created_at");
        this.updatedAt = resultSet.getInt("updated_at");
        this.state = ForumThreadState.fromValue(resultSet.getInt("state"));
        this.pinned = resultSet.getInt("pinned") > 0;
        this.locked = resultSet.getInt("locked") > 0;
        this.adminId = resultSet.getInt("admin_id");
        this.lastComment = null;
        try {
            this.lastComment = ForumThreadComment.getById(resultSet.getInt("last_comment_id"));
        } catch (SQLException e) {
            LOGGER.error("ForumThread last_comment_id exception", e);
        }
        this.comments = new THashMap<>();
        this.needsUpdate = false;
        this.hasCommentsLoaded = false;
        this.commentIndex = 0;
    }

    public static ForumThread create(Guild guild, Habbo habbo, String str, String str2) throws Exception {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ForumThread forumThread = null;
        if (((GuildForumThreadBeforeCreated) Emulator.getPluginManager().fireEvent(new GuildForumThreadBeforeCreated(guild, habbo, str, str2))).isCancelled()) {
            return null;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `guilds_forums_threads`(`guild_id`, `opener_id`, `subject`, `created_at`, `updated_at`) VALUES (?, ?, ?, ?, ?)", 1);
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
            preparedStatementPrepareStatement.setInt(1, guild.getId());
            preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
            preparedStatementPrepareStatement.setString(3, str);
            preparedStatementPrepareStatement.setInt(4, intUnixTimestamp);
            preparedStatementPrepareStatement.setInt(5, intUnixTimestamp);
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
                forumThread = new ForumThread(generatedKeys.getInt(1), guild.getId(), habbo.getHabboInfo().getId(), str, 0, intUnixTimestamp, intUnixTimestamp, ForumThreadState.OPEN, false, false, 0, null);
                cacheThread(forumThread);
                forumThread.addComment(ForumThreadComment.create(forumThread, habbo, str2));
                Emulator.getPluginManager().fireEvent(new GuildForumThreadCreated(forumThread));
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return forumThread;
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

    public static THashSet<ForumThread> getByGuildId(int i) {
        Connection connection;
        if (guildThreadsCache.containsKey(Integer.valueOf(i))) {
            guildThreadsCache.get(Integer.valueOf(i));
        }
        if (0 != 0) {
            return null;
        }
        THashSet<ForumThread> tHashSet = new THashSet<>();
        guildThreadsCache.put(Integer.valueOf(i), tHashSet);
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT A.*, B.`id` AS `last_comment_id` FROM guilds_forums_threads A JOIN (SELECT * FROM `guilds_forums_comments` WHERE `id` IN (SELECT MAX(id) FROM `guilds_forums_comments` B GROUP BY `thread_id` ORDER BY B.`id` ASC ) ORDER BY `id` DESC ) B ON A.`id` = B.`thread_id` WHERE A.`guild_id` = ? ORDER BY A.`pinned` DESC, B.`created_at` DESC ");
            try {
                preparedStatementPrepareStatement.setInt(1, i);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        ForumThread forumThread = new ForumThread(resultSetExecuteQuery);
                        synchronized (tHashSet) {
                            tHashSet.add(forumThread);
                        }
                        cacheThread(forumThread);
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
                return tHashSet;
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
    }

    public static ForumThread getById(int i) throws SQLException {
        Connection connection;
        ForumThread forumThread = (ForumThread) forumThreadsCache.get(Integer.valueOf(i));
        if (forumThread != null) {
            return forumThread;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT A.*, B.`id` AS `last_comment_id` FROM guilds_forums_threads A JOIN (SELECT * FROM `guilds_forums_comments` WHERE `id` IN (SELECT MAX(id) FROM `guilds_forums_comments` B GROUP BY `thread_id` ORDER BY B.`id` ASC ) ORDER BY `id` DESC ) B ON A.`id` = B.`thread_id` WHERE A.`id` = ? ORDER BY A.`pinned` DESC, B.`created_at` DESC LIMIT 1");
            try {
                preparedStatementPrepareStatement.setInt(1, i);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        forumThread = new ForumThread(resultSetExecuteQuery);
                        cacheThread(forumThread);
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
                return forumThread;
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
    }

    private static void cacheThread(ForumThread forumThread) {
        synchronized (forumThreadsCache) {
            forumThreadsCache.put(Integer.valueOf(forumThread.threadId), forumThread);
        }
        THashSet tHashSet = (THashSet) guildThreadsCache.get(Integer.valueOf(forumThread.guildId));
        if (tHashSet == null) {
            tHashSet = new THashSet();
            synchronized (forumThreadsCache) {
                guildThreadsCache.put(Integer.valueOf(forumThread.guildId), tHashSet);
            }
        }
        synchronized (tHashSet) {
            tHashSet.add(forumThread);
        }
    }

    public static void clearCache() {
        Iterator it = guildThreadsCache.values().iterator();
        while (it.hasNext()) {
            TObjectHashIterator it2 = ((THashSet) it.next()).iterator();
            while (it2.hasNext()) {
                ((ForumThread) it2.next()).run();
            }
        }
        synchronized (forumThreadsCache) {
            forumThreadsCache.clear();
        }
        synchronized (guildThreadsCache) {
            guildThreadsCache.clear();
        }
    }

    public int getThreadId() {
        return this.threadId;
    }

    public int getGuildId() {
        return this.guildId;
    }

    public int getOpenerId() {
        return this.openerId;
    }

    public String getSubject() {
        return this.subject;
    }

    public int getCreatedAt() {
        return this.createdAt;
    }

    public int getPostsCount() {
        return this.postsCount;
    }

    public void setPostsCount(int i) {
        this.postsCount = i;
        this.needsUpdate = true;
    }

    public int getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(int i) {
        this.updatedAt = i;
        this.needsUpdate = true;
    }

    public ForumThreadState getState() {
        return this.state;
    }

    public void setState(ForumThreadState forumThreadState) {
        this.state = forumThreadState;
        this.needsUpdate = true;
    }

    public boolean isPinned() {
        return this.pinned;
    }

    public void setPinned(boolean z) {
        this.pinned = z;
        this.needsUpdate = true;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean z) {
        this.locked = z;
        this.needsUpdate = true;
    }

    public int getAdminId() {
        return this.adminId;
    }

    public void setAdminId(int i) {
        this.adminId = i;
        this.needsUpdate = true;
    }

    public ForumThreadComment getLastComment() {
        return this.lastComment;
    }

    public void setLastComment(ForumThreadComment forumThreadComment) {
        this.lastComment = forumThreadComment;
    }

    private void loadComments() {
        if (this.hasCommentsLoaded) {
            return;
        }
        synchronized (this.comments) {
            this.hasCommentsLoaded = true;
            this.commentIndex = 0;
            this.comments.clear();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM `guilds_forums_comments` WHERE `thread_id` = ? ORDER BY `id`");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.threadId);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            addComment(new ForumThreadComment(resultSetExecuteQuery));
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
                } catch (Throwable th3) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public void addComment(ForumThreadComment forumThreadComment) {
        this.comments.put(Integer.valueOf(forumThreadComment.getCommentId()), forumThreadComment);
        forumThreadComment.setIndex(this.commentIndex);
        this.commentIndex++;
        this.lastComment = forumThreadComment;
    }

    public Collection<ForumThreadComment> getComments() {
        if (!this.hasCommentsLoaded) {
            loadComments();
        }
        return this.comments.values();
    }

    public Collection<ForumThreadComment> getComments(int i, int i2) {
        ArrayList arrayList;
        if (!this.hasCommentsLoaded) {
            loadComments();
        }
        synchronized (this.comments) {
            arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList(this.comments.values());
            arrayList2.sort(Comparator.comparingInt((v0) -> {
                return v0.getIndex();
            }));
            Iterator it = arrayList2.iterator();
            while (i2 > 0 && it.hasNext()) {
                it.next();
                i2--;
            }
            while (i > 0 && it.hasNext()) {
                arrayList.add((ForumThreadComment) it.next());
                i--;
            }
        }
        return arrayList;
    }

    public ForumThreadComment getCommentById(int i) {
        ForumThreadComment forumThreadComment;
        if (!this.hasCommentsLoaded) {
            loadComments();
        }
        synchronized (this.comments) {
            forumThreadComment = (ForumThreadComment) this.comments.get(Integer.valueOf(i));
        }
        return forumThreadComment;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.openerId);
        Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.adminId);
        Collection<ForumThreadComment> comments = getComments();
        int size = comments.size();
        int i = 0;
        ForumThreadComment forumThreadComment = this.lastComment;
        if (forumThreadComment == null) {
            for (ForumThreadComment forumThreadComment2 : comments) {
                if (forumThreadComment2.getCreatedAt() > 0) {
                    i++;
                }
                if (forumThreadComment == null || forumThreadComment.getCreatedAt() < forumThreadComment2.getCreatedAt()) {
                    forumThreadComment = forumThreadComment2;
                }
            }
            this.lastComment = forumThreadComment;
        }
        Habbo habbo3 = forumThreadComment != null ? forumThreadComment.getHabbo() : null;
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        serverMessage.appendInt(Integer.valueOf(this.threadId));
        serverMessage.appendInt(Integer.valueOf(this.openerId));
        serverMessage.appendString(habbo != null ? habbo.getHabboInfo().getUsername() : Emulator.PREVIEW);
        serverMessage.appendString(this.subject);
        serverMessage.appendBoolean(Boolean.valueOf(this.pinned));
        serverMessage.appendBoolean(Boolean.valueOf(this.locked));
        serverMessage.appendInt(Integer.valueOf(intUnixTimestamp - this.createdAt));
        serverMessage.appendInt(Integer.valueOf(size));
        serverMessage.appendInt(Integer.valueOf(i));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(habbo3 != null ? habbo3.getHabboInfo().getId() : -1));
        serverMessage.appendString(habbo3 != null ? habbo3.getHabboInfo().getUsername() : Emulator.PREVIEW);
        serverMessage.appendInt(Integer.valueOf(intUnixTimestamp - (forumThreadComment != null ? forumThreadComment.getCreatedAt() : this.updatedAt)));
        serverMessage.appendByte(Integer.valueOf(this.state.getStateId()));
        serverMessage.appendInt(Integer.valueOf(this.adminId));
        serverMessage.appendString(habbo2 != null ? habbo2.getHabboInfo().getUsername() : Emulator.PREVIEW);
        serverMessage.appendInt(Integer.valueOf(this.threadId));
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE `guilds_forums_threads` SET `posts_count` = ?, `updated_at` = ?, `state` = ?, `pinned` = ?, `locked` = ?, `admin_id` = ? WHERE `id` = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.postsCount);
                        preparedStatementPrepareStatement.setInt(2, this.updatedAt);
                        preparedStatementPrepareStatement.setInt(3, this.state.getStateId());
                        preparedStatementPrepareStatement.setInt(4, this.pinned ? 1 : 0);
                        preparedStatementPrepareStatement.setInt(5, this.locked ? 1 : 0);
                        preparedStatementPrepareStatement.setInt(6, this.adminId);
                        preparedStatementPrepareStatement.setInt(7, this.threadId);
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
