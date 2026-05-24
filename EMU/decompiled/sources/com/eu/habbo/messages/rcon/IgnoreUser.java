package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/IgnoreUser.class */
public class IgnoreUser extends RCONMessage<JSONIgnoreUser> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreUser.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/IgnoreUser$JSONIgnoreUser.class */
    static class JSONIgnoreUser {
        public int user_id;
        public int target_id;

        JSONIgnoreUser() {
        }
    }

    public IgnoreUser() {
        super(JSONIgnoreUser.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONIgnoreUser jSONIgnoreUser) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONIgnoreUser.user_id);
        if (habbo != null) {
            habbo.getHabboStats().ignoreUser(habbo.getClient(), jSONIgnoreUser.target_id);
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_ignored (user_id, target_id) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, jSONIgnoreUser.user_id);
                    preparedStatementPrepareStatement.setInt(2, jSONIgnoreUser.target_id);
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
        this.message = "offline";
    }
}
