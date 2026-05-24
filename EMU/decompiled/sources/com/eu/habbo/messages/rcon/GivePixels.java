package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GivePixels.class */
public class GivePixels extends RCONMessage<JSONGivePixels> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GivePixels.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GivePixels$JSONGivePixels.class */
    static class JSONGivePixels {
        public int user_id;
        public int pixels;

        JSONGivePixels() {
        }
    }

    public GivePixels() {
        super(JSONGivePixels.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONGivePixels jSONGivePixels) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONGivePixels.user_id);
        if (habbo != null) {
            habbo.givePixels(jSONGivePixels.pixels);
            return;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_currency SET users_currency.amount = users_currency.amount + ? WHERE users_currency.user_id = ? AND users_currency.type = 0");
            } finally {
            }
        } catch (SQLException e) {
            this.status = 4;
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, jSONGivePixels.pixels);
            preparedStatementPrepareStatement.setInt(2, jSONGivePixels.user_id);
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
