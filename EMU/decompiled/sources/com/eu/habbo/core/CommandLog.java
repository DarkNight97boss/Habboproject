package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/CommandLog.class */
public class CommandLog implements DatabaseLoggable {
    private static final String INSERT_QUERY = "INSERT INTO commandlogs (`user_id`, `timestamp`, `command`, `params`, `succes`) VALUES (?, ?, ?, ?, ?)";
    private final int userId;
    private final int timestamp = Emulator.getIntUnixTimestamp();
    private final Command command;
    private final String params;
    private final boolean succes;

    public CommandLog(int i, Command command, String str, boolean z) {
        this.userId = i;
        this.command = command;
        this.params = str;
        this.succes = z;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public String getQuery() {
        return INSERT_QUERY;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public void log(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.userId);
        preparedStatement.setInt(2, this.timestamp);
        preparedStatement.setString(3, this.command.getClass().getSimpleName());
        preparedStatement.setString(4, this.params);
        preparedStatement.setString(5, this.succes ? "yes" : "no");
        preparedStatement.addBatch();
    }
}
