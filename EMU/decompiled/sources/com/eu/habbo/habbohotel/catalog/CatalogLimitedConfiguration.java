package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogLimitedConfiguration.class */
public class CatalogLimitedConfiguration implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogLimitedConfiguration.class);
    private final int itemId;
    private final LinkedList<Integer> limitedNumbers;
    private int totalSet;

    public CatalogLimitedConfiguration(int i, LinkedList<Integer> linkedList, int i2) {
        this.itemId = i;
        this.totalSet = i2;
        this.limitedNumbers = linkedList;
        if (Emulator.getConfig().getBoolean("catalog.ltd.random", true)) {
            Collections.shuffle(this.limitedNumbers);
        } else {
            Collections.reverse(this.limitedNumbers);
        }
    }

    public int getNumber() {
        int iIntValue;
        synchronized (this.limitedNumbers) {
            iIntValue = this.limitedNumbers.pop().intValue();
            if (this.limitedNumbers.isEmpty()) {
                Emulator.getGameEnvironment().getCatalogManager().moveCatalogItem(Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(this.itemId), Emulator.getConfig().getInt("catalog.ltd.page.soldout"));
            }
        }
        return iIntValue;
    }

    public void limitedSold(int i, Habbo habbo, HabboItem habboItem) {
        Connection connection;
        synchronized (this.limitedNumbers) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE catalog_items_limited SET user_id = ?, timestamp = ?, item_id = ? WHERE catalog_item_id = ? AND number = ? AND user_id = 0 LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.setInt(3, habboItem.getId());
                    preparedStatementPrepareStatement.setInt(4, i);
                    preparedStatementPrepareStatement.setInt(5, habboItem.getLimitedSells());
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
        }
    }

    public void generateNumbers(int i, int i2) {
        Connection connection;
        synchronized (this.limitedNumbers) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO catalog_items_limited (catalog_item_id, number) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.itemId);
                    for (int i3 = i; i3 <= i2; i3++) {
                        preparedStatementPrepareStatement.setInt(2, i3);
                        preparedStatementPrepareStatement.addBatch();
                        this.limitedNumbers.push(Integer.valueOf(i3));
                    }
                    preparedStatementPrepareStatement.executeBatch();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    this.totalSet += i2;
                    if (Emulator.getConfig().getBoolean("catalog.ltd.random", true)) {
                        Collections.shuffle(this.limitedNumbers);
                    } else {
                        Collections.reverse(this.limitedNumbers);
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
        }
    }

    public int available() {
        return this.limitedNumbers.size();
    }

    public int getTotalSet() {
        return this.totalSet;
    }

    public void setTotalSet(int i) {
        this.totalSet = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE catalog_items SET limited_stack = ?, limited_sells = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.totalSet);
                    preparedStatementPrepareStatement.setInt(2, this.totalSet - available());
                    preparedStatementPrepareStatement.setInt(3, this.itemId);
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
            LOGGER.error("Caught SQL exception", e);
        }
    }
}
