package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/QueryDeleteHabboBadge.class */
class QueryDeleteHabboBadge implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDeleteHabboBadge.class);
    private final String name;
    private final Habbo habbo;

    public QueryDeleteHabboBadge(Habbo habbo, String str) {
        this.name = str;
        this.habbo = habbo;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM user_badges WHERE users_id = ? AND badge_code = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setString(2, this.name);
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
}
