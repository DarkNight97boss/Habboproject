package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/MuteUser.class */
public class MuteUser extends RCONMessage<JSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MuteUser.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/MuteUser$JSON.class */
    static class JSON {
        public int user_id;
        public int duration;

        JSON() {
        }
    }

    public MuteUser() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
        if (habbo != null) {
            if (json.duration == 0) {
                habbo.unMute();
                return;
            } else {
                habbo.mute(json.duration, false);
                return;
            }
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET mute_end_timestamp = ? WHERE user_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp() + json.duration);
                    preparedStatementPrepareStatement.setInt(2, json.user_id);
                    if (preparedStatementPrepareStatement.executeUpdate() == 0) {
                        this.status = 2;
                    }
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
    }
}
