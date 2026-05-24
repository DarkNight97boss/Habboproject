package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveRespect.class */
public class GiveRespect extends RCONMessage<JSONGiveRespect> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveRespect.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveRespect$JSONGiveRespect.class */
    static class JSONGiveRespect {
        public int user_id;
        public int respect_given = 0;
        public int respect_received = 0;
        public int daily_respects = 0;

        JSONGiveRespect() {
        }
    }

    public GiveRespect() {
        super(JSONGiveRespect.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONGiveRespect jSONGiveRespect) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONGiveRespect.user_id);
        if (habbo != null) {
            habbo.getHabboStats().respectPointsReceived += jSONGiveRespect.respect_received;
            habbo.getHabboStats().respectPointsGiven += jSONGiveRespect.respect_given;
            habbo.getHabboStats().respectPointsToGive += jSONGiveRespect.daily_respects;
            habbo.getClient().sendResponse(new UserDataComposer(habbo));
            return;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET respects_given = respects_give + ?, respects_received = respects_received + ?, daily_respect_points = daily_respect_points + ? WHERE user_id = ? LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            this.status = 4;
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, jSONGiveRespect.respect_received);
            preparedStatementPrepareStatement.setInt(2, jSONGiveRespect.respect_given);
            preparedStatementPrepareStatement.setInt(3, jSONGiveRespect.daily_respects);
            preparedStatementPrepareStatement.setInt(4, jSONGiveRespect.user_id);
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
