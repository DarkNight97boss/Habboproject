package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.WordFilter;
import com.eu.habbo.habbohotel.modtool.WordFilterWord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/FilterWordCommand.class */
public class FilterWordCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterWordCommand.class);

    public FilterWordCommand() {
        super("cmd_filterword", Emulator.getTexts().getValue("commands.keys.cmd_filterword").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_filterword.missing_word"));
            return true;
        }
        String str = strArr[1];
        String str2 = WordFilter.DEFAULT_REPLACEMENT;
        if (strArr.length == 3) {
            str2 = strArr[2];
        }
        WordFilterWord wordFilterWord = new WordFilterWord(str, str2);
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO wordfilter (`key`, `replacement`) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setString(1, str);
                    preparedStatementPrepareStatement.setString(2, str2);
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_filterword.added").replace("%word%", str).replace("%replacement%", str2));
                    Emulator.getGameEnvironment().getWordFilter().addWord(wordFilterWord);
                    return true;
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
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_filterword.error"));
            return true;
        }
    }
}
