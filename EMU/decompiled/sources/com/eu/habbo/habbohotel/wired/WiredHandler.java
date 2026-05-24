package com.eu.habbo.habbohotel.wired;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectGiveReward;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectTriggerStacks;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredExtraRandom;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredExtraUnseen;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.wired.WiredRewardAlertComposer;
import com.eu.habbo.plugin.events.furniture.wired.WiredConditionFailedEvent;
import com.eu.habbo.plugin.events.furniture.wired.WiredStackExecutedEvent;
import com.eu.habbo.plugin.events.furniture.wired.WiredStackTriggeredEvent;
import com.eu.habbo.plugin.events.users.UserWiredRewardReceived;
import com.google.gson.GsonBuilder;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/WiredHandler.class */
public class WiredHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredHandler.class);
    public static int MAXIMUM_FURNI_SELECTION = 5;
    public static int TELEPORT_DELAY = 500;
    private static GsonBuilder gsonBuilder = null;

    public static boolean handle(WiredTriggerType wiredTriggerType, RoomUnit roomUnit, Room room, Object[] objArr) {
        THashSet<InteractionWiredTrigger> triggers;
        if (wiredTriggerType == WiredTriggerType.CUSTOM) {
            return false;
        }
        boolean z = false;
        if (!Emulator.isReady || room == null || !room.isLoaded() || room.getRoomSpecialTypes() == null || (triggers = room.getRoomSpecialTypes().getTriggers(wiredTriggerType)) == null || triggers.isEmpty()) {
            return false;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        THashSet tHashSet = new THashSet();
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = triggers.iterator();
        while (it.hasNext()) {
            InteractionWiredTrigger interactionWiredTrigger = (InteractionWiredTrigger) it.next();
            RoomTile tile = room.getLayout().getTile(interactionWiredTrigger.getX(), interactionWiredTrigger.getY());
            if (!arrayList.contains(tile)) {
                THashSet tHashSet2 = new THashSet();
                if (handle(interactionWiredTrigger, roomUnit, room, objArr, tHashSet2)) {
                    tHashSet.addAll(tHashSet2);
                    if (wiredTriggerType.equals(WiredTriggerType.SAY_SOMETHING)) {
                        z = true;
                    }
                    arrayList.add(tile);
                }
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            triggerEffect((InteractionWiredEffect) it2.next(), roomUnit, room, objArr, jCurrentTimeMillis);
        }
        return z;
    }

    public static boolean handleCustomTrigger(Class<? extends InteractionWiredTrigger> cls, RoomUnit roomUnit, Room room, Object[] objArr) {
        THashSet<InteractionWiredTrigger> triggers;
        if (!Emulator.isReady || room == null || !room.isLoaded() || room.getRoomSpecialTypes() == null || (triggers = room.getRoomSpecialTypes().getTriggers(WiredTriggerType.CUSTOM)) == null || triggers.isEmpty()) {
            return false;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        THashSet tHashSet = new THashSet();
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = triggers.iterator();
        while (it.hasNext()) {
            InteractionWiredTrigger interactionWiredTrigger = (InteractionWiredTrigger) it.next();
            if (interactionWiredTrigger.getClass() == cls) {
                RoomTile tile = room.getLayout().getTile(interactionWiredTrigger.getX(), interactionWiredTrigger.getY());
                if (!arrayList.contains(tile)) {
                    THashSet tHashSet2 = new THashSet();
                    if (handle(interactionWiredTrigger, roomUnit, room, objArr, tHashSet2)) {
                        tHashSet.addAll(tHashSet2);
                        arrayList.add(tile);
                    }
                }
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            triggerEffect((InteractionWiredEffect) it2.next(), roomUnit, room, objArr, jCurrentTimeMillis);
        }
        return tHashSet.size() > 0;
    }

    public static boolean handle(InteractionWiredTrigger interactionWiredTrigger, RoomUnit roomUnit, Room room, Object[] objArr) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        THashSet tHashSet = new THashSet();
        if (!handle(interactionWiredTrigger, roomUnit, room, objArr, tHashSet)) {
            return false;
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            triggerEffect((InteractionWiredEffect) it.next(), roomUnit, room, objArr, jCurrentTimeMillis);
        }
        return true;
    }

    public static boolean handle(InteractionWiredTrigger interactionWiredTrigger, RoomUnit roomUnit, Room room, Object[] objArr, THashSet<InteractionWiredEffect> tHashSet) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        int id = roomUnit != null ? roomUnit.getId() : -1;
        if (!Emulator.isReady) {
            return false;
        }
        if (((!Emulator.getConfig().getBoolean("wired.custom.enabled", false) || ((!interactionWiredTrigger.canExecute(jCurrentTimeMillis) && id <= -1) || !interactionWiredTrigger.userCanExecute(id, jCurrentTimeMillis))) && (Emulator.getConfig().getBoolean("wired.custom.enabled", false) || !interactionWiredTrigger.canExecute(jCurrentTimeMillis))) || !interactionWiredTrigger.execute(roomUnit, room, objArr)) {
            return false;
        }
        interactionWiredTrigger.activateBox(room, roomUnit, jCurrentTimeMillis);
        THashSet<InteractionWiredCondition> conditions = room.getRoomSpecialTypes().getConditions(interactionWiredTrigger.getX(), interactionWiredTrigger.getY());
        THashSet<InteractionWiredEffect> effects = room.getRoomSpecialTypes().getEffects(interactionWiredTrigger.getX(), interactionWiredTrigger.getY());
        if (((WiredStackTriggeredEvent) Emulator.getPluginManager().fireEvent(new WiredStackTriggeredEvent(room, roomUnit, interactionWiredTrigger, effects, conditions))).isCancelled()) {
            return false;
        }
        if (!conditions.isEmpty()) {
            ArrayList arrayList = new ArrayList(conditions.size());
            TObjectHashIterator it = conditions.iterator();
            while (it.hasNext()) {
                InteractionWiredCondition interactionWiredCondition = (InteractionWiredCondition) it.next();
                if (!arrayList.contains(interactionWiredCondition.getType()) && interactionWiredCondition.operator() == WiredConditionOperator.OR && interactionWiredCondition.execute(roomUnit, room, objArr)) {
                    arrayList.add(interactionWiredCondition.getType());
                }
            }
            TObjectHashIterator it2 = conditions.iterator();
            while (it2.hasNext()) {
                InteractionWiredCondition interactionWiredCondition2 = (InteractionWiredCondition) it2.next();
                if (interactionWiredCondition2.operator() != WiredConditionOperator.OR || !arrayList.contains(interactionWiredCondition2.getType())) {
                    if (interactionWiredCondition2.operator() != WiredConditionOperator.AND || !interactionWiredCondition2.execute(roomUnit, room, objArr)) {
                        if (!((WiredConditionFailedEvent) Emulator.getPluginManager().fireEvent(new WiredConditionFailedEvent(room, roomUnit, interactionWiredTrigger, interactionWiredCondition2))).isCancelled()) {
                            return false;
                        }
                    }
                }
            }
        }
        interactionWiredTrigger.setCooldown(jCurrentTimeMillis);
        boolean zHasExtraType = room.getRoomSpecialTypes().hasExtraType(interactionWiredTrigger.getX(), interactionWiredTrigger.getY(), WiredExtraRandom.class);
        boolean zHasExtraType2 = room.getRoomSpecialTypes().hasExtraType(interactionWiredTrigger.getX(), interactionWiredTrigger.getY(), WiredExtraUnseen.class);
        TObjectHashIterator it3 = room.getRoomSpecialTypes().getExtras(interactionWiredTrigger.getX(), interactionWiredTrigger.getY()).iterator();
        while (it3.hasNext()) {
            ((InteractionWiredExtra) it3.next()).activateBox(room, roomUnit, jCurrentTimeMillis);
        }
        ArrayList arrayList2 = new ArrayList((Collection) effects);
        if (zHasExtraType || zHasExtraType2) {
            Collections.shuffle(arrayList2);
        }
        if (!zHasExtraType2) {
            Iterator it4 = arrayList2.iterator();
            while (it4.hasNext()) {
                boolean zAdd = tHashSet.add((InteractionWiredEffect) it4.next());
                if (zHasExtraType && zAdd) {
                    break;
                }
            }
        } else {
            TObjectHashIterator it5 = room.getRoomSpecialTypes().getExtras(interactionWiredTrigger.getX(), interactionWiredTrigger.getY()).iterator();
            while (true) {
                if (!it5.hasNext()) {
                    break;
                }
                InteractionWiredExtra interactionWiredExtra = (InteractionWiredExtra) it5.next();
                if (interactionWiredExtra instanceof WiredExtraUnseen) {
                    interactionWiredExtra.setExtradata(interactionWiredExtra.getExtradata().equals("1") ? "0" : "1");
                    tHashSet.add(((WiredExtraUnseen) interactionWiredExtra).getUnseenEffect(arrayList2));
                }
            }
        }
        return !((WiredStackExecutedEvent) Emulator.getPluginManager().fireEvent(new WiredStackExecutedEvent(room, roomUnit, interactionWiredTrigger, effects, conditions))).isCancelled();
    }

    private static boolean triggerEffect(InteractionWiredEffect interactionWiredEffect, RoomUnit roomUnit, Room room, Object[] objArr, long j) {
        boolean z = false;
        if (interactionWiredEffect != null && (interactionWiredEffect.canExecute(j) || (roomUnit != null && interactionWiredEffect.requiresTriggeringUser() && Emulator.getConfig().getBoolean("wired.custom.enabled", false) && interactionWiredEffect.userCanExecute(roomUnit.getId(), j)))) {
            z = true;
            if (!interactionWiredEffect.requiresTriggeringUser() || (roomUnit != null && interactionWiredEffect.requiresTriggeringUser())) {
                Emulator.getThreading().run(() -> {
                    if (room.isLoaded()) {
                        try {
                            if (!interactionWiredEffect.execute(roomUnit, room, objArr)) {
                                return;
                            } else {
                                interactionWiredEffect.setCooldown(j);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Caught exception", e);
                        }
                        interactionWiredEffect.activateBox(room, roomUnit, j);
                    }
                }, interactionWiredEffect.getDelay() * 500);
            }
        }
        return z;
    }

    public static GsonBuilder getGsonBuilder() {
        if (gsonBuilder == null) {
            gsonBuilder = new GsonBuilder();
        }
        return gsonBuilder;
    }

    public static boolean executeEffectsAtTiles(THashSet<RoomTile> tHashSet, RoomUnit roomUnit, Room room, Object[] objArr) {
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            if (room != null) {
                THashSet<HabboItem> itemsAt = room.getItemsAt(roomTile);
                long cycleTimestamp = room.getCycleTimestamp();
                TObjectHashIterator it2 = itemsAt.iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem = (HabboItem) it2.next();
                    if ((habboItem instanceof InteractionWiredEffect) && !(habboItem instanceof WiredEffectTriggerStacks)) {
                        triggerEffect((InteractionWiredEffect) habboItem, roomUnit, room, objArr, cycleTimestamp);
                        ((InteractionWiredEffect) habboItem).setCooldown(cycleTimestamp);
                    }
                }
            }
        }
        return true;
    }

    public static void dropRewards(int i) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM wired_rewards_given WHERE wired_item = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
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

    private static void giveReward(Habbo habbo, WiredEffectGiveReward wiredEffectGiveReward, WiredGiveRewardItem wiredGiveRewardItem) {
        HabboItem habboItemCreateItem;
        if (wiredEffectGiveReward.limit > 0) {
            wiredEffectGiveReward.given++;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO wired_rewards_given (wired_item, user_id, reward_id, timestamp) VALUES ( ?, ?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, wiredEffectGiveReward.getId());
                    preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(3, wiredGiveRewardItem.id);
                    preparedStatementPrepareStatement.setInt(4, Emulator.getIntUnixTimestamp());
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
        if (wiredGiveRewardItem.badge) {
            UserWiredRewardReceived userWiredRewardReceived = new UserWiredRewardReceived(habbo, wiredEffectGiveReward, "badge", wiredGiveRewardItem.data);
            if (((UserWiredRewardReceived) Emulator.getPluginManager().fireEvent(userWiredRewardReceived)).isCancelled() || userWiredRewardReceived.value.isEmpty() || habbo.getInventory().getBadgesComponent().hasBadge(userWiredRewardReceived.value)) {
                return;
            }
            HabboBadge habboBadge = new HabboBadge(0, userWiredRewardReceived.value, 0, habbo);
            Emulator.getThreading().run(habboBadge);
            habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
            habbo.getClient().sendResponse(new AddUserBadgeComposer(habboBadge));
            habbo.getClient().sendResponse(new WiredRewardAlertComposer(7));
            return;
        }
        String[] strArrSplit = wiredGiveRewardItem.data.split("#");
        if (strArrSplit.length == 2) {
            UserWiredRewardReceived userWiredRewardReceived2 = new UserWiredRewardReceived(habbo, wiredEffectGiveReward, strArrSplit[0], strArrSplit[1]);
            if (((UserWiredRewardReceived) Emulator.getPluginManager().fireEvent(userWiredRewardReceived2)).isCancelled() || userWiredRewardReceived2.value.isEmpty()) {
                return;
            }
            if (userWiredRewardReceived2.type.equalsIgnoreCase("credits")) {
                habbo.giveCredits(Integer.valueOf(userWiredRewardReceived2.value).intValue());
                return;
            }
            if (userWiredRewardReceived2.type.equalsIgnoreCase("pixels")) {
                habbo.givePixels(Integer.valueOf(userWiredRewardReceived2.value).intValue());
                return;
            }
            if (userWiredRewardReceived2.type.startsWith("points")) {
                int iIntValue = Integer.valueOf(userWiredRewardReceived2.value).intValue();
                int iIntValue2 = 5;
                try {
                    iIntValue2 = Integer.valueOf(userWiredRewardReceived2.type.replace("points", Emulator.PREVIEW)).intValue();
                } catch (Exception e2) {
                }
                habbo.givePoints(iIntValue2, iIntValue);
                return;
            }
            if (userWiredRewardReceived2.type.equalsIgnoreCase("furni")) {
                Item item = Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(userWiredRewardReceived2.value).intValue());
                if (item == null || (habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW)) == null) {
                    return;
                }
                habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                habbo.getClient().getHabbo().getInventory().getItemsComponent().addItem(habboItemCreateItem);
                habbo.getClient().sendResponse(new PurchaseOKComposer(null));
                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                habbo.getClient().sendResponse(new WiredRewardAlertComposer(6));
                return;
            }
            if (userWiredRewardReceived2.type.equalsIgnoreCase("respect")) {
                habbo.getHabboStats().respectPointsReceived += Integer.valueOf(userWiredRewardReceived2.value).intValue();
            } else if (userWiredRewardReceived2.type.equalsIgnoreCase("cata")) {
                CatalogItem catalogItem = Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(Integer.valueOf(userWiredRewardReceived2.value).intValue());
                if (catalogItem != null) {
                    Emulator.getGameEnvironment().getCatalogManager().purchaseItem(null, catalogItem, habbo, 1, Emulator.PREVIEW, true);
                }
                habbo.getClient().sendResponse(new WiredRewardAlertComposer(6));
            }
        }
    }

    public static boolean getReward(Habbo habbo, WiredEffectGiveReward wiredEffectGiveReward) {
        if (wiredEffectGiveReward.limit > 0 && wiredEffectGiveReward.limit - wiredEffectGiveReward.given == 0) {
            habbo.getClient().sendResponse(new WiredRewardAlertComposer(0));
            return false;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) as row_count, wired_rewards_given.* FROM wired_rewards_given WHERE user_id = ? AND wired_item = ? ORDER BY timestamp DESC LIMIT ?", 1004, 1007);
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    preparedStatementPrepareStatement.setInt(2, wiredEffectGiveReward.getId());
                    preparedStatementPrepareStatement.setInt(3, wiredEffectGiveReward.rewardItems.size());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.first()) {
                            if (resultSetExecuteQuery.getInt("row_count") >= 1 && wiredEffectGiveReward.rewardTime == 0) {
                                habbo.getClient().sendResponse(new WiredRewardAlertComposer(1));
                                if (resultSetExecuteQuery != null) {
                                    resultSetExecuteQuery.close();
                                }
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                if (connection != null) {
                                    connection.close();
                                }
                                return false;
                            }
                            resultSetExecuteQuery.beforeFirst();
                            if (resultSetExecuteQuery.next()) {
                                if (wiredEffectGiveReward.rewardTime == 3 && Emulator.getIntUnixTimestamp() - resultSetExecuteQuery.getInt("timestamp") <= 60) {
                                    habbo.getClient().sendResponse(new WiredRewardAlertComposer(8));
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
                                    return false;
                                }
                                if (wiredEffectGiveReward.uniqueRewards && resultSetExecuteQuery.getInt("row_count") == wiredEffectGiveReward.rewardItems.size()) {
                                    habbo.getClient().sendResponse(new WiredRewardAlertComposer(5));
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
                                    return false;
                                }
                                if (wiredEffectGiveReward.rewardTime == 2 && Emulator.getIntUnixTimestamp() - resultSetExecuteQuery.getInt("timestamp") < 3600 * wiredEffectGiveReward.limitationInterval) {
                                    habbo.getClient().sendResponse(new WiredRewardAlertComposer(3));
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
                                    return false;
                                }
                                if (wiredEffectGiveReward.rewardTime == 1 && Emulator.getIntUnixTimestamp() - resultSetExecuteQuery.getInt("timestamp") < 86400 * wiredEffectGiveReward.limitationInterval) {
                                    habbo.getClient().sendResponse(new WiredRewardAlertComposer(2));
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
                                    return false;
                                }
                            }
                            if (wiredEffectGiveReward.uniqueRewards) {
                                TObjectHashIterator it = wiredEffectGiveReward.rewardItems.iterator();
                                while (it.hasNext()) {
                                    WiredGiveRewardItem wiredGiveRewardItem = (WiredGiveRewardItem) it.next();
                                    resultSetExecuteQuery.beforeFirst();
                                    boolean z = false;
                                    while (resultSetExecuteQuery.next()) {
                                        if (resultSetExecuteQuery.getInt("reward_id") == wiredGiveRewardItem.id) {
                                            z = true;
                                        }
                                    }
                                    if (!z) {
                                        giveReward(habbo, wiredEffectGiveReward, wiredGiveRewardItem);
                                        if (resultSetExecuteQuery != null) {
                                            resultSetExecuteQuery.close();
                                        }
                                        if (preparedStatementPrepareStatement != null) {
                                            preparedStatementPrepareStatement.close();
                                        }
                                        if (connection != null) {
                                            connection.close();
                                        }
                                        return true;
                                    }
                                }
                            } else {
                                int iNextInt = Emulator.getRandom().nextInt(RentableSpaceInfoComposer.SPACE_EXTEND_NOT_RENTED);
                                int i = 0;
                                TObjectHashIterator it2 = wiredEffectGiveReward.rewardItems.iterator();
                                while (it2.hasNext()) {
                                    WiredGiveRewardItem wiredGiveRewardItem2 = (WiredGiveRewardItem) it2.next();
                                    if (iNextInt >= i && iNextInt <= i + wiredGiveRewardItem2.probability) {
                                        giveReward(habbo, wiredEffectGiveReward, wiredGiveRewardItem2);
                                        if (resultSetExecuteQuery != null) {
                                            resultSetExecuteQuery.close();
                                        }
                                        if (preparedStatementPrepareStatement != null) {
                                            preparedStatementPrepareStatement.close();
                                        }
                                        if (connection != null) {
                                            connection.close();
                                        }
                                        return true;
                                    }
                                    i += wiredGiveRewardItem2.probability;
                                }
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
                        return false;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public static void resetTimers(Room room) {
        if (!room.isLoaded() || room.getRoomSpecialTypes() == null) {
            return;
        }
        room.getRoomSpecialTypes().getTriggers().forEach(interactionWiredTrigger -> {
            if (interactionWiredTrigger == 0) {
                return;
            }
            if (interactionWiredTrigger.getType() == WiredTriggerType.AT_GIVEN_TIME || interactionWiredTrigger.getType() == WiredTriggerType.PERIODICALLY || interactionWiredTrigger.getType() == WiredTriggerType.PERIODICALLY_LONG) {
                ((WiredTriggerReset) interactionWiredTrigger).resetTimer();
            }
        });
        room.setLastTimerReset(Emulator.getIntUnixTimestamp());
    }
}
