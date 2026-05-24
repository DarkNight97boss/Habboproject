package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/UpdateModToolIssue.class */
public class UpdateModToolIssue implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateModToolIssue.class);
    private final ModToolIssue issue;

    public UpdateModToolIssue(ModToolIssue modToolIssue) {
        this.issue = modToolIssue;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE support_tickets SET state = ?, type = ?, mod_id = ?, category = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.issue.state.getState());
                    preparedStatementPrepareStatement.setInt(2, this.issue.type.getType());
                    preparedStatementPrepareStatement.setInt(3, this.issue.modId);
                    preparedStatementPrepareStatement.setInt(4, this.issue.category);
                    preparedStatementPrepareStatement.setInt(5, this.issue.id);
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
