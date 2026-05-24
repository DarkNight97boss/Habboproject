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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ChangeUsername.class */
public class ChangeUsername extends RCONMessage<JSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeUsername.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ChangeUsername$JSON.class */
    static class JSON {
        public int user_id;
        public boolean canChange;

        JSON() {
        }
    }

    public ChangeUsername() {
        super(JSON.class);
    }

    /* JADX WARN: Not initialized variable reg: 10, insn: 0x00e3: MOVE (r0 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]) = (r10 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]), block:B:36:0x00e3 */
    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        Connection connection;
        try {
            if (json.user_id <= 0) {
                this.status = 2;
                this.message = "User not found";
                return;
            }
            boolean z = true;
            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(json.user_id);
            if (habbo != null) {
                if (json.canChange) {
                    habbo.alert(Emulator.getTexts().getValue("rcon.alert.user.change_username"));
                }
                habbo.getHabboStats().allowNameChange = json.canChange;
                habbo.getClient().sendResponse(new UserDataComposer(habbo));
            } else {
                try {
                    try {
                        Connection connection2 = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            PreparedStatement preparedStatementPrepareStatement = connection2.prepareStatement("UPDATE users_settings SET allow_name_change = ? WHERE user_id = ? LIMIT 1");
                            try {
                                preparedStatementPrepareStatement.setBoolean(1, json.canChange);
                                preparedStatementPrepareStatement.setInt(2, json.user_id);
                                z = preparedStatementPrepareStatement.executeUpdate() >= 1;
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
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
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (connection2 != null) {
                            connection2.close();
                        }
                    } catch (Throwable th3) {
                        if (connection != null) {
                            try {
                                connection.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
            this.status = z ? 0 : 1;
            this.message = z ? "Sent successfully." : "There was an error updating this user.";
        } catch (Exception e3) {
            this.status = 4;
            this.message = "Exception occurred";
            LOGGER.error("Exception occurred", e3);
        }
    }
}
