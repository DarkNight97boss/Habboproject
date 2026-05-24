package com.eu.habbo.habbohotel.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/marketplace/MarketPlaceOffer.class */
public class MarketPlaceOffer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlaceOffer.class);
    public int avarage;
    public int count;
    private int offerId;
    private Item baseItem;
    private int itemId;
    private int price;
    private int limitedStack;
    private int limitedNumber;
    private int timestamp;
    private int soldTimestamp;
    private MarketPlaceState state;
    private boolean needsUpdate;

    public MarketPlaceOffer(ResultSet resultSet, boolean z) throws SQLException {
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.soldTimestamp = 0;
        this.state = MarketPlaceState.OPEN;
        this.needsUpdate = false;
        this.offerId = resultSet.getInt("id");
        this.price = resultSet.getInt("price");
        this.timestamp = resultSet.getInt("timestamp");
        this.soldTimestamp = resultSet.getInt("sold_timestamp");
        this.baseItem = Emulator.getGameEnvironment().getItemManager().getItem(resultSet.getInt("base_item_id"));
        this.state = MarketPlaceState.getType(resultSet.getInt("state"));
        this.itemId = resultSet.getInt("item_id");
        if (!resultSet.getString("ltd_data").split(":")[1].equals("0")) {
            this.limitedStack = Integer.valueOf(resultSet.getString("ltd_data").split(":")[0]).intValue();
            this.limitedNumber = Integer.valueOf(resultSet.getString("ltd_data").split(":")[1]).intValue();
        }
        if (z) {
            return;
        }
        this.avarage = resultSet.getInt("avg");
        this.count = resultSet.getInt("number");
        this.price = resultSet.getInt("minPrice");
    }

    public MarketPlaceOffer(HabboItem habboItem, int i, Habbo habbo) {
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.soldTimestamp = 0;
        this.state = MarketPlaceState.OPEN;
        this.needsUpdate = false;
        this.price = i;
        this.baseItem = habboItem.getBaseItem();
        this.itemId = habboItem.getId();
        if (habboItem.getLimitedSells() > 0) {
            this.limitedNumber = habboItem.getLimitedSells();
            this.limitedStack = habboItem.getLimitedStack();
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO marketplace_items (item_id, user_id, price, timestamp, state) VALUES (?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, habboItem.getId());
                    preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(3, this.price);
                    preparedStatementPrepareStatement.setInt(4, this.timestamp);
                    preparedStatementPrepareStatement.setString(5, this.state.getState() + Emulator.PREVIEW);
                    preparedStatementPrepareStatement.execute();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    while (generatedKeys.next()) {
                        try {
                            this.offerId = generatedKeys.getInt(1);
                        } catch (Throwable th) {
                            if (generatedKeys != null) {
                                try {
                                    generatedKeys.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (generatedKeys != null) {
                        generatedKeys.close();
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

    public static void insert(MarketPlaceOffer marketPlaceOffer, Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO marketplace_items VALUES (?, ?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, marketPlaceOffer.getItemId());
                    preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(3, marketPlaceOffer.getPrice());
                    preparedStatementPrepareStatement.setInt(4, marketPlaceOffer.getTimestamp());
                    preparedStatementPrepareStatement.setInt(5, marketPlaceOffer.getSoldTimestamp());
                    preparedStatementPrepareStatement.setString(6, marketPlaceOffer.getState().getState() + Emulator.PREVIEW);
                    preparedStatementPrepareStatement.execute();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    while (generatedKeys.next()) {
                        try {
                            marketPlaceOffer.setOfferId(generatedKeys.getInt(1));
                        } catch (Throwable th) {
                            if (generatedKeys != null) {
                                try {
                                    generatedKeys.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (generatedKeys != null) {
                        generatedKeys.close();
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

    public int getOfferId() {
        return this.offerId;
    }

    public void setOfferId(int i) {
        this.offerId = i;
    }

    public int getItemId() {
        return this.baseItem.getSpriteId();
    }

    public int getPrice() {
        return this.price;
    }

    public MarketPlaceState getState() {
        return this.state;
    }

    public void setState(MarketPlaceState marketPlaceState) {
        this.state = marketPlaceState;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public int getSoldTimestamp() {
        return this.soldTimestamp;
    }

    public void setSoldTimestamp(int i) {
        this.soldTimestamp = i;
    }

    public int getLimitedStack() {
        return this.limitedStack;
    }

    public int getLimitedNumber() {
        return this.limitedNumber;
    }

    public int getSoldItemId() {
        return this.itemId;
    }

    public void needsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public int getType() {
        if (this.limitedStack > 0) {
            return 3;
        }
        return this.baseItem.getType().equals(FurnitureType.WALL) ? 2 : 1;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE marketplace_items SET state = ?, sold_timestamp = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.state.getState());
                        preparedStatementPrepareStatement.setInt(2, this.soldTimestamp);
                        preparedStatementPrepareStatement.setInt(3, this.offerId);
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
}
