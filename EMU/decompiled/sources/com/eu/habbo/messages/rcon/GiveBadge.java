package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveBadge.class */
public class GiveBadge extends RCONMessage<GiveBadgeJSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveBadge.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/GiveBadge$GiveBadgeJSON.class */
    static class GiveBadgeJSON {
        public int user_id = -1;
        public String badge;

        GiveBadgeJSON() {
        }
    }

    public GiveBadge() {
        super(GiveBadgeJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, GiveBadgeJSON giveBadgeJSON) {
        if (giveBadgeJSON.user_id == -1) {
            this.status = 2;
            return;
        }
        if (giveBadgeJSON.badge.isEmpty()) {
            this.status = 4;
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(giveBadgeJSON.user_id);
        String str = giveBadgeJSON.user_id + Emulator.PREVIEW;
        if (habbo != null) {
            String username = habbo.getHabboInfo().getUsername();
            for (String str2 : giveBadgeJSON.badge.split(";")) {
                if (habbo.getInventory().getBadgesComponent().hasBadge(str2)) {
                    this.status = 1;
                    this.message += Emulator.getTexts().getValue("commands.error.cmd_badge.already_owned").replace("%user%", username).replace("%badge%", str2) + "\r";
                } else {
                    HabboBadge habboBadge = new HabboBadge(0, str2, 0, habbo);
                    habboBadge.run();
                    habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
                    habbo.getClient().sendResponse(new AddUserBadgeComposer(habboBadge));
                    this.message = Emulator.getTexts().getValue("commands.succes.cmd_badge.given").replace("%user%", username).replace("%badge%", str2);
                }
            }
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                for (String str3 : giveBadgeJSON.badge.split(";")) {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(slot_id) FROM users_badges INNER JOIN users ON users.id = user_id WHERE users.id = ? AND badge_code = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, giveBadgeJSON.user_id);
                        preparedStatementPrepareStatement.setString(2, str3);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            int i = resultSetExecuteQuery.next() ? resultSetExecuteQuery.getInt(1) : 0;
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (i != 0) {
                                this.status = 1;
                                this.message += Emulator.getTexts().getValue("commands.error.cmd_badge.already_owns").replace("%user%", str).replace("%badge%", str3) + "\r";
                            } else {
                                PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO users_badges VALUES (null, (SELECT id FROM users WHERE users.id = ? LIMIT 1), 0, ?)", 1);
                                try {
                                    preparedStatementPrepareStatement2.setInt(1, giveBadgeJSON.user_id);
                                    preparedStatementPrepareStatement2.setString(2, str3);
                                    preparedStatementPrepareStatement2.execute();
                                    if (preparedStatementPrepareStatement2 != null) {
                                        preparedStatementPrepareStatement2.close();
                                    }
                                    this.message = Emulator.getTexts().getValue("commands.succes.cmd_badge.given").replace("%user%", str).replace("%badge%", str3);
                                } catch (Throwable th) {
                                    if (preparedStatementPrepareStatement2 != null) {
                                        try {
                                            preparedStatementPrepareStatement2.close();
                                        } catch (Throwable th2) {
                                            th.addSuppressed(th2);
                                        }
                                    }
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th4) {
                                    th3.addSuppressed(th4);
                                }
                            }
                            throw th3;
                        }
                    } catch (Throwable th5) {
                        if (preparedStatementPrepareStatement != null) {
                            try {
                                preparedStatementPrepareStatement.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                }
                if (connection != null) {
                    connection.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            this.status = 1;
            this.message = e.getMessage();
        }
    }
}
