package com.eu.habbo.habbohotel.messenger;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/messenger/Message.class */
public class Message implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);
    private final int fromId;
    private final int toId;
    private final int timestamp = Emulator.getIntUnixTimestamp();
    private String message;

    public Message(int i, int i2, String str) {
        this.fromId = i;
        this.toId = i2;
        this.message = str;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (Messenger.SAVE_PRIVATE_CHATS) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO chatlogs_private (user_from_id, user_to_id, message, timestamp) VALUES (?, ?, ?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.fromId);
                        preparedStatementPrepareStatement.setInt(2, this.toId);
                        preparedStatementPrepareStatement.setString(3, this.message);
                        preparedStatementPrepareStatement.setInt(4, this.timestamp);
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

    public int getToId() {
        return this.toId;
    }

    public int getFromId() {
        return this.fromId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public int getTimestamp() {
        return this.timestamp;
    }
}
