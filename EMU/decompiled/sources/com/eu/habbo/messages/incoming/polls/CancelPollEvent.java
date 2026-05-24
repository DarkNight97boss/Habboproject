package com.eu.habbo.messages.incoming.polls;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/polls/CancelPollEvent.class */
public class CancelPollEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelPollEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (Emulator.getGameEnvironment().getPollManager().getPoll(iIntValue) != null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO polls_answers (poll_id, user_id, question_id, answer) VALUES (?, ?, ?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, iIntValue);
                        preparedStatementPrepareStatement.setInt(2, this.client.getHabbo().getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(3, 0);
                        preparedStatementPrepareStatement.setString(4, Emulator.PREVIEW);
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
}
