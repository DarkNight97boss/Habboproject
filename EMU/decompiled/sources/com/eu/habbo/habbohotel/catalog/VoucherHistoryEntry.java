package com.eu.habbo.habbohotel.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/VoucherHistoryEntry.class */
public class VoucherHistoryEntry {
    private final int voucherId;
    private final int userId;
    private final int timestamp;

    public VoucherHistoryEntry(ResultSet resultSet) throws SQLException {
        this.voucherId = resultSet.getInt("voucher_id");
        this.userId = resultSet.getInt("user_id");
        this.timestamp = resultSet.getInt("timestamp");
    }

    public VoucherHistoryEntry(int i, int i2, int i3) {
        this.voucherId = i;
        this.userId = i2;
        this.timestamp = i3;
    }

    public int getVoucherId() {
        return this.voucherId;
    }

    public int getUserId() {
        return this.userId;
    }

    public int getTimestamp() {
        return this.timestamp;
    }
}
