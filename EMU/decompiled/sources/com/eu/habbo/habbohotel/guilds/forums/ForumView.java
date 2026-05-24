package com.eu.habbo.habbohotel.guilds.forums;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/forums/ForumView.class */
public class ForumView {
    private final int userId;
    private final int guildId;
    private final int timestamp;

    public ForumView(int i, int i2, int i3) {
        this.userId = i;
        this.guildId = i2;
        this.timestamp = i3;
    }

    public ForumView(ResultSet resultSet) throws SQLException {
        this.userId = resultSet.getInt("user_id");
        this.guildId = resultSet.getInt("guild_id");
        this.timestamp = resultSet.getInt("timestamp");
    }

    public int getUserId() {
        return this.userId;
    }

    public int getGuildId() {
        return this.guildId;
    }

    public int getTimestamp() {
        return this.timestamp;
    }
}
