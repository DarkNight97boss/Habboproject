package com.eu.habbo.habbohotel.modtool;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolPreset.class */
public class ModToolPreset {
    public final int id;
    public final String name;
    public final String message;
    public final String reminder;
    public final int banLength;
    public final int muteLength;

    public ModToolPreset(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.message = resultSet.getString("message");
        this.reminder = resultSet.getString("reminder");
        this.banLength = resultSet.getInt("ban_for");
        this.muteLength = resultSet.getInt("mute_for");
    }
}
