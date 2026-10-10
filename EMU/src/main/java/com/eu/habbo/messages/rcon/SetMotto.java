package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetMotto extends RCONMessage<SetMotto.SetMottoJSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMotto.class);

    public SetMotto() {
        super(SetMottoJSON.class);
    }

    @Override
    public void handle(Gson gson, SetMottoJSON json) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);

        if (habbo != null) {
            habbo.getHabboInfo().setMotto(json.motto);
            com.eu.habbo.habbohotel.rooms.Room cur = habbo.getHabboInfo().getCurrentRoom();
            if (cur != null) cur.sendComposer(new RoomUserDataComposer(habbo).compose()); // Fix audit: NPE fuori stanza
            Emulator.getThreading().run(habbo.getHabboInfo()); // persisti subito
        } else {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE users SET motto = ? WHERE id = ? LIMIT 1")) {
                    statement.setString(1, json.motto);
                    statement.setInt(2, json.user_id);
                    statement.execute();
                }
            } catch (SQLException e) {
                LOGGER.error("Eccezione intercettata", e);
            }
        }
    }

    static class SetMottoJSON {

        public int user_id;


        public String motto;
    }
}