package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.DatabaseLoggable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogPurchaseLogEntry.class */
public class CatalogPurchaseLogEntry implements Runnable, DatabaseLoggable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogPurchaseLogEntry.class);
    private static final String QUERY = "INSERT INTO `logs_shop_purchases` (timestamp, user_id, catalog_item_id, item_ids, catalog_name, cost_credits, cost_points, points_type, amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final int timestamp;
    private final int userId;
    private final int catalogItemId;
    private final String itemIds;
    private final String catalogName;
    private final int costCredits;
    private final int costPoints;
    private final int pointsType;
    private final int amount;

    public CatalogPurchaseLogEntry(int i, int i2, int i3, String str, String str2, int i4, int i5, int i6, int i7) {
        this.timestamp = i;
        this.userId = i2;
        this.catalogItemId = i3;
        this.itemIds = str;
        this.catalogName = str2;
        this.costCredits = i4;
        this.costPoints = i5;
        this.pointsType = i6;
        this.amount = i7;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public String getQuery() {
        return QUERY;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public void log(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.timestamp);
        preparedStatement.setInt(2, this.userId);
        preparedStatement.setInt(3, this.catalogItemId);
        preparedStatement.setString(4, this.itemIds);
        preparedStatement.setString(5, this.catalogName);
        preparedStatement.setInt(6, this.costCredits);
        preparedStatement.setInt(7, this.costPoints);
        preparedStatement.setInt(8, this.pointsType);
        preparedStatement.setInt(9, this.amount);
        preparedStatement.addBatch();
    }

    @Override // java.lang.Runnable
    public void run() {
        Emulator.getDatabaseLogger().store(this);
    }
}
