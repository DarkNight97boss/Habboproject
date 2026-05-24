package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GivePoints.class */
public class GivePoints extends RCONMessage<JSONGivePoints> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GivePoints.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GivePoints$JSONGivePoints.class */
    static class JSONGivePoints {
        public int user_id;
        public int points;
        public int type;

        JSONGivePoints() {
        }
    }

    public GivePoints() {
        super(JSONGivePoints.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONGivePoints jSONGivePoints) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONGivePoints.user_id);
        if (habbo != null) {
            habbo.givePoints(jSONGivePoints.type, jSONGivePoints.points);
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_currency (`user_id`, `type`, `amount`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, jSONGivePoints.user_id);
                    preparedStatementPrepareStatement.setInt(2, jSONGivePoints.type);
                    preparedStatementPrepareStatement.setInt(3, jSONGivePoints.points);
                    preparedStatementPrepareStatement.setInt(4, jSONGivePoints.points);
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
            this.status = 4;
            LOGGER.error("Caught SQL exception", e);
        }
        this.message = "offline";
    }
}
