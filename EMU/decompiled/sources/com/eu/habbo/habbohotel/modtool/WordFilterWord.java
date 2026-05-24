package com.eu.habbo.habbohotel.modtool;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/WordFilterWord.class */
public class WordFilterWord {
    public final String key;
    public final String replacement;
    public final boolean hideMessage;
    public final boolean autoReport;
    public final int muteTime;

    public WordFilterWord(ResultSet resultSet) throws SQLException {
        this.key = resultSet.getString("key");
        this.replacement = resultSet.getString("replacement");
        this.hideMessage = resultSet.getInt("hide") == 1;
        this.autoReport = resultSet.getInt("report") == 1;
        this.muteTime = resultSet.getInt("mute");
    }

    public WordFilterWord(String str, String str2) {
        this.key = str;
        this.replacement = str2;
        this.hideMessage = false;
        this.autoReport = false;
        this.muteTime = 0;
    }
}
