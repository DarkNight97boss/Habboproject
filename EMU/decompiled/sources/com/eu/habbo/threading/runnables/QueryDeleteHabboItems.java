package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.map.TIntObjectMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/QueryDeleteHabboItems.class */
public class QueryDeleteHabboItems implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDeleteHabboItems.class);
    private TIntObjectMap<HabboItem> items;

    public QueryDeleteHabboItems(TIntObjectMap<HabboItem> tIntObjectMap) {
        this.items = tIntObjectMap;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM items WHERE id = ?");
                try {
                    for (HabboItem habboItem : this.items.valueCollection()) {
                        if (habboItem.getRoomId() <= 0) {
                            preparedStatementPrepareStatement.setInt(1, habboItem.getId());
                            preparedStatementPrepareStatement.addBatch();
                        }
                    }
                    preparedStatementPrepareStatement.executeBatch();
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
        this.items.clear();
    }
}
