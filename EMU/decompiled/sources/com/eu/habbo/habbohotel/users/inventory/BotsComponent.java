package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/BotsComponent.class */
public class BotsComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotsComponent.class);
    private final THashMap<Integer, Bot> bots = new THashMap<>();

    public BotsComponent(Habbo habbo) {
        loadBots(habbo);
    }

    private void loadBots(Habbo habbo) {
        Connection connection;
        synchronized (this.bots) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username AS owner_name, bots.* FROM bots INNER JOIN users ON users.id = bots.user_id WHERE user_id = ? AND room_id = 0 ORDER BY id ASC");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            Bot botLoadBot = Emulator.getGameEnvironment().getBotManager().loadBot(resultSetExecuteQuery);
                            if (botLoadBot != null) {
                                this.bots.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), botLoadBot);
                            }
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        }
    }

    public Bot getBot(int i) {
        return (Bot) this.bots.get(Integer.valueOf(i));
    }

    public void addBot(Bot bot) {
        synchronized (this.bots) {
            this.bots.put(Integer.valueOf(bot.getId()), bot);
        }
    }

    public void removeBot(Bot bot) {
        synchronized (this.bots) {
            this.bots.remove(Integer.valueOf(bot.getId()));
        }
    }

    public THashMap<Integer, Bot> getBots() {
        return this.bots;
    }

    public void dispose() {
        synchronized (this.bots) {
            for (Map.Entry entry : this.bots.entrySet()) {
                if (((Bot) entry.getValue()).needsUpdate()) {
                    Emulator.getThreading().run((Runnable) entry.getValue());
                }
            }
            this.bots.clear();
        }
    }
}
