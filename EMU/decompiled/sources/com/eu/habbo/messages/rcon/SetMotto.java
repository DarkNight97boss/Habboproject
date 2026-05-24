package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SetMotto.class */
public class SetMotto extends RCONMessage<SetMottoJSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMotto.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SetMotto$SetMottoJSON.class */
    static class SetMottoJSON {
        public int user_id;
        public String motto;

        SetMottoJSON() {
        }
    }

    public SetMotto() {
        super(SetMottoJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, SetMottoJSON setMottoJSON) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(setMottoJSON.user_id);
        if (habbo != null) {
            habbo.getHabboInfo().setMotto(setMottoJSON.motto);
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET motto = ? WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setString(1, setMottoJSON.motto);
                    preparedStatementPrepareStatement.setInt(2, setMottoJSON.user_id);
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
            LOGGER.error("Caught exception", e);
        }
    }
}
