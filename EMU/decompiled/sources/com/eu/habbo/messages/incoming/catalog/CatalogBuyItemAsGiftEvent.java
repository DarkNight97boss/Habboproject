package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogLimitedConfiguration;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionBadgeDisplay;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildGate;
import com.eu.habbo.habbohotel.items.interactions.InteractionHopper;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleport;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile;
import com.eu.habbo.habbohotel.items.interactions.InteractionTrophy;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertLimitedSoldOutComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseUnavailableComposer;
import com.eu.habbo.messages.outgoing.catalog.GiftConfigurationComposer;
import com.eu.habbo.messages.outgoing.catalog.GiftReceiverNotFoundComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomEnterErrorComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogBuyItemAsGiftEvent.class */
public class CatalogBuyItemAsGiftEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogBuyItemAsGiftEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Connection connection;
        Habbo habbo;
        PreparedStatement preparedStatementPrepareStatement;
        if (((long) Emulator.getIntUnixTimestamp()) - this.client.getHabbo().getHabboStats().lastGiftTimestamp < CatalogManager.PURCHASE_COOLDOWN) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        this.client.getHabbo().getHabboStats().lastGiftTimestamp = Emulator.getIntUnixTimestamp();
        if (ShutdownEmulator.timestamp > 0) {
            this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        if (this.client.getHabbo().getHabboStats().isPurchasingFurniture) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        this.client.getHabbo().getHabboStats().isPurchasingFurniture = true;
        try {
            int iIntValue = this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            String string = this.packet.readString();
            String string2 = this.packet.readString();
            String string3 = this.packet.readString();
            int iIntValue3 = this.packet.readInt().intValue();
            int iIntValue4 = this.packet.readInt().intValue();
            int iIntValue5 = this.packet.readInt().intValue();
            boolean z = this.packet.readBoolean();
            if (!Emulator.getGameEnvironment().getCatalogManager().giftWrappers.containsKey(Integer.valueOf(iIntValue3)) && !Emulator.getGameEnvironment().getCatalogManager().giftFurnis.containsKey(Integer.valueOf(iIntValue3))) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (!GiftConfigurationComposer.BOX_TYPES.contains(Integer.valueOf(iIntValue4)) || !GiftConfigurationComposer.RIBBON_TYPES.contains(Integer.valueOf(iIntValue5))) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (string3.length() > Emulator.getConfig().getInt("hotel.gifts.length.max", 300)) {
                string3 = string3.substring(0, Emulator.getConfig().getInt("hotel.gifts.length.max", 300));
            }
            Integer num = (Integer) Emulator.getGameEnvironment().getCatalogManager().giftWrappers.get(Integer.valueOf(iIntValue3));
            if (num == null) {
                num = (Integer) Emulator.getGameEnvironment().getCatalogManager().giftFurnis.get(Integer.valueOf(iIntValue3));
            }
            if (num == null) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            Item item = Emulator.getGameEnvironment().getItemManager().getItem(num.intValue());
            if (item == null) {
                item = Emulator.getGameEnvironment().getItemManager().getItem(((Integer) Emulator.getGameEnvironment().getCatalogManager().giftFurnis.values().toArray()[Emulator.getRandom().nextInt(Emulator.getGameEnvironment().getCatalogManager().giftFurnis.size())]).intValue());
                if (item == null) {
                    this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                    this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                    return;
                }
            }
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(string2);
                    if (habbo == null) {
                        try {
                            preparedStatementPrepareStatement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
                            try {
                                preparedStatementPrepareStatement.setString(1, string2);
                                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                                try {
                                    id = resultSetExecuteQuery.next() ? resultSetExecuteQuery.getInt(1) : 0;
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
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
                            } finally {
                                if (preparedStatementPrepareStatement != null) {
                                    try {
                                        preparedStatementPrepareStatement.close();
                                    } catch (Throwable th3) {
                                        th.addSuppressed(th3);
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            LOGGER.error("Caught SQL exception", e);
                        }
                    } else {
                        id = habbo.getHabboInfo().getId();
                    }
                } catch (Throwable th4) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th5) {
                            th4.addSuppressed(th5);
                        }
                    }
                    throw th4;
                }
            } catch (Exception e2) {
                LOGGER.error("Exception caught", e2);
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            }
            if (id == 0) {
                this.client.sendResponse(new GiftReceiverNotFoundComposer());
                if (connection != null) {
                    connection.close();
                }
                return;
            }
            CatalogPage catalogPage = (CatalogPage) Emulator.getGameEnvironment().getCatalogManager().catalogPages.get(iIntValue);
            if (catalogPage == null) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (catalogPage.getRank() > this.client.getHabbo().getHabboInfo().getRank().getId() || !catalogPage.isEnabled() || !catalogPage.isVisible()) {
                this.client.sendResponse(new AlertPurchaseUnavailableComposer(0));
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            CatalogItem catalogItem = catalogPage.getCatalogItem(iIntValue2);
            if (catalogItem == null) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (catalogItem.isClubOnly() && !this.client.getHabbo().getHabboStats().hasActiveClub()) {
                this.client.sendResponse(new AlertPurchaseUnavailableComposer(1));
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            TObjectHashIterator it = catalogItem.getBaseItems().iterator();
            while (it.hasNext()) {
                if (!((Item) it.next()).allowGift()) {
                    this.client.sendResponse(new AlertPurchaseUnavailableComposer(0));
                    if (connection != null) {
                        connection.close();
                    }
                    this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                    return;
                }
            }
            if (catalogItem.isLimited()) {
                if (catalogItem.getLimitedStack() == catalogItem.getLimitedSells()) {
                    this.client.sendResponse(new AlertLimitedSoldOutComposer());
                    if (connection != null) {
                        connection.close();
                    }
                    this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                    return;
                }
                catalogItem.sellRare();
            }
            int credits = catalogItem.getCredits();
            int points = catalogItem.getPoints();
            if (credits > this.client.getHabbo().getHabboInfo().getCredits() || points > this.client.getHabbo().getHabboInfo().getCurrencyAmount(catalogItem.getPointsType())) {
                this.client.sendResponse(new AlertPurchaseUnavailableComposer(0));
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            int totalSet = 0;
            int number = 0;
            if (catalogItem.isLimited()) {
                if (Emulator.getGameEnvironment().getCatalogManager().getLimitedConfig(catalogItem).available() == 0 && habbo != null) {
                    habbo.getClient().sendResponse(new AlertLimitedSoldOutComposer());
                    if (connection != null) {
                        connection.close();
                    }
                    this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                    return;
                }
                CatalogLimitedConfiguration limitedConfig = Emulator.getGameEnvironment().getCatalogManager().getLimitedConfig(catalogItem);
                if (limitedConfig == null) {
                    limitedConfig = Emulator.getGameEnvironment().getCatalogManager().createOrUpdateLimitedConfig(catalogItem);
                }
                number = limitedConfig.getNumber();
                totalSet = limitedConfig.getTotalSet();
            }
            THashSet tHashSet = new THashSet();
            boolean z2 = false;
            TObjectHashIterator it2 = catalogItem.getBaseItems().iterator();
            while (it2.hasNext()) {
                Item item2 = (Item) it2.next();
                if (item2.getType() == FurnitureType.BADGE) {
                    if (habbo == null) {
                        preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) as c FROM users_badges WHERE user_id = ? AND badge_code LIKE ?");
                        try {
                            preparedStatementPrepareStatement.setInt(1, id);
                            preparedStatementPrepareStatement.setString(2, item2.getName());
                            ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement.executeQuery();
                            try {
                                int i = resultSetExecuteQuery2.next() ? resultSetExecuteQuery2.getInt(RoomEnterErrorComposer.ROOM_NEEDS_VIP) : 0;
                                if (resultSetExecuteQuery2 != null) {
                                    resultSetExecuteQuery2.close();
                                }
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                if (i != 0) {
                                    z2 = true;
                                }
                            } catch (Throwable th6) {
                                if (resultSetExecuteQuery2 != null) {
                                    try {
                                        resultSetExecuteQuery2.close();
                                    } catch (Throwable th7) {
                                        th6.addSuppressed(th7);
                                    }
                                }
                                throw th6;
                            }
                        } finally {
                        }
                    } else if (habbo.getInventory().getBadgesComponent().hasBadge(item2.getName())) {
                        z2 = true;
                    }
                }
            }
            if (z2) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(1));
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (catalogItem.getAmount() > 1 || catalogItem.getBaseItems().size() > 1) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            TObjectHashIterator it3 = catalogItem.getBaseItems().iterator();
            while (it3.hasNext()) {
                Item item3 = (Item) it3.next();
                if (catalogItem.getItemAmount(item3.getId()) > 1) {
                    this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                    if (connection != null) {
                        connection.close();
                    }
                    this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                    return;
                }
                for (int i2 = 0; i2 < catalogItem.getItemAmount(item3.getId()); i2++) {
                    if (item3.getName().contains("avatar_effect")) {
                        this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                        this.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("error.catalog.buy.not_yet")));
                        if (connection != null) {
                            connection.close();
                        }
                        this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                        return;
                    }
                    if (item3.getType() != FurnitureType.BADGE) {
                        if (catalogItem.getName().startsWith("rentable_bot_")) {
                            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                            if (connection != null) {
                                connection.close();
                            }
                            this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                            return;
                        }
                        if (Item.isPet(item3)) {
                            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                            if (connection != null) {
                                connection.close();
                            }
                            this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                            return;
                        }
                        if (item3.getInteractionType().getType() == InteractionTrophy.class || item3.getInteractionType().getType() == InteractionBadgeDisplay.class) {
                            if (item3.getInteractionType().getType() == InteractionBadgeDisplay.class && habbo != null && !habbo.getClient().getHabbo().getInventory().getBadgesComponent().hasBadge(string)) {
                                ScripterManager.scripterDetected(habbo.getClient(), Emulator.getTexts().getValue("scripter.warning.catalog.badge_display").replace("%username%", habbo.getClient().getHabbo().getHabboInfo().getUsername()).replace("%badge%", string));
                                string = "UMAD";
                            }
                            string = this.client.getHabbo().getHabboInfo().getUsername() + '\t' + Calendar.getInstance().get(5) + "-" + (Calendar.getInstance().get(2) + 1) + "-" + Calendar.getInstance().get(1) + '\t' + string;
                        }
                        if (item3.getInteractionType().getType() == InteractionTeleport.class || item3.getInteractionType().getType() == InteractionTeleportTile.class) {
                            HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(0, item3, totalSet, number, string);
                            HabboItem habboItemCreateItem2 = Emulator.getGameEnvironment().getItemManager().createItem(0, item3, totalSet, number, string);
                            Emulator.getGameEnvironment().getItemManager().insertTeleportPair(habboItemCreateItem.getId(), habboItemCreateItem2.getId());
                            tHashSet.add(habboItemCreateItem);
                            tHashSet.add(habboItemCreateItem2);
                        } else if (item3.getInteractionType().getType() == InteractionHopper.class) {
                            HabboItem habboItemCreateItem3 = Emulator.getGameEnvironment().getItemManager().createItem(0, item3, number, number, string);
                            Emulator.getGameEnvironment().getItemManager().insertHopper(habboItemCreateItem3);
                            tHashSet.add(habboItemCreateItem3);
                        } else if (item3.getInteractionType().getType() == InteractionGuildFurni.class || item3.getInteractionType().getType() == InteractionGuildGate.class) {
                            InteractionGuildFurni interactionGuildFurni = (InteractionGuildFurni) Emulator.getGameEnvironment().getItemManager().createItem(0, item3, totalSet, number, string);
                            interactionGuildFurni.setExtradata(Emulator.PREVIEW);
                            interactionGuildFurni.needsUpdate(true);
                            try {
                                int i3 = Integer.parseInt(string);
                                Emulator.getThreading().run(interactionGuildFurni);
                                Emulator.getGameEnvironment().getGuildManager().setGuild(interactionGuildFurni, i3);
                                tHashSet.add(interactionGuildFurni);
                            } catch (Exception e3) {
                                LOGGER.error("Caught exception", e3);
                                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                                if (connection != null) {
                                    connection.close();
                                }
                                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                                return;
                            }
                        } else {
                            tHashSet.add(Emulator.getGameEnvironment().getItemManager().createItem(0, item3, totalSet, number, string));
                        }
                    } else if (z2) {
                        continue;
                    } else {
                        if (habbo != null) {
                            HabboBadge habboBadge = new HabboBadge(0, item3.getName(), 0, habbo);
                            Emulator.getThreading().run(habboBadge);
                            habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
                        } else {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO users_badges (user_id, badge_code) VALUES (?, ?)");
                            try {
                                preparedStatementPrepareStatement2.setInt(1, id);
                                preparedStatementPrepareStatement2.setString(2, item3.getName());
                                preparedStatementPrepareStatement2.execute();
                                if (preparedStatementPrepareStatement2 != null) {
                                    preparedStatementPrepareStatement2.close();
                                }
                            } finally {
                            }
                        }
                        z2 = true;
                    }
                }
            }
            StringBuilder sb = new StringBuilder(tHashSet.size() + "\t");
            TObjectHashIterator it4 = tHashSet.iterator();
            while (it4.hasNext()) {
                sb.append(((HabboItem) it4.next()).getId()).append("\t");
            }
            sb.append(iIntValue4).append("\t").append(iIntValue5).append("\t").append(z ? "1" : "0").append("\t").append(string3.replace("\t", Emulator.PREVIEW)).append("\t").append(this.client.getHabbo().getHabboInfo().getUsername()).append("\t").append(this.client.getHabbo().getHabboInfo().getLook());
            HabboItem habboItemCreateGift = Emulator.getGameEnvironment().getItemManager().createGift(string2, item, sb.toString(), 0, 0);
            if (habboItemCreateGift == null) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (this.client.getHabbo().getHabboInfo().getId() != id) {
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GiftGiver"));
            }
            if (habbo != null) {
                habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateGift));
                habbo.getClient().getHabbo().getInventory().getItemsComponent().addItem(habboItemCreateGift);
                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                THashMap tHashMap = new THashMap();
                tHashMap.put("display", "BUBBLE");
                tHashMap.put("image", "${image.library.url}notifications/gift.gif");
                tHashMap.put("message", Emulator.getTexts().getValue("generic.gift.received.anonymous"));
                if (z) {
                    tHashMap.put("message", Emulator.getTexts().getValue("generic.gift.received").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
                }
                habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap));
            }
            if (this.client.getHabbo().getHabboInfo().getId() != id) {
                AchievementManager.progressAchievement(id, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GiftReceiver"));
            }
            if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS) && credits > 0) {
                this.client.getHabbo().giveCredits(-credits);
            }
            if (points > 0) {
                if (catalogItem.getPointsType() == 0 && !this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_PIXELS)) {
                    this.client.getHabbo().givePixels(-points);
                } else if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                    this.client.getHabbo().givePoints(catalogItem.getPointsType(), -points);
                }
            }
            this.client.sendResponse(new PurchaseOKComposer(catalogItem));
            if (connection != null) {
                connection.close();
            }
            this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
        } finally {
            this.client.getHabbo().getHabboStats().isPurchasingFurniture = false;
        }
    }
}
