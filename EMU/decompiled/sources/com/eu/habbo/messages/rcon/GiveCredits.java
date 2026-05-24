package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveCredits.class */
public class GiveCredits extends RCONMessage<JSONGiveCredits> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveCredits.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveCredits$JSONGiveCredits.class */
    static class JSONGiveCredits {
        public int user_id;
        public int credits;

        JSONGiveCredits() {
        }
    }

    public GiveCredits() {
        super(JSONGiveCredits.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONGiveCredits jSONGiveCredits) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONGiveCredits.user_id);
        if (habbo != null) {
            habbo.giveCredits(jSONGiveCredits.credits);
            return;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET credits = credits + ? WHERE id = ? LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            this.status = 4;
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, jSONGiveCredits.credits);
            preparedStatementPrepareStatement.setInt(2, jSONGiveCredits.user_id);
            preparedStatementPrepareStatement.execute();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            this.message = "offline";
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
    }
}
