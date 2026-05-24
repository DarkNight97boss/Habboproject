package com.eu.habbo.habbohotel.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestOffersEvent;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceBuyErrorComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceCancelSaleComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemCancelledEvent;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemOfferedEvent;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemSoldEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/marketplace/MarketPlace.class */
public class MarketPlace {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlace.class);
    public static boolean MARKETPLACE_ENABLED = true;
    public static int MARKETPLACE_CURRENCY = 0;

    public static THashSet<MarketPlaceOffer> getOwnOffers(Habbo habbo) {
        THashSet<MarketPlaceOffer> tHashSet = new THashSet<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT items_base.type AS type, items.item_id AS base_item_id, items.limited_data AS ltd_data, marketplace_items.* FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE marketplace_items.user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            tHashSet.add(new MarketPlaceOffer(resultSetExecuteQuery, true));
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
        return tHashSet;
    }

    public static void takeBackItem(Habbo habbo, int i) {
        MarketPlaceOffer offer = habbo.getInventory().getOffer(i);
        if (((MarketPlaceItemCancelledEvent) Emulator.getPluginManager().fireEvent(new MarketPlaceItemCancelledEvent(offer))).isCancelled()) {
            return;
        }
        takeBackItem(habbo, offer);
    }

    private static void takeBackItem(Habbo habbo, MarketPlaceOffer marketPlaceOffer) {
        if (marketPlaceOffer == null || !habbo.getInventory().getMarketplaceItems().contains(marketPlaceOffer)) {
            return;
        }
        RequestOffersEvent.cachedResults.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT user_id FROM marketplace_items WHERE id = ?", 1004, 1007);
                try {
                    preparedStatementPrepareStatement.setInt(1, marketPlaceOffer.getOfferId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        resultSetExecuteQuery.last();
                        if (resultSetExecuteQuery.getRow() == 0) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                                return;
                            }
                            return;
                        }
                        PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("DELETE FROM marketplace_items WHERE id = ? AND state != 2");
                        try {
                            preparedStatementPrepareStatement2.setInt(1, marketPlaceOffer.getOfferId());
                            if (preparedStatementPrepareStatement2.executeUpdate() != 0) {
                                habbo.getInventory().removeMarketplaceOffer(marketPlaceOffer);
                                preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE items SET user_id = ? WHERE id = ? LIMIT 1");
                                try {
                                    preparedStatementPrepareStatement2.setInt(1, habbo.getHabboInfo().getId());
                                    preparedStatementPrepareStatement2.setInt(2, marketPlaceOffer.getSoldItemId());
                                    preparedStatementPrepareStatement2.execute();
                                    preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1");
                                    try {
                                        preparedStatementPrepareStatement2.setInt(1, marketPlaceOffer.getSoldItemId());
                                        ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement2.executeQuery();
                                        while (resultSetExecuteQuery2.next()) {
                                            try {
                                                HabboItem habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery2);
                                                habbo.getInventory().getItemsComponent().addItem(habboItemLoadHabboItem);
                                                habbo.getClient().sendResponse(new MarketplaceCancelSaleComposer(marketPlaceOffer, true));
                                                habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemLoadHabboItem));
                                                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                                            } catch (Throwable th) {
                                                if (resultSetExecuteQuery2 != null) {
                                                    try {
                                                        resultSetExecuteQuery2.close();
                                                    } catch (Throwable th2) {
                                                        th.addSuppressed(th2);
                                                    }
                                                }
                                                throw th;
                                            }
                                        }
                                        if (resultSetExecuteQuery2 != null) {
                                            resultSetExecuteQuery2.close();
                                        }
                                        if (preparedStatementPrepareStatement2 != null) {
                                            preparedStatementPrepareStatement2.close();
                                        }
                                        if (preparedStatementPrepareStatement2 != null) {
                                            preparedStatementPrepareStatement2.close();
                                        }
                                    } finally {
                                        if (preparedStatementPrepareStatement2 != null) {
                                            try {
                                                preparedStatementPrepareStatement2.close();
                                            } catch (Throwable th3) {
                                                th.addSuppressed(th3);
                                            }
                                        }
                                    }
                                } finally {
                                }
                            }
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
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
                        } catch (Throwable th4) {
                            throw th4;
                        }
                    } catch (Throwable th5) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                } catch (Throwable th7) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th8) {
                            th7.addSuppressed(th8);
                        }
                    }
                    throw th7;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            habbo.getClient().sendResponse(new MarketplaceCancelSaleComposer(marketPlaceOffer, false));
        }
    }

    public static List<MarketPlaceOffer> getOffers(int i, int i2, String str, int i3) {
        String str2;
        String str3;
        ArrayList arrayList = new ArrayList(10);
        str2 = "SELECT B.* FROM marketplace_items a INNER JOIN (SELECT b.item_id AS base_item_id, b.limited_data AS ltd_data, marketplace_items.*, AVG(price) as avg, MIN(marketplace_items.price) as minPrice, MAX(marketplace_items.price) as maxPrice, COUNT(*) as number, (SELECT COUNT(*) FROM marketplace_items c INNER JOIN items as items_b ON c.item_id = items_b.id WHERE state = 2 AND items_b.item_id = base_item_id AND DATE(from_unixtime(sold_timestamp)) = CURDATE()) as sold_count_today FROM marketplace_items INNER JOIN items b ON marketplace_items.item_id = b.id INNER JOIN items_base bi ON b.item_id = bi.id INNER JOIN catalog_items ci ON bi.id = ci.item_ids WHERE price = (SELECT MIN(e.price) FROM marketplace_items e, items d WHERE e.item_id = d.id AND d.item_id = b.item_id AND e.state = 1 AND e.timestamp > ? GROUP BY d.item_id) AND state = 1 AND timestamp > ?";
        str2 = i > 0 ? str2 + " AND CEIL(price + (price / 100)) >= " + i : "SELECT B.* FROM marketplace_items a INNER JOIN (SELECT b.item_id AS base_item_id, b.limited_data AS ltd_data, marketplace_items.*, AVG(price) as avg, MIN(marketplace_items.price) as minPrice, MAX(marketplace_items.price) as maxPrice, COUNT(*) as number, (SELECT COUNT(*) FROM marketplace_items c INNER JOIN items as items_b ON c.item_id = items_b.id WHERE state = 2 AND items_b.item_id = base_item_id AND DATE(from_unixtime(sold_timestamp)) = CURDATE()) as sold_count_today FROM marketplace_items INNER JOIN items b ON marketplace_items.item_id = b.id INNER JOIN items_base bi ON b.item_id = bi.id INNER JOIN catalog_items ci ON bi.id = ci.item_ids WHERE price = (SELECT MIN(e.price) FROM marketplace_items e, items d WHERE e.item_id = d.id AND d.item_id = b.item_id AND e.state = 1 AND e.timestamp > ? GROUP BY d.item_id) AND state = 1 AND timestamp > ?";
        if (i2 > 0 && i2 > i) {
            str2 = str2 + " AND CEIL(price + (price / 100)) <= " + i2;
        }
        if (str.length() > 0) {
            str2 = str2 + " AND ( bi.public_name LIKE ? OR ci.catalog_name LIKE ? ) ";
        }
        String str4 = str2 + " GROUP BY base_item_id, ltd_data";
        switch (i3) {
            case 1:
            default:
                str3 = str4 + " ORDER BY minPrice DESC";
                break;
            case 2:
                str3 = str4 + " ORDER BY minPrice ASC";
                break;
            case 3:
                str3 = str4 + " ORDER BY sold_count_today DESC";
                break;
            case 4:
                str3 = str4 + " ORDER BY sold_count_today ASC";
                break;
            case 5:
                str3 = str4 + " ORDER BY number DESC";
                break;
            case 6:
                str3 = str4 + " ORDER BY number ASC";
                break;
        }
        String str5 = ((str3 + ")") + " AS B ON a.id = B.id") + " LIMIT 250";
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement(str5);
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp() - 172800);
                    preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp() - 172800);
                    if (str.length() > 0) {
                        preparedStatementPrepareStatement.setString(3, "%" + str + "%");
                        preparedStatementPrepareStatement.setString(4, "%" + str + "%");
                    }
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            arrayList.add(new MarketPlaceOffer(resultSetExecuteQuery, false));
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
        return arrayList;
    }

    public static void serializeItemInfo(int i, ServerMessage serverMessage) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT avg(marketplace_items.price) as price, COUNT(*) as sold, (datediff(NOW(), DATE(from_unixtime(marketplace_items.timestamp)))) as day FROM marketplace_items INNER JOIN items ON items.id = marketplace_items.item_id INNER JOIN items_base ON items.item_id = items_base.id WHERE items.limited_data = '0:0' AND marketplace_items.state = 2 AND items_base.sprite_id = ? AND DATE(from_unixtime(marketplace_items.timestamp)) >= NOW() - INTERVAL 30 DAY GROUP BY DATE(from_unixtime(marketplace_items.timestamp))", 1004, 1007);
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    serverMessage.appendInt(Integer.valueOf(avarageLastXDays(i, 7)));
                    serverMessage.appendInt(Integer.valueOf(itemsOnSale(i)));
                    serverMessage.appendInt((Integer) 30);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        resultSetExecuteQuery.last();
                        serverMessage.appendInt(Integer.valueOf(resultSetExecuteQuery.getRow()));
                        resultSetExecuteQuery.beforeFirst();
                        while (resultSetExecuteQuery.next()) {
                            serverMessage.appendInt(Integer.valueOf(-resultSetExecuteQuery.getInt("day")));
                            serverMessage.appendInt(Integer.valueOf(calculateCommision(resultSetExecuteQuery.getInt("price"))));
                            serverMessage.appendInt(Integer.valueOf(resultSetExecuteQuery.getInt("sold")));
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        serverMessage.appendInt((Integer) 1);
                        serverMessage.appendInt(Integer.valueOf(i));
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
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

    public static int itemsOnSale(int i) {
        int i2 = 0;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) as number, AVG(price) as avg FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE state = 1 AND timestamp >= ? AND items_base.sprite_id = ?", 1004, 1007);
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp() - 172800);
                    preparedStatementPrepareStatement.setInt(2, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        resultSetExecuteQuery.first();
                        i2 = resultSetExecuteQuery.getInt("number");
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
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
        return i2;
    }

    private static int avarageLastXDays(int i, int i2) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        int i3 = 0;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT AVG(price) as avg FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE state = 2 AND DATE(from_unixtime(timestamp)) >= NOW() - INTERVAL ? DAY AND items_base.sprite_id = ?", 1004, 1007);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i2);
            preparedStatementPrepareStatement.setInt(2, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                resultSetExecuteQuery.first();
                i3 = resultSetExecuteQuery.getInt("avg");
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                return calculateCommision(i3);
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
    }

    public static void buyItem(int i, GameClient gameClient) {
        RequestOffersEvent.cachedResults.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM marketplace_items WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1", 1004, 1007);
                            try {
                                preparedStatementPrepareStatement2.setInt(1, resultSetExecuteQuery.getInt("item_id"));
                                ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement2.executeQuery();
                                try {
                                    resultSetExecuteQuery2.first();
                                    if (resultSetExecuteQuery2.getRow() > 0) {
                                        int iCalculateCommision = calculateCommision(resultSetExecuteQuery.getInt("price"));
                                        if (resultSetExecuteQuery.getInt("state") != 1) {
                                            sendErrorMessage(gameClient, resultSetExecuteQuery.getInt("item_id"), i);
                                        } else if ((MARKETPLACE_CURRENCY != 0 || iCalculateCommision <= gameClient.getHabbo().getHabboInfo().getCredits()) && (MARKETPLACE_CURRENCY <= 0 || iCalculateCommision <= gameClient.getHabbo().getHabboInfo().getCurrencyAmount(MARKETPLACE_CURRENCY))) {
                                            PreparedStatement preparedStatementPrepareStatement3 = connection.prepareStatement("UPDATE marketplace_items SET state = 2, sold_timestamp = ? WHERE id = ?");
                                            try {
                                                preparedStatementPrepareStatement3.setInt(1, Emulator.getIntUnixTimestamp());
                                                preparedStatementPrepareStatement3.setInt(2, i);
                                                preparedStatementPrepareStatement3.execute();
                                                if (preparedStatementPrepareStatement3 != null) {
                                                    preparedStatementPrepareStatement3.close();
                                                }
                                                Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(resultSetExecuteQuery.getInt("user_id"));
                                                HabboItem habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery2);
                                                MarketPlaceItemSoldEvent marketPlaceItemSoldEvent = new MarketPlaceItemSoldEvent(habbo, gameClient.getHabbo(), habboItemLoadHabboItem, resultSetExecuteQuery.getInt("price"));
                                                if (((MarketPlaceItemSoldEvent) Emulator.getPluginManager().fireEvent(marketPlaceItemSoldEvent)).isCancelled()) {
                                                    if (resultSetExecuteQuery2 != null) {
                                                        resultSetExecuteQuery2.close();
                                                    }
                                                    if (preparedStatementPrepareStatement2 != null) {
                                                        preparedStatementPrepareStatement2.close();
                                                    }
                                                    if (resultSetExecuteQuery != null) {
                                                        resultSetExecuteQuery.close();
                                                    }
                                                    if (preparedStatementPrepareStatement != null) {
                                                        preparedStatementPrepareStatement.close();
                                                    }
                                                    if (connection != null) {
                                                        connection.close();
                                                        return;
                                                    }
                                                    return;
                                                }
                                                marketPlaceItemSoldEvent.price = calculateCommision(marketPlaceItemSoldEvent.price);
                                                habboItemLoadHabboItem.setUserId(gameClient.getHabbo().getHabboInfo().getId());
                                                habboItemLoadHabboItem.needsUpdate(true);
                                                Emulator.getThreading().run(habboItemLoadHabboItem);
                                                gameClient.getHabbo().getInventory().getItemsComponent().addItem(habboItemLoadHabboItem);
                                                if (MARKETPLACE_CURRENCY == 0) {
                                                    gameClient.getHabbo().giveCredits(-marketPlaceItemSoldEvent.price);
                                                } else {
                                                    gameClient.getHabbo().givePoints(MARKETPLACE_CURRENCY, -marketPlaceItemSoldEvent.price);
                                                }
                                                gameClient.sendResponse(new AddHabboItemComposer(habboItemLoadHabboItem));
                                                gameClient.sendResponse(new InventoryRefreshComposer());
                                                gameClient.sendResponse(new MarketplaceBuyErrorComposer(1, 0, i, iCalculateCommision));
                                                if (habbo != null) {
                                                    habbo.getInventory().getOffer(i).setState(MarketPlaceState.SOLD);
                                                }
                                            } catch (Throwable th) {
                                                if (preparedStatementPrepareStatement3 != null) {
                                                    try {
                                                        preparedStatementPrepareStatement3.close();
                                                    } catch (Throwable th2) {
                                                        th.addSuppressed(th2);
                                                    }
                                                }
                                                throw th;
                                            }
                                        } else {
                                            gameClient.sendResponse(new MarketplaceBuyErrorComposer(4, 0, i, iCalculateCommision));
                                        }
                                    }
                                    if (resultSetExecuteQuery2 != null) {
                                        resultSetExecuteQuery2.close();
                                    }
                                    if (preparedStatementPrepareStatement2 != null) {
                                        preparedStatementPrepareStatement2.close();
                                    }
                                } catch (Throwable th3) {
                                    if (resultSetExecuteQuery2 != null) {
                                        try {
                                            resultSetExecuteQuery2.close();
                                        } catch (Throwable th4) {
                                            th3.addSuppressed(th4);
                                        }
                                    }
                                    throw th3;
                                }
                            } catch (Throwable th5) {
                                if (preparedStatementPrepareStatement2 != null) {
                                    try {
                                        preparedStatementPrepareStatement2.close();
                                    } catch (Throwable th6) {
                                        th5.addSuppressed(th6);
                                    }
                                }
                                throw th5;
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
                    } catch (Throwable th7) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th8) {
                                th7.addSuppressed(th8);
                            }
                        }
                        throw th7;
                    }
                } catch (Throwable th9) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th10) {
                            th9.addSuppressed(th10);
                        }
                    }
                    throw th9;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public static void sendErrorMessage(GameClient gameClient, int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT marketplace_items.*, COUNT( * ) AS count\nFROM marketplace_items\nINNER JOIN items ON marketplace_items.item_id = items.id\nINNER JOIN items_base ON items.item_id = items_base.id\nWHERE items_base.sprite_id = ( \nSELECT items_base.sprite_id\nFROM items_base\nWHERE items_base.id = ? LIMIT 1)\nORDER BY price ASC\nLIMIT 1", 1004, 1007);
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        resultSetExecuteQuery.last();
                        if (resultSetExecuteQuery.getRow() == 0) {
                            gameClient.sendResponse(new MarketplaceBuyErrorComposer(2, 0, i2, 0));
                        } else {
                            resultSetExecuteQuery.first();
                            gameClient.sendResponse(new MarketplaceBuyErrorComposer(3, resultSetExecuteQuery.getInt("count"), resultSetExecuteQuery.getInt("id"), calculateCommision(resultSetExecuteQuery.getInt("price"))));
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

    public static boolean sellItem(GameClient gameClient, HabboItem habboItem, int i) {
        if (habboItem == null || gameClient == null || !habboItem.getBaseItem().allowMarketplace() || i < 0) {
            return false;
        }
        MarketPlaceItemOfferedEvent marketPlaceItemOfferedEvent = new MarketPlaceItemOfferedEvent(gameClient.getHabbo(), habboItem, i);
        if (((MarketPlaceItemOfferedEvent) Emulator.getPluginManager().fireEvent(marketPlaceItemOfferedEvent)).isCancelled()) {
            return false;
        }
        RequestOffersEvent.cachedResults.clear();
        gameClient.sendResponse(new RemoveHabboItemComposer(marketPlaceItemOfferedEvent.item.getGiftAdjustedId()));
        gameClient.sendResponse(new InventoryRefreshComposer());
        marketPlaceItemOfferedEvent.item.setFromGift(false);
        gameClient.getHabbo().getInventory().addMarketplaceOffer(new MarketPlaceOffer(marketPlaceItemOfferedEvent.item, marketPlaceItemOfferedEvent.price, gameClient.getHabbo()));
        gameClient.getHabbo().getInventory().getItemsComponent().removeHabboItem(marketPlaceItemOfferedEvent.item);
        habboItem.setUserId(-1);
        habboItem.needsUpdate(true);
        Emulator.getThreading().run(habboItem);
        return true;
    }

    public static void getCredits(GameClient gameClient) {
        int price = 0;
        THashSet tHashSet = new THashSet();
        tHashSet.addAll(gameClient.getHabbo().getInventory().getMarketplaceItems());
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            MarketPlaceOffer marketPlaceOffer = (MarketPlaceOffer) it.next();
            if (marketPlaceOffer.getState().equals(MarketPlaceState.SOLD)) {
                gameClient.getHabbo().getInventory().removeMarketplaceOffer(marketPlaceOffer);
                price += marketPlaceOffer.getPrice();
                removeUser(marketPlaceOffer);
                marketPlaceOffer.needsUpdate(true);
                Emulator.getThreading().run(marketPlaceOffer);
            }
        }
        tHashSet.clear();
        if (MARKETPLACE_CURRENCY == 0) {
            gameClient.getHabbo().giveCredits(price);
        } else {
            gameClient.getHabbo().givePoints(MARKETPLACE_CURRENCY, price);
        }
    }

    private static void removeUser(MarketPlaceOffer marketPlaceOffer) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE marketplace_items SET user_id = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, -1);
                    preparedStatementPrepareStatement.setInt(2, marketPlaceOffer.getOfferId());
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

    public static int calculateCommision(int i) {
        return i + ((int) Math.ceil(((double) i) / 100.0d));
    }
}
