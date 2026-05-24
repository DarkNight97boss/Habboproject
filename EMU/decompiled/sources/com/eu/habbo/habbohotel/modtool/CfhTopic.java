package com.eu.habbo.habbohotel.modtool;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/CfhTopic.class */
public class CfhTopic {
    public final int id;
    public final String name;
    public final CfhActionType action;
    public final boolean ignoreTarget;
    public final String reply;
    public final ModToolPreset defaultSanction;

    public CfhTopic(ResultSet resultSet, ModToolPreset modToolPreset) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name_internal");
        this.action = CfhActionType.get(resultSet.getString("action"));
        this.ignoreTarget = resultSet.getString("ignore_target").equalsIgnoreCase("1");
        this.reply = resultSet.getString("auto_reply");
        this.defaultSanction = modToolPreset;
    }
}
