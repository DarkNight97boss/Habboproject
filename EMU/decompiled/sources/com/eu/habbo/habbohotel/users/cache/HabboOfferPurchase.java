package com.eu.habbo.habbohotel.users.cache;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/cache/HabboOfferPurchase.class */
public class HabboOfferPurchase {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboOfferPurchase.class);
    private final int userId;
    private final int offerId;
    private int state;
    private int amount;
    private int lastPurchaseTimestamp;
    private boolean needsUpdate = false;

    public HabboOfferPurchase(ResultSet resultSet) throws SQLException {
        this.userId = resultSet.getInt("user_id");
        this.offerId = resultSet.getInt("offer_id");
        this.state = resultSet.getInt("state");
        this.amount = resultSet.getInt("amount");
        this.lastPurchaseTimestamp = resultSet.getInt("last_purchase");
    }

    private HabboOfferPurchase(int i, int i2) {
        this.userId = i;
        this.offerId = i2;
    }

    public static HabboOfferPurchase getOrCreate(Habbo habbo, int i) {
        HabboOfferPurchase habboOfferPurchase = habbo.getHabboStats().getHabboOfferPurchase(i);
        if (habboOfferPurchase == null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_target_offer_purchases (user_id, offer_id) VALUES (?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(2, i);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        habboOfferPurchase = new HabboOfferPurchase(habbo.getHabboInfo().getId(), i);
                        habbo.getHabboStats().addHabboOfferPurchase(habboOfferPurchase);
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
                return null;
            }
        }
        return habboOfferPurchase;
    }

    public int getOfferId() {
        return this.offerId;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int i) {
        this.state = i;
        this.needsUpdate = true;
    }

    public int getAmount() {
        return this.amount;
    }

    public void incrementAmount(int i) {
        this.amount += i;
        this.needsUpdate = true;
    }

    public int getLastPurchaseTimestamp() {
        return this.lastPurchaseTimestamp;
    }

    public void setLastPurchaseTimestamp(int i) {
        this.lastPurchaseTimestamp = i;
        this.needsUpdate = true;
    }

    public void update(int i, int i2) {
        this.amount += i;
        this.lastPurchaseTimestamp = i2;
        this.needsUpdate = true;
    }

    public boolean needsUpdate() {
        return this.needsUpdate;
    }
}
