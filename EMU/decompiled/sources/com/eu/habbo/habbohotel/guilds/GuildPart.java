package com.eu.habbo.habbohotel.guilds;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildPart.class */
public class GuildPart {
    public final int id;
    public final String valueA;
    public final String valueB;

    public GuildPart(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.valueA = resultSet.getString("firstvalue");
        this.valueB = resultSet.getString("secondvalue");
    }
}
