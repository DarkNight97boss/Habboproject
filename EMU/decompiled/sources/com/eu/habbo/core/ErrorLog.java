package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/ErrorLog.class */
public class ErrorLog implements DatabaseLoggable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLog.class);
    private static final String QUERY = "INSERT INTO emulator_errors (timestamp, version, build_hash, type, stacktrace) VALUES (?, ?, ?, ?, ?)";
    public final String version;
    public final String buildHash;
    public final int timeStamp;
    public final String type;
    public final String stackTrace;

    public ErrorLog(String str, Throwable th) {
        this.version = Emulator.version;
        this.buildHash = Emulator.version;
        this.timeStamp = Emulator.getIntUnixTimestamp();
        this.type = str;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        this.stackTrace = stringWriter.toString();
        try {
            printWriter.close();
            stringWriter.close();
        } catch (IOException e) {
            LOGGER.error("Exception caught", e);
        }
    }

    public ErrorLog(String str, String str2) {
        this.version = Emulator.version;
        this.buildHash = Emulator.build;
        this.timeStamp = Emulator.getIntUnixTimestamp();
        this.type = str;
        this.stackTrace = str2;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public String getQuery() {
        return QUERY;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public void log(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.timeStamp);
        preparedStatement.setString(2, this.version);
        preparedStatement.setString(3, this.buildHash);
        preparedStatement.setString(4, this.type);
        preparedStatement.setString(5, this.stackTrace);
        preparedStatement.addBatch();
    }
}
