package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/AllowTradingCommand.class */
public class AllowTradingCommand extends Command {
    public AllowTradingCommand() {
        super("cmd_allow_trading", Emulator.getTexts().getValue("commands.keys.cmd_allow_trading").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_allow_trading.forgot_username"));
            return true;
        }
        if (strArr.length == 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_allow_trading.forgot_trade").replace("%username%", strArr[1]));
            return true;
        }
        String str = strArr[1];
        String str2 = strArr[2];
        if (!str2.equalsIgnoreCase(Emulator.getTexts().getValue("generic.yes")) && !str2.equalsIgnoreCase(Emulator.getTexts().getValue("generic.no"))) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_allow_trading.incorrect_setting").replace("%enabled%", Emulator.getTexts().getValue("generic.yes")).replace("%disabled%", Emulator.getTexts().getValue("generic.no")));
            return true;
        }
        boolean zEqualsIgnoreCase = str2.equalsIgnoreCase(Emulator.getTexts().getValue("generic.yes"));
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
        if (habbo != null) {
            if (!zEqualsIgnoreCase) {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET tradelock_amount = tradelock_amount + 1 WHERE user_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                        preparedStatementPrepareStatement.executeUpdate();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } finally {
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            habbo.getHabboStats().setAllowTrade(zEqualsIgnoreCase);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_allow_trading." + (zEqualsIgnoreCase ? "enabled" : "disabled")).replace("%username%", strArr[1]));
            habbo.getClient().sendResponse(new UserPerksComposer(habbo));
            return true;
        }
        connection = Emulator.getDatabase().getDataSource().getConnection();
        try {
            preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings INNER JOIN users ON users.id = users_settings.user_id SET can_trade = ?, tradelock_amount = tradelock_amount + ? WHERE users.username LIKE ?");
            try {
                preparedStatementPrepareStatement.setString(1, zEqualsIgnoreCase ? "1" : "0");
                preparedStatementPrepareStatement.setInt(2, zEqualsIgnoreCase ? 0 : 1);
                preparedStatementPrepareStatement.setString(3, str);
                boolean z = preparedStatementPrepareStatement.executeUpdate() > 0;
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                if (z) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_allow_trading." + (zEqualsIgnoreCase ? "enabled" : "disabled")).replace("%username%", strArr[1]));
                    return true;
                }
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_allow_trading.user_not_found").replace("%username%", strArr[1]));
                return true;
            } finally {
                if (preparedStatementPrepareStatement != null) {
                    try {
                        preparedStatementPrepareStatement.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            }
        }
    }
}
