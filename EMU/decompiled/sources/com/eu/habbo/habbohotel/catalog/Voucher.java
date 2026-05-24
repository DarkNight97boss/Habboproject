package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/Voucher.class */
public class Voucher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Voucher.class);
    public final int id;
    public final String code;
    public final int credits;
    public final int points;
    public final int pointsType;
    public final int catalogItemId;
    public final int amount;
    public final int limit;
    private final List<VoucherHistoryEntry> history = new ArrayList();

    public Voucher(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.code = resultSet.getString("code");
        this.credits = resultSet.getInt("credits");
        this.points = resultSet.getInt("points");
        this.pointsType = resultSet.getInt("points_type");
        this.catalogItemId = resultSet.getInt("catalog_item_id");
        this.amount = resultSet.getInt("amount");
        this.limit = resultSet.getInt("limit");
        loadHistory();
    }

    private void loadHistory() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM voucher_history WHERE voucher_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.history.add(new VoucherHistoryEntry(resultSetExecuteQuery));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public boolean hasUserExhausted(int i) {
        return this.limit > 0 && Math.toIntExact(this.history.stream().filter(voucherHistoryEntry -> {
            return voucherHistoryEntry.getUserId() == i;
        }).count()) >= this.limit;
    }

    public boolean isExhausted() {
        return this.amount > 0 && this.history.size() >= this.amount;
    }

    public void addHistoryEntry(int i) {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        this.history.add(new VoucherHistoryEntry(this.id, i, intUnixTimestamp));
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO voucher_history (`voucher_id`, `user_id`, `timestamp`) VALUES (?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.id);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.setInt(3, intUnixTimestamp);
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
