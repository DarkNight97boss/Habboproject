package com.eu.habbo.habbohotel.guilds;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/Guild.class */
public class Guild implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Guild.class);
    public boolean needsUpdate;
    public int lastRequested;
    private int id;
    private int ownerId;
    private String ownerName;
    private String name;
    private String description;
    private int roomId;
    private String roomName;
    private GuildState state;
    private boolean rights;
    private int colorOne;
    private int colorTwo;
    private String badge;
    private int dateCreated;
    private int memberCount;
    private int requestCount;
    private boolean forum;
    private SettingsState readForum;
    private SettingsState postMessages;
    private SettingsState postThreads;
    private SettingsState modForum;

    public Guild(ResultSet resultSet) throws SQLException {
        this.lastRequested = Emulator.getIntUnixTimestamp();
        this.forum = false;
        this.readForum = SettingsState.ADMINS;
        this.postMessages = SettingsState.ADMINS;
        this.postThreads = SettingsState.ADMINS;
        this.modForum = SettingsState.ADMINS;
        this.id = resultSet.getInt("id");
        this.ownerId = resultSet.getInt("user_id");
        this.ownerName = resultSet.getString("username");
        this.name = resultSet.getString("name");
        this.description = resultSet.getString("description");
        this.state = GuildState.values()[resultSet.getInt("state")];
        this.roomId = resultSet.getInt("room_id");
        this.roomName = resultSet.getString("room_name");
        this.rights = resultSet.getString("rights").equalsIgnoreCase("1");
        this.colorOne = resultSet.getInt("color_one");
        this.colorTwo = resultSet.getInt("color_two");
        this.badge = resultSet.getString("badge");
        this.dateCreated = resultSet.getInt("date_created");
        this.forum = resultSet.getString("forum").equalsIgnoreCase("1");
        this.readForum = SettingsState.valueOf(resultSet.getString("read_forum"));
        this.postMessages = SettingsState.valueOf(resultSet.getString("post_messages"));
        this.postThreads = SettingsState.valueOf(resultSet.getString("post_threads"));
        this.modForum = SettingsState.valueOf(resultSet.getString("mod_forum"));
        this.memberCount = 0;
        this.requestCount = 0;
    }

    public Guild(int i, String str, int i2, String str2, String str3, String str4, int i3, int i4, String str5) {
        this.lastRequested = Emulator.getIntUnixTimestamp();
        this.forum = false;
        this.readForum = SettingsState.ADMINS;
        this.postMessages = SettingsState.ADMINS;
        this.postThreads = SettingsState.ADMINS;
        this.modForum = SettingsState.ADMINS;
        this.id = 0;
        this.ownerId = i;
        this.ownerName = str;
        this.roomId = i2;
        this.roomName = str2;
        this.name = str3;
        this.description = str4;
        this.state = GuildState.OPEN;
        this.rights = false;
        this.colorOne = i3;
        this.colorTwo = i4;
        this.badge = str5;
        this.memberCount = 0;
        this.dateCreated = Emulator.getIntUnixTimestamp();
    }

    public void loadMemberCount() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(id) as count FROM guilds_members WHERE level_id < 3 AND guild_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            this.memberCount = resultSetExecuteQuery.getInt(1);
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(id) as count FROM guilds_members WHERE level_id = 3 AND guild_id = ?");
                        try {
                            preparedStatementPrepareStatement.setInt(1, this.id);
                            resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                            try {
                                if (resultSetExecuteQuery.next()) {
                                    this.requestCount = resultSetExecuteQuery.getInt(1);
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
                            } catch (Throwable th) {
                                throw th;
                            }
                        } finally {
                        }
                    } finally {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                    }
                } finally {
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE guilds SET name = ?, description = ?, state = ?, rights = ?, color_one = ?, color_two = ?, badge = ?, read_forum = ?, post_messages = ?, post_threads = ?, mod_forum = ?, forum = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setString(1, this.name);
                        preparedStatementPrepareStatement.setString(2, this.description);
                        preparedStatementPrepareStatement.setInt(3, this.state.state);
                        preparedStatementPrepareStatement.setString(4, this.rights ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(5, this.colorOne);
                        preparedStatementPrepareStatement.setInt(6, this.colorTwo);
                        preparedStatementPrepareStatement.setString(7, this.badge);
                        preparedStatementPrepareStatement.setString(8, this.readForum.name());
                        preparedStatementPrepareStatement.setString(9, this.postMessages.name());
                        preparedStatementPrepareStatement.setString(10, this.postThreads.name());
                        preparedStatementPrepareStatement.setString(11, this.modForum.name());
                        preparedStatementPrepareStatement.setString(12, this.forum ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(13, this.id);
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

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public int getRoomId() {
        return this.roomId;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String str) {
        this.roomName = str;
    }

    public GuildState getState() {
        return this.state;
    }

    public void setState(GuildState guildState) {
        this.state = guildState;
    }

    public boolean getRights() {
        return this.rights;
    }

    public void setRights(boolean z) {
        this.rights = z;
    }

    public int getColorOne() {
        return this.colorOne;
    }

    public void setColorOne(int i) {
        this.colorOne = i;
    }

    public int getColorTwo() {
        return this.colorTwo;
    }

    public void setColorTwo(int i) {
        this.colorTwo = i;
    }

    public String getBadge() {
        return this.badge;
    }

    public void setBadge(String str) {
        this.badge = str;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public int getDateCreated() {
        return this.dateCreated;
    }

    public int getMemberCount() {
        return this.memberCount;
    }

    public void increaseMemberCount() {
        this.memberCount++;
    }

    public void decreaseMemberCount() {
        this.memberCount--;
    }

    public int getRequestCount() {
        return this.requestCount;
    }

    public void increaseRequestCount() {
        this.requestCount++;
    }

    public void decreaseRequestCount() {
        this.requestCount--;
    }

    public boolean hasForum() {
        return this.forum;
    }

    public void setForum(boolean z) {
        this.forum = z;
    }

    public SettingsState canReadForum() {
        return this.readForum;
    }

    public void setReadForum(SettingsState settingsState) {
        this.readForum = settingsState;
    }

    public SettingsState canPostMessages() {
        return this.postMessages;
    }

    public void setPostMessages(SettingsState settingsState) {
        this.postMessages = settingsState;
    }

    public SettingsState canPostThreads() {
        return this.postThreads;
    }

    public void setPostThreads(SettingsState settingsState) {
        this.postThreads = settingsState;
    }

    public SettingsState canModForum() {
        return this.modForum;
    }

    public void setModForum(SettingsState settingsState) {
        this.modForum = settingsState;
    }
}
