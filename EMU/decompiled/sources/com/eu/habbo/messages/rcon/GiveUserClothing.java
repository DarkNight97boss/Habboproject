package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.users.UserClothesComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveUserClothing.class */
public class GiveUserClothing extends RCONMessage<JSONGiveUserClothing> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveUserClothing.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveUserClothing$JSONGiveUserClothing.class */
    static class JSONGiveUserClothing {
        public int user_id;
        public int clothing_id;

        JSONGiveUserClothing() {
        }
    }

    public GiveUserClothing() {
        super(JSONGiveUserClothing.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONGiveUserClothing jSONGiveUserClothing) {
        GameClient client;
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONGiveUserClothing.user_id);
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_clothing (user_id, clothing_id) VALUES (?, ?)");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, jSONGiveUserClothing.user_id);
            preparedStatementPrepareStatement.setInt(2, jSONGiveUserClothing.clothing_id);
            preparedStatementPrepareStatement.execute();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (habbo == null || (client = habbo.getClient()) == null) {
                return;
            }
            habbo.getInventory().getWardrobeComponent().getClothing().add(jSONGiveUserClothing.clothing_id);
            client.sendResponse(new UserClothesComposer(habbo));
            client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FIGURESET_REDEEMED.key));
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
