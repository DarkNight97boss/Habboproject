package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/DatabaseLogger.class */
public class DatabaseLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseLogger.class);
    private final ConcurrentLinkedQueue<DatabaseLoggable> loggables = new ConcurrentLinkedQueue<>();

    public void store(DatabaseLoggable databaseLoggable) {
        this.loggables.add(databaseLoggable);
    }

    public void save() {
        if (Emulator.getDatabase() == null || Emulator.getDatabase().getDataSource() == null || this.loggables.isEmpty()) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            while (!this.loggables.isEmpty()) {
                try {
                    DatabaseLoggable databaseLoggableRemove = this.loggables.remove();
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement(databaseLoggableRemove.getQuery());
                    try {
                        databaseLoggableRemove.log(preparedStatementPrepareStatement);
                        preparedStatementPrepareStatement.executeBatch();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
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
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.error("Exception caught while saving loggables to database.", e);
        }
    }
}
