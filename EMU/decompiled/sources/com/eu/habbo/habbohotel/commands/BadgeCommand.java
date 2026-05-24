package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/BadgeCommand.class */
public class BadgeCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadgeCommand.class);

    public BadgeCommand() {
        super("cmd_badge", Emulator.getTexts().getValue("commands.keys.cmd_badge").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_badge.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length == 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_badge.forgot_badge").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length != 3) {
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo != null) {
            if (habbo.addBadge(strArr[2])) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_badge.given").replace("%user%", strArr[1]).replace("%badge%", strArr[2]), RoomChatMessageBubbles.ALERT);
                return true;
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_badge.already_owned").replace("%user%", strArr[1]).replace("%badge%", strArr[2]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(strArr[1]);
        if (offlineHabboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_badge.unknown_user"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT `badge_code` FROM `users_badges` WHERE `user_id` = ? AND `badge_code` = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, offlineHabboInfo.getId());
                    preparedStatementPrepareStatement.setString(2, strArr[2]);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        boolean next = resultSetExecuteQuery.next();
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (next) {
                            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_badge.already_owns").replace("%user%", strArr[1]).replace("%badge%", strArr[2]), RoomChatMessageBubbles.ALERT);
                            if (connection != null) {
                                connection.close();
                            }
                            return true;
                        }
                        PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO users_badges (`id`, `user_id`, `slot_id`, `badge_code`) VALUES (null, ?, 0, ?)");
                        try {
                            preparedStatementPrepareStatement2.setInt(1, offlineHabboInfo.getId());
                            preparedStatementPrepareStatement2.setString(2, strArr[2]);
                            preparedStatementPrepareStatement2.execute();
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
                            }
                            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_badge.given").replace("%user%", strArr[1]).replace("%badge%", strArr[2]), RoomChatMessageBubbles.ALERT);
                            if (connection != null) {
                                connection.close();
                            }
                            return true;
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
                    } catch (Throwable th3) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
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
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return true;
        }
    }
}
