package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.trading.*;
import com.eu.habbo.plugin.events.furniture.FurnitureRedeemedEvent;
import com.eu.habbo.plugin.events.trading.TradeConfirmEvent;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTrade {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomTrade.class);
    //Configuration. Loaded from database & updated accordingly.
    public static boolean TRADING_ENABLED = true;
    public static boolean TRADING_REQUIRES_PERK = true;

    private final List<RoomTradeUser> users;
    private final Room room;
    private boolean tradeCompleted;
    // Timestamp of the last item add/remove on either side. Used by the safety
    // cooldown to require a brief stable window before accept can take effect.
    private volatile long lastModificationAt;

    public RoomTrade(Habbo userOne, Habbo userTwo, Room room) {
        this.users = new ArrayList<>();
        this.tradeCompleted = false;
        this.lastModificationAt = 0L;

        this.users.add(new RoomTradeUser(userOne));
        this.users.add(new RoomTradeUser(userTwo));
        this.room = room;
    }

    private void markModified() {
        this.lastModificationAt = System.currentTimeMillis();
    }

    public void start() {
        this.initializeTradeStatus();
        this.openTrade();
    }

    protected void initializeTradeStatus() {
        for (RoomTradeUser roomTradeUser : this.users) {
            if (!roomTradeUser.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.TRADING)) {
                roomTradeUser.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.TRADING, "");
                if (!roomTradeUser.getHabbo().getRoomUnit().isWalking())
                    this.room.sendComposer(new RoomUserStatusComposer(roomTradeUser.getHabbo().getRoomUnit()).compose());
            }
        }
    }

    protected void openTrade() {
        this.sendMessageToUsers(new TradeStartComposer(this));
    }

    public synchronized void offerItem(Habbo habbo, HabboItem item) {
        RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);

        if (user.getItems().contains(item))
            return;

        habbo.getInventory().getItemsComponent().removeHabboItem(item);
        user.getItems().add(item);

        this.markModified();
        this.clearAccepted();
        this.updateWindow();
    }

    public synchronized void offerMultipleItems(Habbo habbo, THashSet<HabboItem> items) {
        RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);

        for (HabboItem item : items) {
            if (!user.getItems().contains(item)) {
                habbo.getInventory().getItemsComponent().removeHabboItem(item);
                user.getItems().add(item);
            }
        }

        this.markModified();
        this.clearAccepted();
        this.updateWindow();
    }

    public synchronized void removeItem(Habbo habbo, HabboItem item) {
        RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);

        if (!user.getItems().contains(item))
            return;

        habbo.getInventory().getItemsComponent().addItem(item);
        user.getItems().remove(item);

        this.markModified();
        this.clearAccepted();
        this.updateWindow();
    }

    public synchronized void accept(Habbo habbo, boolean value) {
        RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);

        // Safety cooldown: after any item add/remove on either side, accept can
        // only take effect once the trade has been stable for at least N ms.
        // This blocks classic "last-second swap" scam: attacker waits for victim
        // to accept, then yanks an item & re-adds while clicking accept itself.
        long cooldownMs = Emulator.getConfig().getInt("trade.safety.cooldown_ms", 3000);
        if (value && cooldownMs > 0 && this.lastModificationAt > 0
                && (System.currentTimeMillis() - this.lastModificationAt) < cooldownMs) {
            user.setAccepted(false);
            this.sendMessageToUsers(new TradeAcceptedComposer(user));
            try {
                habbo.whisper(
                        Emulator.getTexts().getValue("trade.safety.cooldown", "Aspetta un momento prima di accettare — lo scambio è appena cambiato."),
                        RoomChatMessageBubbles.ALERT);
            } catch (Exception ignored) {
            }
            return;
        }

        user.setAccepted(value);

        this.sendMessageToUsers(new TradeAcceptedComposer(user));
        boolean accepted = true;
        for (RoomTradeUser roomTradeUser : this.users) {
            if (!roomTradeUser.getAccepted())
                accepted = false;
        }
        if (accepted) {
            this.sendMessageToUsers(new TradingWaitingConfirmComposer());
        }
    }

    public synchronized void confirm(Habbo habbo) {
        RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);

        user.confirm();

        this.sendMessageToUsers(new TradeAcceptedComposer(user));
        boolean accepted = true;
        for (RoomTradeUser roomTradeUser : this.users) {
            if (!roomTradeUser.getConfirmed())
                accepted = false;
        }
        if (accepted) {
            if (this.tradeItems()) {
                this.closeWindow();
                this.sendMessageToUsers(new TradeCompleteComposer());
            }

            this.room.stopTrade(this);
        }
    }

    synchronized boolean tradeItems() {
        // Guard against double execution (concurrent confirms) -> would duplicate items.
        if (this.tradeCompleted) {
            return false;
        }
        this.tradeCompleted = true;

        // Re-validate every offered item still belongs to the offerer (anti-dupe / TOCTOU).
        for (RoomTradeUser roomTradeUser : this.users) {
            int ownerId = roomTradeUser.getHabbo().getHabboInfo().getId();
            for (HabboItem item : roomTradeUser.getItems()) {
                if (item.getUserId() != ownerId) {
                    this.sendMessageToUsers(new TradeClosedComposer(roomTradeUser.getHabbo().getRoomUnit().getId(), TradeClosedComposer.ITEMS_NOT_FOUND));
                    this.returnItems();
                    return false;
                }
            }
        }

        RoomTradeUser userOne = this.users.get(0);
        RoomTradeUser userTwo = this.users.get(1);

        // Audit log a "suspicious" trade: large absolute volume or strongly
        // asymmetric (one side gives much more than the other). Detection only,
        // so staff can review for scam patterns. Thresholds are configurable.
        try {
            int aCount = userOne.getItems().size();
            int bCount = userTwo.getItems().size();
            int volumeThreshold = Emulator.getConfig().getInt("trade.safety.audit_min_items", 10);
            int ratioThreshold = Emulator.getConfig().getInt("trade.safety.audit_min_ratio", 5);
            boolean bigVolume = aCount >= volumeThreshold || bCount >= volumeThreshold;
            int small = Math.min(aCount, bCount);
            int big = Math.max(aCount, bCount);
            boolean asymmetric = small == 0 ? big > 0 : (big / Math.max(1, small)) >= ratioThreshold;
            if (bigVolume || asymmetric) {
                com.eu.habbo.core.AuditLog.record(
                        userOne.getHabbo().getHabboInfo().getId(),
                        userOne.getHabbo().getHabboInfo().getUsername(),
                        "TRADE_FLAGGED",
                        "user:" + userTwo.getHabbo().getHabboInfo().getId(),
                        "a=" + userOne.getHabbo().getHabboInfo().getUsername() + ":" + aCount
                                + " b=" + userTwo.getHabbo().getHabboInfo().getUsername() + ":" + bCount
                                + " bigVolume=" + bigVolume + " asym=" + asymmetric);
            }
        } catch (Exception ignored) {
        }

        boolean tradeConfirmEventRegistered = Emulator.getPluginManager().isRegistered(TradeConfirmEvent.class, true);
        TradeConfirmEvent tradeConfirmEvent = new TradeConfirmEvent(userOne, userTwo);
        if (tradeConfirmEventRegistered) {
            Emulator.getPluginManager().fireEvent(tradeConfirmEvent);
        }

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {

            int tradeId = 0;

            boolean logTrades = Emulator.getConfig().getBoolean("hotel.log.trades");
            if (logTrades) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO room_trade_log (user_one_id, user_two_id, user_one_ip, user_two_ip, timestamp, user_one_item_count, user_two_item_count) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, userOne.getHabbo().getHabboInfo().getId());
                    statement.setInt(2, userTwo.getHabbo().getHabboInfo().getId());
                    statement.setString(3, userOne.getHabbo().getHabboInfo().getIpLogin());
                    statement.setString(4, userTwo.getHabbo().getHabboInfo().getIpLogin());
                    statement.setInt(5, Emulator.getIntUnixTimestamp());
                    statement.setInt(6, userOne.getItems().size());
                    statement.setInt(7, userTwo.getItems().size());
                    statement.executeUpdate();
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            tradeId = generatedKeys.getInt(1);
                        }
                    }
                }
            }

            int userOneId = userOne.getHabbo().getHabboInfo().getId();
            int userTwoId = userTwo.getHabbo().getHabboInfo().getId();

            try (PreparedStatement statement = connection.prepareStatement("UPDATE items SET user_id = ? WHERE id = ? AND user_id = ? LIMIT 1")) {
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO room_trade_log_items (id, item_id, user_id) VALUES (?, ?, ?)")) {
                    for (HabboItem item : userOne.getItems()) {
                        item.setUserId(userTwoId);
                        statement.setInt(1, userTwoId);
                        statement.setInt(2, item.getId());
                        statement.setInt(3, userOneId);
                        statement.addBatch();

                        if (logTrades) {
                            stmt.setInt(1, tradeId);
                            stmt.setInt(2, item.getId());
                            stmt.setInt(3, userOneId);
                            stmt.addBatch();
                        }
                    }

                    for (HabboItem item : userTwo.getItems()) {
                        item.setUserId(userOneId);
                        statement.setInt(1, userOneId);
                        statement.setInt(2, item.getId());
                        statement.setInt(3, userTwoId);
                        statement.addBatch();

                        if (logTrades) {
                            stmt.setInt(1, tradeId);
                            stmt.setInt(2, item.getId());
                            stmt.setInt(3, userTwoId);
                            stmt.addBatch();
                        }
                    }

                    if (logTrades) {
                        stmt.executeBatch();
                    }
                }

                statement.executeBatch();
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        THashSet<HabboItem> itemsUserOne = new THashSet<>(userOne.getItems());
        THashSet<HabboItem> itemsUserTwo = new THashSet<>(userTwo.getItems());

        userOne.clearItems();
        userTwo.clearItems();

        int creditsForUserTwo = 0;
        THashSet<HabboItem> creditFurniUserOne = new THashSet<>();
        for (HabboItem item : itemsUserOne) {
            int worth = RoomTrade.getCreditsByItem(item);
            if (worth > 0) {
                creditsForUserTwo += worth;
                creditFurniUserOne.add(item);
                new QueryDeleteHabboItem(item).run();
            }
        }
        itemsUserOne.removeAll(creditFurniUserOne);

        int creditsForUserOne = 0;
        THashSet<HabboItem> creditFurniUserTwo = new THashSet<>();
        for (HabboItem item : itemsUserTwo) {
            int worth = RoomTrade.getCreditsByItem(item);
            if (worth > 0) {
                creditsForUserOne += worth;
                creditFurniUserTwo.add(item);
                new QueryDeleteHabboItem(item).run();
            }
        }
        itemsUserTwo.removeAll(creditFurniUserTwo);

        userOne.getHabbo().giveCredits(creditsForUserOne);
        userTwo.getHabbo().giveCredits(creditsForUserTwo);

        userOne.getHabbo().getInventory().getItemsComponent().addItems(itemsUserTwo);
        userTwo.getHabbo().getInventory().getItemsComponent().addItems(itemsUserOne);

        userOne.getHabbo().getClient().sendResponse(new AddHabboItemComposer(itemsUserTwo));
        userTwo.getHabbo().getClient().sendResponse(new AddHabboItemComposer(itemsUserOne));

        userOne.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());
        userTwo.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());

        com.eu.habbo.core.AuditLog.record(userOne.getHabbo().getHabboInfo().getId(), userOne.getHabbo().getHabboInfo().getUsername(), "TRADE",
                "user:" + userTwo.getHabbo().getHabboInfo().getId(),
                "items=" + itemsUserOne.size() + "/" + itemsUserTwo.size() + " credits=" + creditsForUserOne + "/" + creditsForUserTwo);

        // Cross-feature hook: battle-pass XP for both sides on a completed trade.
        try {
            com.eu.habbo.core.BattlePass.grantTradeXp(userOne.getHabbo());
            com.eu.habbo.core.BattlePass.grantTradeXp(userTwo.getHabbo());
        } catch (Exception ignored) {
        }

        // Cross-feature hook: progress ACH_LifetimeTrades for both sides.
        try {
            com.eu.habbo.habbohotel.achievements.Achievement tradeAch =
                    Emulator.getGameEnvironment().getAchievementManager().getAchievement("ACH_LifetimeTrades");
            if (tradeAch != null) {
                com.eu.habbo.habbohotel.achievements.AchievementManager.progressAchievement(userOne.getHabbo(), tradeAch, 1);
                com.eu.habbo.habbohotel.achievements.AchievementManager.progressAchievement(userTwo.getHabbo(), tradeAch, 1);
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    protected void clearAccepted() {
        for (RoomTradeUser user : this.users) {
            user.setAccepted(false);
        }
    }

    protected void updateWindow() {
        this.sendMessageToUsers(new TradeUpdateComposer(this));
    }

    private void returnItems() {
        for (RoomTradeUser user : this.users) {
            user.putItemsIntoInventory();
        }
    }

    private void closeWindow() {
        this.removeStatusses();
        this.sendMessageToUsers(new TradeCloseWindowComposer());
    }

    public synchronized void stopTrade(Habbo habbo) {
        this.removeStatusses();
        this.clearAccepted();
        this.returnItems();
        for (RoomTradeUser user : this.users) {
            user.clearItems();
        }
        this.updateWindow();
        this.sendMessageToUsers(new TradeClosedComposer(habbo.getHabboInfo().getId(), TradeClosedComposer.USER_CANCEL_TRADE));
        this.room.stopTrade(this);
    }

    private void removeStatusses() {
        for (RoomTradeUser user : this.users) {
            Habbo habbo = user.getHabbo();

            if (habbo == null)
                continue;

            habbo.getRoomUnit().removeStatus(RoomUnitStatus.TRADING);
            this.room.sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
        }
    }

    public RoomTradeUser getRoomTradeUserForHabbo(Habbo habbo) {
        for (RoomTradeUser roomTradeUser : this.users) {
            if (roomTradeUser.getHabbo() == habbo)
                return roomTradeUser;
        }
        return null;
    }

    public void sendMessageToUsers(MessageComposer message) {
        for (RoomTradeUser roomTradeUser : this.users) {
            roomTradeUser.getHabbo().getClient().sendResponse(message);
        }
    }

    public List<RoomTradeUser> getRoomTradeUsers() {
        return this.users;
    }

    public static int getCreditsByItem(HabboItem item) {
        if (!Emulator.getConfig().getBoolean("redeem.currency.trade")) return 0;

        if (!item.getBaseItem().getName().startsWith("CF_") && !item.getBaseItem().getName().startsWith("CFC_")) return 0;

        try {
            return Integer.parseInt(item.getBaseItem().getName().split("_")[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
