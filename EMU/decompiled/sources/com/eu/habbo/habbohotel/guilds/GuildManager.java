package com.eu.habbo.habbohotel.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.guilds.forums.ForumView;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.guilds.GuildJoinErrorComposer;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildManager.class */
public class GuildManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);
    private final THashMap<GuildPartType, THashMap<Integer, GuildPart>> guildParts;
    private final TIntObjectMap<Guild> guilds;
    private final THashSet<ForumView> views = new THashSet<>();

    public GuildManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.guildParts = new THashMap<>();
        this.guilds = TCollections.synchronizedMap(new TIntObjectHashMap());
        loadGuildParts();
        loadGuildViews();
        LOGGER.info("Guild Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public void loadGuildParts() {
        this.guildParts.clear();
        for (GuildPartType guildPartType : GuildPartType.values()) {
            this.guildParts.put(guildPartType, new THashMap());
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM guilds_elements");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            ((THashMap) this.guildParts.get(GuildPartType.valueOf(resultSetExecuteQuery.getString("type").toUpperCase()))).put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new GuildPart(resultSetExecuteQuery));
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
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
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
    }

    public void loadGuildViews() {
        this.views.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM guild_forum_views");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.views.add(new ForumView(resultSetExecuteQuery));
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
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
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
    }

    public Guild createGuild(Habbo habbo, int i, String str, String str2, String str3, String str4, int i2, int i3) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ResultSet generatedKeys;
        Guild guild = new Guild(habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername(), i, str, str2, str3, i2, i3, str4);
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO guilds (name, description, room_id, user_id, color_one, color_two, badge, date_created) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setString(1, str2);
                    preparedStatementPrepareStatement.setString(2, str3);
                    preparedStatementPrepareStatement.setInt(3, i);
                    preparedStatementPrepareStatement.setInt(4, guild.getOwnerId());
                    preparedStatementPrepareStatement.setInt(5, i2);
                    preparedStatementPrepareStatement.setInt(6, i3);
                    preparedStatementPrepareStatement.setString(7, str4);
                    preparedStatementPrepareStatement.setInt(8, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.execute();
                    generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                } finally {
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            if (generatedKeys.next()) {
                guild.setId(generatedKeys.getInt(1));
            }
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO guilds_members (guild_id, user_id, level_id, member_since) VALUES (?, ?, ?, ?)", 1);
            try {
                preparedStatementPrepareStatement.setInt(1, guild.getId());
                preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                preparedStatementPrepareStatement.setInt(3, 0);
                preparedStatementPrepareStatement.setInt(4, Emulator.getIntUnixTimestamp());
                preparedStatementPrepareStatement.execute();
                generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                try {
                    if (generatedKeys.next()) {
                        guild.increaseMemberCount();
                    }
                    if (generatedKeys != null) {
                        generatedKeys.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    habbo.getHabboStats().addGuild(guild.getId());
                    return guild;
                } finally {
                }
            } finally {
            }
        } finally {
        }
    }

    public void deleteGuild(Guild guild) {
        TObjectHashIterator it = getGuildMembers(guild).iterator();
        while (it.hasNext()) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(((GuildMember) it.next()).getUserId());
            if (habbo != null) {
                habbo.getHabboStats().removeGuild(guild.getId());
                if (habbo.getHabboStats().guild == guild.getId()) {
                    habbo.getHabboStats().guild = 0;
                }
            }
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET guild_id = ? WHERE guild_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, 0);
                    preparedStatementPrepareStatement.setInt(2, guild.getId());
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM guilds_members WHERE guild_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, guild.getId());
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM guilds WHERE id = ?");
                        try {
                            preparedStatementPrepareStatement.setInt(1, guild.getId());
                            preparedStatementPrepareStatement.execute();
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(guild.getRoomId());
                            if (room != null) {
                                room.setGuild(0);
                            }
                            if (connection != null) {
                                connection.close();
                            }
                        } finally {
                        }
                    } finally {
                    }
                } finally {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void clearInactiveGuilds() {
        ArrayList arrayList = new ArrayList();
        TIntObjectIterator it = this.guilds.iterator();
        int size = this.guilds.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                if (((Guild) it.value()).lastRequested < Emulator.getIntUnixTimestamp() - 300) {
                    arrayList.add(Integer.valueOf(((Guild) it.value()).getId()));
                }
            } catch (NoSuchElementException e) {
            }
        }
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            this.guilds.remove(((Integer) it2.next()).intValue());
        }
    }

    public void joinGuild(Guild guild, GameClient gameClient, int i, boolean z) {
        boolean z2 = false;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE user_id = ?");
                try {
                    if (i == 0) {
                        preparedStatementPrepareStatement.setInt(1, gameClient.getHabbo().getHabboInfo().getId());
                    } else {
                        preparedStatementPrepareStatement.setInt(1, i);
                    }
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next() && resultSetExecuteQuery.getInt(1) >= 100) {
                            if (i == 0) {
                                gameClient.sendResponse(new GuildJoinErrorComposer(1));
                            } else {
                                gameClient.sendResponse(new GuildJoinErrorComposer(5));
                            }
                            z2 = true;
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (!z2) {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND level_id < 3");
                            try {
                                preparedStatementPrepareStatement2.setInt(1, guild.getId());
                                ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement2.executeQuery();
                                try {
                                    if (resultSetExecuteQuery2.next() && resultSetExecuteQuery2.getInt(1) >= 50000) {
                                        gameClient.sendResponse(new GuildJoinErrorComposer(0));
                                        z2 = true;
                                    }
                                    if (resultSetExecuteQuery2 != null) {
                                        resultSetExecuteQuery2.close();
                                    }
                                    if (preparedStatementPrepareStatement2 != null) {
                                        preparedStatementPrepareStatement2.close();
                                    }
                                    if (i == 0 && !z2) {
                                        if (guild.getState() == GuildState.EXCLUSIVE) {
                                            PreparedStatement preparedStatementPrepareStatement3 = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND level_id = 3");
                                            try {
                                                preparedStatementPrepareStatement3.setInt(1, guild.getId());
                                                ResultSet resultSetExecuteQuery3 = preparedStatementPrepareStatement3.executeQuery();
                                                try {
                                                    if (resultSetExecuteQuery3.next() && resultSetExecuteQuery3.getInt(1) >= 100) {
                                                        gameClient.sendResponse(new GuildJoinErrorComposer(3));
                                                        z2 = true;
                                                    }
                                                    if (resultSetExecuteQuery3 != null) {
                                                        resultSetExecuteQuery3.close();
                                                    }
                                                    if (preparedStatementPrepareStatement3 != null) {
                                                        preparedStatementPrepareStatement3.close();
                                                    }
                                                    if (!z2) {
                                                        preparedStatementPrepareStatement3 = connection.prepareStatement("SELECT COUNT(id) as total FROM guilds_members WHERE guild_id = ? AND user_id = ? LIMIT 1");
                                                        try {
                                                            preparedStatementPrepareStatement3.setInt(1, guild.getId());
                                                            preparedStatementPrepareStatement3.setInt(2, gameClient.getHabbo().getHabboInfo().getId());
                                                            resultSetExecuteQuery2 = preparedStatementPrepareStatement3.executeQuery();
                                                            try {
                                                                if (resultSetExecuteQuery2.next()) {
                                                                    if (resultSetExecuteQuery2.getInt(1) >= 1) {
                                                                        z2 = true;
                                                                    }
                                                                }
                                                                if (resultSetExecuteQuery2 != null) {
                                                                    resultSetExecuteQuery2.close();
                                                                }
                                                                if (preparedStatementPrepareStatement3 != null) {
                                                                    preparedStatementPrepareStatement3.close();
                                                                }
                                                            } catch (Throwable th) {
                                                                throw th;
                                                            }
                                                        } catch (Throwable th2) {
                                                            throw th2;
                                                        }
                                                    }
                                                } finally {
                                                    if (resultSetExecuteQuery3 != null) {
                                                        try {
                                                            resultSetExecuteQuery3.close();
                                                        } catch (Throwable th3) {
                                                            th.addSuppressed(th3);
                                                        }
                                                    }
                                                }
                                            } finally {
                                                if (preparedStatementPrepareStatement3 != null) {
                                                    try {
                                                        preparedStatementPrepareStatement3.close();
                                                    } catch (Throwable th4) {
                                                        th2.addSuppressed(th4);
                                                    }
                                                }
                                            }
                                        }
                                        if (!z2) {
                                            PreparedStatement preparedStatementPrepareStatement4 = connection.prepareStatement("INSERT INTO guilds_members (guild_id, user_id, member_since, level_id) VALUES (?, ?, ?, ?)");
                                            try {
                                                preparedStatementPrepareStatement4.setInt(1, guild.getId());
                                                preparedStatementPrepareStatement4.setInt(2, gameClient.getHabbo().getHabboInfo().getId());
                                                preparedStatementPrepareStatement4.setInt(3, Emulator.getIntUnixTimestamp());
                                                preparedStatementPrepareStatement4.setInt(4, guild.getState() == GuildState.EXCLUSIVE ? GuildRank.REQUESTED.type : GuildRank.MEMBER.type);
                                                preparedStatementPrepareStatement4.execute();
                                                if (preparedStatementPrepareStatement4 != null) {
                                                    preparedStatementPrepareStatement4.close();
                                                }
                                            } catch (Throwable th5) {
                                                if (preparedStatementPrepareStatement4 != null) {
                                                    try {
                                                        preparedStatementPrepareStatement4.close();
                                                    } catch (Throwable th6) {
                                                        th5.addSuppressed(th6);
                                                    }
                                                }
                                                throw th5;
                                            }
                                        }
                                    } else if (!z2) {
                                        PreparedStatement preparedStatementPrepareStatement5 = connection.prepareStatement("UPDATE guilds_members SET level_id = ?, member_since = ? WHERE user_id = ? AND guild_id = ?");
                                        try {
                                            preparedStatementPrepareStatement5.setInt(1, GuildRank.MEMBER.type);
                                            preparedStatementPrepareStatement5.setInt(2, Emulator.getIntUnixTimestamp());
                                            preparedStatementPrepareStatement5.setInt(3, i);
                                            preparedStatementPrepareStatement5.setInt(4, guild.getId());
                                            preparedStatementPrepareStatement5.execute();
                                            if (preparedStatementPrepareStatement5 != null) {
                                                preparedStatementPrepareStatement5.close();
                                            }
                                        } catch (Throwable th7) {
                                            if (preparedStatementPrepareStatement5 != null) {
                                                try {
                                                    preparedStatementPrepareStatement5.close();
                                                } catch (Throwable th8) {
                                                    th7.addSuppressed(th8);
                                                }
                                            }
                                            throw th7;
                                        }
                                    }
                                    if (i == 0 && !z2) {
                                        if (guild.getState() == GuildState.EXCLUSIVE) {
                                            guild.increaseRequestCount();
                                        } else {
                                            guild.increaseMemberCount();
                                            gameClient.getHabbo().getHabboStats().addGuild(guild.getId());
                                        }
                                    }
                                } finally {
                                    if (resultSetExecuteQuery2 != null) {
                                        try {
                                            resultSetExecuteQuery2.close();
                                        } catch (Throwable th9) {
                                            th.addSuppressed(th9);
                                        }
                                    }
                                }
                            } catch (Throwable th10) {
                                if (preparedStatementPrepareStatement2 != null) {
                                    try {
                                        preparedStatementPrepareStatement2.close();
                                    } catch (Throwable th11) {
                                        th10.addSuppressed(th11);
                                    }
                                }
                                throw th10;
                            }
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } finally {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th12) {
                                th.addSuppressed(th12);
                            }
                        }
                    }
                } catch (Throwable th13) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th14) {
                            th13.addSuppressed(th14);
                        }
                    }
                    throw th13;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void setAdmin(Guild guild, int i) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE guilds_members SET level_id = ? WHERE user_id = ? AND guild_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, 1);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.setInt(3, guild.getId());
                    preparedStatementPrepareStatement.execute();
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

    public void removeAdmin(Guild guild, int i) {
        if (guild.getOwnerId() == i) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE guilds_members SET level_id = ? WHERE user_id = ? AND guild_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, 2);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.setInt(3, guild.getId());
                    preparedStatementPrepareStatement.execute();
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

    public void removeMember(Guild guild, int i) {
        if (guild.getOwnerId() == i) {
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
        if (habbo != null && habbo.getHabboStats().guild == guild.getId()) {
            habbo.getHabboStats().removeGuild(guild.getId());
            habbo.getHabboStats().guild = 0;
            habbo.getHabboStats().run();
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM guilds_members WHERE user_id = ? AND guild_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, guild.getId());
                    preparedStatementPrepareStatement.execute();
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

    public void addGuild(Guild guild) {
        guild.lastRequested = Emulator.getIntUnixTimestamp();
        this.guilds.put(guild.getId(), guild);
    }

    public GuildMember getGuildMember(Guild guild, Habbo habbo) {
        return getGuildMember(guild.getId(), habbo.getHabboInfo().getId());
    }

    public GuildMember getGuildMember(int i, int i2) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        GuildMember guildMember = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ? AND guilds_members.user_id = ? LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setInt(2, i2);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                if (resultSetExecuteQuery.next()) {
                    guildMember = new GuildMember(resultSetExecuteQuery);
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
                return guildMember;
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

    public THashSet<GuildMember> getGuildMembers(int i) {
        return getGuildMembers(getGuild(i));
    }

    THashSet<GuildMember> getGuildMembers(Guild guild) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashSet<GuildMember> tHashSet = new THashSet<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, guild.getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    tHashSet.add(new GuildMember(resultSetExecuteQuery));
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
    }

    public ArrayList<GuildMember> getGuildMembers(Guild guild, int i, int i2, String str) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList<GuildMember> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  " + rankQuery(i2) + " AND users.username LIKE ? ORDER BY level_id, member_since ASC LIMIT ?, ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, guild.getId());
            preparedStatementPrepareStatement.setString(2, "%" + str + "%");
            preparedStatementPrepareStatement.setInt(3, i * 14);
            preparedStatementPrepareStatement.setInt(4, (i * 14) + 14);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    arrayList.add(new GuildMember(resultSetExecuteQuery));
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
            return arrayList;
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

    public int getGuildMembersCount(Guild guild, int i, int i2, String str) {
        new ArrayList();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  " + rankQuery(i2) + " AND users.username LIKE ? ORDER BY level_id, member_since ASC");
                try {
                    preparedStatementPrepareStatement.setInt(1, guild.getId());
                    preparedStatementPrepareStatement.setString(2, "%" + str + "%");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (!resultSetExecuteQuery.next()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return 0;
                        }
                        int i3 = resultSetExecuteQuery.getInt(1);
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        return i3;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return 0;
        }
    }

    public THashMap<Integer, GuildMember> getOnlyAdmins(Guild guild) {
        THashMap<Integer, GuildMember> tHashMap = new THashMap<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.look, guilds_members.* FROM guilds_members INNER JOIN users ON guilds_members.user_id = users.id WHERE guilds_members.guild_id = ?  " + rankQuery(1));
                try {
                    preparedStatementPrepareStatement.setInt(1, guild.getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("user_id")), new GuildMember(resultSetExecuteQuery));
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
        return tHashMap;
    }

    private String rankQuery(int i) {
        switch (i) {
            case 1:
                return "AND (guilds_members.level_id = 0 OR guilds_members.level_id = 1)";
            case 2:
                return "AND guilds_members.level_id = 3";
            default:
                return "AND guilds_members.level_id >= 0 AND guilds_members.level_id <= 2";
        }
    }

    public Guild getGuild(int i) {
        Guild guild = (Guild) this.guilds.get(i);
        if (guild == null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, rooms.name as room_name, guilds.* FROM guilds INNER JOIN users ON guilds.user_id = users.id INNER JOIN rooms ON rooms.id = guilds.room_id WHERE guilds.id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            if (resultSetExecuteQuery.next()) {
                                guild = new Guild(resultSetExecuteQuery);
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (guild != null) {
                                guild.loadMemberCount();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
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
        }
        if (guild != null) {
            guild.lastRequested = Emulator.getIntUnixTimestamp();
            if (!this.guilds.containsKey(i)) {
                this.guilds.put(i, guild);
            }
        }
        return guild;
    }

    public List<Guild> getGuilds(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList arrayList = new ArrayList();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT guild_id FROM guilds_members WHERE user_id = ? AND level_id <= 2 ORDER BY member_since ASC");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    Guild guild = getGuild(resultSetExecuteQuery.getInt("guild_id"));
                    if (guild != null) {
                        arrayList.add(guild);
                    }
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
            return arrayList;
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

    public List<Guild> getAllGuilds() {
        ArrayList arrayList = new ArrayList();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT id FROM guilds ORDER BY id DESC LIMIT 20");
                try {
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            Guild guild = getGuild(resultSetExecuteQuery.getInt("id"));
                            if (guild != null) {
                                arrayList.add(guild);
                            }
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
        return arrayList;
    }

    public boolean symbolColor(int i) {
        Iterator<GuildPart> it = getSymbolColors().iterator();
        while (it.hasNext()) {
            if (it.next().id == i) {
                return true;
            }
        }
        return false;
    }

    public boolean backgroundColor(int i) {
        Iterator<GuildPart> it = getBackgroundColors().iterator();
        while (it.hasNext()) {
            if (it.next().id == i) {
                return true;
            }
        }
        return false;
    }

    public THashMap<GuildPartType, THashMap<Integer, GuildPart>> getGuildParts() {
        return this.guildParts;
    }

    public Collection<GuildPart> getBases() {
        return ((THashMap) this.guildParts.get(GuildPartType.BASE)).values();
    }

    public GuildPart getBase(int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(GuildPartType.BASE)).get(Integer.valueOf(i));
    }

    public Collection<GuildPart> getSymbols() {
        return ((THashMap) this.guildParts.get(GuildPartType.SYMBOL)).values();
    }

    public GuildPart getSymbol(int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(GuildPartType.SYMBOL)).get(Integer.valueOf(i));
    }

    public Collection<GuildPart> getBaseColors() {
        return ((THashMap) this.guildParts.get(GuildPartType.BASE_COLOR)).values();
    }

    public GuildPart getBaseColor(int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(GuildPartType.BASE_COLOR)).get(Integer.valueOf(i));
    }

    public Collection<GuildPart> getSymbolColors() {
        return ((THashMap) this.guildParts.get(GuildPartType.SYMBOL_COLOR)).values();
    }

    public GuildPart getSymbolColor(int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(GuildPartType.SYMBOL_COLOR)).get(Integer.valueOf(i));
    }

    public Collection<GuildPart> getBackgroundColors() {
        return ((THashMap) this.guildParts.get(GuildPartType.BACKGROUND_COLOR)).values();
    }

    public GuildPart getBackgroundColor(int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(GuildPartType.BACKGROUND_COLOR)).get(Integer.valueOf(i));
    }

    public GuildPart getPart(GuildPartType guildPartType, int i) {
        return (GuildPart) ((THashMap) this.guildParts.get(guildPartType)).get(Integer.valueOf(i));
    }

    public void setGuild(InteractionGuildFurni interactionGuildFurni, int i) {
        interactionGuildFurni.setGuildId(i);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE items SET guild_id = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, interactionGuildFurni.getId());
                    preparedStatementPrepareStatement.execute();
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

    public void dispose() {
        TIntObjectIterator it = this.guilds.iterator();
        int size = this.guilds.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                LOGGER.info("Guild Manager -> Disposed!");
                return;
            }
            it.advance();
            if (((Guild) it.value()).needsUpdate) {
                ((Guild) it.value()).run();
            }
            it.remove();
        }
    }

    public boolean hasViewedForum(int i, int i2) {
        return this.views.stream().anyMatch(forumView -> {
            return forumView.getUserId() == i && forumView.getGuildId() == i2 && forumView.getTimestamp() > Emulator.getIntUnixTimestamp() - 604800;
        });
    }

    public void addView(int i, int i2) {
        ForumView forumView = new ForumView(i, i2, Emulator.getIntUnixTimestamp());
        this.views.add(forumView);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `guild_forum_views`(`user_id`, `guild_id`, `timestamp`) VALUES (?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, forumView.getUserId());
                    preparedStatementPrepareStatement.setInt(2, forumView.getGuildId());
                    preparedStatementPrepareStatement.setInt(3, forumView.getTimestamp());
                    preparedStatementPrepareStatement.execute();
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

    public Set<Guild> getMostViewed() {
        return (Set) ((Map) this.views.stream().filter(forumView -> {
            return forumView.getTimestamp() > Emulator.getIntUnixTimestamp() - 604800;
        }).collect(Collectors.groupingBy((v0) -> {
            return v0.getGuildId();
        }))).entrySet().stream().sorted(Comparator.comparingInt(entry -> {
            return ((List) entry.getValue()).size();
        })).map(entry2 -> {
            return getGuild(((Integer) entry2.getKey()).intValue());
        }).filter(guild -> {
            return guild != null && guild.canReadForum() == SettingsState.EVERYONE;
        }).limit(100L).collect(Collectors.toSet());
    }
}
