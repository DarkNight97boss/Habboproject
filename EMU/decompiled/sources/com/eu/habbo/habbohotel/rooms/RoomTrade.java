package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.trading.TradeAcceptedComposer;
import com.eu.habbo.messages.outgoing.trading.TradeCloseWindowComposer;
import com.eu.habbo.messages.outgoing.trading.TradeClosedComposer;
import com.eu.habbo.messages.outgoing.trading.TradeCompleteComposer;
import com.eu.habbo.messages.outgoing.trading.TradeStartComposer;
import com.eu.habbo.messages.outgoing.trading.TradeUpdateComposer;
import com.eu.habbo.messages.outgoing.trading.TradingWaitingConfirmComposer;
import com.eu.habbo.plugin.events.trading.TradeConfirmEvent;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomTrade.class */
public class RoomTrade {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomTrade.class);
    public static boolean TRADING_ENABLED = true;
    public static boolean TRADING_REQUIRES_PERK = true;
    private final Room room;
    private final List<RoomTradeUser> users = new ArrayList();
    private boolean tradeCompleted = false;

    public RoomTrade(Habbo habbo, Habbo habbo2, Room room) {
        this.users.add(new RoomTradeUser(habbo));
        this.users.add(new RoomTradeUser(habbo2));
        this.room = room;
    }

    public void start() {
        initializeTradeStatus();
        openTrade();
    }

    protected void initializeTradeStatus() {
        for (RoomTradeUser roomTradeUser : this.users) {
            if (!roomTradeUser.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.TRADING)) {
                roomTradeUser.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.TRADING, Emulator.PREVIEW);
                if (!roomTradeUser.getHabbo().getRoomUnit().isWalking()) {
                    this.room.sendComposer(new RoomUserStatusComposer(roomTradeUser.getHabbo().getRoomUnit()).compose());
                }
            }
        }
    }

    protected void openTrade() {
        sendMessageToUsers(new TradeStartComposer(this));
    }

    public void offerItem(Habbo habbo, HabboItem habboItem) {
        RoomTradeUser roomTradeUserForHabbo = getRoomTradeUserForHabbo(habbo);
        if (roomTradeUserForHabbo.getItems().contains(habboItem)) {
            return;
        }
        habbo.getInventory().getItemsComponent().removeHabboItem(habboItem);
        roomTradeUserForHabbo.getItems().add(habboItem);
        clearAccepted();
        updateWindow();
    }

    public void offerMultipleItems(Habbo habbo, THashSet<HabboItem> tHashSet) {
        RoomTradeUser roomTradeUserForHabbo = getRoomTradeUserForHabbo(habbo);
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (!roomTradeUserForHabbo.getItems().contains(habboItem)) {
                habbo.getInventory().getItemsComponent().removeHabboItem(habboItem);
                roomTradeUserForHabbo.getItems().add(habboItem);
            }
        }
        clearAccepted();
        updateWindow();
    }

    public void removeItem(Habbo habbo, HabboItem habboItem) {
        RoomTradeUser roomTradeUserForHabbo = getRoomTradeUserForHabbo(habbo);
        if (roomTradeUserForHabbo.getItems().contains(habboItem)) {
            habbo.getInventory().getItemsComponent().addItem(habboItem);
            roomTradeUserForHabbo.getItems().remove(habboItem);
            clearAccepted();
            updateWindow();
        }
    }

    public void accept(Habbo habbo, boolean z) {
        RoomTradeUser roomTradeUserForHabbo = getRoomTradeUserForHabbo(habbo);
        roomTradeUserForHabbo.setAccepted(z);
        sendMessageToUsers(new TradeAcceptedComposer(roomTradeUserForHabbo));
        boolean z2 = true;
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            if (!it.next().getAccepted()) {
                z2 = false;
            }
        }
        if (z2) {
            sendMessageToUsers(new TradingWaitingConfirmComposer());
        }
    }

    public void confirm(Habbo habbo) {
        RoomTradeUser roomTradeUserForHabbo = getRoomTradeUserForHabbo(habbo);
        roomTradeUserForHabbo.confirm();
        sendMessageToUsers(new TradeAcceptedComposer(roomTradeUserForHabbo));
        boolean z = true;
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            if (!it.next().getConfirmed()) {
                z = false;
            }
        }
        if (z) {
            if (tradeItems()) {
                closeWindow();
                sendMessageToUsers(new TradeCompleteComposer());
            }
            this.room.stopTrade(this);
        }
    }

    boolean tradeItems() {
        Connection connection;
        for (RoomTradeUser roomTradeUser : this.users) {
            TObjectHashIterator it = roomTradeUser.getItems().iterator();
            while (it.hasNext()) {
                if (roomTradeUser.getHabbo().getInventory().getItemsComponent().getHabboItem(((HabboItem) it.next()).getId()) != null) {
                    sendMessageToUsers(new TradeClosedComposer(roomTradeUser.getHabbo().getRoomUnit().getId(), 1));
                    return false;
                }
            }
        }
        RoomTradeUser roomTradeUser2 = this.users.get(0);
        RoomTradeUser roomTradeUser3 = this.users.get(1);
        boolean zIsRegistered = Emulator.getPluginManager().isRegistered(TradeConfirmEvent.class, true);
        TradeConfirmEvent tradeConfirmEvent = new TradeConfirmEvent(roomTradeUser2, roomTradeUser3);
        if (zIsRegistered) {
            Emulator.getPluginManager().fireEvent(tradeConfirmEvent);
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            boolean z = Emulator.getConfig().getBoolean("hotel.log.trades");
            if (z) {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_trade_log (user_one_id, user_two_id, user_one_ip, user_two_ip, timestamp, user_one_item_count, user_two_item_count) VALUES (?, ?, ?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, roomTradeUser2.getHabbo().getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(2, roomTradeUser3.getHabbo().getHabboInfo().getId());
                    preparedStatementPrepareStatement.setString(3, roomTradeUser2.getHabbo().getHabboInfo().getIpLogin());
                    preparedStatementPrepareStatement.setString(4, roomTradeUser3.getHabbo().getHabboInfo().getIpLogin());
                    preparedStatementPrepareStatement.setInt(5, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.setInt(6, roomTradeUser2.getItems().size());
                    preparedStatementPrepareStatement.setInt(7, roomTradeUser3.getItems().size());
                    preparedStatementPrepareStatement.executeUpdate();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        i = generatedKeys.next() ? generatedKeys.getInt(1) : 0;
                        if (generatedKeys != null) {
                            generatedKeys.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
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
            int id = roomTradeUser2.getHabbo().getHabboInfo().getId();
            int id2 = roomTradeUser3.getHabbo().getHabboInfo().getId();
            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE items SET user_id = ? WHERE id = ? LIMIT 1");
            try {
                preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO room_trade_log_items (id, item_id, user_id) VALUES (?, ?, ?)");
                try {
                    TObjectHashIterator it2 = roomTradeUser2.getItems().iterator();
                    while (it2.hasNext()) {
                        HabboItem habboItem = (HabboItem) it2.next();
                        habboItem.setUserId(id2);
                        preparedStatementPrepareStatement2.setInt(1, id2);
                        preparedStatementPrepareStatement2.setInt(2, habboItem.getId());
                        preparedStatementPrepareStatement2.addBatch();
                        if (z) {
                            preparedStatementPrepareStatement2.setInt(1, i);
                            preparedStatementPrepareStatement2.setInt(2, habboItem.getId());
                            preparedStatementPrepareStatement2.setInt(3, id);
                            preparedStatementPrepareStatement2.addBatch();
                        }
                    }
                    TObjectHashIterator it3 = roomTradeUser3.getItems().iterator();
                    while (it3.hasNext()) {
                        HabboItem habboItem2 = (HabboItem) it3.next();
                        habboItem2.setUserId(id);
                        preparedStatementPrepareStatement2.setInt(1, id);
                        preparedStatementPrepareStatement2.setInt(2, habboItem2.getId());
                        preparedStatementPrepareStatement2.addBatch();
                        if (z) {
                            preparedStatementPrepareStatement2.setInt(1, i);
                            preparedStatementPrepareStatement2.setInt(2, habboItem2.getId());
                            preparedStatementPrepareStatement2.setInt(3, id2);
                            preparedStatementPrepareStatement2.addBatch();
                        }
                    }
                    if (z) {
                        preparedStatementPrepareStatement2.executeBatch();
                    }
                    if (preparedStatementPrepareStatement2 != null) {
                        preparedStatementPrepareStatement2.close();
                    }
                    preparedStatementPrepareStatement2.executeBatch();
                    if (preparedStatementPrepareStatement2 != null) {
                        preparedStatementPrepareStatement2.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    THashSet<HabboItem> tHashSet = new THashSet<>(roomTradeUser2.getItems());
                    THashSet<HabboItem> tHashSet2 = new THashSet<>(roomTradeUser3.getItems());
                    roomTradeUser2.clearItems();
                    roomTradeUser3.clearItems();
                    int i = 0;
                    THashSet tHashSet3 = new THashSet();
                    TObjectHashIterator it4 = tHashSet.iterator();
                    while (it4.hasNext()) {
                        HabboItem habboItem3 = (HabboItem) it4.next();
                        int creditsByItem = getCreditsByItem(habboItem3);
                        if (creditsByItem > 0) {
                            i += creditsByItem;
                            tHashSet3.add(habboItem3);
                            new QueryDeleteHabboItem(habboItem3).run();
                        }
                    }
                    tHashSet.removeAll(tHashSet3);
                    int i2 = 0;
                    THashSet tHashSet4 = new THashSet();
                    TObjectHashIterator it5 = tHashSet2.iterator();
                    while (it5.hasNext()) {
                        HabboItem habboItem4 = (HabboItem) it5.next();
                        int creditsByItem2 = getCreditsByItem(habboItem4);
                        if (creditsByItem2 > 0) {
                            i2 += creditsByItem2;
                            tHashSet4.add(habboItem4);
                            new QueryDeleteHabboItem(habboItem4).run();
                        }
                    }
                    tHashSet2.removeAll(tHashSet4);
                    roomTradeUser2.getHabbo().giveCredits(i2);
                    roomTradeUser3.getHabbo().giveCredits(i);
                    roomTradeUser2.getHabbo().getInventory().getItemsComponent().addItems(tHashSet2);
                    roomTradeUser3.getHabbo().getInventory().getItemsComponent().addItems(tHashSet);
                    roomTradeUser2.getHabbo().getClient().sendResponse(new AddHabboItemComposer(tHashSet2));
                    roomTradeUser3.getHabbo().getClient().sendResponse(new AddHabboItemComposer(tHashSet));
                    roomTradeUser2.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());
                    roomTradeUser3.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());
                    return true;
                } finally {
                    if (preparedStatementPrepareStatement2 != null) {
                        try {
                            preparedStatementPrepareStatement2.close();
                        } catch (Throwable th5) {
                            th.addSuppressed(th5);
                        }
                    }
                }
            } catch (Throwable th6) {
                throw th6;
            }
        } finally {
        }
    }

    protected void clearAccepted() {
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            it.next().setAccepted(false);
        }
    }

    protected void updateWindow() {
        sendMessageToUsers(new TradeUpdateComposer(this));
    }

    private void returnItems() {
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            it.next().putItemsIntoInventory();
        }
    }

    private void closeWindow() {
        removeStatusses();
        sendMessageToUsers(new TradeCloseWindowComposer());
    }

    public void stopTrade(Habbo habbo) {
        removeStatusses();
        clearAccepted();
        returnItems();
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            it.next().clearItems();
        }
        updateWindow();
        sendMessageToUsers(new TradeClosedComposer(habbo.getHabboInfo().getId(), 0));
        this.room.stopTrade(this);
    }

    private void removeStatusses() {
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            Habbo habbo = it.next().getHabbo();
            if (habbo != null) {
                habbo.getRoomUnit().removeStatus(RoomUnitStatus.TRADING);
                this.room.sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
            }
        }
    }

    public RoomTradeUser getRoomTradeUserForHabbo(Habbo habbo) {
        for (RoomTradeUser roomTradeUser : this.users) {
            if (roomTradeUser.getHabbo() == habbo) {
                return roomTradeUser;
            }
        }
        return null;
    }

    public void sendMessageToUsers(MessageComposer messageComposer) {
        Iterator<RoomTradeUser> it = this.users.iterator();
        while (it.hasNext()) {
            it.next().getHabbo().getClient().sendResponse(messageComposer);
        }
    }

    public List<RoomTradeUser> getRoomTradeUsers() {
        return this.users;
    }

    public static int getCreditsByItem(HabboItem habboItem) {
        if (!Emulator.getConfig().getBoolean("redeem.currency.trade")) {
            return 0;
        }
        if (!habboItem.getBaseItem().getName().startsWith("CF_") && !habboItem.getBaseItem().getName().startsWith("CFC_")) {
            return 0;
        }
        try {
            return Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
