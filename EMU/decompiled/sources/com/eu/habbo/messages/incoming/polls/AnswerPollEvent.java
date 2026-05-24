package com.eu.habbo.messages.incoming.polls;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.polls.Poll;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.wired.WiredRewardAlertComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/polls/AnswerPollEvent.class */
public class AnswerPollEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerPollEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iIntValue3; i++) {
            sb.append(":").append(string);
        }
        if (sb.length() <= 0) {
            return;
        }
        if (iIntValue == 0 && iIntValue2 <= 0) {
            this.client.getHabbo().getHabboInfo().getCurrentRoom().handleWordQuiz(this.client.getHabbo(), sb.toString());
            return;
        }
        StringBuilder sb2 = new StringBuilder(sb.substring(1));
        Poll poll = Emulator.getGameEnvironment().getPollManager().getPoll(iIntValue);
        if (poll != null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO polls_answers(poll_id, user_id, question_id, answer) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE answer=VALUES(answer)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, iIntValue);
                        preparedStatementPrepareStatement.setInt(2, this.client.getHabbo().getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(3, iIntValue2);
                        preparedStatementPrepareStatement.setString(4, sb2.toString());
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
            if (poll.lastQuestionId != iIntValue2 || poll.badgeReward.length() <= 0) {
                return;
            }
            if (this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(poll.badgeReward)) {
                this.client.sendResponse(new WiredRewardAlertComposer(1));
                return;
            }
            HabboBadge habboBadge = new HabboBadge(0, poll.badgeReward, 0, this.client.getHabbo());
            Emulator.getThreading().run(habboBadge);
            this.client.getHabbo().getInventory().getBadgesComponent().addBadge(habboBadge);
            this.client.sendResponse(new AddUserBadgeComposer(habboBadge));
            this.client.sendResponse(new WiredRewardAlertComposer(7));
        }
    }
}
