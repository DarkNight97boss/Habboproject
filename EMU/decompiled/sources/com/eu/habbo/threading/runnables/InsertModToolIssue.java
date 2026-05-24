package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/InsertModToolIssue.class */
public class InsertModToolIssue implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertModToolIssue.class);
    private final ModToolIssue issue;

    public InsertModToolIssue(ModToolIssue modToolIssue) {
        this.issue = modToolIssue;
    }

    @Override // java.lang.Runnable
    public void run() {
        Connection connection;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO support_tickets (state, timestamp, score, sender_id, reported_id, room_id, mod_id, issue, category, group_id, thread_id, comment_id, photo_item_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            try {
                preparedStatementPrepareStatement.setInt(1, this.issue.state.getState());
                preparedStatementPrepareStatement.setInt(2, this.issue.timestamp);
                preparedStatementPrepareStatement.setInt(3, this.issue.priority);
                preparedStatementPrepareStatement.setInt(4, this.issue.senderId);
                preparedStatementPrepareStatement.setInt(5, this.issue.reportedId);
                preparedStatementPrepareStatement.setInt(6, this.issue.roomId);
                preparedStatementPrepareStatement.setInt(7, this.issue.modId);
                preparedStatementPrepareStatement.setString(8, this.issue.message);
                preparedStatementPrepareStatement.setInt(9, this.issue.category);
                preparedStatementPrepareStatement.setInt(10, this.issue.groupId);
                preparedStatementPrepareStatement.setInt(11, this.issue.threadId);
                preparedStatementPrepareStatement.setInt(12, this.issue.commentId);
                preparedStatementPrepareStatement.setInt(13, this.issue.photoItem != null ? this.issue.photoItem.getId() : -1);
                preparedStatementPrepareStatement.execute();
                ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                try {
                    if (generatedKeys.first()) {
                        this.issue.id = generatedKeys.getInt(1);
                    }
                    if (generatedKeys != null) {
                        generatedKeys.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    try {
                        connection = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE users_settings SET cfh_send = cfh_send + 1 WHERE user_id = ?");
                            try {
                                preparedStatementPrepareStatement2.setInt(1, this.issue.senderId);
                                preparedStatementPrepareStatement2.execute();
                                if (preparedStatementPrepareStatement2 != null) {
                                    preparedStatementPrepareStatement2.close();
                                }
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (Throwable th) {
                                if (preparedStatementPrepareStatement2 != null) {
                                    try {
                                        preparedStatementPrepareStatement2.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        } finally {
                        }
                    } catch (SQLException e2) {
                        LOGGER.error("Caught SQL exception", e2);
                    }
                } catch (Throwable th3) {
                    if (generatedKeys != null) {
                        try {
                            generatedKeys.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (preparedStatementPrepareStatement != null) {
                    try {
                        preparedStatementPrepareStatement.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable th7) {
                    th.addSuppressed(th7);
                }
            }
        }
    }
}
